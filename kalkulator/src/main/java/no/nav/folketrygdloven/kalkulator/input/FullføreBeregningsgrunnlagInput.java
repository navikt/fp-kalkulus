package no.nav.folketrygdloven.kalkulator.input;

import java.util.Optional;

import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulus.typer.Beløp;

/** Inputstruktur for beregningsgrunnlag tjenester. */
public class FullføreBeregningsgrunnlagInput extends StegProsesseringInput {


    /**
     * Uregulert grunnbeløp om det finnes beregningsgrunnlag som ble fastsatt etter 1. mai.
     *
     * En G-regulering ikke skal påvirke utfallet av beregningsgrunnlagvilkåret (se TFP-3599 og https://confluence.adeo.no/display/TVF/G-regulering)
     */
    private Beløp uregulertGrunnbeløp;


    public FullføreBeregningsgrunnlagInput(StegProsesseringInput input) {
        super(input);
        super.stegTilstand = BeregningsgrunnlagTilstand.FASTSATT;
    }


    public Optional<Beløp> getUregulertGrunnbeløp() {
        return Optional.ofNullable(uregulertGrunnbeløp);
    }


    public FullføreBeregningsgrunnlagInput medUregulertGrunnbeløp(Beløp uregulertGrunnbeløp) {
        var newInput = new FullføreBeregningsgrunnlagInput(this);
        newInput.uregulertGrunnbeløp = uregulertGrunnbeløp;
        return newInput;
    }



}
