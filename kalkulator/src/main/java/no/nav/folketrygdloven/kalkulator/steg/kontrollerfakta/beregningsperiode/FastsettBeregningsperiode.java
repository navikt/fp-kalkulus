package no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta.beregningsperiode;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;

public interface FastsettBeregningsperiode {

    BeregningsgrunnlagDto fastsettBeregningsperiode(BeregningsgrunnlagDto beregningsgrunnlag, InntektArbeidYtelseGrunnlagDto inntektArbeidYtelseGrunnlag);

}
