package no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta.periodisering;

import java.util.ArrayList;
import java.util.List;

import no.nav.folketrygdloven.beregningsgrunnlag.RegelmodellOversetter;
import no.nav.folketrygdloven.beregningsgrunnlag.perioder.naturalytelse.FastsettPerioderNaturalytelseRegel;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.RegelResultat;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.naturalytelse.PeriodeModellNaturalytelse;
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


/**
 * Oppretter perioder der det er endring i naturalytelser for ARBEIDSTAKER. Bortfalte naturalytelser skal tas med i beregningsgrunnlaget. Dersom naturalytelsen tilkommer igjen skal den fjernes.
 *
 * § 8-29: "... Fra det tidspunkt arbeidstakeren ikke lenger mottar ytelsene, tas de med ved beregningen med den verdi som nyttes ved forskottstrekk av skatt."
 *
 *
 */
public class FastsettNaturalytelsePerioderTjeneste {
    public static final int MÅNEDER_I_1_ÅR = 12;

    private final MapFastsettBeregningsgrunnlagPerioderFraRegelTilVLNaturalytelse oversetterFraRegelNaturalytelse = new MapFastsettBeregningsgrunnlagPerioderFraRegelTilVLNaturalytelse();

    public BeregningsgrunnlagRegelResultat fastsettPerioderForNaturalytelse(BeregningsgrunnlagInput input,
                                                                            BeregningsgrunnlagDto beregningsgrunnlag) {
        PeriodeModellNaturalytelse periodeModell = MapNaturalytelserFraVLTilRegel.map(input, beregningsgrunnlag);
        return kjørRegelOgMapTilVLNaturalytelse(beregningsgrunnlag, periodeModell);
    }

    private BeregningsgrunnlagRegelResultat kjørRegelOgMapTilVLNaturalytelse(BeregningsgrunnlagDto beregningsgrunnlag, PeriodeModellNaturalytelse input) {
        String regelInput = toJson(input);
        List<SplittetPeriode> splittedePerioder = new ArrayList<>();
        Evaluation evaluation = new FastsettPerioderNaturalytelseRegel().evaluer(input, splittedePerioder);
        RegelResultat regelResultat = RegelmodellOversetter.getRegelResultat(evaluation, regelInput);
        BeregningsgrunnlagDto nyttBeregningsgrunnlag = oversetterFraRegelNaturalytelse.mapFraRegel(splittedePerioder, beregningsgrunnlag);
        return new BeregningsgrunnlagRegelResultat(nyttBeregningsgrunnlag,
                new RegelSporingAggregat(MapRegelSporingFraRegelTilVL.mapRegelSporingGrunnlag(regelResultat, BeregningsgrunnlagRegelType.PERIODISERING_NATURALYTELSE)));
    }

    private String toJson(Object o) {
        return JsonMapper.toJson(o, BeregningsgrunnlagFeil::kanIkkeSerialisereRegelinput);
    }
}
