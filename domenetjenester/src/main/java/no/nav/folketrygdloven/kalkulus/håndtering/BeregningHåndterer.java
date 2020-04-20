package no.nav.folketrygdloven.kalkulus.håndtering;

import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;

public interface BeregningHåndterer<T> {

    public HåndteringResultat håndter(T dto, BeregningsgrunnlagInput beregningsgrunnlagInput);

}
