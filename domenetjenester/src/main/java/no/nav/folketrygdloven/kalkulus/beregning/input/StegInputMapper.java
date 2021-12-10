package no.nav.folketrygdloven.kalkulus.beregning.input;

import static no.nav.folketrygdloven.kalkulus.beregning.MapStegTilTilstand.mapTilStegTilstand;
import static no.nav.folketrygdloven.kalkulus.beregning.MapStegTilTilstand.mapTilStegUtTilstand;

import java.time.LocalDate;
import java.time.MonthDay;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.beregningsgrunnlag.Grunnbeløp;
import no.nav.folketrygdloven.kalkulator.input.FaktaOmBeregningInput;
import no.nav.folketrygdloven.kalkulator.input.FordelBeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.input.ForeslåBeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.input.ForeslåBesteberegningInput;
import no.nav.folketrygdloven.kalkulator.input.FullføreBeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.input.StegProsesseringInput;
import no.nav.folketrygdloven.kalkulator.input.VurderBeregningsgrunnlagvilkårInput;
import no.nav.folketrygdloven.kalkulator.input.VurderRefusjonBeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagGrunnlagEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.Beløp;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.kobling.KoblingEntitet;
import no.nav.folketrygdloven.kalkulus.felles.v1.KalkulatorInputDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningSteg;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulus.mapFraEntitet.BehandlingslagerTilKalkulusMapper;
import no.nav.folketrygdloven.kalkulus.mappers.GrunnbeløpMapper;
import no.nav.folketrygdloven.kalkulus.mappers.MapFraKalkulator;
import no.nav.folketrygdloven.kalkulus.tjeneste.beregningsgrunnlag.BeregningsgrunnlagRepository;

class StegInputMapper {

    public static final MonthDay ENDRING_AV_GRUNNBELØP = MonthDay.of(5, 1);


    private final BeregningsgrunnlagRepository beregningsgrunnlagRepository;

    StegInputMapper(BeregningsgrunnlagRepository beregningsgrunnlagRepository) {
        this.beregningsgrunnlagRepository = beregningsgrunnlagRepository;
    }

    protected StegProsesseringInput mapStegInput(KoblingEntitet kobling,
                                                 KalkulatorInputDto input,
                                                 BeregningsgrunnlagGrunnlagEntitet grunnlagEntitet,
                                                 BeregningSteg stegType,
                                                 List<Long> originaleKoblinger) {
        StegProsesseringInput stegProsesseringInput = lagStegProsesseringInput(kobling, input, grunnlagEntitet, stegType);
        if (stegType.equals(BeregningSteg.KOFAKBER)) {
            return new FaktaOmBeregningInput(stegProsesseringInput).medGrunnbeløpsatser(finnSatser());
        } else if (stegType.equals(BeregningSteg.FORS_BESTEBEREGNING)) {
            return lagInputForeslåBesteberegning(stegProsesseringInput);
        } else if (stegType.equals(BeregningSteg.FORS_BERGRUNN)) {
            return lagInputForeslå(stegProsesseringInput);
        } else if (stegType.equals(BeregningSteg.VURDER_VILKAR_BERGRUNN)) {
            Optional<BeregningsgrunnlagGrunnlagEntitet> førsteFastsatteGrunnlagEntitet = finnFørsteFastsatteGrunnlagEtterEndringAvGrunnbeløpForVilkårsperiode(kobling, stegProsesseringInput.getSkjæringstidspunktForBeregning());
            return lagInputVurderVilkår(stegProsesseringInput, førsteFastsatteGrunnlagEntitet);
        } else if (stegType.equals(BeregningSteg.VURDER_REF_BERGRUNN)) {
            Optional<BeregningsgrunnlagGrunnlagEntitet> førsteFastsatteGrunnlagEntitet = finnFørsteFastsatteGrunnlagEtterEndringAvGrunnbeløpForVilkårsperiode(kobling, stegProsesseringInput.getSkjæringstidspunktForBeregning());
            return lagInputVurderRefusjon(stegProsesseringInput, førsteFastsatteGrunnlagEntitet, originaleKoblinger);
        } else if (stegType.equals(BeregningSteg.FORDEL_BERGRUNN)) {
            Optional<BeregningsgrunnlagGrunnlagEntitet> førsteFastsatteGrunnlagEntitet = finnFørsteFastsatteGrunnlagEtterEndringAvGrunnbeløpForVilkårsperiode(kobling, stegProsesseringInput.getSkjæringstidspunktForBeregning());
            return lagInputFordel(stegProsesseringInput, førsteFastsatteGrunnlagEntitet);
        } else if (stegType.equals(BeregningSteg.FAST_BERGRUNN)) {
            Optional<BeregningsgrunnlagGrunnlagEntitet> førsteFastsatteGrunnlagEntitet = finnFørsteFastsatteGrunnlagEtterEndringAvGrunnbeløpForVilkårsperiode(kobling, stegProsesseringInput.getSkjæringstidspunktForBeregning());
            return lagInputFullføre(stegProsesseringInput, førsteFastsatteGrunnlagEntitet);
        }
        return stegProsesseringInput;
    }

    private VurderBeregningsgrunnlagvilkårInput lagInputVurderVilkår(StegProsesseringInput stegProsesseringInput,
                                                                     Optional<BeregningsgrunnlagGrunnlagEntitet> førsteFastsatteGrunnlagEntitet) {
        var vurderVilkårInput = new VurderBeregningsgrunnlagvilkårInput(stegProsesseringInput);
        if (førsteFastsatteGrunnlagEntitet.isPresent()) {
            vurderVilkårInput = førsteFastsatteGrunnlagEntitet.get().getBeregningsgrunnlag()
                    .map(BeregningsgrunnlagEntitet::getGrunnbeløp)
                    .map(Beløp::getVerdi)
                    .map(vurderVilkårInput::medUregulertGrunnbeløp)
                    .orElse(vurderVilkårInput);
        }
        return vurderVilkårInput;
    }


    private StegProsesseringInput lagInputVurderRefusjon(StegProsesseringInput stegProsesseringInput,
                                                         Optional<BeregningsgrunnlagGrunnlagEntitet> førsteFastsatteGrunnlagEntitet,
                                                         List<Long> originaleKoblinger) {
        var vurderVilkårOgRefusjonBeregningsgrunnlag = new VurderRefusjonBeregningsgrunnlagInput(stegProsesseringInput);
        List<BeregningsgrunnlagGrunnlagDto> originaltGrunnlag = finnOriginaltGrunnlag(originaleKoblinger);
        vurderVilkårOgRefusjonBeregningsgrunnlag = vurderVilkårOgRefusjonBeregningsgrunnlag.medBeregningsgrunnlagGrunnlagFraForrigeBehandling(originaltGrunnlag);
        if (førsteFastsatteGrunnlagEntitet.isPresent()) {
            vurderVilkårOgRefusjonBeregningsgrunnlag = førsteFastsatteGrunnlagEntitet.get().getBeregningsgrunnlag()
                    .map(BeregningsgrunnlagEntitet::getGrunnbeløp)
                    .map(Beløp::getVerdi)
                    .map(vurderVilkårOgRefusjonBeregningsgrunnlag::medUregulertGrunnbeløp)
                    .orElse(vurderVilkårOgRefusjonBeregningsgrunnlag);
        }
        return vurderVilkårOgRefusjonBeregningsgrunnlag;
    }

    private List<BeregningsgrunnlagGrunnlagDto> finnOriginaltGrunnlag(List<Long> originaleKoblinger) {
        return originaleKoblinger.stream().flatMap(k -> finnGrunnlagForKobling(k).stream()).collect(Collectors.toList());

    }

    private Optional<BeregningsgrunnlagGrunnlagDto> finnGrunnlagForKobling(Long koblingId) {
        Optional<BeregningsgrunnlagGrunnlagEntitet> entitet = beregningsgrunnlagRepository.hentBeregningsgrunnlagGrunnlagEntitet(koblingId);
        return entitet.map(BehandlingslagerTilKalkulusMapper::mapGrunnlag);
    }

    private StegProsesseringInput lagStegProsesseringInput(KoblingEntitet kobling, KalkulatorInputDto input, BeregningsgrunnlagGrunnlagEntitet grunnlagEntitet, BeregningSteg stegType) {
        var beregningsgrunnlagInput = MapFraKalkulator.mapFraKalkulatorInputTilBeregningsgrunnlagInput(kobling, input, Optional.of(grunnlagEntitet));
        var grunnlagFraSteg = beregningsgrunnlagRepository.hentSisteBeregningsgrunnlagGrunnlagEntitetForBehandlinger(
                kobling,
                beregningsgrunnlagInput.getSkjæringstidspunktOpptjening(),
                mapTilStegTilstand(stegType));
        var grunnlagFraStegUt = finnForrigeAvklartGrunnlagHvisFinnes(grunnlagFraSteg, stegType);
        return new StegProsesseringInput(beregningsgrunnlagInput, mapTilStegTilstand(stegType))
                .medForrigeGrunnlagFraStegUt(grunnlagFraStegUt.map(BehandlingslagerTilKalkulusMapper::mapGrunnlag).orElse(null))
                .medForrigeGrunnlagFraSteg(grunnlagFraSteg.map(BehandlingslagerTilKalkulusMapper::mapGrunnlag).orElse(null))
                .medStegUtTilstand(mapTilStegUtTilstand(stegType).orElse(null));
    }

    private ForeslåBesteberegningInput lagInputForeslåBesteberegning(StegProsesseringInput stegProsesseringInput) {
        var input = new ForeslåBesteberegningInput(stegProsesseringInput);
        return input.medGrunnbeløpsatser(finnSatser());
    }

    private ForeslåBeregningsgrunnlagInput lagInputForeslå(StegProsesseringInput stegProsesseringInput) {
        var foreslåBeregningsgrunnlagInput = new ForeslåBeregningsgrunnlagInput(stegProsesseringInput);
        return foreslåBeregningsgrunnlagInput.medGrunnbeløpsatser(finnSatser());
    }

    private FordelBeregningsgrunnlagInput lagInputFordel(StegProsesseringInput stegProsesseringInput, Optional<BeregningsgrunnlagGrunnlagEntitet> førsteFastsatteGrunnlagEntitet) {
        var fordelBeregningsgrunnlagInput = new FordelBeregningsgrunnlagInput(stegProsesseringInput);
        if (førsteFastsatteGrunnlagEntitet.isPresent()) {
            fordelBeregningsgrunnlagInput = førsteFastsatteGrunnlagEntitet.get().getBeregningsgrunnlag()
                    .map(BeregningsgrunnlagEntitet::getGrunnbeløp)
                    .map(Beløp::getVerdi)
                    .map(fordelBeregningsgrunnlagInput::medUregulertGrunnbeløp)
                    .orElse(fordelBeregningsgrunnlagInput);
        }
        return fordelBeregningsgrunnlagInput;
    }

    private FullføreBeregningsgrunnlagInput lagInputFullføre(StegProsesseringInput stegProsesseringInput, Optional<BeregningsgrunnlagGrunnlagEntitet> førsteFastsatteGrunnlagEntitet) {
        var fullføreBeregningsgrunnlagInput = new FullføreBeregningsgrunnlagInput(stegProsesseringInput);
        if (førsteFastsatteGrunnlagEntitet.isPresent()) {
            fullføreBeregningsgrunnlagInput = førsteFastsatteGrunnlagEntitet.get().getBeregningsgrunnlag()
                    .map(BeregningsgrunnlagEntitet::getGrunnbeløp)
                    .map(Beløp::getVerdi)
                    .map(fullføreBeregningsgrunnlagInput::medUregulertGrunnbeløp)
                    .orElse(fullføreBeregningsgrunnlagInput);
        }
        return fullføreBeregningsgrunnlagInput;
    }


    private List<Grunnbeløp> finnSatser() {
        return new GrunnbeløpMapper(beregningsgrunnlagRepository.finnAlleSatser()).mapGrunnbeløpSatser();
    }

    private Optional<BeregningsgrunnlagGrunnlagEntitet> finnFørsteFastsatteGrunnlagEtterEndringAvGrunnbeløpForVilkårsperiode(KoblingEntitet koblingEntitet,
                                                                                                                             LocalDate skjæringstidspunktBeregning) {
        if (MonthDay.from(skjæringstidspunktBeregning).isBefore(ENDRING_AV_GRUNNBELØP)) {
            return Optional.empty();
        }
        return beregningsgrunnlagRepository.hentOriginalGrunnlagForTilstand(koblingEntitet.getId(), BeregningsgrunnlagTilstand.FASTSATT);
    }

    private Optional<BeregningsgrunnlagGrunnlagEntitet> finnForrigeAvklartGrunnlagHvisFinnes(Optional<BeregningsgrunnlagGrunnlagEntitet> forrigeGrunnlagFraSteg,
                                                                                             BeregningSteg stegType) {
        Optional<BeregningsgrunnlagTilstand> tilstandUt = mapTilStegUtTilstand(stegType);
        if (tilstandUt.isEmpty()) {
            return Optional.empty();
        }
        return finnForrigeAvklarteGrunnlagForTilstand(forrigeGrunnlagFraSteg, tilstandUt.get());
    }

    private Optional<BeregningsgrunnlagGrunnlagEntitet> finnForrigeAvklarteGrunnlagForTilstand(Optional<BeregningsgrunnlagGrunnlagEntitet> forrigeGrunnlagFraSteg, BeregningsgrunnlagTilstand beregningsgrunnlagTilstand) {
        return forrigeGrunnlagFraSteg
                .map(BeregningsgrunnlagGrunnlagEntitet::getKoblingId)
                .flatMap(koblingId -> beregningsgrunnlagRepository.hentSisteBeregningsgrunnlagGrunnlagEntitetOpprettetEtter(koblingId, forrigeGrunnlagFraSteg.get().getOpprettetTidspunkt(), beregningsgrunnlagTilstand));
    }


}
