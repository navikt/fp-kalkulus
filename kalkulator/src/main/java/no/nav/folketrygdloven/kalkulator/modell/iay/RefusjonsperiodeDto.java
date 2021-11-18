package no.nav.folketrygdloven.kalkulator.modell.iay;

import java.math.BigDecimal;

import no.nav.folketrygdloven.kalkulator.tid.Intervall;

public record RefusjonsperiodeDto (Intervall periode, BigDecimal beløp) { }
