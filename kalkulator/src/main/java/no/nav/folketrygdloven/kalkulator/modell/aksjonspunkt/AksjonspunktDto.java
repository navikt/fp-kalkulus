package no.nav.folketrygdloven.kalkulator.modell.aksjonspunkt;

import no.nav.folketrygdloven.kalkulus.kodeverk.AksjonspunktDefinisjon;
import no.nav.folketrygdloven.kalkulus.kodeverk.AksjonspunktStatus;

public final class AksjonspunktDto {

    private final AksjonspunktDefinisjon definisjon;
    private final AksjonspunktStatus status;
    private final String begrunnelse;

    public AksjonspunktDto(AksjonspunktDefinisjon definisjon, AksjonspunktStatus status, String begrunnelse) {
        this.definisjon = definisjon;
        this.status = status;
        this.begrunnelse = begrunnelse;
    }

    public AksjonspunktDefinisjon getDefinisjon() {
        return definisjon;
    }

    public AksjonspunktStatus getStatus() {
        return status;
    }

    public String getBegrunnelse() {
        return begrunnelse;
    }
}
