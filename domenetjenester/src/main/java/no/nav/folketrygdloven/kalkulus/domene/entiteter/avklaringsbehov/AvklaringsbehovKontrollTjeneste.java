package no.nav.folketrygdloven.kalkulus.domene.entiteter.avklaringsbehov;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
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

    public AvklaringsbehovEntitet opprettForKoblingLikEksisterende(KoblingEntitet kobling, AvklaringsbehovEntitet kopierFra) {
        no.nav.folketrygdloven.kalkulus.domene.entiteter.avklaringsbehov.AvklaringsbehovEntitet.Builder apBuilder = new no.nav.folketrygdloven.kalkulus.domene.entiteter.avklaringsbehov.AvklaringsbehovEntitet.Builder(kopierFra.getDefinisjon());
        apBuilder.medStatus(kopierFra.getStatus());
        apBuilder.medBegrunnelse(kopierFra.getBegrunnelse());
        return apBuilder.buildFor(kobling);
    }

    public AvklaringsbehovEntitet kopierDataFraAvklaringsbehov(AvklaringsbehovEntitet kopierTil, AvklaringsbehovEntitet kopierFra) {
        kopierTil.setStatus(kopierFra.getStatus());
        kopierTil.setBegrunnelse(kopierFra.getBegrunnelse());
        return kopierTil;
    }


    public void gjennopprett(AvklaringsbehovEntitet avklaringsbehovEntitet) {
        avklaringsbehovEntitet.setStatus(AvklaringsbehovStatus.OPPRETTET);
        avklaringsbehovEntitet.setErTrukket(false);
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


    public AvklaringsbehovEntitet avbryt(no.nav.folketrygdloven.kalkulus.domene.entiteter.avklaringsbehov.AvklaringsbehovEntitet avklaringsbehovEntitet) {
        avklaringsbehovEntitet.setStatus(AvklaringsbehovStatus.AVBRUTT);
        return avklaringsbehovEntitet;
    }

    public AvklaringsbehovEntitet trekkOverstyring(no.nav.folketrygdloven.kalkulus.domene.entiteter.avklaringsbehov.AvklaringsbehovEntitet avklaringsbehovEntitet) {
        avklaringsbehovEntitet.setStatus(AvklaringsbehovStatus.UTFØRT);
        avklaringsbehovEntitet.setErTrukket(true);
        avklaringsbehovEntitet.setBegrunnelse(null);
        return avklaringsbehovEntitet;
    }

}
