package no.nav.folketrygdloven.kalkulus.mapTilKontrakt;

import java.math.BigDecimal;
import java.math.RoundingMode;

import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.UtbetalingsgradTjeneste;
import no.nav.folketrygdloven.kalkulator.input.UtbetalingsgradGrunnlag;
import no.nav.folketrygdloven.kalkulator.input.YtelsespesifiktGrunnlag;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;

/**
 * Utleder data som gjelder gradering fra domene
 */
public class UtledGraderingsdata {

    /**
     * Utleder utbetalingsfaktor fra tilkommet inntekt
     * <p>
     * Angir reduksjon pga gradering mot inntekt sammenlignet med å kun gradere mot uttaksgraden
     *
     * @param beregningsgrunnlagPeriode Beregningsgrunnlagsperiode
     * @param ytelsespesifiktGrunnlag   Ytelsesspesifikt grunnlag
     * @return Utbetalingsfaktor
     */
    public static BigDecimal utledGraderingsfaktorInntekt(no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto beregningsgrunnlagPeriode, YtelsespesifiktGrunnlag ytelsespesifiktGrunnlag) {
        if (ytelsespesifiktGrunnlag instanceof UtbetalingsgradGrunnlag) {
            var inntektgraderingsprosentBrutto = beregningsgrunnlagPeriode.getInntektgraderingsprosentBrutto();
            if (inntektgraderingsprosentBrutto == null) {
                return null;
            }
            var grunnlagGradertMotUttaksgrad = beregningsgrunnlagPeriode.getBeregningsgrunnlagPrStatusOgAndelList()
                    .stream().map(a -> gradertMotUttaksgrad(beregningsgrunnlagPeriode, ytelsespesifiktGrunnlag, a))
                    .reduce(BigDecimal::add)
                    .orElse(BigDecimal.ZERO);
            var totaltGrunnlag = beregningsgrunnlagPeriode.getBeregningsgrunnlagPrStatusOgAndelList()
                    .stream().map(a -> a.getBruttoPrÅr() != null ? a.getBruttoPrÅr() : BigDecimal.ZERO)
                    .reduce(BigDecimal::add)
                    .orElse(BigDecimal.ZERO);


            if (grunnlagGradertMotUttaksgrad.compareTo(BigDecimal.ZERO) == 0) {
                return BigDecimal.ZERO;
            }

            return inntektgraderingsprosentBrutto.multiply(totaltGrunnlag).divide(grunnlagGradertMotUttaksgrad, 2, RoundingMode.HALF_UP);
        }
        return null;
    }


    /**
     * Utleder utbetalingsfaktor fra uttaksgrad/arbeidstid.
     * <p>
     * Utbetalingsfaktoren tilsvarer det som andre steder også kalles ytelsessgrad og er en vektet utbetalingsfaktor basert på uttaksgrad og inntekt/beregningsgrunnlag.
     *
     * @param beregningsgrunnlagPeriode Beregningsgrunnlagsperiode
     * @param ytelsespesifiktGrunnlag   Ytelsesspesifikt grunnlag
     * @return Utbetalingsfaktor
     */
    public static BigDecimal utledGraderingsfaktorTid(no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto beregningsgrunnlagPeriode,
                                                      YtelsespesifiktGrunnlag ytelsespesifiktGrunnlag) {
        if (ytelsespesifiktGrunnlag instanceof UtbetalingsgradGrunnlag) {
            var inntektgraderingsprosentBrutto = beregningsgrunnlagPeriode.getInntektgraderingsprosentBrutto();
            if (inntektgraderingsprosentBrutto == null) {
                return null;
            }
            var grunnlagGradertMotUttaksgrad = beregningsgrunnlagPeriode.getBeregningsgrunnlagPrStatusOgAndelList()
                    .stream().map(a -> gradertMotUttaksgrad(beregningsgrunnlagPeriode, ytelsespesifiktGrunnlag, a))
                    .reduce(BigDecimal::add)
                    .orElse(BigDecimal.ZERO);
            var totaltGrunnlag = beregningsgrunnlagPeriode.getBeregningsgrunnlagPrStatusOgAndelList()
                    .stream().map(a -> a.getBruttoPrÅr() != null ? a.getBruttoPrÅr() : BigDecimal.ZERO)
                    .reduce(BigDecimal::add)
                    .orElse(BigDecimal.ZERO);
            if (totaltGrunnlag.compareTo(BigDecimal.ZERO) == 0) {
                return BigDecimal.valueOf(100);
            }
            return grunnlagGradertMotUttaksgrad.divide(totaltGrunnlag, 2, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
        }
        return null;
    }


    private static BigDecimal gradertMotUttaksgrad(no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto beregningsgrunnlagPeriode, YtelsespesifiktGrunnlag ytelsespesifiktGrunnlag, no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto a) {
        var utbetalingsgradForAndel = UtbetalingsgradTjeneste.finnUtbetalingsgradForAndel(a,
                Intervall.fraOgMedTilOgMed(beregningsgrunnlagPeriode.getBeregningsgrunnlagPeriodeFom(), beregningsgrunnlagPeriode.getBeregningsgrunnlagPeriodeTom()),
                ytelsespesifiktGrunnlag,
                false
        );
        var bruttoInntekt = a.getBruttoPrÅr();
        if (bruttoInntekt == null) {
            return BigDecimal.ZERO;
        }

        return utbetalingsgradForAndel.multiply(bruttoInntekt).divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP);
    }

}
