package no.nav.folketrygdloven.kalkulus.kobling;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.AktørId;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.KoblingReferanse;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.Saksnummer;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.kobling.KoblingEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.kobling.KoblingLås;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.YtelseTyperKalkulusStøtter;
import no.nav.folketrygdloven.kalkulus.tjeneste.kobling.KoblingRepository;
import no.nav.folketrygdloven.kalkulus.tjeneste.kobling.LåsRepository;

@ApplicationScoped
public class KoblingTjeneste {

    private KoblingRepository repository;
    private LåsRepository låsRepository;

    public KoblingTjeneste() {
    }

    @Inject
    public KoblingTjeneste(KoblingRepository repository, LåsRepository låsRepository) {
        this.repository = repository;
        this.låsRepository = låsRepository;
    }

    public KoblingEntitet finnEllerOpprett(KoblingReferanse referanse, YtelseTyperKalkulusStøtter ytelseTyperKalkulusStøtter, AktørId aktørId, Saksnummer saksnummer) {
        KoblingEntitet kobling = hentFor(referanse).orElse(new KoblingEntitet(referanse, ytelseTyperKalkulusStøtter, saksnummer, aktørId));
        repository.lagre(kobling);
        return kobling;
    }

    public Optional<KoblingEntitet> hentFor(KoblingReferanse referanse) {
        return repository.hentForKoblingReferanse(referanse);
    }

    public KoblingEntitet hentFor(KoblingReferanse referanse, YtelseTyperKalkulusStøtter ytelseTyperKalkulusStøtter) {
        return repository.hentSisteKoblingReferanseFor(referanse, ytelseTyperKalkulusStøtter);
    }


    public Long hentKoblingId(KoblingReferanse referanse, YtelseTyperKalkulusStøtter ytelseType) {
        return repository.hentFor(referanse, ytelseType);
    }

    public Optional<KoblingEntitet> hentSisteFor(AktørId aktørId, Saksnummer saksnummer, YtelseTyperKalkulusStøtter ytelseTyperKalkulusStøtter) {
        return repository.hentSisteKoblingReferanseFor(aktørId, saksnummer, ytelseTyperKalkulusStøtter);
    }

    public void lagre(KoblingEntitet kobling) {
        repository.lagre(kobling);
    }

    public KoblingEntitet hent(Long koblingId) {
        return repository.hentForKoblingId(koblingId);
    }

    public Long hentKoblingId(KoblingReferanse koblingReferanse) {
        return repository.hentKoblingIdForKoblingReferanse(koblingReferanse);
    }


    public KoblingLås taSkrivesLås(KoblingReferanse referanse) {
        return taSkrivesLås(repository.hentKoblingIdForKoblingReferanse(referanse));
    }

    public KoblingLås taSkrivesLås(Long koblingId) {
        return låsRepository.taLås(koblingId);
    }

    public void oppdaterLåsVersjon(KoblingLås lås) {
        låsRepository.oppdaterLåsVersjon(lås);
    }

}
