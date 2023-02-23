package no.nav.folketrygdloven.kalkulator.konfig;

import java.math.BigDecimal;

public class FRISINNKonfig extends Konfigverdier {
    private static final BigDecimal ANTALL_G_MS_HAR_KRAV_PÅ = BigDecimal.ZERO;

    private static final BigDecimal ANTALL_G_FOR_OPPFYLT_VILKÅR = BigDecimal.valueOf(0.75);

    public FRISINNKonfig() {
        super(ANTALL_G_MS_HAR_KRAV_PÅ);
    }

    public static BigDecimal getAntallGForOppfyltVilkår() {
        return ANTALL_G_FOR_OPPFYLT_VILKÅR;
    }
}
