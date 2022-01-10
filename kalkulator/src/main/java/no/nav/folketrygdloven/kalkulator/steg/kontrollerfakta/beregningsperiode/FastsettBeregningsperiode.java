package no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta.beregningsperiode;

import java.util.Collection;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;

public interface FastsettBeregningsperiode {

    BeregningsgrunnlagDto fastsettBeregningsperiode(BeregningsgrunnlagDto beregningsgrunnlag, InntektArbeidYtelseGrunnlagDto inntektArbeidYtelseGrunnlag, Collection<InntektsmeldingDto> inntektsmeldinger);

}
