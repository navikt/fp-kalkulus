package no.nav.folketrygdloven.kalkulator.guitjenester.fakta;

import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagRestInput;

public interface FastsettGrunnlag {
    boolean skalGrunnlagFastsettes(BeregningsgrunnlagRestInput input, no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto andel);
}
