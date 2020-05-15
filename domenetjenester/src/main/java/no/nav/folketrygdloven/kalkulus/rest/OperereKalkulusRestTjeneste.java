package no.nav.folketrygdloven.kalkulus.rest;

import static no.nav.folketrygdloven.kalkulus.sikkerhet.KalkulusBeskyttetRessursAttributt.BEREGNINGSGRUNNLAG;
import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursActionAttributt.CREATE;
import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursActionAttributt.UPDATE;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

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

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulus.beregning.BeregningStegTjeneste;
import no.nav.folketrygdloven.kalkulus.beregning.KalkulatorInputTjeneste;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.AktørId;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.KoblingReferanse;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.Saksnummer;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.kobling.KoblingEntitet;
import no.nav.folketrygdloven.kalkulus.felles.FellesRestTjeneste;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.YtelseTyperKalkulusStøtter;
import no.nav.folketrygdloven.kalkulus.felles.metrikker.MetrikkerTjeneste;
import no.nav.folketrygdloven.kalkulus.håndtering.HåndtererApplikasjonTjeneste;
import no.nav.folketrygdloven.kalkulus.kobling.KoblingTjeneste;
import no.nav.folketrygdloven.kalkulus.response.v1.TilstandResponse;
import no.nav.folketrygdloven.kalkulus.response.v1.håndtering.OppdateringRespons;
import no.nav.folketrygdloven.kalkulus.rest.abac.FortsettBeregningRequestAbacDto;
import no.nav.folketrygdloven.kalkulus.rest.abac.HentBeregningsgrunnlagRequestAbacDto;
import no.nav.folketrygdloven.kalkulus.rest.abac.HåndterBeregningRequestAbacDto;
import no.nav.folketrygdloven.kalkulus.rest.abac.StartBeregningRequestAbacDto;
import no.nav.folketrygdloven.kalkulus.tjeneste.beregningsgrunnlag.RullTilbakeTjeneste;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;

@Produces(MediaType.APPLICATION_JSON)
@OpenAPIDefinition(tags = @Tag(name = "beregn"))
@Path("/kalkulus/v1")
@ApplicationScoped
@Transactional
public class OperereKalkulusRestTjeneste extends FellesRestTjeneste {

    private KoblingTjeneste koblingTjeneste;
    private BeregningStegTjeneste beregningStegTjeneste;
    private KalkulatorInputTjeneste kalkulatorInputTjeneste;
    private HåndtererApplikasjonTjeneste håndtererApplikasjonTjeneste;
    private RullTilbakeTjeneste rullTilbakeTjeneste;
    private MetrikkerTjeneste metrikkerTjeneste;

    public OperereKalkulusRestTjeneste() {
        // for CDI
    }

    @Inject
    public OperereKalkulusRestTjeneste(KoblingTjeneste koblingTjeneste,
                                       BeregningStegTjeneste beregningStegTjeneste,
                                       KalkulatorInputTjeneste kalkulatorInputTjeneste,
                                       HåndtererApplikasjonTjeneste håndtererApplikasjonTjeneste,
                                       RullTilbakeTjeneste rullTilbakeTjeneste,
                                       MetrikkerTjeneste metrikkerTjeneste) {
        super(metrikkerTjeneste);
        this.koblingTjeneste = koblingTjeneste;
        this.beregningStegTjeneste = beregningStegTjeneste;
        this.kalkulatorInputTjeneste = kalkulatorInputTjeneste;
        this.håndtererApplikasjonTjeneste = håndtererApplikasjonTjeneste;
        this.rullTilbakeTjeneste = rullTilbakeTjeneste;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/start")
    @Operation(description = "Utfører beregning basert på reqest", tags = "beregn",
            summary = ("Starter en beregning basert på gitt input."),
            responses = {@ApiResponse(description = "Liste med aksjonspunkter som har oppstått",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = TilstandResponse.class)))
            })
    @BeskyttetRessurs(action = CREATE, resource = BEREGNINGSGRUNNLAG)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public Response beregn(@NotNull @Valid StartBeregningRequestAbacDto spesifikasjon) {
        var startTx = Instant.now();
        var koblingReferanse = new KoblingReferanse(spesifikasjon.getEksternReferanse().getReferanse());
        var aktørId = new AktørId(spesifikasjon.getAktør().getIdent());
        var saksnummer = new Saksnummer(spesifikasjon.getSaksnummer());
        var ytelseTyperKalkulusStøtter = YtelseTyperKalkulusStøtter.fraKode(spesifikasjon.getYtelseSomSkalBeregnes().getKode());

        KoblingEntitet koblingEntitet = koblingTjeneste.finnEllerOpprett(koblingReferanse, ytelseTyperKalkulusStøtter, aktørId, saksnummer);

        boolean inputHarEndretSeg = kalkulatorInputTjeneste.lagreKalkulatorInput(koblingEntitet.getId(), spesifikasjon.getKalkulatorInput());

        BeregningsgrunnlagInput input = kalkulatorInputTjeneste.lagInput(koblingEntitet.getId(), Optional.empty());

        if (inputHarEndretSeg) {
            rullTilbakeTjeneste.rullTilbakeTilObligatoriskTilstandFørVedBehov(koblingEntitet.getId(), BeregningsgrunnlagTilstand.OPPRETTET);
        }

        TilstandResponse tilstandResponse = beregningStegTjeneste.fastsettBeregningsaktiviteter(input);

        logMetrikk("/kalkulus/v1/start", Duration.between(startTx, Instant.now()));
        return Response.ok(tilstandResponse).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/fortsett")
    @Operation(description = "Utfører beregning basert på reqest", tags = "beregn",
            summary = ("Fortsetter en beregning basert på stegInput."),
            responses = {@ApiResponse(description = "Liste med aksjonspunkter som har oppstått",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = TilstandResponse.class)))
            })
    @BeskyttetRessurs(action = UPDATE, resource = BEREGNINGSGRUNNLAG)
    public Response beregnVidere(@NotNull @Valid FortsettBeregningRequestAbacDto spesifikasjon) {
        var startTx = Instant.now();
        var koblingReferanse = new KoblingReferanse(spesifikasjon.getEksternReferanse());
        var ytelseTyperKalkulusStøtter = YtelseTyperKalkulusStøtter.fraKode(spesifikasjon.getYtelseSomSkalBeregnes().getKode());

        KoblingEntitet koblingEntitet = koblingTjeneste.hentFor(koblingReferanse, ytelseTyperKalkulusStøtter);
        BeregningsgrunnlagInput input = kalkulatorInputTjeneste.lagInputMedBeregningsgrunnlag(koblingEntitet.getId());
        TilstandResponse tilstandResponse = beregningStegTjeneste.beregnFor(spesifikasjon.getStegType(), input, koblingEntitet.getId());

        logMetrikk("/kalkulus/v1/fortsett", Duration.between(startTx, Instant.now()));
        return Response.ok(tilstandResponse).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/oppdater")
    @Operation(description = "Utfører beregning basert på reqest", tags = "beregn",
            summary = ("Oppdaterer beregningsgrunnlag basert på løsning av aksjonspunkt."),
            responses = {@ApiResponse(description = "DETTE MÅ VI FINNE UT AV",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = TilstandResponse.class)))
            })
    @BeskyttetRessurs(action = UPDATE, resource = BEREGNINGSGRUNNLAG)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public Response håndter(@NotNull @Valid HåndterBeregningRequestAbacDto spesifikasjon) {
        var startTx = Instant.now();
        var koblingReferanse = new KoblingReferanse(spesifikasjon.getEksternReferanse());
        Long koblingId = koblingTjeneste.hentKoblingId(koblingReferanse);
        OppdateringRespons respons = håndtererApplikasjonTjeneste.håndter(koblingId, spesifikasjon.getHåndterBeregning());
        logMetrikk("/kalkulus/v1/oppdater", Duration.between(startTx, Instant.now()));
        return Response.ok(Objects.requireNonNullElseGet(respons, OppdateringRespons::TOM_RESPONS)).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/deaktiver")
    @Operation(description = "Deaktiverer aktivt beregningsgrunnlag. Nullstiller beregning.", tags = "deaktiver", summary = ("Deaktiverer aktivt beregningsgrunnlag."))
    @BeskyttetRessurs(action = UPDATE, resource = BEREGNINGSGRUNNLAG)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public Response deaktiverBeregningsgrunnlag(@NotNull @Valid HentBeregningsgrunnlagRequestAbacDto spesifikasjon) {
        var startTx = Instant.now();
        var koblingReferanse = new KoblingReferanse(spesifikasjon.getKoblingReferanse());
        Long koblingId = koblingTjeneste.hentKoblingId(koblingReferanse);
        rullTilbakeTjeneste.deaktiverAktivtBeregningsgrunnlagOgInput(koblingId);
        logMetrikk("/kalkulus/v1/deaktiver", Duration.between(startTx, Instant.now()));
        return Response.ok().build();
    }

}
