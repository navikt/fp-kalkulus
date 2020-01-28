package no.nav.folketrygdloven.kalkulator.ytelse.fp;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;

import no.nav.folketrygdloven.kalkulator.FagsakYtelseTypeRef;
import no.nav.folketrygdloven.kalkulator.FullføreBeregningsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.RegelmodellOversetter;
import no.nav.folketrygdloven.beregningsgrunnlag.fastsette.RegelFullføreBeregningsgrunnlag;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.RegelResultat;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.Beregningsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.fpsak.nare.evaluation.Evaluation;

@FagsakYtelseTypeRef("FP")
@ApplicationScoped
public class FullføreBeregningsgrunnlagFPImpl extends FullføreBeregningsgrunnlag {

    @Override
    protected List<RegelResultat> evaluerRegelmodell(Beregningsgrunnlag beregningsgrunnlagRegel, BeregningsgrunnlagInput bgInput) {
        List<RegelResultat> regelResultater = new ArrayList<>();
        String inputJson = toJson(beregningsgrunnlagRegel);
        for (BeregningsgrunnlagPeriode periode : beregningsgrunnlagRegel.getBeregningsgrunnlagPerioder()) {
            RegelFullføreBeregningsgrunnlag regel = new RegelFullføreBeregningsgrunnlag(periode);
            Evaluation evaluation = regel.evaluer(periode);
            regelResultater.add(RegelmodellOversetter.getRegelResultat(evaluation, inputJson));
        }
        return regelResultater;
    }
}
