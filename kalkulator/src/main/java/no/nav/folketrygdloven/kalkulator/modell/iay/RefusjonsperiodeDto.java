package no.nav.folketrygdloven.kalkulator.modell.iay;

import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.typer.Beløp;

public record RefusjonsperiodeDto (Intervall periode, Beløp beløp) { }
