package no.nav.folketrygdloven.kalkulator.modell.iay;

import java.math.BigDecimal;

import no.nav.folketrygdloven.kalkulator.tid.Intervall;

public interface OppgittPeriodeInntekt {

    Intervall getPeriode();

    BigDecimal getInntekt();

}
