package no.nav.folketrygdloven.kalkulator.steg.besteberegning;

import static no.nav.folketrygdloven.kalkulator.adapter.regelmodelltilvl.MapRegelSporingFraRegelTilVL.mapRegelSporingGrunnlag;
import static no.nav.folketrygdloven.kalkulator.steg.besteberegning.MapBesteberegningFraRegelTilVL.mapTilBeregningsgrunnlag;

import java.math.BigDecimal;

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
        var seksBesteMåneder = MapBesteberegningFraRegelTilVL.mapSeksBesteMåneder(output);

        // Bryr oss kun om avvik om beregning etter tredje ledd (seks beste måneder) blir brukt
        var avvikFraFørsteLedd = regelmodell.getOutput().getSkalBeregnesEtterSeksBesteMåneder()
                ? finnAvvik(regelmodell, besteberegnetGrunnlag)
                : null;

        var besteberegningVurderingsgrunnlag = new BesteberegningVurderingGrunnlag(seksBesteMåneder, avvikFraFørsteLedd);
        return new BesteberegningRegelResultat(besteberegnetGrunnlag,
                new RegelSporingAggregat(mapRegelSporingGrunnlag(regelResultat, BeregningsgrunnlagRegelType.BESTEBEREGNING)),
                besteberegningVurderingsgrunnlag);
    }

    private BigDecimal finnAvvik(BesteberegningRegelmodell regelmodell, BeregningsgrunnlagDto besteberegnetGrunnlag) {
        var beregningEtter1ledd = regelmodell.getInput().getBeregnetGrunnlag();
        var beregningEtter3ledd = besteberegnetGrunnlag.getBeregningsgrunnlagPerioder().get(0).getBruttoPrÅr();
        var avvik = beregningEtter3ledd.subtract(beregningEtter1ledd);
        if (avvik.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalStateException("Avvik kan ikke være mindre enn 0 kr når sak skal besteberegnes");
        }
        return avvik;
    }

    private static String toJson(BesteberegningRegelmodell regelmodell) {
        return JsonMapper.toJson(regelmodell, BeregningsgrunnlagFeil::kanIkkeSerialisereRegelinput);
    }

}
