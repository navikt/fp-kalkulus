package no.nav.folketrygdloven.kalkulus.domene.kobling;

import java.util.List;
import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.AktørId;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.KoblingReferanse;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.Saksnummer;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.kobling.KoblingEntitet;
import no.nav.folketrygdloven.kalkulus.kodeverk.FagsakYtelseType;
import no.nav.folketrygdloven.kalkulus.domene.tjeneste.kobling.KoblingRepository;
import no.nav.vedtak.exception.TekniskException;

@ApplicationScoped
public class KoblingTjeneste {

    private KoblingRepository repository;

    KoblingTjeneste() {
    }

    @Inject
    public KoblingTjeneste(KoblingRepository repository) {
        this.repository = repository;
    }

    public KoblingEntitet finnEllerOpprett(KoblingReferanse referanse, FagsakYtelseType ytelseType, AktørId aktørId, Saksnummer saksnummer,
                                           Optional<KoblingReferanse> originalKoblingRef) {
        var eksisterendeKobling = repository.hentForKoblingReferanse(referanse);
        if (eksisterendeKobling.isPresent()) {
            return eksisterendeKobling.get();
        }
        originalKoblingRef.ifPresent(koblingReferanse -> validerOriginalKoblingRef(koblingReferanse, ytelseType, aktørId, saksnummer));
        var nyKobling = new KoblingEntitet(referanse, ytelseType, saksnummer, aktørId, originalKoblingRef);
        repository.lagre(nyKobling);
        return nyKobling;
    }

    public KoblingEntitet hentKobling(Long koblingId) {
        return repository.hentKoblingMedId(koblingId).orElseThrow(() -> new IllegalStateException("Fant ikke kobling med id" + koblingId));
    }

    public KoblingEntitet hentKobling(KoblingReferanse referanse) {
        return repository.hentForKoblingReferanse(referanse).orElseThrow(() -> new IllegalStateException("Fant ikke kobling med referanse" + referanse));
    }

    public void markerKoblingSomAvsluttet(KoblingEntitet koblingEntitet) {
        repository.markerKoblingSomAvsluttet(koblingEntitet);
    }

    public Optional<KoblingEntitet> hentKoblingOptional(KoblingReferanse referanse) {
        return repository.hentForKoblingReferanse(referanse);
    }

    private void validerOriginalKoblingRef(KoblingReferanse koblingReferanse, FagsakYtelseType ytelseType, AktørId aktørId, Saksnummer saksnummer) {
        var koblingEntitet = hentKoblingOptional(koblingReferanse).orElseThrow(
            () -> new IllegalStateException("Forventet å finne kobling med referanse " + koblingReferanse));
        if (!koblingEntitet.getSaksnummer().equals(saksnummer) || !koblingEntitet.getYtelseType().equals(ytelseType) || !koblingEntitet.getAktørId().equals(aktørId)) {
            throw new TekniskException("FT-47712", String.format(
                "Prøver å sette en koblingrelasjon uten at alle felter matcher. Kobling som skulle brukes %s hadde ikke korrekte data for ytelse %s, aktørId %s eller saksnummer %s",
                koblingEntitet, ytelseType, aktørId, saksnummer));
        }
    }

    public List<KoblingEntitet> hentAlleKoblingerForSaksnummer(Saksnummer saksnummer) {
        return repository.hentAlleKoblingerForSaksnummer(saksnummer);
    }
}
