package no.nav.folketrygdloven.kalkulus.app.sikkerhet;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import no.nav.k9.felles.konfigurasjon.konfig.KonfigVerdi;

@ApplicationScoped
public class DomeneAbacAttributter {

    private String attributtnøkkelAksjonspunktType;
    private String attributtnøkkelBehandlingstatus;
    private String attributtnøkkelSakstatus;

    public DomeneAbacAttributter() {
    }

    @Inject
    public DomeneAbacAttributter(@KonfigVerdi(value = "abac.attributt.aksjonspunkttype") String attributtnøkkelAksjonspunktType,
                                 @KonfigVerdi(value = "abac.attributt.behandlingstatus") String attributtnøkkelBehandlingstatus,
                                 @KonfigVerdi(value = "abac.attributt.sakstatus") String attributtSakstatus
                             ) {
        this.attributtnøkkelAksjonspunktType = attributtnøkkelAksjonspunktType;
        this.attributtnøkkelBehandlingstatus = attributtnøkkelBehandlingstatus;
        this.attributtnøkkelSakstatus = attributtSakstatus;
    }

    public String getAttributtnøkkelAksjonspunktType() {
        return attributtnøkkelAksjonspunktType;
    }

    public String getAttributtnøkkelBehandlingstatus() {
        return attributtnøkkelBehandlingstatus;
    }

    public String getAttributtnøkkelSakstatus() {
        return attributtnøkkelSakstatus;
    }
}
