package no.nav.folketrygdloven.kalkulus.domene.beregning.input;

import static no.nav.folketrygdloven.kalkulus.domene.beregning.MapStegTilTilstand.mapTilStegTilstand;
import static no.nav.folketrygdloven.kalkulus.domene.beregning.MapStegTilTilstand.mapTilStegUtTilstand;

import java.time.LocalDate;
import java.time.MonthDay;
import java.util.List;
import java.util.Optional;

import no.nav.folketrygdloven.kalkulator.input.FaktaOmBeregningInput;
import no.nav.folketrygdloven.kalkulator.input.FastsettBeregningsaktiviteterInput;
import no.nav.folketrygdloven.kalkulator.input.FordelBeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.input.ForeslåBeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.input.ForeslåBesteberegningInput;
import no.nav.folketrygdloven.kalkulator.input.FortsettForeslåBeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.input.FullføreBeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.input.GrunnbeløpInput;
import no.nav.folketrygdloven.kalkulator.input.StegProsesseringInput;
import no.nav.folketrygdloven.kalkulator.input.VurderBeregningsgrunnlagvilkårInput;
import no.nav.folketrygdloven.kalkulator.input.VurderRefusjonBeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagGrunnlagEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.kobling.KoblingEntitet;
import no.nav.folketrygdloven.kalkulus.domene.kobling.KoblingTjeneste;
import no.nav.folketrygdloven.kalkulus.domene.mapFraEntitet.BehandlingslagerTilKalkulusMapper;
import no.nav.folketrygdloven.kalkulus.domene.mappers.GrunnbeløpMapper;
import no.nav.folketrygdloven.kalkulus.domene.mappers.MapFraKalkulator;
import no.nav.folketrygdloven.kalkulus.domene.mappers.VerdityperMapper;
import no.nav.folketrygdloven.kalkulus.domene.tjeneste.beregningsgrunnlag.BeregningsgrunnlagRepository;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningSteg;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand;
import no.nav.foreldrepenger.kalkulus.kontrakt.request.input.KalkulatorInputDto;

class StegInputMapper {

    public static final MonthDay ENDRING_AV_GRUNNBELØP = MonthDay.of(5, 1);


    private final BeregningsgrunnlagRepository beregningsgrunnlagRepository;
    private final KoblingTjeneste koblingTjeneste;

    StegInputMapper(BeregningsgrunnlagRepository beregningsgrunnlagRepository, KoblingTjeneste koblingTjeneste) {
        this.beregningsgrunnlagRepository = beregningsgrunnlagRepository;
        this.koblingTjeneste = koblingTjeneste;
    }

    protected StegProsesseringInput mapStegInput(KoblingEntitet kobling,
                                                 KalkulatorInputDto input,
                                                 Optional<BeregningsgrunnlagGrunnlagEntitet> grunnlagEntitet,
                                                 BeregningSteg stegType,
                                                 Optional<BeregningsgrunnlagGrunnlagEntitet> originaltGrunnlag) {
        StegProsesseringInput stegProsesseringInput = lagStegProsesseringInput(kobling, input, grunnlagEntitet, stegType);
        if (stegType.equals(BeregningSteg.FASTSETT_STP_BER)) {
            return new FastsettBeregningsaktiviteterInput(stegProsesseringInput).medGrunnbeløpInput(finnInputSatser());
        } else if (stegType.equals(BeregningSteg.KOFAKBER)) {
            return new FaktaOmBeregningInput(stegProsesseringInput).medGrunnbeløpInput(finnInputSatser());
        } else if (stegType.equals(BeregningSteg.FORS_BESTEBEREGNING)) {
            return lagInputForeslåBesteberegning(stegProsesseringInput);
        } else if (stegType.equals(BeregningSteg.FORS_BERGRUNN)) {
            return lagInputForeslå(stegProsesseringInput);
        } else if (stegType.equals(BeregningSteg.FORS_BERGRUNN_2)) {
            return lagInputFortsettForeslå(stegProsesseringInput);
        } else if (stegType.equals(BeregningSteg.VURDER_VILKAR_BERGRUNN)) {
            Optional<BeregningsgrunnlagGrunnlagEntitet> førsteFastsatteGrunnlagEntitet = finnFørsteFastsatteGrunnlagEtterEndringAvGrunnbeløp(kobling, stegProsesseringInput.getSkjæringstidspunktForBeregning());
            return lagInputVurderVilkår(stegProsesseringInput, førsteFastsatteGrunnlagEntitet);
        } else if (stegType.equals(BeregningSteg.VURDER_REF_BERGRUNN)) {
            Optional<BeregningsgrunnlagGrunnlagEntitet> førsteFastsatteGrunnlagEntitet = finnFørsteFastsatteGrunnlagEtterEndringAvGrunnbeløp(kobling, stegProsesseringInput.getSkjæringstidspunktForBeregning());
            return lagInputVurderRefusjon(stegProsesseringInput, førsteFastsatteGrunnlagEntitet, originaltGrunnlag);
        } else if (stegType.equals(BeregningSteg.FORDEL_BERGRUNN)) {
            Optional<BeregningsgrunnlagGrunnlagEntitet> førsteFastsatteGrunnlagEntitet = finnFørsteFastsatteGrunnlagEtterEndringAvGrunnbeløp(kobling, stegProsesseringInput.getSkjæringstidspunktForBeregning());
            return lagInputFordel(stegProsesseringInput, førsteFastsatteGrunnlagEntitet);
        } else if (stegType.equals(BeregningSteg.FAST_BERGRUNN)) {
            Optional<BeregningsgrunnlagGrunnlagEntitet> førsteFastsatteGrunnlagEntitet = finnFørsteFastsatteGrunnlagEtterEndringAvGrunnbeløp(kobling, stegProsesseringInput.getSkjæringstidspunktForBeregning());
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
                    .map(VerdityperMapper::beløpFraDao)
                    .map(vurderVilkårInput::medUregulertGrunnbeløp)
                    .orElse(vurderVilkårInput);
        }
        return vurderVilkårInput;
    }


    private StegProsesseringInput lagInputVurderRefusjon(StegProsesseringInput stegProsesseringInput,
                                                         Optional<BeregningsgrunnlagGrunnlagEntitet> førsteFastsatteGrunnlagEntitet,
                                                         Optional<BeregningsgrunnlagGrunnlagEntitet> originaltGrunnlag) {
        var vurderVilkårOgRefusjonBeregningsgrunnlag = new VurderRefusjonBeregningsgrunnlagInput(stegProsesseringInput);
        if (originaltGrunnlag.isPresent()) {
            vurderVilkårOgRefusjonBeregningsgrunnlag = vurderVilkårOgRefusjonBeregningsgrunnlag
                .medBeregningsgrunnlagGrunnlagFraForrigeBehandling(BehandlingslagerTilKalkulusMapper.mapGrunnlag(originaltGrunnlag.get()));
        }
        if (førsteFastsatteGrunnlagEntitet.isPresent()) {
            vurderVilkårOgRefusjonBeregningsgrunnlag = førsteFastsatteGrunnlagEntitet.get().getBeregningsgrunnlag()
                    .map(BeregningsgrunnlagEntitet::getGrunnbeløp)
                    .map(VerdityperMapper::beløpFraDao)
                    .map(vurderVilkårOgRefusjonBeregningsgrunnlag::medUregulertGrunnbeløp)
                    .orElse(vurderVilkårOgRefusjonBeregningsgrunnlag);
        }
        return vurderVilkårOgRefusjonBeregningsgrunnlag;
    }

    private StegProsesseringInput lagStegProsesseringInput(KoblingEntitet kobling,
                                                           KalkulatorInputDto input,
                                                           Optional<BeregningsgrunnlagGrunnlagEntitet> grunnlagEntitet,
                                                           BeregningSteg stegType) {
        var beregningsgrunnlagInput = MapFraKalkulator.mapFraKalkulatorInputTilBeregningsgrunnlagInput(kobling, input, grunnlagEntitet);
        var originalKoblingEntitet = kobling.getOriginalKoblingReferanse().map(koblingTjeneste::hentKobling);
        var grunnlagFraSteg = beregningsgrunnlagRepository.hentSisteBeregningsgrunnlagGrunnlagEntitetForBehandlinger(
                kobling,
                mapTilStegTilstand(stegType),
                originalKoblingEntitet);
        var grunnlagFraStegUt = finnForrigeAvklartGrunnlagHvisFinnes(grunnlagFraSteg, stegType);
        return new StegProsesseringInput(beregningsgrunnlagInput, mapTilStegTilstand(stegType))
                .medForrigeGrunnlagFraStegUt(grunnlagFraStegUt.map(BehandlingslagerTilKalkulusMapper::mapGrunnlag).orElse(null))
                .medForrigeGrunnlagFraSteg(grunnlagFraSteg.map(BehandlingslagerTilKalkulusMapper::mapGrunnlag).orElse(null))
                .medStegUtTilstand(mapTilStegUtTilstand(stegType).orElse(null));
    }

    private ForeslåBesteberegningInput lagInputForeslåBesteberegning(StegProsesseringInput stegProsesseringInput) {
        var input = new ForeslåBesteberegningInput(stegProsesseringInput);
        return input.medGrunnbeløpInput(finnInputSatser());
    }

    private FortsettForeslåBeregningsgrunnlagInput lagInputFortsettForeslå(StegProsesseringInput stegProsesseringInput) {
        var foreslåBeregningsgrunnlagInput = new FortsettForeslåBeregningsgrunnlagInput(stegProsesseringInput);
        return foreslåBeregningsgrunnlagInput.medGrunnbeløpInput(finnInputSatser());
    }


    private ForeslåBeregningsgrunnlagInput lagInputForeslå(StegProsesseringInput stegProsesseringInput) {
        var foreslåBeregningsgrunnlagInput = new ForeslåBeregningsgrunnlagInput(stegProsesseringInput);
        return foreslåBeregningsgrunnlagInput.medGrunnbeløpInput(finnInputSatser());
    }

    private FordelBeregningsgrunnlagInput lagInputFordel(StegProsesseringInput stegProsesseringInput,
                                                         Optional<BeregningsgrunnlagGrunnlagEntitet> førsteFastsatteGrunnlagEntitet) {
        var fordelBeregningsgrunnlagInput = new FordelBeregningsgrunnlagInput(stegProsesseringInput);
        if (førsteFastsatteGrunnlagEntitet.isPresent()) {
            fordelBeregningsgrunnlagInput = førsteFastsatteGrunnlagEntitet.get().getBeregningsgrunnlag()
                    .map(BeregningsgrunnlagEntitet::getGrunnbeløp)
                    .map(VerdityperMapper::beløpFraDao)
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
                    .map(VerdityperMapper::beløpFraDao)
                    .map(fullføreBeregningsgrunnlagInput::medUregulertGrunnbeløp)
                    .orElse(fullføreBeregningsgrunnlagInput);
        }
        return fullføreBeregningsgrunnlagInput;
    }


    private List<GrunnbeløpInput> finnInputSatser() {
        return GrunnbeløpMapper.mapGrunnbeløpInput(beregningsgrunnlagRepository.finnAlleSatser());
    }

    private Optional<BeregningsgrunnlagGrunnlagEntitet> finnFørsteFastsatteGrunnlagEtterEndringAvGrunnbeløp(KoblingEntitet koblingEntitet,
                                                                                                            LocalDate skjæringstidspunktBeregning) {
        if (MonthDay.from(skjæringstidspunktBeregning).isBefore(ENDRING_AV_GRUNNBELØP)) {
            return Optional.empty();
        }
        var alleKoblingIderForSaksnummer = koblingTjeneste.hentAlleKoblingerForSaksnummer(koblingEntitet.getSaksnummer()).stream().map(KoblingEntitet::getId).toList();
        return beregningsgrunnlagRepository.hentFørsteFastsatteGrunnlagForSak(alleKoblingIderForSaksnummer);
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
