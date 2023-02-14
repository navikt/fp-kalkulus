package no.nav.folketrygdloven.kalkulator.input;

import java.util.ArrayList;
import java.util.List;

import no.nav.folketrygdloven.beregningsgrunnlag.Grunnbeløp;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand;

/** Inputstruktur for beregningsgrunnlag tjenester. */
public class ForeslåBesteberegningInput extends StegProsesseringInput {

    /**
     * Grunnbeløpsatser
     */
    private List<Grunnbeløp> grunnbeløpsatser = new ArrayList<>();
    private List<GrunnbeløpInput> grunnbeløpInput = new ArrayList<>();

    public ForeslåBesteberegningInput(StegProsesseringInput input) {
        super(input);
        super.stegTilstand = BeregningsgrunnlagTilstand.BESTEBEREGNET;
    }

    @Deprecated(forRemoval = true)
    public List<Grunnbeløp> getGrunnbeløpsatser() {
        return grunnbeløpsatser;
    }

    @Deprecated(forRemoval = true)
    public ForeslåBesteberegningInput medGrunnbeløpsatser(List<Grunnbeløp> grunnbeløpsatser) {
        var newInput = new ForeslåBesteberegningInput(this);
        newInput.grunnbeløpsatser = grunnbeløpsatser;
        return newInput;
    }

    public List<GrunnbeløpInput> getGrunnbeløpInput() {
        return grunnbeløpInput;
    }

    public ForeslåBesteberegningInput medGrunnbeløpInput(List<GrunnbeløpInput> grunnbeløpInput) {
        var newInput = new ForeslåBesteberegningInput(this);
        newInput.grunnbeløpInput = grunnbeløpInput;
        return newInput;
    }

}
