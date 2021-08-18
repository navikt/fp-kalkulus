package no.nav.folketrygdloven.kalkulator.modell.avklaringsbehov;

import no.nav.folketrygdloven.kalkulus.kodeverk.AvklaringsbehovDefinisjon;
import no.nav.folketrygdloven.kalkulus.kodeverk.AvklaringsbehovStatus;

public final class AvklaringsbehovDto {

    private final AvklaringsbehovDefinisjon definisjon;
    private final AvklaringsbehovStatus status;
    private final String begrunnelse;

    public AvklaringsbehovDto(AvklaringsbehovDefinisjon definisjon, AvklaringsbehovStatus status, String begrunnelse) {
        this.definisjon = definisjon;
        this.status = status;
        this.begrunnelse = begrunnelse;
    }

    public AvklaringsbehovDefinisjon getDefinisjon() {
        return definisjon;
    }

    public AvklaringsbehovStatus getStatus() {
        return status;
    }

    public String getBegrunnelse() {
        return begrunnelse;
    }
}
