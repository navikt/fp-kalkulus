package no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta.fakta;

import java.util.Optional;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.FaktaAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;

public interface FastsettFakta {

    Optional<FaktaAggregatDto> fastsettFakta(BeregningsgrunnlagDto beregningsgrunnlag, InntektArbeidYtelseGrunnlagDto iayGrunnlag);

}
