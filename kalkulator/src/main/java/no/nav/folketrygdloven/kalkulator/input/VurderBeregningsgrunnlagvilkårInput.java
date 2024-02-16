package no.nav.folketrygdloven.kalkulator.input;

import java.math.BigDecimal;
import java.util.Optional;

import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulus.typer.Beløp;

/** Inputstruktur for beregningsgrunnlag tjenester. */
public class VurderBeregningsgrunnlagvilkårInput extends StegProsesseringInput {


    /**
     * Uregulert grunnbeløp om det finnes beregningsgrunnlag som ble fastsatt etter 1. mai.
     *
     * En G-regulering ikke skal påvirke utfallet av beregningsgrunnlagvilkåret (se TFP-3599 og https://confluence.adeo.no/display/TVF/G-regulering)
     */
    private Beløp uregulertGrunnbeløp;



    public VurderBeregningsgrunnlagvilkårInput(StegProsesseringInput input) {
        super(input);
        super.stegTilstand = BeregningsgrunnlagTilstand.VURDERT_VILKÅR;
    }


    public Optional<Beløp> getUregulertGrunnbeløp() {
        return Optional.ofNullable(uregulertGrunnbeløp);
    }


    public VurderBeregningsgrunnlagvilkårInput medUregulertGrunnbeløp(BigDecimal uregulertGrunnbeløp) {
        var newInput = new VurderBeregningsgrunnlagvilkårInput(this);
        newInput.uregulertGrunnbeløp = Beløp.fra(uregulertGrunnbeløp);
        return newInput;
    }

}
