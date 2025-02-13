package no.nav.folketrygdloven.kalkulus.domene.entiteter.avklaringsbehov;

import java.time.LocalDateTime;

import no.nav.folketrygdloven.kalkulus.domene.entiteter.kobling.KoblingEntitet;
import no.nav.folketrygdloven.kalkulus.kodeverk.AvklaringsbehovDefinisjon;
import no.nav.folketrygdloven.kalkulus.kodeverk.AvklaringsbehovStatus;
import no.nav.vedtak.sikkerhet.kontekst.KontekstHolder;

/**
 * Tjeneste som skal håndtere all opprettelse og endring av AvklaringsbehovEntitet
 */
public class AvklaringsbehovKontrollTjeneste {

    public AvklaringsbehovEntitet opprettForKobling(KoblingEntitet koblingId, AvklaringsbehovDefinisjon definisjon) {
        no.nav.folketrygdloven.kalkulus.domene.entiteter.avklaringsbehov.AvklaringsbehovEntitet.Builder apBuilder = new no.nav.folketrygdloven.kalkulus.domene.entiteter.avklaringsbehov.AvklaringsbehovEntitet.Builder(definisjon);
        apBuilder.medStatus(AvklaringsbehovStatus.OPPRETTET);
        return apBuilder.buildFor(koblingId);
    }

    public AvklaringsbehovEntitet opprettForKoblingLikEksisterende(KoblingEntitet kobling, AvklaringsbehovEntitet kopierFra) {
        no.nav.folketrygdloven.kalkulus.domene.entiteter.avklaringsbehov.AvklaringsbehovEntitet.Builder apBuilder = new no.nav.folketrygdloven.kalkulus.domene.entiteter.avklaringsbehov.AvklaringsbehovEntitet.Builder(kopierFra.getDefinisjon());
        apBuilder.medStatus(kopierFra.getStatus());
        apBuilder.medBegrunnelse(kopierFra.getBegrunnelse());
        apBuilder.medVurdertAv(kopierFra.getVurdertAv());
        apBuilder.medVurdertTidspunkt(kopierFra.getVurdertTidspunkt());
        return apBuilder.buildFor(kobling);
    }

    public AvklaringsbehovEntitet kopierDataFraAvklaringsbehov(AvklaringsbehovEntitet kopierTil, AvklaringsbehovEntitet kopierFra) {
        kopierTil.setStatus(kopierFra.getStatus());
        kopierTil.setBegrunnelse(kopierFra.getBegrunnelse());
        kopierTil.setVurdertAv(kopierFra.getVurdertAv());
        kopierTil.setVurdertTidspunkt(kopierFra.getVurdertTidspunkt());
        return kopierTil;
    }


    public void gjennopprett(AvklaringsbehovEntitet avklaringsbehovEntitet) {
        avklaringsbehovEntitet.setStatus(AvklaringsbehovStatus.OPPRETTET);
        avklaringsbehovEntitet.setErTrukket(false);
    }

    public void løs(AvklaringsbehovEntitet avklaringsbehovEntitet, String begrunnelse) {
        avklaringsbehovEntitet.setStatus(AvklaringsbehovStatus.UTFØRT);
        avklaringsbehovEntitet.setBegrunnelse(begrunnelse);
        setVurdert(avklaringsbehovEntitet);
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

    private void setVurdert(AvklaringsbehovEntitet avklaringsbehovEntitet) {
        avklaringsbehovEntitet.setVurdertAv(KontekstHolder.getKontekst().getKompaktUid());
        avklaringsbehovEntitet.setVurdertTidspunkt(LocalDateTime.now());
    }
}
