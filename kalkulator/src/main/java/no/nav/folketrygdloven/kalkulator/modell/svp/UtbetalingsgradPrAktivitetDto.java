package no.nav.folketrygdloven.kalkulator.modell.svp;

import java.util.List;

public class UtbetalingsgradPrAktivitetDto {

    private UtbetalingsgradArbeidsforholdDto utbetalingsgradArbeidsforhold;
    private List<PeriodeMedUtbetalingsgradDto> periodeMedUtbetalingsgrad;

    public UtbetalingsgradPrAktivitetDto(UtbetalingsgradArbeidsforholdDto utbetalingsgradArbeidsforhold,
                                         List<PeriodeMedUtbetalingsgradDto> periodeMedUtbetalingsgrad) {
        this.utbetalingsgradArbeidsforhold = utbetalingsgradArbeidsforhold;
        this.periodeMedUtbetalingsgrad = periodeMedUtbetalingsgrad;
    }

    public List<PeriodeMedUtbetalingsgradDto> getPeriodeMedUtbetalingsgrad() {
        return periodeMedUtbetalingsgrad;
    }

    public UtbetalingsgradArbeidsforholdDto getUtbetalingsgradArbeidsforhold() {
        return utbetalingsgradArbeidsforhold;
    }

}
