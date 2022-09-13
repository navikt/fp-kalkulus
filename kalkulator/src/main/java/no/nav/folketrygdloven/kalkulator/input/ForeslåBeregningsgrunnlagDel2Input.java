package no.nav.folketrygdloven.kalkulator.input;

import java.util.ArrayList;
import java.util.List;

import no.nav.folketrygdloven.beregningsgrunnlag.Grunnbeløp;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand;

/** Inputstruktur for beregningsgrunnlag tjenester. */
public class ForeslåBeregningsgrunnlagDel2Input extends StegProsesseringInput {

    /**
     * Grunnbeløpsatser
     */
    private List<Grunnbeløp> grunnbeløpsatser = new ArrayList<>();

    public ForeslåBeregningsgrunnlagDel2Input(StegProsesseringInput input) {
        super(input);
        super.stegTilstand = BeregningsgrunnlagTilstand.FORESLÅTT_2;
        super.stegUtTilstand = BeregningsgrunnlagTilstand.FORESLÅTT_2_UT;
    }

    public List<Grunnbeløp> getGrunnbeløpsatser() {
        return grunnbeløpsatser;
    }

    public ForeslåBeregningsgrunnlagDel2Input medGrunnbeløpsatser(List<Grunnbeløp> grunnbeløpsatser) {
        var newInput = new ForeslåBeregningsgrunnlagDel2Input(this);
        newInput.grunnbeløpsatser = grunnbeløpsatser;
        return newInput;
    }

}
