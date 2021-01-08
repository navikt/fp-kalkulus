package no.nav.folketrygdloven.kalkulator.steg.refusjon;

import java.time.LocalDate;
import java.time.Month;
import java.time.temporal.TemporalAdjusters;

/**
 * Utbetalingsdato i NAV varierer, vi bruker den 18. i hver måned da dette som oftest vil være rett,
 * men ikke i alle måneder.
 */
public final class FinnAlleredeUtbetaltTom {
    private FinnAlleredeUtbetaltTom() {
        // skjul public constructor
    }
    static LocalDate finn() {
        LocalDate idag = LocalDate.now();
        int utbetalingsdagIMåned = finnUtbetalingsdagForMåned(idag.getMonth());
        if (idag.getDayOfMonth() > utbetalingsdagIMåned) {
            return idag.with(TemporalAdjusters.lastDayOfMonth());
        } else {
            return idag.minusMonths(1).with(TemporalAdjusters.lastDayOfMonth());
        }
    }

    private static int finnUtbetalingsdagForMåned(Month month) {
        // Desember utbetaling er alltid tidligere enn andre måneder, spesialbehandles.
        if (month == Month.DECEMBER) {
            return 7;
        }
        return 18;
    }

}
