package no.nav.folketrygdloven.kalkulus.beregning;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.folketrygdloven.kalkulator.BeregningsgrunnlagTjeneste;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.output.BeregningResultatAggregat;

@ApplicationScoped
public class BeregningTjeneste {

    BeregningsgrunnlagTjeneste beregningsgrunnlagTjeneste;
    BeregningTilInputTjeneste beregningTilInputTjeneste;

    @Inject
    public BeregningTjeneste(BeregningsgrunnlagTjeneste beregningsgrunnlagTjeneste) {
        this.beregningsgrunnlagTjeneste = beregningsgrunnlagTjeneste;
    }




    public void fastsettBeregningsaktiviteter(BeregningsgrunnlagInput input) {
//        BeregningResultatAggregat resultat = beregningsgrunnlagTjeneste.fastsettBeregningsaktiviteter(beregningTilInputTjeneste.lagInputMedVerdierFraBeregning(input));

    }
}
