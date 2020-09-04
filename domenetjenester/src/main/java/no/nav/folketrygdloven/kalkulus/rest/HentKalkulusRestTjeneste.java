package no.nav.folketrygdloven.kalkulus.rest;

import static no.nav.folketrygdloven.kalkulus.sikkerhet.KalkulusBeskyttetRessursAttributt.BEREGNINGSGRUNNLAG;
import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursActionAttributt.READ;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
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

import org.jboss.logging.MDC;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import no.nav.folketrygdloven.kalkulator.guitjenester.BeregningsgrunnlagDtoTjeneste;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagRestInput;
import no.nav.folketrygdloven.kalkulator.kontrakt.v1.ArbeidsgiverOpplysningerDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.ArbeidsforholdReferanseDto;
import no.nav.folketrygdloven.kalkulus.beregning.KalkulatorInputTjeneste;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagGrunnlagEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.KoblingReferanse;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.kobling.KoblingEntitet;
import no.nav.folketrygdloven.kalkulus.felles.FellesRestTjeneste;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.YtelseTyperKalkulusStøtter;
import no.nav.folketrygdloven.kalkulus.felles.metrikker.MetrikkerTjeneste;
import no.nav.folketrygdloven.kalkulus.kobling.KoblingTjeneste;
import no.nav.folketrygdloven.kalkulus.mapTilKontrakt.MapBeregningsgrunnlag;
import no.nav.folketrygdloven.kalkulus.mapTilKontrakt.MapBeregningsgrunnlagFRISINN;
import no.nav.folketrygdloven.kalkulus.mapTilKontrakt.MapDetaljertBeregningsgrunnlag;
import no.nav.folketrygdloven.kalkulus.mappers.MapIAYTilKalulator;
import no.nav.folketrygdloven.kalkulus.request.v1.HentBeregningsgrunnlagDtoForGUIRequest;
import no.nav.folketrygdloven.kalkulus.request.v1.HentBeregningsgrunnlagRequest;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.BeregningsgrunnlagPrReferanse;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.BeregningsgrunnlagListe;
import no.nav.folketrygdloven.kalkulus.rest.abac.HentBeregningsgrunnlagDtoForGUIRequestAbacDto;
import no.nav.folketrygdloven.kalkulus.rest.abac.HentBeregningsgrunnlagDtoListeForGUIRequestAbacDto;
import no.nav.folketrygdloven.kalkulus.rest.abac.HentBeregningsgrunnlagListeRequestAbacDto;
import no.nav.folketrygdloven.kalkulus.rest.abac.HentBeregningsgrunnlagRequestAbacDto;
import no.nav.folketrygdloven.kalkulus.tjeneste.beregningsgrunnlag.BeregningsgrunnlagRepository;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;

@Produces(MediaType.APPLICATION_JSON)
@OpenAPIDefinition(tags = @Tag(name = "beregningsgrunnlag"))
@Path("/kalkulus/v1")
@ApplicationScoped
@Transactional
public class HentKalkulusRestTjeneste extends FellesRestTjeneste {

    private KoblingTjeneste koblingTjeneste;
    private BeregningsgrunnlagRepository beregningsgrunnlagRepository;
    private KalkulatorInputTjeneste kalkulatorInputTjeneste;
    private BeregningsgrunnlagDtoTjeneste beregningsgrunnlagDtoTjeneste;

    public HentKalkulusRestTjeneste() {
        // for CDI
    }

    @Inject
    public HentKalkulusRestTjeneste(KoblingTjeneste koblingTjeneste,
                                    BeregningsgrunnlagRepository beregningsgrunnlagRepository,
                                    KalkulatorInputTjeneste kalkulatorInputTjeneste,
                                    BeregningsgrunnlagDtoTjeneste beregningsgrunnlagDtoTjeneste, MetrikkerTjeneste metrikkerTjeneste) {
        super(metrikkerTjeneste);
        this.koblingTjeneste = koblingTjeneste;
        this.beregningsgrunnlagRepository = beregningsgrunnlagRepository;
        this.kalkulatorInputTjeneste = kalkulatorInputTjeneste;
        this.beregningsgrunnlagDtoTjeneste = beregningsgrunnlagDtoTjeneste;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(description = "Hent beregningsgrunnlag for angitt behandling", summary = ("Returnerer beregningsgrunnlag for behandling."), tags = "beregningsgrunnlag")
    @BeskyttetRessurs(action = READ, resource = BEREGNINGSGRUNNLAG)
    @Path("/fastsatt")
    @SuppressWarnings({ "findsecbugs:JAXRS_ENDPOINT", "resource" })
    public Response hentFastsattBeregningsgrunnlag(@NotNull @Valid HentBeregningsgrunnlagRequestAbacDto spesifikasjon) {
        var startTx = Instant.now();
        final Response response = hentFastsattBeregningsgrunnlagForSpesifikasjon(spesifikasjon)
            .map(bgDto -> Response.ok(bgDto).build())
            .orElse(Response.noContent().build());
        logMetrikk("/kalkulus/v1/fastsatt", Duration.between(startTx, Instant.now()));
        return response;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(description = "Hent beregningsgrunnlag for angitt behandling", summary = ("Returnerer beregningsgrunnlag for behandling."), tags = "beregningsgrunnlag")
    @BeskyttetRessurs(action = READ, resource = BEREGNINGSGRUNNLAG)
    @Path("/fastsattListe")
    @SuppressWarnings({ "findsecbugs:JAXRS_ENDPOINT" })
    public Response hentFastsattBeregningsgrunnlagListe(@NotNull @Valid HentBeregningsgrunnlagListeRequestAbacDto spesifikasjon) {
        var startTx = Instant.now();
        var dtoPrReferanse = spesifikasjon.getRequestPrReferanse().stream()
            .map(spes -> this.hentFastsattBeregningsgrunnlagForSpesifikasjon(spes)
                .map(dto -> new BeregningsgrunnlagPrReferanse<>(spes.getKoblingReferanse(), dto))
                .orElse(new BeregningsgrunnlagPrReferanse<no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.fastsatt.BeregningsgrunnlagDto>(
                    spes.getKoblingReferanse(), null)))
            .collect(Collectors.toList());
        logMetrikk("/kalkulus/v1/fastsattListe", Duration.between(startTx, Instant.now()));
        return Response.ok(new no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.fastsatt.BeregningsgrunnlagListe(dtoPrReferanse)).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(description = "Hent aktivt BeregningsgrunnlagGrunnlag for angitt behandling", summary = ("Returnerer aktivt BeregningsgrunnlagGrunnlag for behandling."), tags = "beregningsgrunnlag")
    @BeskyttetRessurs(action = READ, resource = BEREGNINGSGRUNNLAG)
    @Path("/grunnlag")
    @SuppressWarnings({ "findsecbugs:JAXRS_ENDPOINT", "resource" })
    public Response hentAktivtBeregningsgrunnlagGrunnlag(@NotNull @Valid HentBeregningsgrunnlagRequestAbacDto spesifikasjon) {
        var startTx = Instant.now();
        final Response response = hentBeregningsgrunnlagGrunnlagEntitetForSpesifikasjon(spesifikasjon).stream()
            .map(bg -> MapDetaljertBeregningsgrunnlag.mapGrunnlag(bg, spesifikasjon.getInkluderRegelSporing()))
            .map(bgDto -> Response.ok(bgDto).build())
            .findFirst()
            .orElse(Response.noContent().build());
        logMetrikk("/kalkulus/v1/grunnlag", Duration.between(startTx, Instant.now()));
        return response;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(description = "Hent beregningsgrunnlagDto for angitt behandling som brukes frontend", summary = ("Returnerer beregningsgrunnlagDto for behandling."), tags = "beregningsgrunnlag")
    @BeskyttetRessurs(action = READ, resource = BEREGNINGSGRUNNLAG)
    @Path("/beregningsgrunnlag")
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public Response hentBeregningsgrunnlagDto(@NotNull @Valid HentBeregningsgrunnlagDtoForGUIRequestAbacDto spesifikasjon) {
        var startTx = Instant.now();
        Response response;
        response = hentBeregningsgrunnlagDtoForGUIForSpesifikasjon(spesifikasjon)
            .map(Response::ok)
            .orElse(Response.noContent())
            .build();
        logMetrikk("/kalkulus/v1/beregningsgrunnlag", Duration.between(startTx, Instant.now()));
        return response;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(description = "Hent beregningsgrunnlagDto for angitt behandling som brukes frontend", summary = ("Returnerer beregningsgrunnlagDto for behandling."), tags = "beregningsgrunnlag")
    @BeskyttetRessurs(action = READ, resource = BEREGNINGSGRUNNLAG)
    @Path("/beregningsgrunnlagListe")
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public Response hentBeregningsgrunnlagDtoListe(@NotNull @Valid HentBeregningsgrunnlagDtoListeForGUIRequestAbacDto spesifikasjon) {
        var startTx = Instant.now();
        List<BeregningsgrunnlagPrReferanse<BeregningsgrunnlagDto>> dtoPrReferanse = spesifikasjon.getRequestPrReferanse().stream()
            .map(spes -> this.hentBeregningsgrunnlagDtoForGUIForSpesifikasjon(spes)
                .map(dto -> new BeregningsgrunnlagPrReferanse<>(spes.getKoblingReferanse(), dto))
                .orElse(new BeregningsgrunnlagPrReferanse<BeregningsgrunnlagDto>(spes.getKoblingReferanse(), null)))
            .collect(Collectors.toList());
        logMetrikk("/kalkulus/v1/beregningsgrunnlag", Duration.between(startTx, Instant.now()));
        return Response.ok(new BeregningsgrunnlagListe(dtoPrReferanse)).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(description = "Hent grunnlag for frisinn", summary = ("Returnerer frisinngrunnlag for behandling."), tags = "beregningsgrunnlag")
    @BeskyttetRessurs(action = READ, resource = BEREGNINGSGRUNNLAG)
    @Path("/frisinnGrunnlag")
    @SuppressWarnings({ "findsecbugs:JAXRS_ENDPOINT", "resource" })
    public Response hentFrisinnGrunnlag(@NotNull @Valid HentBeregningsgrunnlagRequestAbacDto spesifikasjon) {
        var startTx = Instant.now();
        var koblingReferanse = new KoblingReferanse(spesifikasjon.getKoblingReferanse());
        koblingTjeneste.hentFor(koblingReferanse).map(KoblingEntitet::getSaksnummer)
                .ifPresent(saksnummer -> MDC.put("prosess_saksnummer", saksnummer.getVerdi()));
        var ytelseTyperKalkulusStøtter = YtelseTyperKalkulusStøtter.fraKode(spesifikasjon.getYtelseSomSkalBeregnes().getKode());
        Optional<Long> koblingId = koblingTjeneste.hentKoblingHvisFinnes(koblingReferanse, ytelseTyperKalkulusStøtter);
        if (koblingId.isEmpty() || !harKalkulatorInput(koblingId)) {
            return Response.noContent().build();
        }
        Optional<BeregningsgrunnlagGrunnlagEntitet> beregningsgrunnlagGrunnlagEntitet = beregningsgrunnlagRepository
            .hentBeregningsgrunnlagGrunnlagEntitet(koblingId.get());
        BeregningsgrunnlagInput input = kalkulatorInputTjeneste.lagInputMedBeregningsgrunnlagUtenSporingslogg(koblingId.get());
        final Response response = beregningsgrunnlagGrunnlagEntitet.stream()
            .flatMap(gr -> gr.getBeregningsgrunnlag().stream())
            .map(bg -> MapBeregningsgrunnlagFRISINN.map(bg, input.getIayGrunnlag().getOppgittOpptjening(), input.getYtelsespesifiktGrunnlag()))
            .map(bgDto -> Response.ok(bgDto).build())
            .findFirst()
            .orElse(Response.noContent().build());
        logMetrikk("/kalkulus/v1/frisinnGrunnlag", Duration.between(startTx, Instant.now()));
        return response;
    }

    private Optional<no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.fastsatt.BeregningsgrunnlagDto> hentFastsattBeregningsgrunnlagForSpesifikasjon(@NotNull @Valid HentBeregningsgrunnlagRequest spesifikasjon) {
        return hentBeregningsgrunnlagGrunnlagEntitetForSpesifikasjon(spesifikasjon).stream()
            .filter(grunnlag -> grunnlag.getBeregningsgrunnlagTilstand().equals(BeregningsgrunnlagTilstand.FASTSATT))
            .flatMap(gr -> gr.getBeregningsgrunnlag().stream())
            .map(MapBeregningsgrunnlag::map)
            .findFirst();
    }

    private Optional<BeregningsgrunnlagGrunnlagEntitet> hentBeregningsgrunnlagGrunnlagEntitetForSpesifikasjon(HentBeregningsgrunnlagRequest spesifikasjon) {
        var koblingReferanse = new KoblingReferanse(spesifikasjon.getKoblingReferanse());
        koblingTjeneste.hentFor(koblingReferanse).map(KoblingEntitet::getSaksnummer)
                .ifPresent(saksnummer -> MDC.put("prosess_saksnummer", saksnummer.getVerdi()));
        var ytelseTyperKalkulusStøtter = YtelseTyperKalkulusStøtter.fraKode(spesifikasjon.getYtelseSomSkalBeregnes().getKode());
        Optional<Long> koblingId = koblingTjeneste.hentKoblingHvisFinnes(koblingReferanse, ytelseTyperKalkulusStøtter);
        if (!harKalkulatorInput(koblingId)) {
            return Optional.empty();
        }
        return beregningsgrunnlagRepository.hentBeregningsgrunnlagGrunnlagEntitet(koblingId.get());
    }

    private Optional<BeregningsgrunnlagDto> hentBeregningsgrunnlagDtoForGUIForSpesifikasjon(HentBeregningsgrunnlagDtoForGUIRequest spesifikasjon) {
        var koblingReferanse = new KoblingReferanse(spesifikasjon.getKoblingReferanse());
        koblingTjeneste.hentFor(koblingReferanse).map(KoblingEntitet::getSaksnummer)
                .ifPresent(saksnummer -> MDC.put("prosess_saksnummer", saksnummer.getVerdi()));
        var ytelseTyperKalkulusStøtter = YtelseTyperKalkulusStøtter.fraKode(spesifikasjon.getYtelseSomSkalBeregnes().getKode());
        Optional<Long> koblingId = koblingTjeneste.hentKoblingHvisFinnes(koblingReferanse, ytelseTyperKalkulusStøtter);
        if (koblingId.isEmpty() || !harKalkulatorInput(koblingId)) {
            return Optional.empty();
        }
        BeregningsgrunnlagInput beregningsgrunnlagInput = kalkulatorInputTjeneste.lagInputForGUI(koblingId.get());
        if (beregningsgrunnlagInput.getBeregningsgrunnlagGrunnlag().getBeregningsgrunnlag().isEmpty()) {
            return Optional.empty();
        } else {
            Set<ArbeidsforholdReferanseDto> referanser = MapIAYTilKalulator.mapArbeidsgiverReferanser(spesifikasjon.getReferanser());
            List<ArbeidsgiverOpplysningerDto> arbeidsgiverOpplysninger = MapIAYTilKalulator
                .mapArbeidsgiverOpplysninger(spesifikasjon.getArbeidsgiverOpplysninger());
            BeregningsgrunnlagRestInput restInput = new BeregningsgrunnlagRestInput(beregningsgrunnlagInput, arbeidsgiverOpplysninger, referanser);
            BeregningsgrunnlagDto beregningsgrunnlagDto = beregningsgrunnlagDtoTjeneste.lagBeregningsgrunnlagDto(restInput);
            beregningsgrunnlagDto.setVilkårsperiodeFom(spesifikasjon.getVilkårsperiodeFom());
            return Optional.of(beregningsgrunnlagDto);
        }
    }

    private Boolean harKalkulatorInput(Optional<Long> koblingId) {
        return koblingId.map(id -> beregningsgrunnlagRepository.hvisEksistererKalkulatorInput(id)).orElse(false);
    }

}
