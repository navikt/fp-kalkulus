package no.nav.folketrygdloven.kalkulus.domene.entiteter.aksjonspunkt;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.folketrygdloven.kalkulus.domene.entiteter.kobling.KoblingEntitet;
import no.nav.folketrygdloven.kalkulus.kodeverk.AksjonspunktDefinisjon;
import no.nav.folketrygdloven.kalkulus.kodeverk.AksjonspunktStatus;

/**
 * Tjeneste som skal håndtere all opprettelse og endring av AksjonspunktEntitet
 */
@ApplicationScoped
public class AksjonspunktKontrollTjeneste {

    @Inject
    public AksjonspunktKontrollTjeneste() {
    }

    public AksjonspunktEntitet opprettForKobling(KoblingEntitet koblingId, AksjonspunktDefinisjon definisjon) {
        AksjonspunktEntitet.Builder apBuilder = new AksjonspunktEntitet.Builder(definisjon);
        apBuilder.medStatus(AksjonspunktStatus.OPPRETTET);
        return apBuilder.buildFor(koblingId);
    }

    public void gjennopprett(AksjonspunktEntitet aksjonspunktEntitet) {
        aksjonspunktEntitet.setStatus(AksjonspunktStatus.OPPRETTET);
    }

    public void løs(AksjonspunktEntitet aksjonspunktEntitet, String begrunnelse) {
        aksjonspunktEntitet.setStatus(AksjonspunktStatus.UTFØRT);
        aksjonspunktEntitet.setBegrunnelse(begrunnelse);
    }

    public AksjonspunktEntitet avbryt(AksjonspunktEntitet aksjonspunktEntitet) {
        aksjonspunktEntitet.setStatus(AksjonspunktStatus.AVBRUTT);
        return aksjonspunktEntitet;
    }
}
