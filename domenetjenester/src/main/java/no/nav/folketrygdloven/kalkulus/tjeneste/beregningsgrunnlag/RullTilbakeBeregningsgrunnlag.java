package no.nav.folketrygdloven.kalkulus.tjeneste.beregningsgrunnlag;

import java.util.Set;

import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand;

public interface RullTilbakeBeregningsgrunnlag {

    public void rullTilbakeGrunnlag(BeregningsgrunnlagTilstand tilstand, Set<Long> rullTilbakeKoblinger);

}
