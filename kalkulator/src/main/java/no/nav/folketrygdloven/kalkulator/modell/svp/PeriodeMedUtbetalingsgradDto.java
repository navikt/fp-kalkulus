package no.nav.folketrygdloven.kalkulator.modell.svp;

import java.math.BigDecimal;

import no.nav.folketrygdloven.kalkulator.tid.Intervall;

public class PeriodeMedUtbetalingsgradDto {
    private Intervall periode;
    private BigDecimal utbetalingsgrad;

    public PeriodeMedUtbetalingsgradDto(Intervall periode, BigDecimal utbetalingsgrad) {
        this.periode = periode;
        this.utbetalingsgrad = utbetalingsgrad;
    }

    public Intervall getPeriode() {
        return periode;
    }

    public BigDecimal getUtbetalingsgrad() {
        return utbetalingsgrad;
    }
}

