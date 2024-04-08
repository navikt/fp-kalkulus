package no.nav.folketrygdloven.kalkulus.tjeneste.beregningsgrunnlag;

import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand;

public interface RullTilbakeBeregningsgrunnlag {

    void rullTilbakeGrunnlag(BeregningsgrunnlagTilstand tilstand, Long rullTilbakeKobling);

}
