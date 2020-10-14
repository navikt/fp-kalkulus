package no.nav.folketrygdloven.kalkulator.input;

import java.util.ArrayList;
import java.util.List;

import no.nav.folketrygdloven.beregningsgrunnlag.Grunnbeløp;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.BeregningsgrunnlagTilstand;

/** Inputstruktur for beregningsgrunnlag tjenester. */
public class ForeslåBeregningsgrunnlagInput extends StegProsesseringInput {

    /**
     * Grunnbeløpsatser
     */
    private List<Grunnbeløp> grunnbeløpsatser = new ArrayList<>();

    public ForeslåBeregningsgrunnlagInput(StegProsesseringInput input) {
        super(input);
        super.stegTilstand = BeregningsgrunnlagTilstand.FORESLÅTT;
        super.stegUtTilstand = BeregningsgrunnlagTilstand.FASTSATT_INN;
    }

    public List<Grunnbeløp> getGrunnbeløpsatser() {
        return grunnbeløpsatser;
    }

    public ForeslåBeregningsgrunnlagInput medGrunnbeløpsatser(List<Grunnbeløp> grunnbeløpsatser) {
        var newInput = new ForeslåBeregningsgrunnlagInput(this);
        newInput.grunnbeløpsatser = grunnbeløpsatser;
        return newInput;
    }

}
