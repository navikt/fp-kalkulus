package no.nav.folketrygdloven.kalkulator.steg.fastsettskjæringstidspunkt;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.folketrygdloven.kalkulator.input.FastsettBeregningsaktiviteterInput;
import no.nav.folketrygdloven.kalkulator.output.BeregningVilkårResultat;
import no.nav.folketrygdloven.kalkulator.output.BeregningsgrunnlagRegelResultat;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.Vilkårsavslagsårsak;

@ApplicationScoped
public class ForeslåSkjæringstidspunktTjeneste {

    protected FastsettBeregningAktiviteter fastsettBeregningAktiviteter;
    protected OpprettBeregningsgrunnlagTjeneste opprettBeregningsgrunnlagTjeneste;

    public ForeslåSkjæringstidspunktTjeneste() {
        // CDI
    }

    @Inject
    public ForeslåSkjæringstidspunktTjeneste(FastsettBeregningAktiviteter fastsettBeregningAktiviteter, OpprettBeregningsgrunnlagTjeneste opprettBeregningsgrunnlagTjeneste) {
        this.fastsettBeregningAktiviteter = fastsettBeregningAktiviteter;
        this.opprettBeregningsgrunnlagTjeneste = opprettBeregningsgrunnlagTjeneste;
    }

    public BeregningsgrunnlagRegelResultat foreslåSkjæringstidspunkt(FastsettBeregningsaktiviteterInput input) {
        var beregningAktivitetAggregat = fastsettBeregningAktiviteter.fastsettAktiviteter(input);
        if (beregningAktivitetAggregat.getBeregningAktiviteter().isEmpty()) {
            // Avslår vilkår
            var beregningsgrunnlagRegelResultat = new BeregningsgrunnlagRegelResultat(beregningAktivitetAggregat);
            beregningsgrunnlagRegelResultat.setVilkårsresultat(List.of(new BeregningVilkårResultat(false, Vilkårsavslagsårsak.FOR_LAVT_BG, Intervall.fraOgMed(input.getSkjæringstidspunktOpptjening()))));
            return beregningsgrunnlagRegelResultat;
        }
        var beregningsgrunnlagRegelResultat = opprettBeregningsgrunnlagTjeneste.fastsettSkjæringstidspunktOgStatuser(input, beregningAktivitetAggregat, input.getIayGrunnlag());
        beregningsgrunnlagRegelResultat.setRegisterAktiviteter(beregningAktivitetAggregat);
        return beregningsgrunnlagRegelResultat;
    }

}
