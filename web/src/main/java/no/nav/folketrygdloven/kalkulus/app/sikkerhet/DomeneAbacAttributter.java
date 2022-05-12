package no.nav.folketrygdloven.kalkulus.app.sikkerhet;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import no.nav.k9.felles.konfigurasjon.konfig.KonfigVerdi;

@ApplicationScoped
public class DomeneAbacAttributter {

    private String attributtnøkkelAksjonspunktType;
    private String attributtnøkkelBehandlingsuuid;
    
    public DomeneAbacAttributter() {
    }

    @Inject
    public DomeneAbacAttributter(@KonfigVerdi(value = "abac.attributt.aksjonspunkttype") String attributtnøkkelAksjonspunktType,
                                 @KonfigVerdi(value = "abac.attributt.behandlingsuuid") String attributtnøkkelBehandlingsuuid
    ) {
        this.attributtnøkkelAksjonspunktType = attributtnøkkelAksjonspunktType;
        this.attributtnøkkelBehandlingsuuid = attributtnøkkelBehandlingsuuid;
    }

    public String getAttributtnøkkelAksjonspunktType() {
        return attributtnøkkelAksjonspunktType;
    }

    public String getAttributtnøkkelBehandlingsuuid() {
        return attributtnøkkelBehandlingsuuid;
    }
}
