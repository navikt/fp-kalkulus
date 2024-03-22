package no.nav.folketrygdloven.kalkulus.beregning;

import static no.nav.folketrygdloven.kalkulus.mapFraEntitet.BehandlingslagerTilKalkulusMapper.mapAvklaringsbehov;
import static no.nav.folketrygdloven.kalkulus.mapFraEntitet.BehandlingslagerTilKalkulusMapper.mapGrunnlag;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagGUIInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.avklaringsbehov.AvklaringsbehovEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagGrunnlagEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.kobling.KoblingEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.kobling.KoblingRelasjon;
import no.nav.folketrygdloven.kalkulus.felles.feil.TekniskException;
import no.nav.folketrygdloven.kalkulus.felles.v1.KalkulatorInputDto;
import no.nav.folketrygdloven.kalkulus.mapFraEntitet.BehandlingslagerTilKalkulusMapper;
import no.nav.folketrygdloven.kalkulus.mappers.MapTilGUIInputFraKalkulator;
import no.nav.folketrygdloven.kalkulus.tjeneste.avklaringsbehov.AvklaringsbehovTjeneste;
import no.nav.folketrygdloven.kalkulus.tjeneste.beregningsgrunnlag.BeregningsgrunnlagRepository;
import no.nav.folketrygdloven.kalkulus.tjeneste.kobling.KoblingRepository;

@Dependent
public class GUIBeregningsgrunnlagInputTjeneste {

    private BeregningsgrunnlagRepository beregningsgrunnlagRepository;
    private KoblingRepository koblingRepository;
    private AvklaringsbehovTjeneste avklaringsbehovTjeneste;

    @Inject
    public GUIBeregningsgrunnlagInputTjeneste(BeregningsgrunnlagRepository beregningsgrunnlagRepository,
                                              KoblingRepository koblingRepository,
                                              AvklaringsbehovTjeneste avklaringsbehovTjeneste) {
        this.beregningsgrunnlagRepository = beregningsgrunnlagRepository;
        this.koblingRepository = koblingRepository;
        this.avklaringsbehovTjeneste = avklaringsbehovTjeneste;
    }

    /**
     * Mapper alle databaseentiteter til GUI-input (for å lage beregningsgrunnlagdto til gui)
     * <p>
     * Ingen databasekall skal gjøres her inne for å unngå at databasekall gjøres i loop.
     *
     * @param beregningsgrunnlagGrunnlagEntiteter Alle aktive grunnlagsentiteter for koblinger
     * @param avklaringsbehovPrKobling             Avklaringsbehov for koblinger
     * @param koblingKalkulatorInput              KalkulatorInput for koblinger
     * @param koblinger                           Koblingentiteter
     * @param originaleGrunnlagMap
     * @return Liste med restinput
     */
    private static Map<Long, BeregningsgrunnlagGUIInput> mapInputListe(
            List<BeregningsgrunnlagGrunnlagEntitet> beregningsgrunnlagGrunnlagEntiteter,
            Map<Long, List<AvklaringsbehovEntitet>> avklaringsbehovPrKobling,
            Map<Long, KalkulatorInputDto> koblingKalkulatorInput,
            Map<Long, KoblingEntitet> koblinger,
            Map<Long, List<BeregningsgrunnlagGrunnlagEntitet>> originaleGrunnlagMap) {
        return beregningsgrunnlagGrunnlagEntiteter.stream()
                .map(grunnlagEntitet -> {
                    Long koblingId = grunnlagEntitet.getKoblingId();
                    var kalkulatorInput = Optional.ofNullable(koblingKalkulatorInput.get(koblingId))
                            .orElseThrow(() -> new TekniskException("FP-KALKULUS-INPUT-1000000", String.format("Kalkulus finner ikke kalkulator input for koblingId: %s", koblingId)));
                    var kobling = Optional.ofNullable(koblinger.get(koblingId))
                            .orElseThrow(() -> new TekniskException("FP-KALKULUS-INPUT-1000003", String.format("Kalkulus finner ikke kobling: %s", koblingId)));
                    var avklaringsbehov = avklaringsbehovPrKobling.getOrDefault(koblingId, Collections.emptyList());
                    BeregningsgrunnlagGUIInput input = lagInput(kobling, kalkulatorInput);
                    return input.medBeregningsgrunnlagGrunnlag(mapGrunnlag(grunnlagEntitet))
                            .medBeregningsgrunnlagGrunnlagFraForrigeBehandling(mapOriginaleGrunnlag(originaleGrunnlagMap, koblingId))
                            .medAvklaringsbehov(mapAvklaringsbehov(avklaringsbehov));
                }).collect(Collectors.toMap(g -> g.getKoblingReferanse().getKoblingId(), Function.identity()));
    }

    private static List<BeregningsgrunnlagGrunnlagDto> mapOriginaleGrunnlag(Map<Long, List<BeregningsgrunnlagGrunnlagEntitet>> originaleGrunnlagMap, Long koblingId) {
        return originaleGrunnlagMap.get(koblingId).stream().map(BehandlingslagerTilKalkulusMapper::mapGrunnlag).collect(Collectors.toList());
    }

    private static BeregningsgrunnlagGUIInput lagInput(KoblingEntitet koblingEntitet,
                                                       KalkulatorInputDto kalkulatorInput) {
        return MapTilGUIInputFraKalkulator.mapFraKalkulatorInput(
                koblingEntitet,
                kalkulatorInput);
    }

    /**
     * Returnerer BeregningsgrunnlagInput for alle angitte koblinger (hvis eksisterer).
     * @return
     */
    public Map<Long, BeregningsgrunnlagGUIInput> lagInputForKoblinger(List<Long> koblingIder, List<KoblingRelasjon> koblingRelasjoner) {
        var beregningsgrunnlagGrunnlagEntiteter = beregningsgrunnlagRepository
                .hentBeregningsgrunnlagGrunnlagEntiteter(koblingIder);

        var koblingerMedBeregningsgrunnlag = beregningsgrunnlagGrunnlagEntiteter
                .stream().map(BeregningsgrunnlagGrunnlagEntitet::getKoblingId).collect(Collectors.toSet());

    var originaleGrunnlag = beregningsgrunnlagRepository
            .hentBeregningsgrunnlagGrunnlagEntiteter(koblingRelasjoner.stream().map(KoblingRelasjon::getOriginalKoblingId).collect(Collectors.toList()));
    Map<Long, KalkulatorInputDto> inputMap = Map.of();
    if (inputMap.isEmpty()) {
        // TODO tfp-5742 fiks input her
        throw new IllegalStateException("Må ha kalkulatorinput for å kunne lage GUI dto");
    }
    return mapKalkulatorInputTilModell(koblingerMedBeregningsgrunnlag,
            koblingRelasjoner,
            beregningsgrunnlagGrunnlagEntiteter,
            originaleGrunnlag,
            inputMap);

    }

    private Map<Long, BeregningsgrunnlagGUIInput> mapKalkulatorInputTilModell(Set<Long> koblingIder,
                                                                              List<KoblingRelasjon> koblingRelasjoner,
                                                                              List<BeregningsgrunnlagGrunnlagEntitet> beregningsgrunnlagGrunnlagEntiteter,
                                                                              List<BeregningsgrunnlagGrunnlagEntitet> originaleGrunnlag,
                                                                              Map<Long, KalkulatorInputDto> koblingKalkulatorInput) {

        Map<Long, KoblingEntitet> koblinger = koblingRepository.hentKoblingerFor(koblingIder)
                .stream().collect(Collectors.toMap(KoblingEntitet::getId, Function.identity()));

        Map<Long, List<AvklaringsbehovEntitet>> avklaringsbehovPrKobling = avklaringsbehovTjeneste.hentAlleAvklaringsbehovForKoblinger(koblingIder).stream()
                .collect(Collectors.groupingBy(AvklaringsbehovEntitet::getKoblingId));

        var originaleGrunnlagMap = koblingIder.stream().collect(Collectors.toMap(
                Function.identity(),
                id -> koblingRelasjoner.stream().filter(r -> r.getKoblingId().equals(id)).map(KoblingRelasjon::getOriginalKoblingId)
                        .flatMap(orginalKobling -> originaleGrunnlag.stream().filter(gr -> gr.getKoblingId().equals(orginalKobling)))
                        .collect(Collectors.toList())));

        return mapInputListe(
                beregningsgrunnlagGrunnlagEntiteter,
                avklaringsbehovPrKobling,
                koblingKalkulatorInput,
                koblinger,
                originaleGrunnlagMap);
    }

}
