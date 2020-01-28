package no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.dto;

import java.time.LocalDate;
import java.util.List;

public class FastsettBeregningsgrunnlagPeriodeDto {

    private List<FastsettBeregningsgrunnlagAndelDto> andeler;
    private LocalDate fom;
    private LocalDate tom;


    public FastsettBeregningsgrunnlagPeriodeDto(List<FastsettBeregningsgrunnlagAndelDto> andeler, LocalDate fom, LocalDate tom) { // NOSONAR
        this.andeler = andeler;
        this.fom = fom;
        this.tom = tom;
    }

    public LocalDate getFom() {
        return fom;
    }

    public LocalDate getTom() {
        return tom;
    }

    public List<FastsettBeregningsgrunnlagAndelDto> getAndeler() {
        return andeler;
    }

}
