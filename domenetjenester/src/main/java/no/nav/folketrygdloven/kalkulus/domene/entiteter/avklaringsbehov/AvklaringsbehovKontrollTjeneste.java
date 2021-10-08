package no.nav.folketrygdloven.kalkulus.domene.entiteter.avklaringsbehov;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.folketrygdloven.kalkulus.domene.entiteter.kobling.KoblingEntitet;
import no.nav.folketrygdloven.kalkulus.kodeverk.AvklaringsbehovDefinisjon;
import no.nav.folketrygdloven.kalkulus.kodeverk.AvklaringsbehovStatus;

/**
 * Tjeneste som skal håndtere all opprettelse og endring av AvklaringsbehovEntitet
 */
@ApplicationScoped
public class AvklaringsbehovKontrollTjeneste {

    @Inject
    public AvklaringsbehovKontrollTjeneste() {
    }

    public AvklaringsbehovEntitet opprettForKobling(KoblingEntitet koblingId, AvklaringsbehovDefinisjon definisjon) {
        no.nav.folketrygdloven.kalkulus.domene.entiteter.avklaringsbehov.AvklaringsbehovEntitet.Builder apBuilder = new no.nav.folketrygdloven.kalkulus.domene.entiteter.avklaringsbehov.AvklaringsbehovEntitet.Builder(definisjon);
        apBuilder.medStatus(AvklaringsbehovStatus.OPPRETTET);
        return apBuilder.buildFor(koblingId);
    }

    public void gjennopprett(AvklaringsbehovEntitet avklaringsbehovEntitet) {
        avklaringsbehovEntitet.setStatus(AvklaringsbehovStatus.OPPRETTET);
    }

    public void løs(AvklaringsbehovEntitet avklaringsbehovEntitet, String begrunnelse) {
        avklaringsbehovEntitet.setStatus(AvklaringsbehovStatus.UTFØRT);
        avklaringsbehovEntitet.setBegrunnelse(begrunnelse);
    }

    public void løsForMigrering(AvklaringsbehovEntitet avklaringsbehovEntitet, String begrunnelse) {
        avklaringsbehovEntitet.setStatus(AvklaringsbehovStatus.UTFØRT);
        if (begrunnelse != null) {
            avklaringsbehovEntitet.setBegrunnelse(begrunnelse);
        }
    }


    public no.nav.folketrygdloven.kalkulus.domene.entiteter.avklaringsbehov.AvklaringsbehovEntitet avbryt(no.nav.folketrygdloven.kalkulus.domene.entiteter.avklaringsbehov.AvklaringsbehovEntitet avklaringsbehovEntitet) {
        avklaringsbehovEntitet.setStatus(AvklaringsbehovStatus.AVBRUTT);
        return avklaringsbehovEntitet;
    }
}
