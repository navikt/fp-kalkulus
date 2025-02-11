package no.nav.folketrygdloven.kalkulus.håndtering;

import no.nav.folketrygdloven.kalkulator.input.HåndterBeregningsgrunnlagInput;

public interface BeregningHåndterer<T> {

    public HåndteringResultat håndter(T dto, HåndterBeregningsgrunnlagInput beregningsgrunnlagInput);

}
