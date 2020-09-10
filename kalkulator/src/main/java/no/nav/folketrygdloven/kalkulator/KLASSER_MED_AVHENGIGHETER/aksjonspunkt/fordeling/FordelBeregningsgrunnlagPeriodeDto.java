package no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.fordeling;

import java.time.LocalDate;
import java.util.List;

import no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.dto.FastsettBeregningsgrunnlagAndelDto;

public class FordelBeregningsgrunnlagPeriodeDto {

    private List<FordelBeregningsgrunnlagAndelDto> andeler;
    private LocalDate fom;
    private LocalDate tom;


    public FordelBeregningsgrunnlagPeriodeDto(List<FordelBeregningsgrunnlagAndelDto> andeler, LocalDate fom, LocalDate tom) { // NOSONAR
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

    public List<FordelBeregningsgrunnlagAndelDto> getAndeler() {
        return andeler;
    }

}
