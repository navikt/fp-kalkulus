package no.nav.folketrygdloven.kalkulator.input;

import java.util.ArrayList;
import java.util.List;

import no.nav.folketrygdloven.beregningsgrunnlag.Grunnbeløp;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand;

/** Inputstruktur for beregningsgrunnlag tjenester. */
public class FortsettForeslåBeregningsgrunnlagInput extends StegProsesseringInput {

    /**
     * Grunnbeløpsatser
     */
    private List<Grunnbeløp> grunnbeløpsatser = new ArrayList<>();

    public FortsettForeslåBeregningsgrunnlagInput(StegProsesseringInput input) {
        super(input);
        super.stegTilstand = BeregningsgrunnlagTilstand.FORESLÅTT_2;
        super.stegUtTilstand = BeregningsgrunnlagTilstand.FORESLÅTT_2_UT;
    }

    public List<Grunnbeløp> getGrunnbeløpsatser() {
        return grunnbeløpsatser;
    }

    public FortsettForeslåBeregningsgrunnlagInput medGrunnbeløpsatser(List<Grunnbeløp> grunnbeløpsatser) {
        var newInput = new FortsettForeslåBeregningsgrunnlagInput(this);
        newInput.grunnbeløpsatser = grunnbeløpsatser;
        return newInput;
    }

}
