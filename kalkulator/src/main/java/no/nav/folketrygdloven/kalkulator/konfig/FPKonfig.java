package no.nav.folketrygdloven.kalkulator.konfig;

import java.math.BigDecimal;

public class FPKonfig extends Konfigverdier {
    private static final BigDecimal ANTALL_G_MS_HAR_KRAV_PÅ = BigDecimal.valueOf(3);

    public FPKonfig() {
        super(ANTALL_G_MS_HAR_KRAV_PÅ);
    }

}
