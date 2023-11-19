package no.nav.folketrygdloven.kalkulator.ytelse.frisinn;

import java.util.Collections;

import no.nav.folketrygdloven.kalkulator.input.FortsettForeslåBeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.output.BeregningsgrunnlagRegelResultat;
import no.nav.folketrygdloven.kalkulator.steg.fortsettForeslå.FortsettForeslåBeregningsgrunnlag;

/**
 * FRISINN trenger ikke kjøre dette steget. Hvis det mot formodning skjer, skal ingenting gjøres her.
 * Alt håndteres i foreslå-steget, da frisinn ikke følger samme regler som resten av ytelsene
 * og ikke trenger beregne enkelte statuser før andre.
 */
public class FortsettForeslåBeregningsgrunnlagFRISINN extends FortsettForeslåBeregningsgrunnlag {

    public FortsettForeslåBeregningsgrunnlagFRISINN() {
        super();
    }

    @Override
    public BeregningsgrunnlagRegelResultat fortsettForeslåBeregningsgrunnlag(FortsettForeslåBeregningsgrunnlagInput input) {
        BeregningsgrunnlagDto beregningsgrunnlag = input.getBeregningsgrunnlagGrunnlag().getBeregningsgrunnlag()
                .orElseThrow(() -> new IllegalStateException("Skal ha beregningsgrunnlag her"));
        return new BeregningsgrunnlagRegelResultat(beregningsgrunnlag, Collections.emptyList());
    }
}
