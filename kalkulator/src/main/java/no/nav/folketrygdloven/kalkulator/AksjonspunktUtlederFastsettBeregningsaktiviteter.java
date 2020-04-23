package no.nav.folketrygdloven.kalkulator;

import java.util.List;
import java.util.Optional;

import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.output.BeregningAksjonspunktResultat;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.FagsakYtelseType;

public interface AksjonspunktUtlederFastsettBeregningsaktiviteter {

    List<BeregningAksjonspunktResultat> utledAksjonspunkter(Optional<BeregningsgrunnlagDto> beregningsgrunnlag,
                                                            BeregningAktivitetAggregatDto beregningAktivitetAggregat,
                                                            BeregningsgrunnlagInput input,
                                                            boolean erOverstyrt,
                                                            FagsakYtelseType fagsakYtelseType);

}
