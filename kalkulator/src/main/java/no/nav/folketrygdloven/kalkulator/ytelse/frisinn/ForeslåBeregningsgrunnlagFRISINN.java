package no.nav.folketrygdloven.kalkulator.ytelse.frisinn;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.folketrygdloven.beregningsgrunnlag.RegelmodellOversetter;
import no.nav.folketrygdloven.beregningsgrunnlag.foreslå.frisinn.RegelForeslåBeregningsgrunnlagFRISINN;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.RegelResultat;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.Beregningsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.kalkulator.AksjonspunktUtlederForeslåBeregning;
import no.nav.folketrygdloven.kalkulator.FagsakYtelseTypeRef;
import no.nav.folketrygdloven.kalkulator.ForeslåBeregningsgrunnlag;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.MapBeregningsgrunnlagFraVLTilRegel;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.output.BeregningAksjonspunktResultat;
import no.nav.fpsak.nare.evaluation.Evaluation;

@ApplicationScoped
@FagsakYtelseTypeRef("FRISINN")
public class ForeslåBeregningsgrunnlagFRISINN extends ForeslåBeregningsgrunnlag {

    public ForeslåBeregningsgrunnlagFRISINN() {
        // CDI
    }

    @Inject
    public ForeslåBeregningsgrunnlagFRISINN(MapBeregningsgrunnlagFraVLTilRegel mapBeregningsgrunnlagFraVLTilRegel) {
        super(mapBeregningsgrunnlagFraVLTilRegel);
    }

    @Override
    protected List<BeregningAksjonspunktResultat> utledAksjonspunkter(BeregningsgrunnlagInput input, List<RegelResultat> regelResultater) {
        return Collections.emptyList();
    }

    @Override
    protected List<RegelResultat> kjørRegelForeslåBeregningsgrunnlag(Beregningsgrunnlag regelmodellBeregningsgrunnlag, String jsonInput) {
        // Evaluerer hver BeregningsgrunnlagPeriode fra initielt Beregningsgrunnlag
        List<RegelResultat> regelResultater = new ArrayList<>();
        for (BeregningsgrunnlagPeriode periode : regelmodellBeregningsgrunnlag.getBeregningsgrunnlagPerioder()) {
            Evaluation evaluation = new RegelForeslåBeregningsgrunnlagFRISINN(periode).evaluer(periode);
            regelResultater.add(RegelmodellOversetter.getRegelResultat(evaluation, jsonInput));
        }
        return regelResultater;
    }

}
