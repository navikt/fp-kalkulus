package no.nav.folketrygdloven.kalkulator.steg.fastsettskjæringstidspunkt;

import java.util.List;

import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.output.BeregningAksjonspunktResultat;
import no.nav.folketrygdloven.kalkulator.output.BeregningsgrunnlagRegelResultat;

public interface AksjonspunktUtlederFastsettBeregningsaktiviteter {

    List<BeregningAksjonspunktResultat> utledAksjonspunkter(BeregningsgrunnlagRegelResultat beregningsgrunnlag,
                                                            BeregningsgrunnlagInput input,
                                                            boolean erOverstyrt);

}
