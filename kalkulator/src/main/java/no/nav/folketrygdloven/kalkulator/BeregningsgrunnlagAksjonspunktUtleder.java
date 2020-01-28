package no.nav.folketrygdloven.kalkulator;

import java.util.List;

import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.output.BeregningAksjonspunktResultat;

public interface BeregningsgrunnlagAksjonspunktUtleder {

    List<BeregningAksjonspunktResultat> utledAksjonspunkterFor(BeregningsgrunnlagInput input,
                                                               BeregningsgrunnlagGrunnlagDto beregningsgrunnlagGrunnlag,
                                                               boolean erOverstyrt);

}
