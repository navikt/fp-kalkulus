package no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta.periodisering;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;

import no.nav.folketrygdloven.beregningsgrunnlag.RegelmodellOversetter;
import no.nav.folketrygdloven.beregningsgrunnlag.perioder.FastsettPeriodeRegel;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.RegelResultat;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.PeriodeModell;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.SplittetPeriode;
import no.nav.folketrygdloven.kalkulator.BeregningsgrunnlagFeil;
import no.nav.folketrygdloven.kalkulator.JsonMapper;
import no.nav.folketrygdloven.kalkulator.adapter.regelmodelltilvl.MapFastsettBeregningsgrunnlagPerioderFraRegelTilVLNaturalytelse;
import no.nav.folketrygdloven.kalkulator.adapter.regelmodelltilvl.MapRegelSporingFraRegelTilVL;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.periodisering.naturalytelse.MapNaturalytelserFraVLTilRegel;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.output.BeregningsgrunnlagRegelResultat;
import no.nav.folketrygdloven.kalkulator.output.RegelSporingAggregat;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagRegelType;
import no.nav.fpsak.nare.evaluation.Evaluation;


@ApplicationScoped
public class FastsettBeregningsgrunnlagPerioderTjeneste {
    public static final int MÅNEDER_I_1_ÅR = 12;

    private MapFastsettBeregningsgrunnlagPerioderFraRegelTilVLNaturalytelse oversetterFraRegelNaturalytelse = new MapFastsettBeregningsgrunnlagPerioderFraRegelTilVLNaturalytelse();

    public BeregningsgrunnlagRegelResultat fastsettPerioderForNaturalytelse(BeregningsgrunnlagInput input,
                                                                            BeregningsgrunnlagDto beregningsgrunnlag) {
        PeriodeModell periodeModell = MapNaturalytelserFraVLTilRegel.map(input, beregningsgrunnlag);
        return kjørRegelOgMapTilVLNaturalytelse(beregningsgrunnlag, periodeModell);
    }

    private BeregningsgrunnlagRegelResultat kjørRegelOgMapTilVLNaturalytelse(BeregningsgrunnlagDto beregningsgrunnlag, PeriodeModell input) {
        String regelInput = toJson(input);
        List<SplittetPeriode> splittedePerioder = new ArrayList<>();
        Evaluation evaluation = new FastsettPeriodeRegel().evaluer(input, splittedePerioder);
        RegelResultat regelResultat = RegelmodellOversetter.getRegelResultat(evaluation, regelInput);
        BeregningsgrunnlagDto nyttBeregningsgrunnlag = oversetterFraRegelNaturalytelse.mapFraRegel(splittedePerioder, beregningsgrunnlag);
        return new BeregningsgrunnlagRegelResultat(nyttBeregningsgrunnlag,
                new RegelSporingAggregat(MapRegelSporingFraRegelTilVL.mapRegelSporingGrunnlag(regelResultat, BeregningsgrunnlagRegelType.PERIODISERING_NATURALYTELSE)));
    }

    private String toJson(Object o) {
        return JsonMapper.toJson(o, BeregningsgrunnlagFeil::kanIkkeSerialisereRegelinput);
    }
}
