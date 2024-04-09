package no.nav.folketrygdloven.kalkulus.rest;

import java.util.Objects;
import java.util.function.Function;

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

import org.slf4j.MDC;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import no.nav.folketrygdloven.fpkalkulus.kontrakt.BeregnRequestDto;
import no.nav.folketrygdloven.fpkalkulus.kontrakt.EnkelFpkalkulusRequestDto;
import no.nav.folketrygdloven.fpkalkulus.kontrakt.FpkalkulusYtelser;
import no.nav.folketrygdloven.fpkalkulus.kontrakt.HåndterBeregningRequestDto;
import no.nav.folketrygdloven.fpkalkulus.kontrakt.KopierBeregningsgrunnlagRequestDto;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.KoblingReferanse;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.Saksnummer;
import no.nav.folketrygdloven.kalkulus.kobling.KoblingTjeneste;
import no.nav.folketrygdloven.kalkulus.kodeverk.FagsakYtelseType;
import no.nav.folketrygdloven.kalkulus.kopiering.KopierBeregningsgrunnlagTjeneste;
import no.nav.folketrygdloven.kalkulus.response.v1.KalkulusRespons;
import no.nav.folketrygdloven.kalkulus.response.v1.KopiResponse;
import no.nav.folketrygdloven.kalkulus.response.v1.TilstandResponse;
import no.nav.folketrygdloven.kalkulus.response.v1.håndtering.OppdateringListeRespons;
import no.nav.folketrygdloven.kalkulus.response.v1.håndtering.OppdateringRespons;
import no.nav.folketrygdloven.kalkulus.tjeneste.beregningsgrunnlag.RullTilbakeTjeneste;
import no.nav.folketrygdloven.kalkulus.typer.AktørId;
import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
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
    @Operation(description = "Utfører beregning basert på reqest", tags = "beregn", summary = ("Starter en beregning basert på gitt input."), responses = {@ApiResponse(description = "Liste med avklaringsbehov som har oppstått per angitt eksternReferanse", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = TilstandResponse.class)))})
    @BeskyttetRessurs(actionType = ActionType.CREATE, resourceType = ResourceType.FAGSAK)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public Response beregn(@TilpassetAbacAttributt(supplierClass = BeregnRequestAbacSupplier.class) @NotNull @Valid BeregnRequestDto request) {
        var saksnummer = new Saksnummer(request.saksnummer().verdi());
        MDC.put("prosess_saksnummer", saksnummer.getVerdi());
        var kobling = koblingTjeneste.finnEllerOpprett(new KoblingReferanse(request.behandlingUuid()),
            mapTilYtelseKodeverk(request.ytelseSomSkalBeregnes()), new AktørId(request.aktør().getIdent()), saksnummer);
        TilstandResponse respons = (TilstandResponse) orkestrerer.beregn(request.stegType(), kobling, request.kalkulatorInput());
        return Response.ok(respons).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/kopier")
    @Operation(description = "Kopierer beregning fra eksisterende referanse til ny referanse. Kopien som opprettes er fra steget som defineres.", tags = "beregn", summary = ("Kopierer en beregning."), responses = {@ApiResponse(description = "Liste med kopierte referanser dersom alle koblinger er kopiert", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = KopiResponse.class)))})
    @BeskyttetRessurs(actionType = ActionType.UPDATE, resourceType = ResourceType.FAGSAK)
    public Response kopierBeregning(@TilpassetAbacAttributt(supplierClass = KopierBeregningsgrunnlagRequestAbacSupplier.class) @NotNull @Valid KopierBeregningsgrunnlagRequestDto request) {
        MDC.put("prosess_saksnummer", request.saksnummer().verdi());
        kopierTjeneste.kopierGrunnlagOgOpprettKoblinger(new KoblingReferanse(request.behandlingUuid()),
            new KoblingReferanse(request.originalBehandlingUuid()), new Saksnummer(request.saksnummer().verdi()), request.steg());
        return Response.ok().build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/avklaringsbehov")
    @Operation(description = "Oppdaterer beregningsgrunnlag for oppgitt liste", tags = "beregn", summary = ("Oppdaterer beregningsgrunnlag basert på løsning av avklaringsbehov for oppgitt liste."), responses = {@ApiResponse(description = "Liste med endringer som ble gjort under oppdatering", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = OppdateringListeRespons.class)))})
    @BeskyttetRessurs(actionType = ActionType.UPDATE, resourceType = ResourceType.FAGSAK)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public Response oppdaterListe(@TilpassetAbacAttributt(supplierClass = HåndterBeregningRequestAbacSupplier.class) @NotNull @Valid HåndterBeregningRequestDto spesifikasjon) {
        var kobling = koblingTjeneste.hentFor(new KoblingReferanse(spesifikasjon.behandlingUuid()))
            .orElseThrow(() -> new IllegalStateException(
                "Kan ikke løse avklaringsbehov i beregning uten en eksisterende kobling. Gjelder behandlingUuid " + spesifikasjon.behandlingUuid()));
        MDC.put("prosess_saksnummer", kobling.getSaksnummer().getVerdi());
        KalkulusRespons respons;
        try {
            respons = orkestrerer.håndter(kobling, spesifikasjon.kalkulatorInput(), spesifikasjon.håndterBeregningDtoList());
        } catch (UgyldigInputException e) {
            return Response.ok(new OppdateringListeRespons(true)).build();
        }
        var test = (OppdateringRespons) respons;
        return Response.ok(Objects.requireNonNull(test)).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/deaktiver")
    @Operation(description = "Deaktiverer aktivt beregningsgrunnlag. Nullstiller beregning.", tags = "deaktiver", summary = ("Deaktiverer aktivt beregningsgrunnlag."))
    @BeskyttetRessurs(actionType = ActionType.UPDATE, resourceType = ResourceType.FAGSAK)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public Response deaktiverBeregningsgrunnlag(@TilpassetAbacAttributt(supplierClass = EnkelFpkalkulusRequestAbacSupplier.class) @NotNull @Valid EnkelFpkalkulusRequestDto request) {
        var saksnummer = new Saksnummer(request.saksnummer().verdi());
        MDC.put("prosess_saksnummer", saksnummer.getVerdi());
        var koblingReferanse = new KoblingReferanse(request.behandlingUuid());
        MDC.put("prosess_koblingreferanse", koblingReferanse.getReferanse().toString());
        var kopt = koblingTjeneste.hentFor(koblingReferanse)
            .orElseThrow(() -> new TekniskException("FT-47197",
                String.format("Pøver å deaktivere data på en kobling som ikke finnes, koblingRef %s", koblingReferanse)));
        if (!kopt.getSaksnummer().equals(saksnummer)) {
            throw new TekniskException("FT-47198", String.format(
                "Missmatch mellom forventet saksnummer og faktisk saksnummer for koblingRef %s. Forventet saksnummer var %s, faktisk saksnummer var %s",
                koblingReferanse, saksnummer, kopt.getSaksnummer()));
        }
        rullTilbakeTjeneste.deaktiverAllKoblingdata(kopt.getId());
        return Response.ok().build();
    }

    private FagsakYtelseType mapTilYtelseKodeverk(FpkalkulusYtelser ytelse) {
        return switch (ytelse) {
            case FORELDREPENGER -> FagsakYtelseType.FORELDREPENGER;
            case SVANGERSKAPSPENGER -> FagsakYtelseType.SVANGERSKAPSPENGER;
        };
    }

    public static class BeregnRequestAbacSupplier implements Function<Object, AbacDataAttributter> {
        @Override
        public AbacDataAttributter apply(Object o) {
            var req = (BeregnRequestDto) o;
            return AbacDataAttributter.opprett().leggTil(StandardAbacAttributtType.BEHANDLING_UUID, req.behandlingUuid());
        }
    }

    public static class HåndterBeregningRequestAbacSupplier implements Function<Object, AbacDataAttributter> {
        @Override
        public AbacDataAttributter apply(Object o) {
            var req = (HåndterBeregningRequestDto) o;
            return AbacDataAttributter.opprett().leggTil(StandardAbacAttributtType.BEHANDLING_UUID, req.behandlingUuid());
        }
    }

    public static class KopierBeregningsgrunnlagRequestAbacSupplier implements Function<Object, AbacDataAttributter> {
        @Override
        public AbacDataAttributter apply(Object o) {
            var req = (KopierBeregningsgrunnlagRequestDto) o;
            return AbacDataAttributter.opprett().leggTil(StandardAbacAttributtType.BEHANDLING_UUID, req.behandlingUuid());
        }
    }

    public static class EnkelFpkalkulusRequestAbacSupplier implements Function<Object, AbacDataAttributter> {
        @Override
        public AbacDataAttributter apply(Object o) {
            var req = (no.nav.folketrygdloven.fpkalkulus.kontrakt.EnkelFpkalkulusRequestDto) o;
            return AbacDataAttributter.opprett().leggTil(StandardAbacAttributtType.BEHANDLING_UUID, req.behandlingUuid());
        }
    }

}
