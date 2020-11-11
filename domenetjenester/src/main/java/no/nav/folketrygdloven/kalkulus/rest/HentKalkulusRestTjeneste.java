package no.nav.folketrygdloven.kalkulus.rest;

import static no.nav.folketrygdloven.kalkulus.sikkerhet.KalkulusBeskyttetRessursAttributt.BEREGNINGSGRUNNLAG;
import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursActionAttributt.READ;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Optional;
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
import javax.ws.rs.core.Response.Status;

import org.jboss.logging.MDC;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import no.nav.folketrygdloven.kalkulator.guitjenester.BeregningsgrunnlagDtoTjeneste;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagGUIInput;
import no.nav.folketrygdloven.kalkulus.beregning.GUIBeregningsgrunnlagInputTjeneste;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagGrunnlagEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.KoblingReferanse;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.kobling.KoblingEntitet;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.YtelseTyperKalkulusStøtter;
import no.nav.folketrygdloven.kalkulus.kobling.KoblingTjeneste;
import no.nav.folketrygdloven.kalkulus.mapTilKontrakt.MapBeregningsgrunnlag;
import no.nav.folketrygdloven.kalkulus.mapTilKontrakt.MapBeregningsgrunnlagFRISINN;
import no.nav.folketrygdloven.kalkulus.mapTilKontrakt.MapDetaljertBeregningsgrunnlag;
import no.nav.folketrygdloven.kalkulus.mappers.MapIAYTilKalulator;
import no.nav.folketrygdloven.kalkulus.request.v1.HentBeregningsgrunnlagDtoForGUIRequest;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.BeregningsgrunnlagPrReferanse;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.BeregningsgrunnlagListe;
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
public class HentKalkulusRestTjeneste {

    private KoblingTjeneste koblingTjeneste;
    private BeregningsgrunnlagRepository beregningsgrunnlagRepository;
    private GUIBeregningsgrunnlagInputTjeneste kalkulatorInputTjeneste;
    private BeregningsgrunnlagDtoTjeneste beregningsgrunnlagDtoTjeneste;

    public HentKalkulusRestTjeneste() {
        // for CDI
    }

    @Inject
    public HentKalkulusRestTjeneste(KoblingTjeneste koblingTjeneste,
                                    BeregningsgrunnlagRepository beregningsgrunnlagRepository,
                                    GUIBeregningsgrunnlagInputTjeneste kalkulatorInputTjeneste,
                                    BeregningsgrunnlagDtoTjeneste beregningsgrunnlagDtoTjeneste) {
        this.koblingTjeneste = koblingTjeneste;
        this.beregningsgrunnlagRepository = beregningsgrunnlagRepository;
        this.kalkulatorInputTjeneste = kalkulatorInputTjeneste;
        this.beregningsgrunnlagDtoTjeneste = beregningsgrunnlagDtoTjeneste;
    }


    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(description = "Hent aktive BeregningsgrunnlagGrunnlag for angitte referanser", summary = ("Returnerer aktive BeregningsgrunnlagGrunnlag for angitte kobling referanser."), tags = "beregningsgrunnlag")
    @BeskyttetRessurs(action = READ, resource = BEREGNINGSGRUNNLAG)
    @Path("/grunnlag/bolk")
    @SuppressWarnings({ "findsecbugs:JAXRS_ENDPOINT", "resource" })
    public Response hentAktiveBeregningsgrunnlagGrunnlag(@NotNull @Valid HentBeregningsgrunnlagListeRequestAbacDto spesifikasjon) {
        if (spesifikasjon.getRequestPrReferanse().isEmpty()) {
            return Response.noContent().build();
        }
        var ytelseTyper = spesifikasjon.getRequestPrReferanse().stream().map(v -> v.getYtelseSomSkalBeregnes()).collect(Collectors.toSet());
        if (ytelseTyper.size() != 1) {
            return Response.status(Status.BAD_REQUEST).entity("Feil input, all requests må ha samme ytelsetype. Fikk: " + ytelseTyper).build();
        }
        var ytelseType = YtelseTyperKalkulusStøtter.fraKode(ytelseTyper.iterator().next().getKode());

        var koblingReferanser = spesifikasjon.getRequestPrReferanse().stream().map(v -> new KoblingReferanse(v.getKoblingReferanse()))
            .collect(Collectors.toList());
        var dtoer = hentBeregningsgrunnlagGrunnlagEntitetForSpesifikasjon(koblingReferanser, ytelseType).stream()
            .map(MapDetaljertBeregningsgrunnlag::mapGrunnlag)
            .collect(Collectors.toList());

        return dtoer.isEmpty() ? Response.noContent().build() : Response.ok(dtoer).build();
    }


    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(description = "Hent beregningsgrunnlagDto for angitt behandling som brukes frontend", summary = ("Returnerer beregningsgrunnlagDto for behandling."), tags = "beregningsgrunnlag")
    @BeskyttetRessurs(action = READ, resource = BEREGNINGSGRUNNLAG)
    @Path("/beregningsgrunnlagListe")
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public Response hentBeregningsgrunnlagDtoListe(@NotNull @Valid HentBeregningsgrunnlagDtoListeForGUIRequestAbacDto spesifikasjon) {
        List<BeregningsgrunnlagPrReferanse<BeregningsgrunnlagDto>> dtoPrReferanse = hentBeregningsgrunnlagDtoForGUIForSpesifikasjon(
            spesifikasjon.getRequestPrReferanse()).entrySet()
                .stream()
                .map(e -> new BeregningsgrunnlagPrReferanse<>(e.getKey(), e.getValue()))
                .collect(Collectors.toList());
        return Response.ok(new BeregningsgrunnlagListe(dtoPrReferanse)).build();
    }

    /** @deprecated fjernes når frisinn ikke er mer. */
    @Deprecated(forRemoval = true, since="1.1")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(description = "Hent grunnlag for frisinn", summary = ("Returnerer frisinngrunnlag for behandling."), tags = "beregningsgrunnlag")
    @BeskyttetRessurs(action = READ, resource = BEREGNINGSGRUNNLAG)
    @Path("/frisinnGrunnlag")
    @SuppressWarnings({ "findsecbugs:JAXRS_ENDPOINT", "resource" })
    public Response hentFrisinnGrunnlag(@NotNull @Valid HentBeregningsgrunnlagRequestAbacDto spesifikasjon) {
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
        BeregningsgrunnlagGUIInput input = kalkulatorInputTjeneste.lagInputForKoblinger(List.of(koblingId.get())).iterator().next();
        final Response response = beregningsgrunnlagGrunnlagEntitet.stream()
            .flatMap(gr -> gr.getBeregningsgrunnlag().stream())
            .map(bg -> MapBeregningsgrunnlagFRISINN.map(bg, input.getIayGrunnlag().getOppgittOpptjening(), input.getYtelsespesifiktGrunnlag()))
            .map(bgDto -> Response.ok(bgDto).build())
            .findFirst()
            .orElse(Response.noContent().build());
        return response;
    }

    private Optional<no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.fastsatt.BeregningsgrunnlagDto> hentFastsattBeregningsgrunnlagForSpesifikasjon(KoblingReferanse koblingReferanse,
                                                                                                                                                                   YtelseTyperKalkulusStøtter ytelseType) {
        return hentBeregningsgrunnlagGrunnlagEntitetForSpesifikasjon(koblingReferanse, ytelseType).stream()
            .filter(grunnlag -> grunnlag.getBeregningsgrunnlagTilstand().equals(BeregningsgrunnlagTilstand.FASTSATT))
            .flatMap(gr -> gr.getBeregningsgrunnlag().stream())
            .map(MapBeregningsgrunnlag::map)
            .findFirst();
    }

    private Optional<BeregningsgrunnlagGrunnlagEntitet> hentBeregningsgrunnlagGrunnlagEntitetForSpesifikasjon(KoblingReferanse koblingReferanse,
                                                                                                              YtelseTyperKalkulusStøtter ytelseType) {
        koblingTjeneste.hentFor(koblingReferanse).map(KoblingEntitet::getSaksnummer)
            .ifPresent(saksnummer -> MDC.put("prosess_saksnummer", saksnummer.getVerdi()));
        Optional<Long> koblingId = koblingTjeneste.hentKoblingHvisFinnes(koblingReferanse, ytelseType);
        if (!harKalkulatorInput(koblingId)) {
            return Optional.empty();
        }
        return beregningsgrunnlagRepository.hentBeregningsgrunnlagGrunnlagEntitet(koblingId.get());
    }

    private List<BeregningsgrunnlagGrunnlagEntitet> hentBeregningsgrunnlagGrunnlagEntitetForSpesifikasjon(Collection<KoblingReferanse> koblingReferanser,
                                                                                                          YtelseTyperKalkulusStøtter ytelseType) {
        var koblinger = koblingTjeneste.hentKoblinger(koblingReferanser, ytelseType);
        if (koblinger.isEmpty()) {
            return Collections.emptyList();
        }
        var saksnummer = koblinger.stream().map(KoblingEntitet::getSaksnummer).collect(Collectors.toSet());
        if (saksnummer.size() != 1) {
            throw new IllegalArgumentException("Angitte koblinger må tilhøre samme saksnummer. Fikk: " + saksnummer);
        }

        var saksnummer1 = saksnummer.iterator().next();
        MDC.put("prosess_saksnummer", saksnummer1.getVerdi());

        var koblingerMedKalkulatorInput = hentKoblingerMedKalkulatorInput(koblinger);
        if (koblingerMedKalkulatorInput.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> koblingIder = koblingerMedKalkulatorInput.stream().map(v -> v.getId()).collect(Collectors.toList());
        return beregningsgrunnlagRepository.hentBeregningsgrunnlagGrunnlagEntiteter(koblingIder);
    }

    private Map<UUID, BeregningsgrunnlagDto> hentBeregningsgrunnlagDtoForGUIForSpesifikasjon(Collection<HentBeregningsgrunnlagDtoForGUIRequest> spesifikasjoner) {

        var koblingReferanser = spesifikasjoner.stream().map(HentBeregningsgrunnlagDtoForGUIRequest::getKoblingReferanse)
            .map(KoblingReferanse::new)
            .collect(Collectors.toSet());

        var ytelseSomSkalBeregnes = spesifikasjoner.stream()
            .map(HentBeregningsgrunnlagDtoForGUIRequest::getYtelseSomSkalBeregnes)
            .map(y -> YtelseTyperKalkulusStøtter.fraKode(y.getKode()))
            .collect(Collectors.toSet());

        if (ytelseSomSkalBeregnes.isEmpty()) {
            return Collections.emptyMap();
        } else if (ytelseSomSkalBeregnes.size() != 1) {
            throw new IllegalArgumentException("Støtter kun at alle har samme ytelse type. Fikk: " + ytelseSomSkalBeregnes);
        }
        var ytelseType = ytelseSomSkalBeregnes.iterator().next();
        var koblinger = koblingTjeneste.hentKoblinger(koblingReferanser, ytelseType);
        var koblingIds = koblinger.stream().map(KoblingEntitet::getId).collect(Collectors.toList());

        List<BeregningsgrunnlagGUIInput> beregningsgrunnlagInput = kalkulatorInputTjeneste.lagInputForKoblinger(koblingIds);
        Map<UUID, BeregningsgrunnlagDto> resultater = beregningsgrunnlagInput
            .stream().collect(Collectors.toMap(input -> input.getKoblingReferanse().getKoblingUuid(), input -> mapTilDto(spesifikasjoner, input)));

        return resultater;
    }

    private BeregningsgrunnlagDto mapTilDto(Collection<HentBeregningsgrunnlagDtoForGUIRequest> spesifikasjoner, BeregningsgrunnlagGUIInput input) {
        var spesifikasjon = spesifikasjoner.stream().filter(s -> s.getKoblingReferanse().equals(input.getKoblingReferanse().getKoblingUuid()))
            .findFirst().orElseThrow(() -> new IllegalStateException("Ingen match blant koblinger"));
        input.oppdaterArbeidsgiverinformasjon(
            MapIAYTilKalulator.mapArbeidsgiverOpplysninger(spesifikasjon.getArbeidsgiverOpplysninger()),
            MapIAYTilKalulator.mapArbeidsgiverReferanser(spesifikasjon.getReferanser()));
        BeregningsgrunnlagDto beregningsgrunnlagDto = beregningsgrunnlagDtoTjeneste.lagBeregningsgrunnlagDto(input);
        beregningsgrunnlagDto.setVilkårsperiodeFom(spesifikasjon.getVilkårsperiodeFom());
        return beregningsgrunnlagDto;
    }

    private Boolean harKalkulatorInput(Optional<Long> koblingId) {
        return koblingId.map(id -> beregningsgrunnlagRepository.hvisEksistererKalkulatorInput(id)).orElse(false);
    }

    /** returner de av angitte koblinger som har kalkulatorinput. */
    private List<KoblingEntitet> hentKoblingerMedKalkulatorInput(List<KoblingEntitet> koblinger) {
        var koblingIder = koblinger.stream().map(KoblingEntitet::getId).collect(Collectors.toSet());
        NavigableSet<Long> koblingIderMedKalkulatorInput = beregningsgrunnlagRepository.hvisEksistererKalkulatorInput(koblingIder);

        return koblinger.stream()
            .filter(k -> koblingIderMedKalkulatorInput.contains(k.getId()))
            .collect(Collectors.toList());

    }

}
