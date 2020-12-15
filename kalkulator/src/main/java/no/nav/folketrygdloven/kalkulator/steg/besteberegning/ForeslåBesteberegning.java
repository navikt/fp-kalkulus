package no.nav.folketrygdloven.kalkulator.steg.besteberegning;

import static no.nav.folketrygdloven.kalkulator.steg.besteberegning.MapBesteberegningFraRegelTilVL.mapTilBeregningsgrunnlag;
import static no.nav.folketrygdloven.kalkulator.adapter.regelmodelltilvl.MapRegelSporingFraRegelTilVL.mapRegelSporingGrunnlag;

import no.nav.folketrygdloven.beregningsgrunnlag.RegelmodellOversetter;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.RegelResultat;
import no.nav.folketrygdloven.besteberegning.RegelForeslåBesteberegning;
import no.nav.folketrygdloven.besteberegning.modell.BesteberegningRegelmodell;
import no.nav.folketrygdloven.besteberegning.modell.output.BesteberegningOutput;
import no.nav.folketrygdloven.kalkulator.BeregningsgrunnlagFeil;
import no.nav.folketrygdloven.kalkulator.JsonMapper;
import no.nav.folketrygdloven.kalkulator.input.ForeslåBesteberegningInput;
import no.nav.folketrygdloven.kalkulator.output.RegelSporingAggregat;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagRegelType;
import no.nav.fpsak.nare.evaluation.Evaluation;

public class ForeslåBesteberegning {


    /** Foreslår besteberegning
     *
     * @param input Input til foreslå besteberegning
     * @return Beregningsresultat med nytt besteberegnet grunnlag
     */
    public BesteberegningRegelResultat foreslåBesteberegning(ForeslåBesteberegningInput input) {
        BesteberegningRegelmodell regelmodell = MapTilBesteberegningRegelmodell.map(input);
        Evaluation evaluering = new RegelForeslåBesteberegning().evaluer(regelmodell);
        RegelResultat regelResultat = RegelmodellOversetter.getRegelResultat(evaluering, toJson(regelmodell));
        BesteberegningOutput output = regelmodell.getOutput();
        return new BesteberegningRegelResultat(mapTilBeregningsgrunnlag(input.getBeregningsgrunnlagGrunnlag(), output),
                new RegelSporingAggregat(mapRegelSporingGrunnlag(regelResultat, BeregningsgrunnlagRegelType.BESTEBEREGNING)),
                MapBesteberegningFraRegelTilVL.mapSeksBesteMåneder(output));
    }

    private static String toJson(BesteberegningRegelmodell regelmodell) {
        return JsonMapper.toJson(regelmodell, BeregningsgrunnlagFeil.FEILFACTORY::kanIkkeSerialisereRegelinput);
    }

}
