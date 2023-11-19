package no.nav.folketrygdloven.kalkulator.steg.fastsettskjæringstidspunkt;

import java.util.List;

import no.nav.folketrygdloven.kalkulator.input.FastsettBeregningsaktiviteterInput;
import no.nav.folketrygdloven.kalkulator.input.GrunnbeløpMapper;
import no.nav.folketrygdloven.kalkulator.output.BeregningVilkårResultat;
import no.nav.folketrygdloven.kalkulator.output.BeregningsgrunnlagRegelResultat;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.Vilkårsavslagsårsak;

public class ForeslåSkjæringstidspunktTjeneste {

    public BeregningsgrunnlagRegelResultat foreslåSkjæringstidspunkt(FastsettBeregningsaktiviteterInput input) {
        var beregningAktivitetAggregat = new FastsettBeregningAktiviteter().fastsettAktiviteter(input);
        if (beregningAktivitetAggregat.getBeregningAktiviteter().isEmpty() && !input.getOpptjeningAktiviteter().erMidlertidigInaktiv()) {
            // Avslår vilkår
            var beregningsgrunnlagRegelResultat = new BeregningsgrunnlagRegelResultat(beregningAktivitetAggregat);
            beregningsgrunnlagRegelResultat.setVilkårsresultat(List.of(new BeregningVilkårResultat(false, Vilkårsavslagsårsak.FOR_LAVT_BG, Intervall.fraOgMed(input.getSkjæringstidspunktOpptjening()))));
            return beregningsgrunnlagRegelResultat;
        }
        var beregningsgrunnlagRegelResultat = new OpprettBeregningsgrunnlagTjeneste().fastsettSkjæringstidspunktOgStatuser(input, beregningAktivitetAggregat, GrunnbeløpMapper.mapGrunnbeløpInput(input.getGrunnbeløpsatser(), input.getGrunnbeløpInput()));
        beregningsgrunnlagRegelResultat.setRegisterAktiviteter(beregningAktivitetAggregat);
        return beregningsgrunnlagRegelResultat;
    }

}
