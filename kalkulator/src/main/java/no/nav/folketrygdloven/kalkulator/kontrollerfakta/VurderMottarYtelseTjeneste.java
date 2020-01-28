package no.nav.folketrygdloven.kalkulator.kontrollerfakta;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;

public class VurderMottarYtelseTjeneste {

    private VurderMottarYtelseTjeneste() {
        // Skjul
    }

    public static boolean skalVurdereMottattYtelse(BeregningsgrunnlagDto beregningsgrunnlag, InntektArbeidYtelseGrunnlagDto iayGrunnlag) {
        boolean erFrilanser = erFrilanser(beregningsgrunnlag);
        if (erFrilanser) {
            return true;
        }
        return !ArbeidstakerUtenInntektsmeldingTjeneste
            .finnArbeidstakerAndelerUtenInntektsmelding(beregningsgrunnlag, iayGrunnlag).isEmpty();
    }

    public static boolean erFrilanser(BeregningsgrunnlagDto beregningsgrunnlag) {
        return beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList().stream()
            .anyMatch(andel -> andel.getAktivitetStatus().erFrilanser());
    }

}
