package no.nav.folketrygdloven.kalkulator.steg.fastsettskjæringstidspunkt;

import java.util.List;

import no.nav.folketrygdloven.beregningsgrunnlag.Grunnbeløp;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetAggregatDto;
import no.nav.folketrygdloven.kalkulator.output.BeregningsgrunnlagRegelResultat;

public interface FastsettSkjæringstidspunktOgStatuser {

    // TODO: Skriv om FastsettSkjæringstidspunktOgStatuser til å ikkje returnere eit beregningsgrunnlag med andeler, men kun aktiviteter og skjæringstidspunkt
    BeregningsgrunnlagRegelResultat fastsett(BeregningsgrunnlagInput input, BeregningAktivitetAggregatDto beregningAktivitetAggregat, List<Grunnbeløp> grunnbeløpSatser);

}
