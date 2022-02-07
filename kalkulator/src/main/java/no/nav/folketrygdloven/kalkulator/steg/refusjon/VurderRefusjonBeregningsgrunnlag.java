package no.nav.folketrygdloven.kalkulator.steg.refusjon;

import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.output.BeregningsgrunnlagRegelResultat;

public interface VurderRefusjonBeregningsgrunnlag {

    BeregningsgrunnlagRegelResultat vurderRefusjon(BeregningsgrunnlagInput input);

}
