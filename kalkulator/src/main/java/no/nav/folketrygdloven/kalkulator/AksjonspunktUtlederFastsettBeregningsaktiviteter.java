package no.nav.folketrygdloven.kalkulator;

import java.util.List;

import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetAggregatDto;
import no.nav.folketrygdloven.kalkulator.output.BeregningAksjonspunktResultat;
import no.nav.folketrygdloven.kalkulator.output.BeregningsgrunnlagRegelResultat;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.FagsakYtelseType;

public interface AksjonspunktUtlederFastsettBeregningsaktiviteter {

    List<BeregningAksjonspunktResultat> utledAksjonspunkter(BeregningsgrunnlagRegelResultat beregningsgrunnlag,
                                                            BeregningAktivitetAggregatDto beregningAktivitetAggregat,
                                                            BeregningsgrunnlagInput input,
                                                            boolean erOverstyrt,
                                                            FagsakYtelseType fagsakYtelseType);

}
