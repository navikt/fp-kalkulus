package no.nav.folketrygdloven.kalkulus.rest;

import static no.nav.folketrygdloven.kalkulus.sikkerhet.KalkulusBeskyttetRessursAttributt.BEREGNINGSGRUNNLAG;
import static no.nav.k9.felles.sikkerhet.abac.BeskyttetRessursActionAttributt.READ;

import java.util.ArrayList;
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

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import no.nav.folketrygdloven.kalkulator.guitjenester.BeregningsgrunnlagDtoTjeneste;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagGUIInput;
import no.nav.folketrygdloven.kalkulus.beregning.GUIBeregningsgrunnlagInputTjeneste;
import no.nav.folketrygdloven.kalkulus.beregning.input.HentInputResponsKode;
import no.nav.folketrygdloven.kalkulus.beregning.input.KalkulatorInputTjeneste;
import no.nav.folketrygdloven.kalkulus.beregning.input.Resultat;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagGrunnlagEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.KoblingReferanse;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.kobling.KoblingEntitet;
import no.nav.folketrygdloven.kalkulus.kobling.KoblingTjeneste;
import no.nav.folketrygdloven.kalkulus.kodeverk.YtelseTyperKalkulusStøtterKontrakt;
import no.nav.folketrygdloven.kalkulus.mapTilKontrakt.MapBeregningsgrunnlagFRISINN;
import no.nav.folketrygdloven.kalkulus.mapTilKontrakt.MapBrevBeregningsgrunnlag;
import no.nav.folketrygdloven.kalkulus.mapTilKontrakt.MapDetaljertBeregningsgrunnlag;
import no.nav.folketrygdloven.kalkulus.mappers.MapIAYTilKalulator;
import no.nav.folketrygdloven.kalkulus.request.v1.HentBeregningsgrunnlagDtoForGUIRequest;
import no.nav.folketrygdloven.kalkulus.request.v1.HentBeregningsgrunnlagDtoListeForGUIRequest;
import no.nav.folketrygdloven.kalkulus.request.v1.HentBeregningsgrunnlagListeRequest;
import no.nav.folketrygdloven.kalkulus.request.v1.HentBeregningsgrunnlagRequest;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.BeregningsgrunnlagPrReferanse;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.detaljert.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.BeregningsgrunnlagListe;
import no.nav.folketrygdloven.kalkulus.tjeneste.beregningsgrunnlag.BeregningsgrunnlagRepository;
import no.nav.k9.felles.sikkerhet.abac.AbacDataAttributter;
import no.nav.k9.felles.sikkerhet.abac.BeskyttetRessurs;
import no.nav.k9.felles.sikkerhet.abac.StandardAbacAttributtType;

@Produces(MediaType.APPLICATION_JSON)
@OpenAPIDefinition(tags = @Tag(name = "beregningsgrunnlag"))
@Path("/kalkulus/v1")
@ApplicationScoped
@Transactional
public class HentKalkulusRestTjeneste {

    private KoblingTjeneste koblingTjeneste;
    private BeregningsgrunnlagRepository beregningsgrunnlagRepository;
    private KalkulatorInputTjeneste kalkulatorInputTjeneste;
    private GUIBeregningsgrunnlagInputTjeneste guiInputTjeneste;
    private BeregningsgrunnlagDtoTjeneste beregningsgrunnlagDtoTjeneste;

    public HentKalkulusRestTjeneste() {
        // for CDI
    }

    @Inject
    public HentKalkulusRestTjeneste(KoblingTjeneste koblingTjeneste,
                                    BeregningsgrunnlagRepository beregningsgrunnlagRepository,
                                    KalkulatorInputTjeneste kalkulatorInputTjeneste,
                                    GUIBeregningsgrunnlagInputTjeneste guiInputTjeneste,
                                    BeregningsgrunnlagDtoTjeneste beregningsgrunnlagDtoTjeneste) {
        this.koblingTjeneste = koblingTjeneste;
        this.beregningsgrunnlagRepository = beregningsgrunnlagRepository;
        this.kalkulatorInputTjeneste = kalkulatorInputTjeneste;
        this.guiInputTjeneste = guiInputTjeneste;
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
        var ytelseTyper = spesifikasjon.getRequestPrReferanse().stream()
                .map(HentBeregningsgrunnlagRequest::getYtelseSomSkalBeregnes).collect(Collectors.toSet());
        if (ytelseTyper.size() != 1) {
            return Response.status(Status.BAD_REQUEST).entity("Feil input, alle requests må ha samme ytelsetype. Fikk: " + ytelseTyper).build();
        }
        var ytelseType = YtelseTyperKalkulusStøtterKontrakt.fraKode(ytelseTyper.iterator().next().getKode());
        var koblingReferanser = spesifikasjon.getRequestPrReferanse().stream().map(v -> new KoblingReferanse(v.getKoblingReferanse()))
                .collect(Collectors.toList());
        List<BeregningsgrunnlagGrunnlagDto> dtoer;

        // TODO Fjern dette, lag egen tjeneste for brev
        if (YtelseTyperKalkulusStøtterKontrakt.OMSORGSPENGER.equals(ytelseType) || YtelseTyperKalkulusStøtterKontrakt.PLEIEPENGER_SYKT_BARN.equals(ytelseType)) {
            List<Long> koblinger = new ArrayList<>();
            koblingReferanser.forEach(ref -> koblingTjeneste.hentKoblingHvisFinnes(ref, ytelseType).ifPresent(koblinger::add));
            Resultat<BeregningsgrunnlagGUIInput> input = guiInputTjeneste.lagInputForKoblinger(koblinger, List.of());
            List<BeregningsgrunnlagGrunnlagEntitet> grunnlag = hentBeregningsgrunnlagGrunnlagEntitetForSpesifikasjon(koblingReferanser, ytelseType);
            dtoer = new ArrayList<>();
            input.getResultatPrKobling().forEach((key, value) -> {
                Optional<BeregningsgrunnlagGrunnlagEntitet> grunnlagForKobling = grunnlag.stream()
                        .filter(gr -> gr.getKoblingId().equals(key))
                        .findFirst();
                grunnlagForKobling.ifPresent(gr -> dtoer.add(MapDetaljertBeregningsgrunnlag.mapMedBrevfelt(gr, value)));
            });
        } else {
            dtoer = hentBeregningsgrunnlagGrunnlagEntitetForSpesifikasjon(koblingReferanser, ytelseType).stream()
                    .map(MapDetaljertBeregningsgrunnlag::mapGrunnlag)
                    .collect(Collectors.toList());
        }
        return dtoer.isEmpty() ? Response.noContent().build() : Response.ok(dtoer).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(description = "Hent forenklet BeregningsgrunnlagGrunnlag for angitte referanser", summary = ("Returnerer forenklet BeregningsgrunnlagGrunnlag for angitte kobling referanser."), tags = "beregningsgrunnlag")
    @BeskyttetRessurs(action = READ, resource = BEREGNINGSGRUNNLAG)
    @Path("/forenklet-grunnlag/bolk")
    @SuppressWarnings({ "findsecbugs:JAXRS_ENDPOINT", "resource" })
    public Response hentForenkletBeregningsgrunnlag(@NotNull @Valid HentBeregningsgrunnlagListeRequestAbacDto spesifikasjon) {
        if (spesifikasjon.getRequestPrReferanse().isEmpty()) {
            return Response.noContent().build();
        }
        var ytelseTyper = spesifikasjon.getRequestPrReferanse().stream()
                .map(HentBeregningsgrunnlagRequest::getYtelseSomSkalBeregnes).collect(Collectors.toSet());
        if (ytelseTyper.size() != 1) {
            return Response.status(Status.BAD_REQUEST).entity("Feil input, alle requests må ha samme ytelsetype. Fikk: " + ytelseTyper).build();
        }
        var ytelseType = YtelseTyperKalkulusStøtterKontrakt.fraKode(ytelseTyper.iterator().next().getKode());
        var koblingReferanser = spesifikasjon.getRequestPrReferanse().stream().map(v -> new KoblingReferanse(v.getKoblingReferanse()))
                .collect(Collectors.toList());
        List<Long> koblinger = new ArrayList<>();
        koblingReferanser.forEach(ref -> koblingTjeneste.hentKoblingHvisFinnes(ref, ytelseType).ifPresent(koblinger::add));
        // TODO Ikkje bruk gui-input til henting av brevfelter
        Resultat<BeregningsgrunnlagGUIInput> input = guiInputTjeneste.lagInputForKoblinger(koblinger, List.of());
        var dtoer = hentBeregningsgrunnlagGrunnlagEntitetForSpesifikasjon(koblingReferanser, ytelseType).stream()
                .map(gr -> MapBrevBeregningsgrunnlag.mapGrunnlag(gr, input.getResultatPrKobling().get(gr.getKoblingId()).getYtelsespesifiktGrunnlag()))
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
        if (spesifikasjon.getSaksnummer() != null) {
            MDC.put("prosess_saksnummer", spesifikasjon.getSaksnummer());
        }
        List<HentBeregningsgrunnlagDtoForGUIRequest> spesifikasjoner = spesifikasjon.getRequestPrReferanse();
        if (spesifikasjon.getKalkulatorInputPerKoblingReferanse() != null) {
            var ytelseTyper = spesifikasjoner.stream().map(HentBeregningsgrunnlagDtoForGUIRequest::getYtelseSomSkalBeregnes).collect(Collectors.toSet());
            var ytelseType = YtelseTyperKalkulusStøtterKontrakt.fraKode(ytelseTyper.iterator().next().getKode());
            kalkulatorInputTjeneste.lagreKalkulatorInput(ytelseType, spesifikasjon.getKalkulatorInputPerKoblingReferanse());
        }
        Resultat<BeregningsgrunnlagGUIInput> inputResultat = finnInputForGenereringAvDtoTilGUI(spesifikasjoner);
        if (inputResultat.getKode().equals(HentInputResponsKode.ETTERSPØR_NY_INPUT)) {
            return Response.ok(new BeregningsgrunnlagListe(true)).build();
        } else {
            List<BeregningsgrunnlagPrReferanse<BeregningsgrunnlagDto>> dtoPrReferanse = hentBeregningsgrunnlagDtoForGUIForSpesifikasjon(
                    inputResultat.getResultatPrKobling(), spesifikasjoner).entrySet()
                    .stream()
                    .map(e -> new BeregningsgrunnlagPrReferanse<>(e.getKey(), e.getValue()))
                    .collect(Collectors.toList());
            return Response.ok(new BeregningsgrunnlagListe(dtoPrReferanse)).build();
        }
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
        var ytelseTyperKalkulusStøtter = YtelseTyperKalkulusStøtterKontrakt.fraKode(spesifikasjon.getYtelseSomSkalBeregnes().getKode());
        Optional<Long> koblingId = koblingTjeneste.hentKoblingHvisFinnes(koblingReferanse, ytelseTyperKalkulusStøtter);
        if (koblingId.isEmpty() || !harKalkulatorInput(koblingId)) {
            return Response.noContent().build();
        }
        Optional<BeregningsgrunnlagGrunnlagEntitet> beregningsgrunnlagGrunnlagEntitet = beregningsgrunnlagRepository
            .hentBeregningsgrunnlagGrunnlagEntitet(koblingId.get());
        Resultat<BeregningsgrunnlagGUIInput> resultatInput = guiInputTjeneste.lagInputForKoblinger(List.of(koblingId.get()), List.of());
        if (resultatInput.getKode().equals(HentInputResponsKode.ETTERSPØR_NY_INPUT)) {
            throw new IllegalStateException("Kan ikke hente ny input for kall til frisinnGrunnlag");
        } else {
            BeregningsgrunnlagGUIInput input = guiInputTjeneste.lagInputForKoblinger(List.of(koblingId.get()), List.of()).getResultatPrKobling().values().iterator().next();
            final Response response = beregningsgrunnlagGrunnlagEntitet.stream()
                    .flatMap(gr -> gr.getBeregningsgrunnlag().stream())
                    .map(bg -> MapBeregningsgrunnlagFRISINN.map(bg, input.getIayGrunnlag().getOppgittOpptjening(), input.getYtelsespesifiktGrunnlag()))
                    .map(bgDto -> Response.ok(bgDto).build())
                    .findFirst()
                    .orElse(Response.noContent().build());
            return response;
        }
    }

    private List<BeregningsgrunnlagGrunnlagEntitet> hentBeregningsgrunnlagGrunnlagEntitetForSpesifikasjon(Collection<KoblingReferanse> koblingReferanser,
                                                                                                          YtelseTyperKalkulusStøtterKontrakt ytelseType) {
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
        List<Long> koblingIder = koblingerMedKalkulatorInput.stream().map(KoblingEntitet::getId).collect(Collectors.toList());
        return beregningsgrunnlagRepository.hentBeregningsgrunnlagGrunnlagEntiteter(koblingIder);
    }

    private Map<UUID, BeregningsgrunnlagDto> hentBeregningsgrunnlagDtoForGUIForSpesifikasjon(Map<Long, BeregningsgrunnlagGUIInput> inputPrKobling, List<HentBeregningsgrunnlagDtoForGUIRequest> spesifikasjoner) {
        return inputPrKobling.values()
            .stream().collect(Collectors.toMap(input -> input.getKoblingReferanse().getKoblingUuid(), input -> mapTilDto(spesifikasjoner, input)));
    }

    private Resultat<BeregningsgrunnlagGUIInput> finnInputForGenereringAvDtoTilGUI(Collection<HentBeregningsgrunnlagDtoForGUIRequest> spesifikasjoner) {
        var koblingReferanser = spesifikasjoner.stream().map(HentBeregningsgrunnlagDtoForGUIRequest::getKoblingReferanse)
            .map(KoblingReferanse::new)
            .collect(Collectors.toSet());

        var ytelseSomSkalBeregnes = spesifikasjoner.stream()
            .map(HentBeregningsgrunnlagDtoForGUIRequest::getYtelseSomSkalBeregnes)
            .map(y -> YtelseTyperKalkulusStøtterKontrakt.fraKode(y.getKode()))
            .collect(Collectors.toSet());

        if (ytelseSomSkalBeregnes.isEmpty()) {
            return Resultat.forGyldigInputMedData(Map.of());
        } else if (ytelseSomSkalBeregnes.size() != 1) {
            throw new IllegalArgumentException("Støtter kun at alle har samme ytelse type. Fikk: " + ytelseSomSkalBeregnes);
        }
        var ytelseType = ytelseSomSkalBeregnes.iterator().next();
        var koblinger = koblingTjeneste.hentKoblinger(koblingReferanser, ytelseType);
        var koblingIds = koblinger.stream().map(KoblingEntitet::getId).collect(Collectors.toList());
        var koblingRelasjoner = koblingTjeneste.hentKoblingRelasjoner(koblingIds);

        Resultat<BeregningsgrunnlagGUIInput> inputResultat = guiInputTjeneste.lagInputForKoblinger(koblingIds, koblingRelasjoner);
        return inputResultat;
    }

    private BeregningsgrunnlagDto mapTilDto(Collection<HentBeregningsgrunnlagDtoForGUIRequest> spesifikasjoner, BeregningsgrunnlagGUIInput input) {
        var spesifikasjon = spesifikasjoner.stream().filter(s -> s.getKoblingReferanse().equals(input.getKoblingReferanse().getKoblingUuid()))
            .findFirst().orElseThrow(() -> new IllegalStateException("Ingen match blant koblinger"));
        input.oppdaterArbeidsgiverinformasjon(MapIAYTilKalulator.mapArbeidsgiverReferanser(spesifikasjon.getReferanser()));
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

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(value = JsonInclude.Include.NON_ABSENT, content = JsonInclude.Include.NON_EMPTY)
    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
    public static class HentBeregningsgrunnlagListeRequestAbacDto extends HentBeregningsgrunnlagListeRequest implements no.nav.k9.felles.sikkerhet.abac.AbacDto {


        public HentBeregningsgrunnlagListeRequestAbacDto() {
            // For Json deserialisering
        }

        @Override
        public AbacDataAttributter abacAttributter() {
            final var abacDataAttributter = AbacDataAttributter.opprett();
            abacDataAttributter.leggTil(StandardAbacAttributtType.BEHANDLING_UUID, getBehandlingUuid());
            return abacDataAttributter;
        }
    }

    @Deprecated(forRemoval=true, since="1.0")
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(value = JsonInclude.Include.NON_ABSENT, content = JsonInclude.Include.NON_EMPTY)
    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
    public static class HentBeregningsgrunnlagRequestAbacDto extends HentBeregningsgrunnlagRequest implements no.nav.k9.felles.sikkerhet.abac.AbacDto {

        @Override
        public AbacDataAttributter abacAttributter() {
            final var abacDataAttributter = AbacDataAttributter.opprett();
            abacDataAttributter.leggTil(StandardAbacAttributtType.BEHANDLING_UUID, getKoblingReferanse());
            return abacDataAttributter;
        }
    }

    /**
     * Json bean med Abac.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(value = JsonInclude.Include.NON_ABSENT, content = JsonInclude.Include.NON_EMPTY)
    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
    public static class HentBeregningsgrunnlagDtoListeForGUIRequestAbacDto extends HentBeregningsgrunnlagDtoListeForGUIRequest implements no.nav.k9.felles.sikkerhet.abac.AbacDto {


        public HentBeregningsgrunnlagDtoListeForGUIRequestAbacDto() {
            // For Json deserialisering
        }

        @Override
        public AbacDataAttributter abacAttributter() {
            final var abacDataAttributter = AbacDataAttributter.opprett();
            abacDataAttributter.leggTil(StandardAbacAttributtType.BEHANDLING_UUID, getBehandlingUuid());
            return abacDataAttributter;
        }
    }

}
