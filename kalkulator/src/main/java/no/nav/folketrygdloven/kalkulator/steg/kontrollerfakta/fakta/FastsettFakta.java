package no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta.fakta;

import java.util.Collection;
import java.util.Optional;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.FaktaAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;

/**
 * Utleder fakta som kan utledes fra registerdata uten avklaring fra saksbehandler
 */
public interface FastsettFakta {

    Optional<FaktaAggregatDto> fastsettFakta(BeregningsgrunnlagDto beregningsgrunnlag, InntektArbeidYtelseGrunnlagDto iayGrunnlag,
                                             Collection<InntektsmeldingDto> inntektsmeldinger);

}
