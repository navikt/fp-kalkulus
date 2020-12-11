package no.nav.folketrygdloven.kalkulator.input;

import java.math.BigDecimal;
import java.util.Optional;

import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand;

/** Inputstruktur for beregningsgrunnlag tjenester. */
public class FordelBeregningsgrunnlagInput extends StegProsesseringInput {


    /**
     * Uregulert grunnbeløp om det finnes beregningsgrunnlag som ble fastsatt etter 1. mai.
     *
     * En G-regulering ikke skal påvirke utfallet av beregningsgrunnlagvilkåret (se TFP-3599 og https://confluence.adeo.no/display/TVF/G-regulering)
     */
    private Beløp uregulertGrunnbeløp;


    public FordelBeregningsgrunnlagInput(StegProsesseringInput input) {
        super(input);
        super.stegTilstand = BeregningsgrunnlagTilstand.OPPDATERT_MED_REFUSJON_OG_GRADERING;
        super.stegUtTilstand = BeregningsgrunnlagTilstand.FASTSATT_INN;
    }


    public Optional<Beløp> getUregulertGrunnbeløp() {
        return Optional.ofNullable(uregulertGrunnbeløp);
    }


    public FordelBeregningsgrunnlagInput medUregulertGrunnbeløp(BigDecimal uregulertGrunnbeløp) {
        var newInput = new FordelBeregningsgrunnlagInput(this);
        newInput.uregulertGrunnbeløp = new Beløp(uregulertGrunnbeløp);
        return newInput;
    }



}
