package no.nav.folketrygdloven.kalkulator.konfig;

import java.math.BigDecimal;

public class FRISINNKonfig extends Konfigverdier {
    private static final BigDecimal ANTALL_G_MS_HAR_KRAV_PÅ = BigDecimal.ZERO;

    public FRISINNKonfig() {
        super(ANTALL_G_MS_HAR_KRAV_PÅ);
        antallGForOppfyltVilkår = BigDecimal.valueOf(0.75);
    }

}
