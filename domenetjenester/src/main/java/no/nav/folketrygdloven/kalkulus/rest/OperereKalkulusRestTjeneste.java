package no.nav.folketrygdloven.kalkulus.rest;

import static no.nav.folketrygdloven.kalkulus.sikkerhet.KalkulusBeskyttetRessursAttributt.BEREGNINGSGRUNNLAG;
import static no.nav.k9.felles.sikkerhet.abac.BeskyttetRessursActionAttributt.CREATE;
import static no.nav.k9.felles.sikkerhet.abac.BeskyttetRessursActionAttributt.UPDATE;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.KoblingReferanse;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.Saksnummer;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.kobling.KoblingEntitet;
import no.nav.folketrygdloven.kalkulus.felles.v1.PersonIdent;
import no.nav.folketrygdloven.kalkulus.kobling.KoblingTjeneste;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningSteg;
import no.nav.folketrygdloven.kalkulus.kodeverk.StegType;
import no.nav.folketrygdloven.kalkulus.kodeverk.YtelseTyperKalkulusStøtterKontrakt;
import no.nav.folketrygdloven.kalkulus.kopiering.KopierBeregningsgrunnlagTjeneste;
import no.nav.folketrygdloven.kalkulus.request.v1.BeregnForRequest;
import no.nav.folketrygdloven.kalkulus.request.v1.BeregnListeRequest;
import no.nav.folketrygdloven.kalkulus.request.v1.BeregningsgrunnlagListeRequest;
import no.nav.folketrygdloven.kalkulus.request.v1.BeregningsgrunnlagRequest;
import no.nav.folketrygdloven.kalkulus.request.v1.HåndterBeregningListeRequest;
import no.nav.folketrygdloven.kalkulus.request.v1.KopierBeregningListeRequest;
import no.nav.folketrygdloven.kalkulus.request.v1.KopierBeregningRequest;
import no.nav.folketrygdloven.kalkulus.response.v1.KalkulusRespons;
import no.nav.folketrygdloven.kalkulus.response.v1.KopiResponse;
import no.nav.folketrygdloven.kalkulus.response.v1.TilstandListeResponse;
import no.nav.folketrygdloven.kalkulus.response.v1.TilstandResponse;
import no.nav.folketrygdloven.kalkulus.response.v1.håndtering.OppdateringListeRespons;
import no.nav.folketrygdloven.kalkulus.response.v1.håndtering.OppdateringPrRequest;
import no.nav.folketrygdloven.kalkulus.response.v1.håndtering.OppdateringRespons;
import no.nav.folketrygdloven.kalkulus.tjeneste.beregningsgrunnlag.RullTilbakeTjeneste;
import no.nav.folketrygdloven.kalkulus.typer.AktørId;
import no.nav.k9.felles.sikkerhet.abac.AbacDataAttributter;
import no.nav.k9.felles.sikkerhet.abac.AbacDto;
import no.nav.k9.felles.sikkerhet.abac.BeskyttetRessurs;
import no.nav.k9.felles.sikkerhet.abac.StandardAbacAttributtType;

@Produces(MediaType.APPLICATION_JSON)
@OpenAPIDefinition(tags = @Tag(name = "beregn"))
@Path("/kalkulus/v1")
@ApplicationScoped
@Transactional
public class OperereKalkulusRestTjeneste {

    private static final Logger LOG = LoggerFactory.getLogger(OperereKalkulusRestTjeneste.class);

    private KoblingTjeneste koblingTjeneste;
    private RullTilbakeTjeneste rullTilbakeTjeneste;
    private OperereKalkulusOrkestrerer orkestrerer;
    private KopierBeregningsgrunnlagTjeneste kopierTjeneste;

    public OperereKalkulusRestTjeneste() {
        // for CDI
    }

    @Inject
    public OperereKalkulusRestTjeneste(KoblingTjeneste koblingTjeneste,
                                       RullTilbakeTjeneste rullTilbakeTjeneste,
                                       OperereKalkulusOrkestrerer orkestrerer,
                                       KopierBeregningsgrunnlagTjeneste kopierTjeneste) {
        this.koblingTjeneste = koblingTjeneste;
        this.rullTilbakeTjeneste = rullTilbakeTjeneste;
        this.orkestrerer = orkestrerer;
        this.kopierTjeneste = kopierTjeneste;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/beregn/bolk")
    @Operation(description = "Utfører beregning basert på reqest", tags = "beregn", summary = ("Starter en beregning basert på gitt input."), responses = {
            @ApiResponse(description = "Liste med avklaringsbehov som har oppstått per angitt eksternReferanse", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = TilstandResponse.class)))
    })
    @BeskyttetRessurs(action = CREATE, resource = BEREGNINGSGRUNNLAG)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public Response beregn(@NotNull @Valid BeregnListeRequestAbacDto spesifikasjon) {
        var saksnummer = new Saksnummer(spesifikasjon.getSaksnummer());
        MDC.put("prosess_saksnummer", saksnummer.getVerdi());
        Map<Long, KalkulusRespons> respons;
        try {
            respons = orkestrerer.beregn(
                    BeregningSteg.fraKode(spesifikasjon.getStegType().getKode()),
                    new Saksnummer(spesifikasjon.getSaksnummer()),
                    new AktørId(spesifikasjon.getAktør().getIdent()),
                    spesifikasjon.getYtelseSomSkalBeregnes(),
                    spesifikasjon.getBeregnForListe()
            );
        } catch (UgyldigInputException e) {
            LOG.warn("Konvertering av input feilet: " + e.getMessage());
            return Response.ok(new OppdateringListeRespons(true)).build();
        }
        return Response.ok(new TilstandListeResponse(respons.values().stream().map(r -> (TilstandResponse) r).toList())).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/kopier/bolk")
    @Operation(description = "Kopierer beregning fra eksisterende referanse til ny referanse. Kopien som opprettes er fra steget som vurderer vilkår for beregning.",
            tags = "beregn",
            summary = ("Kopierer en beregning."), responses = {
            @ApiResponse(description = "Liste med kopierte referanser dersom alle koblinger er kopiert", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = KopiResponse.class)))
    })
    @BeskyttetRessurs(action = UPDATE, resource = BEREGNINGSGRUNNLAG)
    public Response kopierBeregning(@NotNull @Valid KopierBeregningListeRequestAbacDto spesifikasjon) {
        MDC.put("prosess_saksnummer", spesifikasjon.getSaksnummer());
        kopierTjeneste.kopierGrunnlagOgOpprettKoblinger(
                spesifikasjon.getKopierBeregningListe(),
                spesifikasjon.getYtelseSomSkalBeregnes(),
                new Saksnummer(spesifikasjon.getSaksnummer()),
                spesifikasjon.getStegType() == null ? BeregningSteg.VURDER_VILKAR_BERGRUNN : BeregningSteg.fraKode(spesifikasjon.getStegType().getKode())
        );
        return Response.ok(spesifikasjon.getKopierBeregningListe()
                .stream()
                .map(KopierBeregningRequest::getEksternReferanse)
                .map(KopiResponse::new).collect(Collectors.toList())).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/oppdaterListe")
    @Operation(description = "Oppdaterer beregningsgrunnlag for oppgitt liste", tags = "beregn", summary = ("Oppdaterer beregningsgrunnlag basert på løsning av avklaringsbehov for oppgitt liste."), responses = {
            @ApiResponse(description = "Liste med endringer som ble gjort under oppdatering", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = OppdateringListeRespons.class)))
    })
    @BeskyttetRessurs(action = UPDATE, resource = BEREGNINGSGRUNNLAG)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public Response oppdaterListe(@NotNull @Valid HåndterBeregningListeRequestAbacDto spesifikasjon) {
        MDC.put("prosess_saksnummer", spesifikasjon.getSaksnummer());
        Map<Long, KalkulusRespons> respons;
        try {
            respons = orkestrerer.håndter(
                    spesifikasjon.getKalkulatorInputPerKoblingReferanse(),
                    spesifikasjon.getYtelseSomSkalBeregnes(),
                    new Saksnummer(spesifikasjon.getSaksnummer()),
                    spesifikasjon.getHåndterBeregningListe());
        } catch (UgyldigInputException e) {
            return Response.ok(new OppdateringListeRespons(true)).build();
        }

        List<OppdateringPrRequest> oppdateringer = respons.values().stream()
                .map(kalkulusRespons -> new OppdateringPrRequest((OppdateringRespons) kalkulusRespons, kalkulusRespons.getEksternReferanse()))
                .collect(Collectors.toList());
        return Response.ok(Objects.requireNonNullElseGet(new OppdateringListeRespons(oppdateringer), OppdateringRespons::TOM_RESPONS)).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/deaktiver/bolk")
    @Operation(description = "Deaktiverer aktivt beregningsgrunnlag. Nullstiller beregning.", tags = "deaktiver", summary = ("Deaktiverer aktivt beregningsgrunnlag."))
    @BeskyttetRessurs(action = UPDATE, resource = BEREGNINGSGRUNNLAG)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public Response deaktiverBeregningsgrunnlag(@NotNull @Valid DeaktiverBeregningsgrunnlagRequestAbacDto spesifikasjon) {
        String saksnummer = spesifikasjon.getSaksnummer();
        MDC.put("prosess_saksnummer", saksnummer);
        for (var k : spesifikasjon.getRequestPrReferanse()) {
            var koblingReferanse = new KoblingReferanse(k.getKoblingReferanse());
            MDC.put("prosess_koblingreferanse", koblingReferanse.getReferanse().toString());
            var kopt = koblingTjeneste.hentFor(koblingReferanse);

            // Vi kaster ikkje feil om vi ikkje finner kobling
            // Grunnen til dette er fordi i nokon FRISINN-saker hopper man over beregning ved avslag og dermed vil ikkje beregning vere kjørt
            // Vurderingen er at dette er noko som kan håndteres utan feil sidan målet er å deaktivere (vi vil deaktivere ein beregning som blei avslått
            // før den nådde beregning)
            if (kopt.isPresent()) {
                KoblingEntitet kobling = kopt.get();
                if (!Objects.equals(kobling.getSaksnummer().getVerdi(), saksnummer)) {
                    throw new IllegalArgumentException("Kobling tilhører ikke saksnummer [" + saksnummer + "]: " + kobling);
                }
                rullTilbakeTjeneste.deaktiverAllKoblingdata(kobling.getId());
            }
        }

        return Response.ok().build();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(value = JsonInclude.Include.NON_ABSENT, content = JsonInclude.Include.NON_EMPTY)
    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
    public static class BeregnListeRequestAbacDto extends BeregnListeRequest implements AbacDto {

        public BeregnListeRequestAbacDto() {
        }

        public BeregnListeRequestAbacDto(String saksnummer,
                                         PersonIdent aktør,
                                         YtelseTyperKalkulusStøtterKontrakt ytelseSomSkalBeregnes,
                                         StegType stegType,
                                         List<BeregnForRequest> beregnForListe) {
            super(saksnummer, aktør, ytelseSomSkalBeregnes, stegType, beregnForListe);
        }

        @Override
        public AbacDataAttributter abacAttributter() {
            var abacDataAttributter = AbacDataAttributter.opprett();

            abacDataAttributter.leggTil(StandardAbacAttributtType.BEHANDLING_UUID, getBeregnForListe().stream().map(BeregnForRequest::getEksternReferanse).collect(Collectors.toList()));
            abacDataAttributter.leggTil(StandardAbacAttributtType.SAKSNUMMER, getSaksnummer());
            return abacDataAttributter.leggTil(StandardAbacAttributtType.AKTØR_ID, getAktør().getIdent());
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(value = JsonInclude.Include.NON_ABSENT, content = JsonInclude.Include.NON_EMPTY)
    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
    public static class KopierBeregningListeRequestAbacDto extends KopierBeregningListeRequest implements AbacDto {

        public KopierBeregningListeRequestAbacDto() {
        }

        public KopierBeregningListeRequestAbacDto(String saksnummer,
                                                  YtelseTyperKalkulusStøtterKontrakt ytelseSomSkalBeregnes,
                                                  List<KopierBeregningRequest> kopierBeregningListe, StegType stegType) {
            super(saksnummer, ytelseSomSkalBeregnes, stegType, kopierBeregningListe);
        }


        @Override
        public AbacDataAttributter abacAttributter() {
            final var abacDataAttributter = AbacDataAttributter.opprett();
            abacDataAttributter.leggTil(StandardAbacAttributtType.BEHANDLING_UUID, getKopierBeregningListe().stream().map(KopierBeregningRequest::getEksternReferanse).collect(Collectors.toList()));
            return abacDataAttributter;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(value = JsonInclude.Include.NON_ABSENT, content = JsonInclude.Include.NON_EMPTY)
    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
    public static class DeaktiverBeregningsgrunnlagRequestAbacDto extends BeregningsgrunnlagListeRequest implements AbacDto {

        public DeaktiverBeregningsgrunnlagRequestAbacDto() {
        }

        public DeaktiverBeregningsgrunnlagRequestAbacDto(String saksnummer, List<BeregningsgrunnlagRequest> requestPrReferanse) {
            super(saksnummer, requestPrReferanse);
        }

        @Override
        public AbacDataAttributter abacAttributter() {
            var abacDataAttributter = AbacDataAttributter.opprett();
            List<UUID> eksternReferanser = getRequestPrReferanse().stream().map(BeregningsgrunnlagRequest::getKoblingReferanse).collect(Collectors.toList());
            abacDataAttributter.leggTil(StandardAbacAttributtType.BEHANDLING_UUID, eksternReferanser);
            return abacDataAttributter;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(value = JsonInclude.Include.NON_ABSENT, content = JsonInclude.Include.NON_EMPTY)
    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
    public static class HåndterBeregningListeRequestAbacDto extends HåndterBeregningListeRequest implements AbacDto {

        public HåndterBeregningListeRequestAbacDto() {
            // Jackson
        }

        @Override
        public AbacDataAttributter abacAttributter() {
            final var abacDataAttributter = AbacDataAttributter.opprett();

            abacDataAttributter.leggTil(StandardAbacAttributtType.BEHANDLING_UUID, getBehandlingUuid());
            return abacDataAttributter;
        }
    }

}
