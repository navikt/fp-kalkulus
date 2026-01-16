package no.nav.folketrygdloven.kalkulus.domene.rest;

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

import no.nav.foreldrepenger.kalkulus.kontrakt.request.EnkelBeregnRequestDto;
import no.nav.foreldrepenger.kalkulus.kontrakt.request.EnkelFpkalkulusRequestDto;
import no.nav.foreldrepenger.kalkulus.kontrakt.request.EnkelHåndterBeregningRequestDto;
import no.nav.foreldrepenger.kalkulus.kontrakt.request.EnkelKopierBeregningsgrunnlagRequestDto;
import no.nav.foreldrepenger.kalkulus.kontrakt.request.KopierFastsattGrunnlagRequest;
import no.nav.foreldrepenger.kalkulus.kontrakt.response.TilstandResponse;

import no.nav.foreldrepenger.kalkulus.kontrakt.response.håndtering.OppdateringListeRespons;

import org.slf4j.MDC;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.AktørId;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.KoblingReferanse;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.Saksnummer;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.kobling.KoblingEntitet;
import no.nav.folketrygdloven.kalkulus.domene.kobling.KoblingTjeneste;
import no.nav.folketrygdloven.kalkulus.kodeverk.FagsakYtelseType;
import no.nav.folketrygdloven.kalkulus.domene.kopiering.KopierBeregningsgrunnlagTjeneste;
import no.nav.folketrygdloven.kalkulus.domene.tjeneste.beregningsgrunnlag.RullTilbakeTjeneste;
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
    private static final String PROSESS_SAKSNUMMER = "prosess_saksnummer";
    private static final String PROSESS_KOBLING_REF = "prosess_koblingreferanse";

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
    @BeskyttetRessurs(actionType = ActionType.CREATE, resourceType = ResourceType.FAGSAK, sporingslogg = true)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public Response beregn(@TilpassetAbacAttributt(supplierClass = BeregnRequestAbacSupplier.class) @NotNull @Valid EnkelBeregnRequestDto request) {
        validerYtelse(request.ytelseSomSkalBeregnes());
        var saksnummer = new Saksnummer(request.saksnummer().verdi());
        MDC.put(PROSESS_SAKSNUMMER, saksnummer.getVerdi());
        Optional<KoblingReferanse> originalKoblingRef =
            request.originalBehandlingUuid() == null ? Optional.empty() : Optional.of(new KoblingReferanse(request.originalBehandlingUuid()));
        var kobling = koblingTjeneste.finnEllerOpprett(new KoblingReferanse(request.behandlingUuid()),
            request.ytelseSomSkalBeregnes(), new AktørId(request.aktør().getIdent()), saksnummer, originalKoblingRef);
        validerIkkeAvsluttet(kobling);
        TilstandResponse respons = (TilstandResponse) orkestrerer.beregn(request.stegType(), kobling, request.kalkulatorInput());
        return Response.ok(respons).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/kopier")
    @Operation(description = "Kopierer beregning fra eksisterende referanse til ny referanse for å kunne fortsette beregningen fra angitt steg.", tags = "beregn", summary = ("Kopierer en beregning."))
    @BeskyttetRessurs(actionType = ActionType.UPDATE, resourceType = ResourceType.FAGSAK, sporingslogg = true)
    public Response kopierBeregning(@TilpassetAbacAttributt(supplierClass = KopierBeregningsgrunnlagRequestAbacSupplier.class) @NotNull @Valid EnkelKopierBeregningsgrunnlagRequestDto request) {
        MDC.put(PROSESS_SAKSNUMMER, request.saksnummer().verdi());
        kopierTjeneste.kopierBeregningsgrunlagForStartISteg(new KoblingReferanse(request.behandlingUuid()),
            new KoblingReferanse(request.originalBehandlingUuid()), new Saksnummer(request.saksnummer().verdi()), request.steg(), request.kalkulatorInput());
        return Response.ok().build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/kopier-fastsatt")
    @Operation(description = "Kopierer fastsatt beregningsgrunnlag fra eksisterende referanse til ny referanse. Forutsetter at originalBehandlingUuid har et fastsatt grunnlag og at koblingen er avsluttet.", tags = "beregn", summary = ("Kopierer en fastsatt beregning."))
    @BeskyttetRessurs(actionType = ActionType.UPDATE, resourceType = ResourceType.FAGSAK, sporingslogg = true)
    public Response kopierFastsattBeregning(@TilpassetAbacAttributt(supplierClass = KopierFastsattGrunnlagRequestAbacSupplier.class) @NotNull @Valid KopierFastsattGrunnlagRequest request) {
        MDC.put(PROSESS_SAKSNUMMER, request.saksnummer().verdi());
        kopierTjeneste.kopierFastsattBeregningsgrunnlag(new KoblingReferanse(request.behandlingUuid()),
            new KoblingReferanse(request.originalBehandlingUuid()),
            new Saksnummer(request.saksnummer().verdi()));
        return Response.ok().build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/avklaringsbehov")
    @Operation(description = "Oppdaterer beregningsgrunnlag for oppgitt liste", tags = "beregn", summary = ("Oppdaterer beregningsgrunnlag basert på løsning av avklaringsbehov for oppgitt liste."), responses = {@ApiResponse(description = "Liste med endringer som ble gjort under oppdatering", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = OppdateringListeRespons.class)))})
    @BeskyttetRessurs(actionType = ActionType.UPDATE, resourceType = ResourceType.FAGSAK, sporingslogg = true)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public Response oppdaterListe(@TilpassetAbacAttributt(supplierClass = HåndterBeregningRequestAbacSupplier.class) @NotNull @Valid EnkelHåndterBeregningRequestDto request) {
        var kobling = koblingTjeneste.hentKoblingOptional(new KoblingReferanse(request.behandlingUuid()))
            .orElseThrow(() -> new IllegalStateException(
                "Kan ikke løse avklaringsbehov i beregning uten en eksisterende kobling. Gjelder behandlingUuid " + request.behandlingUuid()));
        MDC.put(PROSESS_SAKSNUMMER, kobling.getSaksnummer().getVerdi());
        validerIkkeAvsluttet(kobling);
        try {
            var respons = orkestrerer.håndter(kobling, request.kalkulatorInput(), request.håndterBeregningDtoList());
            return Response.ok(respons).build();
        } catch (UgyldigInputException e) {
            return Response.ok(new OppdateringListeRespons(true)).build();
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/deaktiver")
    @Operation(description = "Deaktiverer aktivt beregningsgrunnlag. Nullstiller beregning.", tags = "deaktiver", summary = ("Deaktiverer aktivt beregningsgrunnlag."))
    @BeskyttetRessurs(actionType = ActionType.UPDATE, resourceType = ResourceType.FAGSAK, sporingslogg = true)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public Response deaktiverBeregningsgrunnlag(@TilpassetAbacAttributt(supplierClass = EnkelFpkalkulusRequestAbacSupplier.class) @NotNull @Valid EnkelFpkalkulusRequestDto request) {
        var saksnummer = new Saksnummer(request.saksnummer().verdi());
        MDC.put(PROSESS_SAKSNUMMER, saksnummer.getVerdi());
        var koblingReferanse = new KoblingReferanse(request.behandlingUuid());
        MDC.put(PROSESS_KOBLING_REF, koblingReferanse.getReferanse().toString());
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
    @BeskyttetRessurs(actionType = ActionType.UPDATE, resourceType = ResourceType.FAGSAK, sporingslogg = true)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public Response avslutt(@TilpassetAbacAttributt(supplierClass = EnkelFpkalkulusRequestAbacSupplier.class) @NotNull @Valid EnkelFpkalkulusRequestDto request) {
        var saksnummer = new Saksnummer(request.saksnummer().verdi());
        MDC.put(PROSESS_SAKSNUMMER, saksnummer.getVerdi());
        var koblingReferanse = new KoblingReferanse(request.behandlingUuid());
        MDC.put(PROSESS_KOBLING_REF, koblingReferanse.getReferanse().toString());
        var kopt = koblingTjeneste.hentKoblingOptional(koblingReferanse)
            .orElseThrow(() -> new TekniskException("FT-47197",
                String.format("Prøver å markere en kobling som ikke finnes som avsluttet, koblingRef %s", koblingReferanse)));
        validerIkkeAvsluttet(kopt);
        validerKoblingOgSaksnummer(kopt, saksnummer);
        koblingTjeneste.markerKoblingSomAvsluttet(kopt);
        return Response.ok().build();
    }

    private void validerYtelse(FagsakYtelseType fagsakYtelseType) {
        if (!fagsakYtelseType.equals(FagsakYtelseType.FORELDREPENGER) && !fagsakYtelseType.equals(FagsakYtelseType.SVANGERSKAPSPENGER)) {
            throw new TekniskException("FT-41000", String.format(
                "Forsøk på å kalle fpkalkulus med ugyldig ytelse  %s", fagsakYtelseType));
        }
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

    public static class BeregnRequestAbacSupplier implements Function<Object, AbacDataAttributter> {
        @Override
        public AbacDataAttributter apply(Object o) {
            var req = (EnkelBeregnRequestDto) o;
            return AbacDataAttributter.opprett()
                .leggTil(StandardAbacAttributtType.BEHANDLING_UUID, req.behandlingUuid())
                .leggTil(StandardAbacAttributtType.SAKSNUMMER, req.saksnummer().verdi());
        }
    }

    public static class HåndterBeregningRequestAbacSupplier implements Function<Object, AbacDataAttributter> {
        @Override
        public AbacDataAttributter apply(Object o) {
            var req = (EnkelHåndterBeregningRequestDto) o;
            return AbacDataAttributter.opprett()
                .leggTil(StandardAbacAttributtType.BEHANDLING_UUID, req.behandlingUuid())
                .leggTil(StandardAbacAttributtType.SAKSNUMMER, req.saksnummer().verdi());
        }
    }

    public static class KopierBeregningsgrunnlagRequestAbacSupplier implements Function<Object, AbacDataAttributter> {
        @Override
        public AbacDataAttributter apply(Object o) {
            var req = (EnkelKopierBeregningsgrunnlagRequestDto) o;
            return AbacDataAttributter.opprett()
                .leggTil(StandardAbacAttributtType.BEHANDLING_UUID, req.behandlingUuid())
                .leggTil(StandardAbacAttributtType.SAKSNUMMER, req.saksnummer().verdi());
        }
    }

    public static class EnkelFpkalkulusRequestAbacSupplier implements Function<Object, AbacDataAttributter> {
        @Override
        public AbacDataAttributter apply(Object o) {
            var req = (EnkelFpkalkulusRequestDto) o;
            return AbacDataAttributter.opprett()
                .leggTil(StandardAbacAttributtType.BEHANDLING_UUID, req.behandlingUuid())
                .leggTil(StandardAbacAttributtType.SAKSNUMMER, req.saksnummer().verdi());
        }
    }

    public static class KopierFastsattGrunnlagRequestAbacSupplier implements Function<Object, AbacDataAttributter> {
        @Override
        public AbacDataAttributter apply(Object o) {
            var req = (KopierFastsattGrunnlagRequest) o;
            return AbacDataAttributter.opprett()
                .leggTil(StandardAbacAttributtType.BEHANDLING_UUID, req.behandlingUuid())
                .leggTil(StandardAbacAttributtType.SAKSNUMMER, req.saksnummer().verdi());
        }
    }

}
