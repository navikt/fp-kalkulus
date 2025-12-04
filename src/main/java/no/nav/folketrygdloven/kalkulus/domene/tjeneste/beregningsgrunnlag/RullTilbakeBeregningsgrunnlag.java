package no.nav.folketrygdloven.kalkulus.domene.tjeneste.beregningsgrunnlag;

import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand;

public interface RullTilbakeBeregningsgrunnlag {

    void rullTilbakeGrunnlag(BeregningsgrunnlagTilstand tilstand, Long rullTilbakeKobling);

}
