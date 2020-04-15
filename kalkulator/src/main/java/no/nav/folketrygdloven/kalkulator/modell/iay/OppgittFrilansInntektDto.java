package no.nav.folketrygdloven.kalkulator.modell.iay;

import java.math.BigDecimal;

import no.nav.folketrygdloven.kalkulator.tid.Intervall;

public class OppgittFrilansInntektDto {

    private Intervall periode;
    private BigDecimal beløp;

    public OppgittFrilansInntektDto(Intervall periode, BigDecimal beløp) {
        this.periode = periode;
        this.beløp = beløp;
    }

    public Intervall getPeriode() {
        return periode;
    }

    public BigDecimal getBeløp() {
        return beløp;
    }

}
