package no.nav.folketrygdloven.kalkulator.steg.fordeling.periodisering;

import static no.nav.folketrygdloven.kalkulator.adapter.regelmodelltilvl.MapRegelSporingFraRegelTilVL.mapRegelSporingGrunnlag;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import no.nav.folketrygdloven.beregningsgrunnlag.RegelmodellOversetter;
import no.nav.folketrygdloven.beregningsgrunnlag.perioder.gradering.FastsettPerioderGraderingRegel;
import no.nav.folketrygdloven.beregningsgrunnlag.perioder.refusjon.FastsettPerioderRefusjonRegel;
import no.nav.folketrygdloven.beregningsgrunnlag.perioder.utbetalingsgrad.FastsettPerioderForUtbetalingsgradRegel;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.RegelResultat;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.gradering.PeriodeModellGradering;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.refusjon.PeriodeModellRefusjon;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.utbetalingsgrad.PeriodeModellUtbetalingsgrad;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.SplittetPeriode;
import no.nav.folketrygdloven.kalkulator.BeregningsgrunnlagFeil;
import no.nav.folketrygdloven.kalkulator.FagsakYtelseTypeRef;
import no.nav.folketrygdloven.kalkulator.JsonMapper;
import no.nav.folketrygdloven.kalkulator.adapter.regelmodelltilvl.MapFastsettBeregningsgrunnlagPerioderFraRegelTilVLGraderingOgUtbetalingsgrad;
import no.nav.folketrygdloven.kalkulator.adapter.regelmodelltilvl.MapFastsettBeregningsgrunnlagPerioderFraRegelTilVLRefusjon;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.periodisering.gradering.MapPerioderForGraderingFraVLTilRegel;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.periodisering.refusjon.MapRefusjonPerioderFraVLTilRegel;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.output.BeregningsgrunnlagRegelResultat;
import no.nav.folketrygdloven.kalkulator.output.RegelSporingAggregat;
import no.nav.folketrygdloven.kalkulator.ytelse.utbgradytelse.MapPerioderForUtbetalingsgradFraVLTilRegel;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagRegelType;
import no.nav.folketrygdloven.kalkulus.kodeverk.FagsakYtelseType;
import no.nav.fpsak.nare.evaluation.Evaluation;


/**
 * Splitter periode ved endring i refusjon, gradering og utbetalingsgrad
 *
 * Sette refusjon på andeler med gyldig refusjon
 */
@ApplicationScoped
public class FordelPerioderTjeneste {
    private Instance<MapRefusjonPerioderFraVLTilRegel> oversetterTilRegelRefusjon;
    private final MapFastsettBeregningsgrunnlagPerioderFraRegelTilVLGraderingOgUtbetalingsgrad oversetterFraRegelGraderingOgUtbetalingsgrad = new MapFastsettBeregningsgrunnlagPerioderFraRegelTilVLGraderingOgUtbetalingsgrad();
    private final MapFastsettBeregningsgrunnlagPerioderFraRegelTilVLRefusjon oversetterFraRegelRefusjon = new MapFastsettBeregningsgrunnlagPerioderFraRegelTilVLRefusjon();

    FordelPerioderTjeneste() {
        // For CDI
    }

    @Inject
    public FordelPerioderTjeneste(@Any Instance<MapRefusjonPerioderFraVLTilRegel> oversetterTilRegelRefusjon) {
        this.oversetterTilRegelRefusjon = oversetterTilRegelRefusjon;
    }



    public BeregningsgrunnlagRegelResultat fastsettPerioderForRefusjon(BeregningsgrunnlagInput input) {
        var beregningsgrunnlag = input.getBeregningsgrunnlag();
        var ref = input.getKoblingReferanse();
        var mapperForYtelse = FagsakYtelseTypeRef.Lookup.find(oversetterTilRegelRefusjon, ref.getFagsakYtelseType());

        return mapperForYtelse.map(mapper -> {
                    PeriodeModellRefusjon periodeModell = mapper.map(input, beregningsgrunnlag);
                    return kjørRegelOgMapTilVLRefusjon(beregningsgrunnlag, periodeModell);
                }
        ).orElse(new BeregningsgrunnlagRegelResultat(beregningsgrunnlag, List.of()));
    }

    public BeregningsgrunnlagRegelResultat fastsettPerioderForUtbetalingsgradEllerGradering(BeregningsgrunnlagInput input,
                                                                                            BeregningsgrunnlagDto beregningsgrunnlag) {
        if (input.getFagsakYtelseType().equals(FagsakYtelseType.FORELDREPENGER)) {
            return fastsettPerioderForGradering(input, beregningsgrunnlag);
        } else {
            return fastsettPerioderForUtbetalingsgrad(input, beregningsgrunnlag);
        }
    }

    private BeregningsgrunnlagRegelResultat fastsettPerioderForUtbetalingsgrad(BeregningsgrunnlagInput input,
                                                                                            BeregningsgrunnlagDto beregningsgrunnlag) {
        var periodeModell = MapPerioderForUtbetalingsgradFraVLTilRegel.map(input, beregningsgrunnlag);
        return kjørRegelOgMapTilVLUtbetalingsgrad(beregningsgrunnlag, periodeModell);
    }

    private BeregningsgrunnlagRegelResultat fastsettPerioderForGradering(BeregningsgrunnlagInput input,
                                                                                         BeregningsgrunnlagDto beregningsgrunnlag) {
        var periodeModell = MapPerioderForGraderingFraVLTilRegel.map(input, beregningsgrunnlag);
        return kjørRegelOgMapTilVLGradering(beregningsgrunnlag, periodeModell);
    }

    private BeregningsgrunnlagRegelResultat kjørRegelOgMapTilVLRefusjon(BeregningsgrunnlagDto beregningsgrunnlag,
                                                                        PeriodeModellRefusjon input) {
        String regelInput = toJson(input);
        List<SplittetPeriode> splittedePerioder = new ArrayList<>();
        Evaluation evaluation = new FastsettPerioderRefusjonRegel().evaluer(input, splittedePerioder);
        RegelResultat regelResultat = RegelmodellOversetter.getRegelResultat(evaluation, regelInput);
        var nyttBeregningsgrunnlag = oversetterFraRegelRefusjon.mapFraRegel(splittedePerioder, beregningsgrunnlag);
        return new BeregningsgrunnlagRegelResultat(
                nyttBeregningsgrunnlag,
                new RegelSporingAggregat(mapRegelSporingGrunnlag(regelResultat, BeregningsgrunnlagRegelType.PERIODISERING_REFUSJON)));
    }


    private BeregningsgrunnlagRegelResultat kjørRegelOgMapTilVLGradering(BeregningsgrunnlagDto beregningsgrunnlag,
                                                                                          PeriodeModellGradering input) {
        String regelInput = toJson(input);
        List<SplittetPeriode> splittedePerioder = new ArrayList<>();
        Evaluation evaluation = new FastsettPerioderGraderingRegel().evaluer(input, splittedePerioder);
        RegelResultat regelResultat = RegelmodellOversetter.getRegelResultat(evaluation, regelInput);
        var nyttBeregningsgrunnlag = oversetterFraRegelGraderingOgUtbetalingsgrad.mapFraRegel(splittedePerioder, beregningsgrunnlag);
        return new BeregningsgrunnlagRegelResultat(
                nyttBeregningsgrunnlag,
                new RegelSporingAggregat(mapRegelSporingGrunnlag(regelResultat, BeregningsgrunnlagRegelType.PERIODISERING_GRADERING)));
    }

    private BeregningsgrunnlagRegelResultat kjørRegelOgMapTilVLUtbetalingsgrad(BeregningsgrunnlagDto beregningsgrunnlag,
                                                                               PeriodeModellUtbetalingsgrad input) {
        String regelInput = toJson(input);
        List<SplittetPeriode> splittedePerioder = new ArrayList<>();
        Evaluation evaluation = new FastsettPerioderForUtbetalingsgradRegel().evaluer(input, splittedePerioder);
        RegelResultat regelResultat = RegelmodellOversetter.getRegelResultat(evaluation, regelInput);
        var nyttBeregningsgrunnlag = oversetterFraRegelGraderingOgUtbetalingsgrad.mapFraRegel(splittedePerioder, beregningsgrunnlag);
        return new BeregningsgrunnlagRegelResultat(
                nyttBeregningsgrunnlag,
                new RegelSporingAggregat(mapRegelSporingGrunnlag(regelResultat, BeregningsgrunnlagRegelType.PERIODISERING_UTBETALINGSGRAD)));
    }

    private String toJson(Object o) {
        return JsonMapper.toJson(o, BeregningsgrunnlagFeil::kanIkkeSerialisereRegelinput);
    }
}
