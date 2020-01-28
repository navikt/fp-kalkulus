package no.nav.folketrygdloven.kalkulator.modell.svp;

import java.util.List;

public class TilretteleggingMedUtbelingsgradDto {

    private TilretteleggingArbeidsforholdDto tilretteleggingArbeidsforhold;
    private List<PeriodeMedUtbetalingsgradDto> periodeMedUtbetalingsgrad;

    public TilretteleggingMedUtbelingsgradDto(TilretteleggingArbeidsforholdDto tilretteleggingArbeidsforhold,
                                              List<PeriodeMedUtbetalingsgradDto> periodeMedUtbetalingsgrad) {
        this.tilretteleggingArbeidsforhold = tilretteleggingArbeidsforhold;
        this.periodeMedUtbetalingsgrad = periodeMedUtbetalingsgrad;
    }

    public List<PeriodeMedUtbetalingsgradDto> getPeriodeMedUtbetalingsgrad() {
        return periodeMedUtbetalingsgrad;
    }

    public TilretteleggingArbeidsforholdDto getTilretteleggingArbeidsforhold() {
        return tilretteleggingArbeidsforhold;
    }

}
