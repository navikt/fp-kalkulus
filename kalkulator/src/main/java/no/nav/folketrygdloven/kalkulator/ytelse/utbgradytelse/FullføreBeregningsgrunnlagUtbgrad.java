package no.nav.folketrygdloven.kalkulator.ytelse.utbgradytelse;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.folketrygdloven.beregningsgrunnlag.RegelmodellOversetter;
import no.nav.folketrygdloven.beregningsgrunnlag.fastsette.RegelFullføreBeregningsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.RegelResultat;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.Beregningsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.ytelse.svp.RegelFinnGrenseverdi;
import no.nav.folketrygdloven.kalkulator.FagsakYtelseTypeRef;
import no.nav.folketrygdloven.kalkulator.FullføreBeregningsgrunnlag;
import no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.UtbetalingsgradGrunnlag;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.MapBeregningsgrunnlagFraVLTilRegel;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.svp.UtbetalingsgradPrAktivitetDto;
import no.nav.fpsak.nare.evaluation.Evaluation;

@ApplicationScoped
public class FullføreBeregningsgrunnlagUtbgrad extends FullføreBeregningsgrunnlag {

    public FullføreBeregningsgrunnlagUtbgrad() {
        // CDI
    }

    @Inject
    public FullføreBeregningsgrunnlagUtbgrad(MapBeregningsgrunnlagFraVLTilRegel mapBeregningsgrunnlagFraVLTilRegel) {
        super(mapBeregningsgrunnlagFraVLTilRegel);
    }

    @Override
    protected List<RegelResultat> evaluerRegelmodell(Beregningsgrunnlag beregningsgrunnlagRegel, BeregningsgrunnlagInput bgInput) {
        var ytelsespesifiktGrunnlag = (UtbetalingsgradGrunnlag)bgInput.getYtelsespesifiktGrunnlag();

        tilpassRegelModellForUtbetalingsgrad(ytelsespesifiktGrunnlag, beregningsgrunnlagRegel);


        String input = toJson(beregningsgrunnlagRegel);
        // Regel for å finne grenseverdi for andre gjennomkjøring
        List<String> sporingerFinnGrenseverdi = kjørRegelFinnGrenseverdi(beregningsgrunnlagRegel);

        String inputNrTo = toJson(beregningsgrunnlagRegel);

        //Andre gjennomkjøring av regel
        List<RegelResultat> regelResultater = kjørRegelFullførberegningsgrunnlag(beregningsgrunnlagRegel, inputNrTo);

        leggTilSporingerForFinnGrenseverdi(input, sporingerFinnGrenseverdi, regelResultater);

        return regelResultater;
    }

    private void leggTilSporingerForFinnGrenseverdi(String input, List<String> sporingerFinnGrenseverdi, List<RegelResultat> regelResultater) {
        if (regelResultater.size() == sporingerFinnGrenseverdi.size()) {
            for (int i = 0; i < regelResultater.size(); i++) {
                RegelResultat res = regelResultater.get(i);
                res.medRegelsporingFinnGrenseverdi(input, sporingerFinnGrenseverdi.get(i));
            }
        } else {
            throw new IllegalStateException("Utviklerfeil: Antall kjøringer for finn grenseverdi var ulik fastsetting.");
        }
    }

    protected List<RegelResultat> kjørRegelFullførberegningsgrunnlag(Beregningsgrunnlag beregningsgrunnlagRegel, String input) {
        List<RegelResultat> regelResultater = new ArrayList<>();
        for (BeregningsgrunnlagPeriode periode : beregningsgrunnlagRegel.getBeregningsgrunnlagPerioder()) {
            RegelFullføreBeregningsgrunnlag regel = new RegelFullføreBeregningsgrunnlag(periode);
            Evaluation evaluation = regel.evaluer(periode);
            regelResultater.add(RegelmodellOversetter.getRegelResultat(evaluation, input));
        }
        return regelResultater;
    }

    protected List<String> kjørRegelFinnGrenseverdi(Beregningsgrunnlag beregningsgrunnlagRegel) {
        return beregningsgrunnlagRegel.getBeregningsgrunnlagPerioder().stream()
            .map(periode -> {
                RegelFinnGrenseverdi regel = new RegelFinnGrenseverdi(periode);
                Evaluation evaluering = regel.evaluer(periode);
                return RegelmodellOversetter.getSporing(evaluering);
            }).collect(Collectors.toList());
    }


    private void tilpassRegelModellForUtbetalingsgrad(UtbetalingsgradGrunnlag utbetalingsgradGrunnlag, Beregningsgrunnlag beregningsgrunnlagRegel) {
        List<UtbetalingsgradPrAktivitetDto> tilretteleggingMedUtbelingsgrad = utbetalingsgradGrunnlag.getUtbetalingsgradPrAktivitet();
        RegelmodellModifiserer.tilpassRegelModellForUtbetalingsgrad(beregningsgrunnlagRegel, tilretteleggingMedUtbelingsgrad);
    }

}
