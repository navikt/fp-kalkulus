package no.nav.folketrygdloven.kalkulus.rest;

import java.util.Objects;
import java.util.Optional;
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

import no.nav.folketrygdloven.fpkalkulus.kontrakt.migrering.MigrerBeregningsgrunnlagRequest;
import no.nav.folketrygdloven.fpkalkulus.kontrakt.migrering.MigrerBeregningsgrunnlagResponse;
import no.nav.folketrygdloven.kalkulus.beregning.MigreringTjeneste;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.AktørId;

import no.nav.folketrygdloven.kalkulus.felles.v1.Periode;
import no.nav.folketrygdloven.kalkulus.mapTilKontrakt.MapDetaljertBeregningsgrunnlag;

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
import no.nav.folketrygdloven.kalkulus.domene.entiteter.kobling.KoblingEntitet;
import no.nav.folketrygdloven.kalkulus.kobling.KoblingTjeneste;
import no.nav.folketrygdloven.kalkulus.kodeverk.FagsakYtelseType;
import no.nav.folketrygdloven.kalkulus.kopiering.KopierBeregningsgrunnlagTjeneste;
import no.nav.folketrygdloven.kalkulus.response.v1.KalkulusRespons;
import no.nav.folketrygdloven.kalkulus.response.v1.KopiResponse;
import no.nav.folketrygdloven.kalkulus.response.v1.TilstandResponse;
import no.nav.folketrygdloven.kalkulus.response.v1.håndtering.OppdateringListeRespons;
import no.nav.folketrygdloven.kalkulus.response.v1.håndtering.OppdateringRespons;
import no.nav.folketrygdloven.kalkulus.tjeneste.beregningsgrunnlag.RullTilbakeTjeneste;
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
    private MigreringTjeneste migreringTjeneste;

    public OperereKalkulusRestTjeneste() {
        // for CDI
    }

    @Inject
    public OperereKalkulusRestTjeneste(KoblingTjeneste koblingTjeneste,
                                       RullTilbakeTjeneste rullTilbakeTjeneste,
                                       OperereKalkulusOrkestrerer orkestrerer,
                                       KopierBeregningsgrunnlagTjeneste kopierTjeneste,
                                       MigreringTjeneste migreringTjeneste) {
        this.koblingTjeneste = koblingTjeneste;
        this.rullTilbakeTjeneste = rullTilbakeTjeneste;
        this.orkestrerer = orkestrerer;
        this.kopierTjeneste = kopierTjeneste;
        this.migreringTjeneste = migreringTjeneste;
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
        Optional<KoblingReferanse> originalKoblingRef =
            request.originalBehandlingUuid() == null ? Optional.empty() : Optional.of(new KoblingReferanse(request.originalBehandlingUuid()));
        var kobling = koblingTjeneste.finnEllerOpprett(new KoblingReferanse(request.behandlingUuid()),
            mapTilYtelseKodeverk(request.ytelseSomSkalBeregnes()), new AktørId(request.aktør().getIdent()), saksnummer, originalKoblingRef);
        validerIkkeAvsluttet(kobling);
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
    public Response oppdaterListe(@TilpassetAbacAttributt(supplierClass = HåndterBeregningRequestAbacSupplier.class) @NotNull @Valid HåndterBeregningRequestDto request) {
        var kobling = koblingTjeneste.hentKoblingOptional(new KoblingReferanse(request.behandlingUuid()))
            .orElseThrow(() -> new IllegalStateException(
                "Kan ikke løse avklaringsbehov i beregning uten en eksisterende kobling. Gjelder behandlingUuid " + request.behandlingUuid()));
        MDC.put("prosess_saksnummer", kobling.getSaksnummer().getVerdi());
        validerIkkeAvsluttet(kobling);
        KalkulusRespons respons;
        try {
            respons = orkestrerer.håndter(kobling, request.kalkulatorInput(), request.håndterBeregningDtoList());
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
        var kopt = koblingTjeneste.hentKoblingOptional(koblingReferanse)
            .orElseThrow(() -> new TekniskException("FT-47197",
                String.format("Pøver å deaktivere data på en kobling som ikke finnes, koblingRef %s", koblingReferanse)));
        validerIkkeAvsluttet(kopt);
        validerKoblingOgSaksnummer(kopt, saksnummer);
        rullTilbakeTjeneste.deaktiverAllKoblingdata(kopt.getId());
        return Response.ok().build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/avslutt")
    @Operation(description = "Markerer en kobling som avsluttet. Hindrer fremtidige endringer å koblingen.", tags = "avslutt", summary = ("Markerer en kobling som avsluttet."))
    @BeskyttetRessurs(actionType = ActionType.UPDATE, resourceType = ResourceType.FAGSAK)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public Response avslutt(@TilpassetAbacAttributt(supplierClass = EnkelFpkalkulusRequestAbacSupplier.class) @NotNull @Valid EnkelFpkalkulusRequestDto request) {
        var saksnummer = new Saksnummer(request.saksnummer().verdi());
        MDC.put("prosess_saksnummer", saksnummer.getVerdi());
        var koblingReferanse = new KoblingReferanse(request.behandlingUuid());
        MDC.put("prosess_koblingreferanse", koblingReferanse.getReferanse().toString());
        var kopt = koblingTjeneste.hentKoblingOptional(koblingReferanse)
            .orElseThrow(() -> new TekniskException("FT-47197",
                String.format("Prøver å markere en kobling som ikke finnes som avsluttet, koblingRef %s", koblingReferanse)));
        validerIkkeAvsluttet(kopt);
        validerKoblingOgSaksnummer(kopt, saksnummer);
        koblingTjeneste.markerKoblingSomAvsluttet(kopt);
        return Response.ok().build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/migrer")
    @Operation(description = "Migrer et grunnlag på en kobling.", tags = "migrer", summary = ("Migrer et grunnlag på en kobling."))
    @BeskyttetRessurs(actionType = ActionType.CREATE, resourceType = ResourceType.FAGSAK)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public Response migrer(@TilpassetAbacAttributt(supplierClass = MigrerBeregningsgrunnlagRequestAbacSupplier.class) @NotNull @Valid MigrerBeregningsgrunnlagRequest request) {
        var saksnummer = new Saksnummer(request.saksnummer().verdi());
        MDC.put("prosess_saksnummer", saksnummer.getVerdi());
        var koblingReferanse = new KoblingReferanse(request.behandlingUuid());
        MDC.put("prosess_koblingreferanse", koblingReferanse.getReferanse().toString());
        var kopt = koblingTjeneste.hentKoblingOptional(koblingReferanse);
        if (kopt.isPresent()) {
            throw new IllegalStateException("Prøver å migrere en kobling som allerede finnes");
        }
        Optional<KoblingReferanse> originalKoblingRef =
            request.originalBehandlingUuid() == null ? Optional.empty() : Optional.of(new KoblingReferanse(request.originalBehandlingUuid()));
        var kobling = koblingTjeneste.finnEllerOpprett(new KoblingReferanse(request.behandlingUuid()),
            mapTilYtelseKodeverk(request.ytelseSomSkalBeregnes()), new AktørId(request.aktør().getIdent()), saksnummer, originalKoblingRef);
        koblingTjeneste.markerKoblingSomAvsluttet(kobling);
        var migreringsresultat = migreringTjeneste.mapOgLagreGrunnlag(kobling, request.grunnlag());
        var respons = mapMigreringRespons(migreringsresultat);
        return Response.ok(respons).build();
    }

    private static MigrerBeregningsgrunnlagResponse mapMigreringRespons(MigreringTjeneste.Migreringsresultat migreringsresultat) {
        var entitet = migreringsresultat.grunnlag();
        var mappetGrunnlag = MapDetaljertBeregningsgrunnlag.map(entitet);
        var bbGrunnlag = entitet.getBeregningsgrunnlag()
            .flatMap(BeregningsgrunnlagEntitet::getBesteberegninggrunnlag)
            .map(MapDetaljertBeregningsgrunnlag::mapBesteberegningsgrunlag);
        var sporingerPeriode = migreringsresultat.periodeSporinger()
            .stream()
            .map(p -> new MigrerBeregningsgrunnlagResponse.RegelsporingPeriode(p.getRegelType(), p.getRegelEvaluering(), p.getRegelInput(),
                p.getRegelVersjon(), new Periode(p.getPeriode().getFomDato(), p.getPeriode().getTomDato())))
            .toList();
        var sporingerGrunnlag= migreringsresultat.grunnlagSporinger()
            .stream()
            .map(p -> new MigrerBeregningsgrunnlagResponse.RegelsporingGrunnlag(p.getRegelType(), p.getRegelEvaluering(), p.getRegelInput(),
                p.getRegelVersjon()))
            .toList();
        return new MigrerBeregningsgrunnlagResponse(mappetGrunnlag, bbGrunnlag.orElse(null), sporingerPeriode, sporingerGrunnlag);
    }

    private void validerKoblingOgSaksnummer(KoblingEntitet kobling, Saksnummer saksnummer) {
        if (!kobling.getSaksnummer().equals(saksnummer)) {
            throw new TekniskException("FT-47198", String.format(
                "Missmatch mellom forventet saksnummer og faktisk saksnummer for koblingRef %s. Forventet saksnummer var %s, faktisk saksnummer var %s",
                kobling.getKoblingReferanse(), saksnummer, kobling.getSaksnummer()));
        }
    }

    private void validerIkkeAvsluttet(KoblingEntitet kobling) {
        if (kobling.getErAvsluttet()) {
            throw new TekniskException("FT-49000", String.format(
                "Ikke tillatt å gjøre endringer på en avsluttet kobling. Gjelder kobling med referanse %s",
                kobling.getKoblingReferanse()));
        }
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

    public static class MigrerBeregningsgrunnlagRequestAbacSupplier implements Function<Object, AbacDataAttributter> {
        @Override
        public AbacDataAttributter apply(Object o) {
            var req = (no.nav.folketrygdloven.fpkalkulus.kontrakt.migrering.MigrerBeregningsgrunnlagRequest) o;
            return AbacDataAttributter.opprett().leggTil(StandardAbacAttributtType.BEHANDLING_UUID, req.behandlingUuid());
        }
    }

}
