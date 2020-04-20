package no.nav.folketrygdloven.kalkulator.ytelse.frisinn;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.folketrygdloven.beregningsgrunnlag.RegelmodellOversetter;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.RegelResultat;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.Beregningsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.vurder.frisinn.RegelVurderBeregningsgrunnlagFRISINN;
import no.nav.folketrygdloven.kalkulator.FagsakYtelseTypeRef;
import no.nav.folketrygdloven.kalkulator.VurderBeregningsgrunnlagTjeneste;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.MapBeregningsgrunnlagFraVLTilRegel;
import no.nav.fpsak.nare.evaluation.Evaluation;

@ApplicationScoped
@FagsakYtelseTypeRef("FRISINN")
public class VurderBeregningsgrunnlagTjenesteFRISINN extends VurderBeregningsgrunnlagTjeneste {

    public VurderBeregningsgrunnlagTjenesteFRISINN() {
        // CDI
    }

    @Inject
    public VurderBeregningsgrunnlagTjenesteFRISINN(MapBeregningsgrunnlagFraVLTilRegel mapBeregningsgrunnlagFraVLTilRegel) {
        super(mapBeregningsgrunnlagFraVLTilRegel);
    }

    @Override
    protected List<RegelResultat> kjørRegel(Beregningsgrunnlag beregningsgrunnlagRegel) {
        String jsonInput = toJson(beregningsgrunnlagRegel);
        // Evaluerer hver BeregningsgrunnlagPeriode fra foreslått Beregningsgrunnlag
        List<RegelResultat> regelResultater = new ArrayList<>();
        for (BeregningsgrunnlagPeriode periode : beregningsgrunnlagRegel.getBeregningsgrunnlagPerioder()) {
            RegelVurderBeregningsgrunnlagFRISINN regel = new RegelVurderBeregningsgrunnlagFRISINN(periode);
            Evaluation evaluation = regel.evaluer(periode);
            regelResultater.add(RegelmodellOversetter.getRegelResultat(evaluation, jsonInput));
        }
        return regelResultater;
    }

}
