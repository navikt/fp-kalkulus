package no.nav.folketrygdloven.kalkulator.steg.besteberegning;

import static no.nav.folketrygdloven.kalkulator.adapter.regelmodelltilvl.MapBesteberegningFraRegelTilVL.mapTilBeregningsgrunnlag;

import java.util.List;

import no.nav.folketrygdloven.besteberegning.RegelForeslåBesteberegning;
import no.nav.folketrygdloven.besteberegning.modell.BesteberegningRegelmodell;
import no.nav.folketrygdloven.besteberegning.modell.output.BesteberegningOutput;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.MapTilBesteberegningRegelmodell;
import no.nav.folketrygdloven.kalkulator.input.ForeslåBesteberegningInput;
import no.nav.folketrygdloven.kalkulator.output.BeregningsgrunnlagRegelResultat;

public class ForeslåBesteberegning {


    /** Foreslår besteberegning
     *
     * @param input Input til foreslå besteberegning
     * @return Beregningsresultat med nytt besteberegnet grunnlag
     */
    public BeregningsgrunnlagRegelResultat foreslåBesteberegning(ForeslåBesteberegningInput input) {
        BesteberegningRegelmodell regelmodell = MapTilBesteberegningRegelmodell.map(input);
        new RegelForeslåBesteberegning().evaluer(regelmodell);
        BesteberegningOutput output = regelmodell.getOutput();
        return new BeregningsgrunnlagRegelResultat(mapTilBeregningsgrunnlag(input.getBeregningsgrunnlagGrunnlag(), output),
                List.of());
    }

}
