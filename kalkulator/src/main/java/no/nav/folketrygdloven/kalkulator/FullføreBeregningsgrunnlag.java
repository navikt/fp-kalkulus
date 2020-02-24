package no.nav.folketrygdloven.kalkulator;

import java.util.List;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.RegelResultat;
import no.nav.folketrygdloven.kalkulator.adapter.regelmodelltilvl.MapBeregningsgrunnlagFraRegelTilVL;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.MapBeregningsgrunnlagFraVLTilRegel;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;

public abstract class FullføreBeregningsgrunnlag {

    protected FullføreBeregningsgrunnlag() {
        //for CDI proxy
    }

    public BeregningsgrunnlagDto fullføreBeregningsgrunnlag(BeregningsgrunnlagInput input) {
        var grunnlag = input.getBeregningsgrunnlagGrunnlag();

        // Oversetter foreslått Beregningsgrunnlag -> regelmodell
        var beregningsgrunnlagRegel = MapBeregningsgrunnlagFraVLTilRegel.map(input, grunnlag);

        // Evaluerer hver BeregningsgrunnlagPeriode fra foreslått Beregningsgrunnlag
        List<RegelResultat> regelResultater = evaluerRegelmodell(beregningsgrunnlagRegel, input);

        // Oversett endelig resultat av regelmodell til fastsatt Beregningsgrunnlag  (+ spore input -> evaluation)
        BeregningsgrunnlagDto beregningsgrunnlag = grunnlag.getBeregningsgrunnlag().orElse(null);
        BeregningsgrunnlagDto fastsattBeregningsgrunnlag = MapBeregningsgrunnlagFraRegelTilVL.mapFastsettBeregningsgrunnlag(beregningsgrunnlagRegel, regelResultater, beregningsgrunnlag);
        BeregningsgrunnlagVerifiserer.verifiserFastsattBeregningsgrunnlag(fastsattBeregningsgrunnlag, input.getAktivitetGradering());
        return fastsattBeregningsgrunnlag;
    }

    protected abstract List<RegelResultat> evaluerRegelmodell(no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.Beregningsgrunnlag beregningsgrunnlagRegel, BeregningsgrunnlagInput input);

    protected static String toJson(no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.Beregningsgrunnlag beregningsgrunnlagRegel) {
        return JsonMapper.toJson(beregningsgrunnlagRegel, BeregningsgrunnlagFeil.FEILFACTORY::kanIkkeSerialisereRegelinput);
    }
}
