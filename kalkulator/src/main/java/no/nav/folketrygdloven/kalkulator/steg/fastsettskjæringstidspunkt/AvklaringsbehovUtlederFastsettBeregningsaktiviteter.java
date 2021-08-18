package no.nav.folketrygdloven.kalkulator.steg.fastsettskjæringstidspunkt;

import java.util.List;

import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.output.BeregningAvklaringsbehovResultat;
import no.nav.folketrygdloven.kalkulator.output.BeregningsgrunnlagRegelResultat;

public interface AvklaringsbehovUtlederFastsettBeregningsaktiviteter {

    List<BeregningAvklaringsbehovResultat> utledAvklaringsbehov(BeregningsgrunnlagRegelResultat beregningsgrunnlag,
                                                            BeregningsgrunnlagInput input,
                                                            boolean erOverstyrt);

}
