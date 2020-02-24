package no.nav.folketrygdloven.kalkulator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import no.nav.folketrygdloven.beregningsgrunnlag.RegelmodellOversetter;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.RegelMerknad;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.RegelResultat;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.vurder.RegelVurderBeregningsgrunnlag;
import no.nav.folketrygdloven.kalkulator.adapter.regelmodelltilvl.MapBeregningsgrunnlagFraRegelTilVL;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.MapBeregningsgrunnlagFraVLTilRegel;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.output.BeregningAksjonspunktResultat;
import no.nav.folketrygdloven.kalkulator.output.BeregningsgrunnlagRegelResultat;
import no.nav.fpsak.nare.evaluation.Evaluation;

public class VurderBeregningsgrunnlagTjeneste {

    private VurderBeregningsgrunnlagTjeneste() {
        // Engler daler ned i skjul
    }

    public static BeregningsgrunnlagRegelResultat vurderBeregningsgrunnlag(BeregningsgrunnlagInput input, BeregningsgrunnlagGrunnlagDto oppdatertGrunnlag) {
        // Oversetter foreslått Beregningsgrunnlag -> regelmodell
        var beregningsgrunnlagRegel = MapBeregningsgrunnlagFraVLTilRegel.map(input, oppdatertGrunnlag);

        String jsonInput = toJson(beregningsgrunnlagRegel);
        // Evaluerer hver BeregningsgrunnlagPeriode fra foreslått Beregningsgrunnlag
        List<RegelResultat> regelResultater = new ArrayList<>();
        for (BeregningsgrunnlagPeriode periode : beregningsgrunnlagRegel.getBeregningsgrunnlagPerioder()) {
            RegelVurderBeregningsgrunnlag regel = new RegelVurderBeregningsgrunnlag(periode);
            Evaluation evaluation = regel.evaluer(periode);
            regelResultater.add(RegelmodellOversetter.getRegelResultat(evaluation, jsonInput));
        }
        BeregningsgrunnlagDto beregningsgrunnlag = MapBeregningsgrunnlagFraRegelTilVL.mapVurdertBeregningsgrunnlag(regelResultater, oppdatertGrunnlag.getBeregningsgrunnlag().orElse(null));
        List<BeregningAksjonspunktResultat> aksjonspunkter = Collections.emptyList();
        boolean vilkårOppfylt = erVilkårOppfylt(regelResultater);
        BeregningsgrunnlagRegelResultat beregningsgrunnlagRegelResultat = new BeregningsgrunnlagRegelResultat(beregningsgrunnlag, aksjonspunkter);
        beregningsgrunnlagRegelResultat.setVilkårOppfylt(vilkårOppfylt);
        return beregningsgrunnlagRegelResultat;
    }

    private static boolean erVilkårOppfylt(List<RegelResultat> regelResultater) {
        return regelResultater.stream()
            .flatMap(regelResultat -> regelResultat.getMerknader().stream())
            .map(RegelMerknad::getMerknadKode)
            //Avslagsårsak.FOR_LAVT_BEREGNINGSGRUNNLAG
            .noneMatch(avslagskode -> avslagskode.equals("1041"));
    }

    private static String toJson(no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.Beregningsgrunnlag beregningsgrunnlagRegel) {
        return JsonMapper.toJson(beregningsgrunnlagRegel, BeregningsgrunnlagFeil.FEILFACTORY::kanIkkeSerialisereRegelinput);
    }
}
