package no.nav.folketrygdloven.kalkulator.input;

import java.math.BigDecimal;
import java.util.Optional;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.BeregningsgrunnlagTilstand;

/** Inputstruktur for beregningsgrunnlag tjenester. */
public class VurderRefusjonBeregningsgrunnlagInput extends StegProsesseringInput {


    /**
     * Uregulert grunnbeløp om det finnes beregningsgrunnlag som ble fastsatt etter 1. mai.
     *
     * En G-regulering ikke skal påvirke utfallet av beregningsgrunnlagvilkåret (se TFP-3599 og https://confluence.adeo.no/display/TVF/G-regulering)
     */
    private Beløp uregulertGrunnbeløp;

    /** Grunnlag for Beregningsgrunnlg opprettet eller modifisert av modulen i original behandling. Settes på av modulen. */
    private BeregningsgrunnlagGrunnlagDto beregningsgrunnlagGrunnlagFraForrigeBehandling;


    public VurderRefusjonBeregningsgrunnlagInput(StegProsesseringInput input) {
        super(input);
        super.stegTilstand = BeregningsgrunnlagTilstand.VURDERT_REFUSJON;
        super.stegUtTilstand = BeregningsgrunnlagTilstand.VURDERT_REFUSJON_UT;
        if (input instanceof VurderRefusjonBeregningsgrunnlagInput) {
            VurderRefusjonBeregningsgrunnlagInput vurderRefInput = (VurderRefusjonBeregningsgrunnlagInput) input;
            beregningsgrunnlagGrunnlagFraForrigeBehandling = vurderRefInput.getBeregningsgrunnlagGrunnlagFraForrigeBehandling().orElse(null);
        }
    }


    public Optional<Beløp> getUregulertGrunnbeløp() {
        return Optional.ofNullable(uregulertGrunnbeløp);
    }


    public VurderRefusjonBeregningsgrunnlagInput medUregulertGrunnbeløp(BigDecimal uregulertGrunnbeløp) {
        var newInput = new VurderRefusjonBeregningsgrunnlagInput(this);
        newInput.uregulertGrunnbeløp = new Beløp(uregulertGrunnbeløp);
        return newInput;
    }

    // Brukes av fp-sak
    public VurderRefusjonBeregningsgrunnlagInput medBeregningsgrunnlagGrunnlagFraForrigeBehandling(BeregningsgrunnlagGrunnlagDto grunnlag) {
        var newInput = new VurderRefusjonBeregningsgrunnlagInput(this);
        newInput.beregningsgrunnlagGrunnlagFraForrigeBehandling = grunnlag;
        return newInput;
    }


    public Optional<BeregningsgrunnlagGrunnlagDto> getBeregningsgrunnlagGrunnlagFraForrigeBehandling() {
        return Optional.ofNullable(beregningsgrunnlagGrunnlagFraForrigeBehandling);
    }


}
