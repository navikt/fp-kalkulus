package no.nav.folketrygdloven.kalkulator.ytelse.svp;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;

import no.nav.folketrygdloven.beregningsgrunnlag.RegelmodellOversetter;
import no.nav.folketrygdloven.beregningsgrunnlag.fastsette.RegelFullføreBeregningsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.RegelResultat;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.Beregningsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.ytelse.svp.RegelFinnGrenseverdi;
import no.nav.folketrygdloven.kalkulator.FagsakYtelseTypeRef;
import no.nav.folketrygdloven.kalkulator.FullføreBeregningsgrunnlag;
import no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.SvangerskapspengerGrunnlag;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.svp.TilretteleggingMedUtbelingsgradDto;
import no.nav.fpsak.nare.evaluation.Evaluation;

@FagsakYtelseTypeRef("SVP")
@ApplicationScoped
public class FullføreBeregningsgrunnlagSVPImpl extends FullføreBeregningsgrunnlag {

    @Override
    protected List<RegelResultat> evaluerRegelmodell(Beregningsgrunnlag beregningsgrunnlagRegel, BeregningsgrunnlagInput bgInput) {
        var svangerskapspengerGrunnlag = (SvangerskapspengerGrunnlag)bgInput.getYtelsespesifiktGrunnlag();

        tilpassRegelModellForSVP(svangerskapspengerGrunnlag, beregningsgrunnlagRegel);


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

    private List<RegelResultat> kjørRegelFullførberegningsgrunnlag(Beregningsgrunnlag beregningsgrunnlagRegel, String input) {
        List<RegelResultat> regelResultater = new ArrayList<>();
        for (BeregningsgrunnlagPeriode periode : beregningsgrunnlagRegel.getBeregningsgrunnlagPerioder()) {
            RegelFullføreBeregningsgrunnlag regel = new RegelFullføreBeregningsgrunnlag(periode);
            Evaluation evaluation = regel.evaluer(periode);
            regelResultater.add(RegelmodellOversetter.getRegelResultat(evaluation, input));
        }
        return regelResultater;
    }

    private List<String> kjørRegelFinnGrenseverdi(Beregningsgrunnlag beregningsgrunnlagRegel) {
        return beregningsgrunnlagRegel.getBeregningsgrunnlagPerioder().stream()
            .map(periode -> {
                RegelFinnGrenseverdi regel = new RegelFinnGrenseverdi(periode);
                Evaluation evaluering = regel.evaluer(periode);
                return RegelmodellOversetter.getSporing(evaluering);
            }).collect(Collectors.toList());
    }


    private void tilpassRegelModellForSVP(SvangerskapspengerGrunnlag svpGrunnlag, Beregningsgrunnlag beregningsgrunnlagRegel) {
        List<TilretteleggingMedUtbelingsgradDto> tilretteleggingMedUtbelingsgrad = svpGrunnlag.getTilretteleggingMedUtbelingsgrad();
        RegelmodellModifiserer.tilpassRegelModellForSVP(beregningsgrunnlagRegel, tilretteleggingMedUtbelingsgrad);
    }

}
