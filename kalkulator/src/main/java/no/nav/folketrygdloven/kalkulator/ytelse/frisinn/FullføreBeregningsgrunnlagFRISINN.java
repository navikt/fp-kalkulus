package no.nav.folketrygdloven.kalkulator.ytelse.frisinn;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.folketrygdloven.beregningsgrunnlag.RegelmodellOversetter;
import no.nav.folketrygdloven.beregningsgrunnlag.fastsette.RegelFullføreBeregningsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.RegelResultat;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.Beregningsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.ytelse.frisinn.RegelFinnGrenseverdiFRISINN;
import no.nav.folketrygdloven.beregningsgrunnlag.ytelse.frisinn.RegelFullføreBeregningsgrunnlagFRISINN;
import no.nav.folketrygdloven.beregningsgrunnlag.ytelse.svp.RegelFinnGrenseverdi;
import no.nav.folketrygdloven.kalkulator.FagsakYtelseTypeRef;
import no.nav.folketrygdloven.kalkulator.FullføreBeregningsgrunnlag;
import no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.UtbetalingsgradGrunnlag;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.MapBeregningsgrunnlagFraVLTilRegel;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.ytelse.utbgradytelse.FullføreBeregningsgrunnlagUtbgrad;
import no.nav.fpsak.nare.evaluation.Evaluation;

@FagsakYtelseTypeRef("FRISINN")
@ApplicationScoped
public class FullføreBeregningsgrunnlagFRISINN extends FullføreBeregningsgrunnlagUtbgrad {

    public FullføreBeregningsgrunnlagFRISINN() {
        // CDI
    }

    @Inject
    public FullføreBeregningsgrunnlagFRISINN(MapBeregningsgrunnlagFraVLTilRegel mapBeregningsgrunnlagFraVLTilRegel) {
        super(mapBeregningsgrunnlagFraVLTilRegel);
    }


    @Override
    protected List<RegelResultat> kjørRegelFullførberegningsgrunnlag(Beregningsgrunnlag beregningsgrunnlagRegel, String input) {
        List<RegelResultat> regelResultater = new ArrayList<>();
        for (BeregningsgrunnlagPeriode periode : beregningsgrunnlagRegel.getBeregningsgrunnlagPerioder()) {
            RegelFullføreBeregningsgrunnlagFRISINN regel = new RegelFullføreBeregningsgrunnlagFRISINN(periode);
            Evaluation evaluation = regel.evaluer(periode);
            regelResultater.add(RegelmodellOversetter.getRegelResultat(evaluation, input));
        }
        return regelResultater;
    }

    @Override
    protected List<String> kjørRegelFinnGrenseverdi(Beregningsgrunnlag beregningsgrunnlagRegel) {
        return beregningsgrunnlagRegel.getBeregningsgrunnlagPerioder().stream()
                .map(periode -> {
                    RegelFinnGrenseverdiFRISINN regel = new RegelFinnGrenseverdiFRISINN(periode);
                    Evaluation evaluering = regel.evaluer(periode);
                    return RegelmodellOversetter.getSporing(evaluering);
                }).collect(Collectors.toList());
    }





}


