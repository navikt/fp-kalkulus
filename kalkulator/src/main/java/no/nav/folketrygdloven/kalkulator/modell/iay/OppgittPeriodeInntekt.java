package no.nav.folketrygdloven.kalkulator.modell.iay;

import java.math.BigDecimal;

import no.nav.folketrygdloven.kalkulator.tid.Intervall;

public interface OppgittPeriodeInntekt {

    public Intervall getPeriode();

    public BigDecimal getInntekt();

}
