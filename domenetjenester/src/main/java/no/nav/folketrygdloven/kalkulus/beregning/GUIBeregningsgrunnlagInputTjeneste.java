package no.nav.folketrygdloven.kalkulus.beregning;

import static no.nav.folketrygdloven.kalkulus.mapFraEntitet.BehandlingslagerTilKalkulusMapper.mapGrunnlag;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagGUIInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulus.beregning.input.KalkulatorInputFeil;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.KalkulatorInputEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagGrunnlagEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.kobling.KoblingEntitet;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulus.mappers.MapTilGUIInputFraKalkulator;
import no.nav.folketrygdloven.kalkulus.tjeneste.beregningsgrunnlag.BeregningsgrunnlagRepository;
import no.nav.folketrygdloven.kalkulus.tjeneste.kobling.KoblingRepository;
import no.nav.vedtak.feil.FeilFactory;

@Dependent
public class GUIBeregningsgrunnlagInputTjeneste {

    private final boolean medSporingslogg = false;
    private BeregningsgrunnlagRepository beregningsgrunnlagRepository;
    private KoblingRepository koblingRepository;

    @Inject
    public GUIBeregningsgrunnlagInputTjeneste(BeregningsgrunnlagRepository beregningsgrunnlagRepository,
                                              KoblingRepository koblingRepository) {
        this.beregningsgrunnlagRepository = beregningsgrunnlagRepository;
        this.koblingRepository = koblingRepository;
    }

    /**
     * Returnerer BeregningsgrunnlagInput for alle angitte koblinger (hvis eksisterer).
     */
    public List<BeregningsgrunnlagGUIInput> lagInputForKoblinger(List<Long> koblingIder) {
        List<BeregningsgrunnlagGrunnlagEntitet> beregningsgrunnlagGrunnlagEntiteter = beregningsgrunnlagRepository
                .hentBeregningsgrunnlagGrunnlagEntiteter(koblingIder);

        List<BeregningsgrunnlagTilstand> aktiveTilstander = beregningsgrunnlagGrunnlagEntiteter.stream().map(BeregningsgrunnlagGrunnlagEntitet::getBeregningsgrunnlagTilstand)
                .distinct()
                .collect(Collectors.toList());

        Set<Long> koblingerMedBeregningsgrunnlag = beregningsgrunnlagGrunnlagEntiteter
                .stream().map(BeregningsgrunnlagGrunnlagEntitet::getKoblingId).collect(Collectors.toSet());

        Map<Long, KalkulatorInputEntitet> koblingKalkulatorInput = beregningsgrunnlagRepository
                .hentHvisEksistererKalkulatorInput(koblingerMedBeregningsgrunnlag)
                .stream().collect(Collectors.toMap(KalkulatorInputEntitet::getKoblingId, Function.identity()));

        Map<Long, KoblingEntitet> koblinger = koblingRepository.hentKoblingerFor(koblingerMedBeregningsgrunnlag)
                .stream().collect(Collectors.toMap(KoblingEntitet::getId, Function.identity()));


        Map<Long, BeregningsgrunnlagGrunnlagEntitet> grunnlagFraFordel = alleTilstanderErFørFordel(aktiveTilstander) ? Map.of() :
                beregningsgrunnlagRepository.hentSisteBeregningsgrunnlagGrunnlagEntitetForKoblinger(koblingIder, BeregningsgrunnlagTilstand.OPPDATERT_MED_REFUSJON_OG_GRADERING)
                        .stream().collect(Collectors.toMap(BeregningsgrunnlagGrunnlagEntitet::getKoblingId, Function.identity()));

        return mapInputListe(beregningsgrunnlagGrunnlagEntiteter, grunnlagFraFordel, koblingKalkulatorInput, koblinger, medSporingslogg);
    }

    private boolean alleTilstanderErFørFordel(List<BeregningsgrunnlagTilstand> aktiveTilstander) {
        return aktiveTilstander.stream()
                .allMatch(tilstand -> tilstand.erFør(BeregningsgrunnlagTilstand.OPPDATERT_MED_REFUSJON_OG_GRADERING));
    }

    /**
     * Mapper alle databaseentiteter til GUI-input (for å lage beregningsgrunnlagdto til gui)
     * <p>
     * Ingen databasekall skal gjøres her inne for å unngå at databasekall gjøres i loop.
     *
     * @param beregningsgrunnlagGrunnlagEntiteter Alle aktive grunnlagsentiteter for koblinger
     * @param grunnlagFraFordel                   Grunnlag fra fordel-steget om dette er kjørt
     * @param koblingKalkulatorInput              KalkulatorInput for koblinger
     * @param koblinger                           Koblingentiteter
     * @param medSporingslogg                     Flagg for om sporingslogg skal mappes
     * @return Liste med restinput
     */
    private static List<BeregningsgrunnlagGUIInput> mapInputListe(
            List<BeregningsgrunnlagGrunnlagEntitet> beregningsgrunnlagGrunnlagEntiteter,
            Map<Long, BeregningsgrunnlagGrunnlagEntitet> grunnlagFraFordel,
            Map<Long, KalkulatorInputEntitet> koblingKalkulatorInput,
            Map<Long, KoblingEntitet> koblinger,
            boolean medSporingslogg) {
        return beregningsgrunnlagGrunnlagEntiteter.stream()
                .map(grunnlagEntitet -> {
                    Long koblingId = grunnlagEntitet.getKoblingId();
                    var kalkulatorInput = Optional.ofNullable(koblingKalkulatorInput.get(koblingId))
                            .orElseThrow(() -> FeilFactory.create(KalkulatorInputFeil.class).kalkulusFinnerIkkeKalkulatorInput(koblingId).toException());
                    var kobling = Optional.ofNullable(koblinger.get(koblingId))
                            .orElseThrow(() -> FeilFactory.create(KalkulatorInputFeil.class).kalkulusFinnerIkkeKobling(koblingId).toException());
                    BeregningsgrunnlagGUIInput input = lagInput(kobling, kalkulatorInput, Optional.of(grunnlagEntitet));
                    BeregningsgrunnlagGrunnlagDto mappedGrunnlag = mapGrunnlag(grunnlagEntitet, medSporingslogg);
                    return leggTilGrunnlagFraFordel(input, grunnlagFraFordel, medSporingslogg).medBeregningsgrunnlagGrunnlag(mappedGrunnlag);
                }).collect(Collectors.toList());
    }

    private static BeregningsgrunnlagGUIInput lagInput(KoblingEntitet koblingEntitet,
                                                       KalkulatorInputEntitet kalkulatorInput,
                                                       Optional<BeregningsgrunnlagGrunnlagEntitet> beregningsgrunnlagGrunnlagEntitet) {
        return MapTilGUIInputFraKalkulator.map(
                koblingEntitet,
                kalkulatorInput,
                beregningsgrunnlagGrunnlagEntitet);
    }

    private static BeregningsgrunnlagGUIInput leggTilGrunnlagFraFordel(BeregningsgrunnlagGUIInput input,
                                                                       Map<Long, BeregningsgrunnlagGrunnlagEntitet> grunnlagFraFordel,
                                                                       boolean medSporingslogg) {
        Long koblingId = input.getKoblingReferanse().getKoblingId();
        if (grunnlagFraFordel.containsKey(koblingId)) {
            return input.medBeregningsgrunnlagGrunnlagFraFordel(mapGrunnlag(grunnlagFraFordel.get(koblingId), medSporingslogg));
        }
        return input;
    }
}
