package no.nav.folketrygdloven.kalkulator.ytelse.frisinn;

import static no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta.beregningsperiode.FastsettBeregningsperiodeATFL.fastsettBeregningsperiodeForATFL;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;

public class FastsettBeregningsperiodeTjenesteFRISINN {

    public BeregningsgrunnlagDto fastsettBeregningsperiode(BeregningsgrunnlagDto beregningsgrunnlag) {
        return fastsettBeregningsperiodeForATFL(beregningsgrunnlag, new BeregningsperiodeTjenesteFRISINN().fastsettBeregningsperiodeForATFLAndeler(beregningsgrunnlag.getSkjæringstidspunkt()));
    }


}
