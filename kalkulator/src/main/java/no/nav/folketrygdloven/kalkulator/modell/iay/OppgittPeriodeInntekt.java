package no.nav.folketrygdloven.kalkulator.modell.iay;

import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.typer.Beløp;

public interface OppgittPeriodeInntekt {

    Intervall getPeriode();

    Beløp getInntekt();

}
