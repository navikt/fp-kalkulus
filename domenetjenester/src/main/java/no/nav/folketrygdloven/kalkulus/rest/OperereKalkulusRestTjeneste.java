package no.nav.folketrygdloven.kalkulus.rest;

import static no.nav.folketrygdloven.kalkulus.sikkerhet.KalkulusBeskyttetRessursAttributt.BEREGNINGSGRUNNLAG;
import static no.nav.k9.felles.sikkerhet.abac.BeskyttetRessursActionAttributt.CREATE;
import static no.nav.k9.felles.sikkerhet.abac.BeskyttetRessursActionAttributt.UPDATE;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

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
import no.nav.folketrygdloven.kalkulus.beregning.input.HentInputResponsKode;
import no.nav.folketrygdloven.kalkulus.beregning.input.Resultat;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.KoblingReferanse;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.Saksnummer;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.kobling.KoblingEntitet;
import no.nav.folketrygdloven.kalkulus.felles.v1.KalkulatorInputDto;
import no.nav.folketrygdloven.kalkulus.felles.v1.PersonIdent;
import no.nav.folketrygdloven.kalkulus.kobling.KoblingTjeneste;
import no.nav.folketrygdloven.kalkulus.kodeverk.StegType;
import no.nav.folketrygdloven.kalkulus.kodeverk.YtelseTyperKalkulusStøtterKontrakt;
import no.nav.folketrygdloven.kalkulus.request.v1.BeregningsgrunnlagListeRequest;
import no.nav.folketrygdloven.kalkulus.request.v1.BeregningsgrunnlagRequest;
import no.nav.folketrygdloven.kalkulus.request.v1.FortsettBeregningListeRequest;
import no.nav.folketrygdloven.kalkulus.request.v1.HåndterBeregningListeRequest;
import no.nav.folketrygdloven.kalkulus.request.v1.StartBeregningListeRequest;
import no.nav.folketrygdloven.kalkulus.response.v1.KalkulusRespons;
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

    private KoblingTjeneste koblingTjeneste;
    private RullTilbakeTjeneste rullTilbakeTjeneste;
    private OperereKalkulusOrkestrerer orkestrerer;

    public OperereKalkulusRestTjeneste() {
        // for CDI
    }

    @Inject
    public OperereKalkulusRestTjeneste(KoblingTjeneste koblingTjeneste,
                                       RullTilbakeTjeneste rullTilbakeTjeneste,
                                       OperereKalkulusOrkestrerer orkestrerer) {
        this.koblingTjeneste = koblingTjeneste;
        this.rullTilbakeTjeneste = rullTilbakeTjeneste;
        this.orkestrerer = orkestrerer;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/start/bolk")
    @Operation(description = "Utfører beregning basert på reqest", tags = "beregn", summary = ("Starter en beregning basert på gitt input."), responses = {
            @ApiResponse(description = "Liste med avklaringsbehov som har oppstått per angitt eksternReferanse", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = TilstandResponse.class)))
    })
    @BeskyttetRessurs(action = CREATE, resource = BEREGNINGSGRUNNLAG)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public Response beregn(@NotNull @Valid StartBeregningListeRequestAbacDto spesifikasjon) {
        var saksnummer = new Saksnummer(spesifikasjon.getSaksnummer());
        MDC.put("prosess_saksnummer", saksnummer.getVerdi());

        Resultat<KalkulusRespons> respons = orkestrerer.lagInputOgStartBeregning(
                spesifikasjon.getKalkulatorInputPerKoblingReferanse(),
                spesifikasjon.getYtelseSomSkalBeregnes(),
                new Saksnummer(spesifikasjon.getSaksnummer()),
                new AktørId(spesifikasjon.getAktør().getIdent())
        );

        return Response.ok(new TilstandListeResponse(respons.getResultatPrReferanse()
                .values().stream()
                .map(r -> (TilstandResponse) r)
                .toList())).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/fortsett/bolk")
    @Operation(description = "Utfører beregning basert på reqest", tags = "beregn", summary = ("Fortsetter en beregning basert på stegInput."), responses = {
            @ApiResponse(description = "Liste med avklaringsbehov som har oppstått", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = TilstandResponse.class)))
    })
    @BeskyttetRessurs(action = UPDATE, resource = BEREGNINGSGRUNNLAG)
    public Response beregnVidere(@NotNull @Valid FortsettBeregningListeRequestAbacDto spesifikasjon) {
        MDC.put("prosess_saksnummer", spesifikasjon.getSaksnummer());

        Resultat<KalkulusRespons> respons = orkestrerer.lagInputOgBeregnVidere(
                spesifikasjon.getKalkulatorInputPerKoblingReferanse(),
                spesifikasjon.getEksternReferanser(),
                spesifikasjon.getKoblingRelasjon().orElse(Map.of()),
                spesifikasjon.getYtelseSomSkalBeregnes(),
                new Saksnummer(spesifikasjon.getSaksnummer()),
                spesifikasjon.getStegType()
        );

        if (respons.getKode() == HentInputResponsKode.ETTERSPØR_NY_INPUT) {
            return Response.ok(new OppdateringListeRespons(true)).build();
        } else {
            return Response.ok(new TilstandListeResponse(respons.getResultatPrKobling()
                    .values().stream()
                    .map(r -> (TilstandResponse) r)
                    .toList())).build();
        }
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

        Resultat<KalkulusRespons> respons = orkestrerer.lagInputOgHåndter(
                spesifikasjon.getKalkulatorInputPerKoblingReferanse(),
                spesifikasjon.getYtelseSomSkalBeregnes(),
                new Saksnummer(spesifikasjon.getSaksnummer()),
                spesifikasjon.getHåndterBeregningListe());

        if (respons.getKode() == HentInputResponsKode.ETTERSPØR_NY_INPUT) {
            return Response.ok(new OppdateringListeRespons(true)).build();
        } else {
            List<OppdateringPrRequest> oppdateringer = respons.getResultatPrReferanse().entrySet().stream()
                    .sorted(Comparator.comparing(e -> respons.getSkjæringstidspunktPrReferanse().get(e.getKey())))
                    .map(res -> new OppdateringPrRequest((OppdateringRespons) res.getValue(), res.getKey()))
                    .collect(Collectors.toList());
            return Response.ok(Objects.requireNonNullElseGet(new OppdateringListeRespons(oppdateringer), OppdateringRespons::TOM_RESPONS)).build();
        }
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
            MDC.put("prosess_koblingreferanse", k.toString());
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
    public static class StartBeregningListeRequestAbacDto extends StartBeregningListeRequest implements AbacDto {

        public StartBeregningListeRequestAbacDto() {
        }

        public StartBeregningListeRequestAbacDto(Map<UUID, KalkulatorInputDto> kalkulatorInput, String saksnummer, PersonIdent aktør,
                                                 YtelseTyperKalkulusStøtterKontrakt ytelseSomSkalBeregnes) {
            super(kalkulatorInput, saksnummer, aktør, ytelseSomSkalBeregnes);
        }

        @Override
        public AbacDataAttributter abacAttributter() {
            var abacDataAttributter = AbacDataAttributter.opprett();

            abacDataAttributter.leggTil(StandardAbacAttributtType.BEHANDLING_UUID, getKalkulatorInputPerKoblingReferanse().keySet());
            abacDataAttributter.leggTil(StandardAbacAttributtType.SAKSNUMMER, getSaksnummer());
            return abacDataAttributter.leggTil(StandardAbacAttributtType.AKTØR_ID, getAktør().getIdent());
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(value = JsonInclude.Include.NON_ABSENT, content = JsonInclude.Include.NON_EMPTY)
    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
    public static class FortsettBeregningListeRequestAbacDto extends FortsettBeregningListeRequest implements AbacDto {

        public FortsettBeregningListeRequestAbacDto() {
        }

        public FortsettBeregningListeRequestAbacDto(String saksnummer,
                                                    List<UUID> eksternReferanser,
                                                    Map<UUID, KalkulatorInputDto> kalkulatorInputPerKoblingReferanse,
                                                    StegType stegType, YtelseTyperKalkulusStøtterKontrakt ytelseSomSkalBeregnes) {
            super(saksnummer, eksternReferanser, kalkulatorInputPerKoblingReferanse, ytelseSomSkalBeregnes, stegType);
        }

        @Override
        public AbacDataAttributter abacAttributter() {
            final var abacDataAttributter = AbacDataAttributter.opprett();
            abacDataAttributter.leggTil(StandardAbacAttributtType.BEHANDLING_UUID, getEksternReferanser());
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
