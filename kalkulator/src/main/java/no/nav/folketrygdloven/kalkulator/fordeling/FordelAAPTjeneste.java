package no.nav.folketrygdloven.kalkulator.fordeling;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.AktivitetStatus;

import java.math.BigDecimal;

public final class FordelAAPTjeneste {

    private FordelAAPTjeneste() {
        // Skjuler default
    }

    public static boolean periodeHarAndelerMedAAP(BeregningsgrunnlagPeriodeDto periode) {
        return periode.getBeregningsgrunnlagPrStatusOgAndelList().stream()
                .anyMatch(a -> AktivitetStatus.ARBEIDSAVKLARINGSPENGER.equals(a.getAktivitetStatus()));
    }

    public static boolean harAndelerMedAAPOgRefusjonOverstigerInntekt(BeregningsgrunnlagPrStatusOgAndelDto andel,
                                                                      BeregningsgrunnlagPeriodeDto periode,
                                                                      BigDecimal refusjonForAndelIPeriode) {
        boolean harAndelMedAAPIPeriode = periodeHarAndelerMedAAP(periode);
        return harAndelMedAAPIPeriode && harHøyereRefusjonEnnInntekt(refusjonForAndelIPeriode, andel);
    }

    private static boolean harHøyereRefusjonEnnInntekt(BigDecimal refusjonskravPrÅr, BeregningsgrunnlagPrStatusOgAndelDto andel) {
        return harHøyereRefusjonEnnBeregningsgrunnlag(refusjonskravPrÅr, hentBeløpForAndelSomErGjeldendeForFordeling(andel));
    }

    private static boolean harHøyereRefusjonEnnBeregningsgrunnlag(BigDecimal refusjonskravPrÅr, BigDecimal bruttoPrÅr) {
        return refusjonskravPrÅr.compareTo(bruttoPrÅr) > 0;
    }

    public static BigDecimal hentBeløpForAndelSomErGjeldendeForFordeling(BeregningsgrunnlagPrStatusOgAndelDto andel) {
        BigDecimal overstyrtPrÅr = andel.getOverstyrtPrÅr();
        BigDecimal beregnetPrÅr = andel.getBeregnetPrÅr();
        return overstyrtPrÅr == null ? beregnetPrÅr : overstyrtPrÅr;
    }
}
