package no.nav.folketrygdloven.kalkulator.steg.besteberegning;

import static no.nav.folketrygdloven.kalkulator.adapter.regelmodelltilvl.MapRegelSporingFraRegelTilVL.mapRegelSporingGrunnlag;
import static no.nav.folketrygdloven.kalkulator.steg.besteberegning.MapBesteberegningFraRegelTilVL.mapTilBeregningsgrunnlag;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.folketrygdloven.beregningsgrunnlag.RegelmodellOversetter;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.RegelResultat;
import no.nav.folketrygdloven.besteberegning.RegelForeslåBesteberegning;
import no.nav.folketrygdloven.besteberegning.modell.BesteberegningRegelmodell;
import no.nav.folketrygdloven.besteberegning.modell.output.BesteberegningOutput;
import no.nav.folketrygdloven.kalkulator.BeregningsgrunnlagFeil;
import no.nav.folketrygdloven.kalkulator.JsonMapper;
import no.nav.folketrygdloven.kalkulator.input.ForeslåBesteberegningInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.output.RegelSporingAggregat;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagRegelType;
import no.nav.fpsak.nare.evaluation.Evaluation;

public class ForeslåBesteberegning {
    private static final Logger LOG = LoggerFactory.getLogger(ForeslåBesteberegning.class);


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
        var besteberegnetGrunnlag = mapTilBeregningsgrunnlag(input.getBeregningsgrunnlagGrunnlag(), output);
        if (regelmodell.getOutput().getSkalBeregnesEtterSeksBesteMåneder()) {
            loggAvvik(regelmodell, besteberegnetGrunnlag);
        }
        return new BesteberegningRegelResultat(besteberegnetGrunnlag,
                new RegelSporingAggregat(mapRegelSporingGrunnlag(regelResultat, BeregningsgrunnlagRegelType.BESTEBEREGNING)),
                MapBesteberegningFraRegelTilVL.mapSeksBesteMåneder(output));
    }

    private void loggAvvik(BesteberegningRegelmodell regelmodell, BeregningsgrunnlagDto besteberegnetGrunnlag) {
        var beregningEtter1ledd = regelmodell.getInput().getBeregnetGrunnlag();
        var beregningEtter3ledd = besteberegnetGrunnlag.getBeregningsgrunnlagPerioder().get(0).getBruttoPrÅr();
        var avvikIKroner = beregningEtter3ledd.subtract(beregningEtter1ledd).abs();
        var avvikIProsent = avvikIKroner.divide(beregningEtter1ledd, 1, RoundingMode.HALF_EVEN).multiply(BigDecimal.valueOf(100));
        LOG.info("FT-984394: Avvik i kroner {}. Avvik i prosent {}", avvikIKroner, avvikIProsent);
    }

    private static String toJson(BesteberegningRegelmodell regelmodell) {
        return JsonMapper.toJson(regelmodell, BeregningsgrunnlagFeil::kanIkkeSerialisereRegelinput);
    }

}
