package no.nav.folketrygdloven.kalkulus.rest;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

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
import no.nav.folketrygdloven.fpkalkulus.kontrakt.BeregnRequestDto;
import no.nav.folketrygdloven.fpkalkulus.kontrakt.FpkalkulusYtelser;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.KoblingReferanse;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.Saksnummer;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.kobling.KoblingEntitet;
import no.nav.folketrygdloven.kalkulus.felles.v1.PersonIdent;
import no.nav.folketrygdloven.kalkulus.håndtering.v1.HåndterBeregningDto;
import no.nav.folketrygdloven.kalkulus.kobling.KoblingTjeneste;
import no.nav.folketrygdloven.kalkulus.kodeverk.AvklaringsbehovDefinisjon;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningSteg;
import no.nav.folketrygdloven.kalkulus.kodeverk.FagsakYtelseType;
import no.nav.folketrygdloven.kalkulus.kopiering.KopierBeregningsgrunnlagTjeneste;
import no.nav.folketrygdloven.kalkulus.request.v1.BeregnForRequest;
import no.nav.folketrygdloven.kalkulus.request.v1.BeregnListeRequest;
import no.nav.folketrygdloven.kalkulus.request.v1.BeregningsgrunnlagListeRequest;
import no.nav.folketrygdloven.kalkulus.request.v1.BeregningsgrunnlagRequest;
import no.nav.folketrygdloven.kalkulus.request.v1.HåndterBeregningListeRequest;
import no.nav.folketrygdloven.kalkulus.request.v1.HåndterBeregningRequest;
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
import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.AbacDto;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;
import no.nav.vedtak.sikkerhet.abac.StandardAbacAttributtType;
import no.nav.vedtak.sikkerhet.abac.TilpassetAbacAttributt;
import no.nav.vedtak.sikkerhet.abac.beskyttet.ActionType;
import no.nav.vedtak.sikkerhet.abac.beskyttet.ResourceType;

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
    @Path("/beregn")
    @Operation(description = "Utfører beregning basert på reqest", tags = "beregn", summary = ("Starter en beregning basert på gitt input."), responses = {
            @ApiResponse(description = "Liste med avklaringsbehov som har oppstått per angitt eksternReferanse", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = TilstandResponse.class)))
    })
    @BeskyttetRessurs(actionType = ActionType.CREATE, resourceType = ResourceType.FAGSAK)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public Response beregn(@TilpassetAbacAttributt(supplierClass = BeregnRequestAbacSupplier.class) @NotNull @Valid BeregnRequestDto request) {
        var saksnummer = new Saksnummer(request.saksnummer().verdi());
        MDC.put("prosess_saksnummer", saksnummer.getVerdi());
//        var kobling = koblingTjeneste.finnEllerOpprett(new KoblingReferanse(request.behandlingUuid()),
//            mapTilYtelseKodeverk(request.ytelseSomSkalBeregnes()), new AktørId(request.aktør().getIdent()), saksnummer);
//        Map<Long, KalkulusRespons> respons;
//        respons = orkestrerer.beregn(request.stegType(), kobling, request.kalkulatorInput())
//        );
//        return Response.ok(new TilstandListeResponse(respons.values().stream().map(r -> (TilstandResponse) r).toList())).build();
        return Response.ok().build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/kopier/bolk")
    @Operation(description = "Kopierer beregning fra eksisterende referanse til ny referanse. Kopien som opprettes er fra steget som vurderer vilkår for beregning.",
            tags = "beregn",
            summary = ("Kopierer en beregning."), responses = {
            @ApiResponse(description = "Liste med kopierte referanser dersom alle koblinger er kopiert", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = KopiResponse.class)))
    })
    @BeskyttetRessurs(actionType = ActionType.UPDATE, resourceType = ResourceType.FAGSAK)
    public Response kopierBeregning(@NotNull @Valid KopierBeregningListeRequestAbacDto spesifikasjon) {
        MDC.put("prosess_saksnummer", spesifikasjon.getSaksnummer().verdi());
        var saksnummerEntitet = new Saksnummer(spesifikasjon.getSaksnummer().verdi());
        kopierTjeneste.kopierGrunnlagOgOpprettKoblinger(
                spesifikasjon.getKopierBeregningListe(),
                spesifikasjon.getYtelseSomSkalBeregnes(),
                saksnummerEntitet,
                spesifikasjon.getStegType() == null ? BeregningSteg.VURDER_VILKAR_BERGRUNN : spesifikasjon.getStegType(),
                null);
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
    @BeskyttetRessurs(actionType = ActionType.UPDATE, resourceType = ResourceType.FAGSAK)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public Response oppdaterListe(@NotNull @Valid HåndterBeregningListeRequestAbacDto spesifikasjon) {
        MDC.put("prosess_saksnummer", spesifikasjon.getSaksnummer().verdi());
        Map<Long, KalkulusRespons> respons;
        try {
            var saksnummerEntitet = new Saksnummer(spesifikasjon.getSaksnummer().verdi());
            respons = orkestrerer.håndter(
                    spesifikasjon.getKalkulatorInputPerKoblingReferanse(),
                    spesifikasjon.getYtelseSomSkalBeregnes(),
                    saksnummerEntitet,
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
    @BeskyttetRessurs(actionType = ActionType.UPDATE, resourceType = ResourceType.FAGSAK)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public Response deaktiverBeregningsgrunnlag(@NotNull @Valid DeaktiverBeregningsgrunnlagRequestAbacDto spesifikasjon) {
        String saksnummer = spesifikasjon.getSaksnummer().verdi();
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

    private FagsakYtelseType mapTilYtelseKodeverk(FpkalkulusYtelser ytelse) {
        return switch (ytelse) {
            case FORELDREPENGER -> FagsakYtelseType.FORELDREPENGER;
            case SVANGERSKAPSPENGER -> FagsakYtelseType.SVANGERSKAPSPENGER;
        };
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(value = JsonInclude.Include.NON_ABSENT, content = JsonInclude.Include.NON_EMPTY)
    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
    public static class BeregnListeRequestAbacDto extends BeregnListeRequest implements AbacDto {

        public BeregnListeRequestAbacDto() {
        }

        public BeregnListeRequestAbacDto(no.nav.folketrygdloven.kalkulus.felles.v1.Saksnummer saksnummer,
                                         UUID behandlingUuid,
                                         PersonIdent aktør,
                                         FagsakYtelseType ytelseSomSkalBeregnes,
                                         BeregningSteg stegType,
                                         List<BeregnForRequest> beregnForListe) {
            super(saksnummer, behandlingUuid, aktør, ytelseSomSkalBeregnes, stegType, beregnForListe);
        }

        @Override
        public AbacDataAttributter abacAttributter() {
            var abacDataAttributter = AbacDataAttributter.opprett();
            if (getBehandlingUuid() != null) {
                abacDataAttributter.leggTil(StandardAbacAttributtType.BEHANDLING_UUID, getBehandlingUuid());
            }
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

        public KopierBeregningListeRequestAbacDto(no.nav.folketrygdloven.kalkulus.felles.v1.Saksnummer saksnummer,
                                                  UUID behandlingUuid,
                                                  FagsakYtelseType ytelseSomSkalBeregnes,
                                                  List<KopierBeregningRequest> kopierBeregningListe, BeregningSteg stegType) {
            super(saksnummer, behandlingUuid, ytelseSomSkalBeregnes, stegType, kopierBeregningListe);
        }


        @Override
        public AbacDataAttributter abacAttributter() {
            final var abacDataAttributter = AbacDataAttributter.opprett();
            if (getBehandlingUuid() != null) {
                abacDataAttributter.leggTil(StandardAbacAttributtType.BEHANDLING_UUID, getBehandlingUuid());
            }
            abacDataAttributter.leggTil(StandardAbacAttributtType.SAKSNUMMER, getSaksnummer());
            return abacDataAttributter;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(value = JsonInclude.Include.NON_ABSENT, content = JsonInclude.Include.NON_EMPTY)
    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
    public static class DeaktiverBeregningsgrunnlagRequestAbacDto extends BeregningsgrunnlagListeRequest implements AbacDto {

        public DeaktiverBeregningsgrunnlagRequestAbacDto() {
        }

        public DeaktiverBeregningsgrunnlagRequestAbacDto(no.nav.folketrygdloven.kalkulus.felles.v1.Saksnummer saksnummer, List<BeregningsgrunnlagRequest> requestPrReferanse, UUID behandlingUuid) {
            super(saksnummer, requestPrReferanse, behandlingUuid);
        }

        @Override
        public AbacDataAttributter abacAttributter() {
            var abacDataAttributter = AbacDataAttributter.opprett();
            if (getBehandlingUuid() != null) {
                abacDataAttributter.leggTil(StandardAbacAttributtType.BEHANDLING_UUID, getBehandlingUuid());
            }
            abacDataAttributter.leggTil(StandardAbacAttributtType.SAKSNUMMER, getSaksnummer());
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
            var aksjonspunktKoder = getAksjonspunktKoder();
            aksjonspunktKoder.forEach(it -> abacDataAttributter.leggTil(StandardAbacAttributtType.AKSJONSPUNKT_KODE, it));
            abacDataAttributter.leggTil(StandardAbacAttributtType.BEHANDLING_UUID, getBehandlingUuid());
            abacDataAttributter.leggTil(StandardAbacAttributtType.SAKSNUMMER, getSaksnummer());
            return abacDataAttributter;
        }

        private List<AvklaringsbehovDefinisjon> getAksjonspunktKoder() {
            return getHåndterBeregningListe().stream().map(HåndterBeregningRequest::getHåndterBeregning)
                    .map(HåndterBeregningDto::getAvklaringsbehovDefinisjon)
                    .collect(Collectors.toList());
        }

    }

    public static class BeregnRequestAbacSupplier implements Function<Object, AbacDataAttributter> {
        @Override
        public AbacDataAttributter apply(Object o) {
            var req = (BeregnRequestDto) o;
            return AbacDataAttributter.opprett().leggTil(StandardAbacAttributtType.BEHANDLING_UUID, req.behandlingUuid());
        }
    }

}
