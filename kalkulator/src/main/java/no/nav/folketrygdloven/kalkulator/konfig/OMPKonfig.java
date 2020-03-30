package no.nav.folketrygdloven.kalkulator.konfig;

import java.math.BigDecimal;

public class OMPKonfig extends Konfigverdier {
    private static final BigDecimal ANTALL_G_MS_HAR_KRAV_PÅ = BigDecimal.valueOf(2);

    public OMPKonfig() {
        super(ANTALL_G_MS_HAR_KRAV_PÅ);
    }

}
