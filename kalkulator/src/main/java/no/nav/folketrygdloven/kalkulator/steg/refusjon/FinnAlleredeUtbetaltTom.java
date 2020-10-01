package no.nav.folketrygdloven.kalkulator.steg.refusjon;

import java.time.LocalDate;
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
        if (idag.getDayOfMonth() > 18) {
            return idag.with(TemporalAdjusters.lastDayOfMonth());
        } else {
            return idag.minusMonths(1).with(TemporalAdjusters.lastDayOfMonth());
        }
    }

}
