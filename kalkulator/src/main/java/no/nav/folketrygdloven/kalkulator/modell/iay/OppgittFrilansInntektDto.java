package no.nav.folketrygdloven.kalkulator.modell.iay;

import java.math.BigDecimal;

import no.nav.folketrygdloven.kalkulator.tid.Intervall;

public class OppgittFrilansInntektDto implements OppgittPeriodeInntekt {

    private Intervall periode;
    private BigDecimal inntekt;

    public OppgittFrilansInntektDto(Intervall periode, BigDecimal inntekt) {
        this.periode = periode;
        this.inntekt = inntekt;
    }

    @Override
    public Intervall getPeriode() {
        return periode;
    }

    @Override
    public BigDecimal getInntekt() {
        return inntekt;
    }

}
