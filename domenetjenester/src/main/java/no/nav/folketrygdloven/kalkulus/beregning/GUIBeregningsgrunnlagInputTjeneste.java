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

import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.BeregningSats;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.KalkulatorInputEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagGrunnlagEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.kobling.KoblingEntitet;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulus.mappers.MapFraKalkulator;
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

    public BeregningsgrunnlagInput lagInputForKobling(Long koblingId) {

        List<BeregningSats> satser = beregningsgrunnlagRepository.finnAlleSatser();
        var beregningsgrunnlagGrunnlagEntitet = beregningsgrunnlagRepository
            .hentBeregningsgrunnlagGrunnlagEntitet(koblingId);
        var kobling = koblingRepository.hentForKoblingId(koblingId);
        var kalkulatorInput = beregningsgrunnlagRepository.hentHvisEksitererKalkulatorInput(koblingId)
            .orElseThrow(() -> FeilFactory.create(KalkulatorInputFeil.class).kalkulusFinnerIkkeKalkulatorInput(koblingId).toException());
        BeregningsgrunnlagInput input = lagInput(kobling, kalkulatorInput, beregningsgrunnlagGrunnlagEntitet, satser);
        BeregningsgrunnlagGrunnlagDto mappedGrunnlag = beregningsgrunnlagGrunnlagEntitet
            .map(grunnlagEntitet -> mapGrunnlag(grunnlagEntitet, input.getInntektsmeldinger(), medSporingslogg))
            .orElseThrow(() -> FeilFactory.create(KalkulatorInputFeil.class).kalkulusHarIkkeBeregningsgrunnlag(koblingId).toException());
        leggTilTilstandhistorikk(input, medSporingslogg);
        return input.medBeregningsgrunnlagGrunnlag(mappedGrunnlag);
    }

    /** Returnerer BeregningsgrunnlagInput for alle angitte koblinger (hvis eksisterer). */
    public List<BeregningsgrunnlagInput> lagInputForKobling(List<Long> koblingIder) {
        List<BeregningSats> satser = beregningsgrunnlagRepository.finnAlleSatser();
        List<BeregningsgrunnlagGrunnlagEntitet> beregningsgrunnlagGrunnlagEntiteter = beregningsgrunnlagRepository
            .hentBeregningsgrunnlagGrunnlagEntiteter(koblingIder);
        
        Set<Long> koblingerMedBeregningsgrunnlag = beregningsgrunnlagGrunnlagEntiteter
            .stream().map(BeregningsgrunnlagGrunnlagEntitet::getKoblingId).collect(Collectors.toSet());

        Map<Long, KalkulatorInputEntitet> koblingKalkulatorInput = beregningsgrunnlagRepository
            .hentHvisEksistererKalkulatorInput(koblingerMedBeregningsgrunnlag)
            .stream().collect(Collectors.toMap(KalkulatorInputEntitet::getKoblingId, Function.identity()));

        Map<Long, KoblingEntitet> koblinger = koblingRepository.hentKoblingerFor(koblingerMedBeregningsgrunnlag)
            .stream().collect(Collectors.toMap(KoblingEntitet::getId, Function.identity()));

        return beregningsgrunnlagGrunnlagEntiteter.stream()
            .map(grunnlagEntitet -> {
                Long koblingId = grunnlagEntitet.getKoblingId();
                var kalkulatorInput = Optional.ofNullable(koblingKalkulatorInput.get(koblingId))
                    .orElseThrow(() -> FeilFactory.create(KalkulatorInputFeil.class).kalkulusFinnerIkkeKalkulatorInput(koblingId).toException());
                var kobling = Optional.ofNullable(koblinger.get(koblingId))
                    .orElseThrow(() -> FeilFactory.create(KalkulatorInputFeil.class).kalkulusFinnerIkkeKobling(koblingId).toException());
                BeregningsgrunnlagInput input = lagInput(kobling, kalkulatorInput, Optional.of(grunnlagEntitet), satser);
                BeregningsgrunnlagGrunnlagDto mappedGrunnlag = mapGrunnlag(grunnlagEntitet, input.getInntektsmeldinger(), medSporingslogg);
                leggTilGrunnlagForTilstand(input, false, BeregningsgrunnlagTilstand.OPPDATERT_MED_REFUSJON_OG_GRADERING);
                return input.medBeregningsgrunnlagGrunnlag(mappedGrunnlag);
            }).collect(Collectors.toList());
    }

    private BeregningsgrunnlagInput lagInputHvisFinnes(KoblingEntitet koblingEntitet,
                                                       KalkulatorInputEntitet kalkulatorInputEntitet,
                                                       Optional<BeregningsgrunnlagGrunnlagEntitet> beregningsgrunnlagGrunnlagEntitet,
                                                       List<BeregningSats> satser) {
        return MapFraKalkulator.mapFraKalkulatorInputEntitetTilBeregningsgrunnlagInput(
            koblingEntitet,
            kalkulatorInputEntitet,
            beregningsgrunnlagGrunnlagEntitet,
            satser);
    }

    private BeregningsgrunnlagInput lagInput(KoblingEntitet koblingEntitet,
                                             KalkulatorInputEntitet kalkulatorInput,
                                             Optional<BeregningsgrunnlagGrunnlagEntitet> beregningsgrunnlagGrunnlagEntitet,
                                             List<BeregningSats> satser) {
        return lagInputHvisFinnes(koblingEntitet, kalkulatorInput, beregningsgrunnlagGrunnlagEntitet, satser);
    }

    private void leggTilTilstandhistorikk(final BeregningsgrunnlagInput input, final boolean medSporingslogg) {
        for (var tilstand : BeregningsgrunnlagTilstand.values()) {
            leggTilGrunnlagForTilstand(input, medSporingslogg, tilstand);
        }
    }

    private void leggTilGrunnlagForTilstand(BeregningsgrunnlagInput input, boolean medSporingslogg, BeregningsgrunnlagTilstand tilstand) {
        Optional<BeregningsgrunnlagGrunnlagEntitet> sisteBg = beregningsgrunnlagRepository.hentSisteBeregningsgrunnlagGrunnlagEntitetForBehandlinger(
            input.getKoblingReferanse().getKoblingId(),
            input.getKoblingReferanse().getOriginalKoblingId(), tilstand);
        sisteBg.ifPresent(gr -> input.leggTilBeregningsgrunnlagIHistorikk(mapGrunnlag(gr, input.getInntektsmeldinger(), medSporingslogg),
            BeregningsgrunnlagTilstand.fraKode(tilstand.getKode())));
    }
}
