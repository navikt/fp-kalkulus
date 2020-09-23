package no.nav.folketrygdloven.kalkulus.kobling;

import java.util.Collection;
import java.util.List;
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
        return repository.hentKoblingReferanseFor(referanse, ytelseTyperKalkulusStøtter);
    }

    public Long hentKoblingId(KoblingReferanse referanse, YtelseTyperKalkulusStøtter ytelseType) {
        Optional<Long> koblingId = repository.hentFor(referanse, ytelseType);
        if (koblingId.isPresent()) {
            return koblingId.get();
        }
        throw new IllegalStateException("Kalkulus kjenner ikke til kombinasjonen av eksternRef:" + referanse.getReferanse() + " og ytelseType:" + ytelseType.getNavn());
    }

    public Optional<Long> hentKoblingHvisFinnes(KoblingReferanse referanse, YtelseTyperKalkulusStøtter ytelseType) {
        return repository.hentFor(referanse, ytelseType);
    }

    public List<KoblingEntitet> hentKoblinger(Collection<KoblingReferanse> koblingReferanser, YtelseTyperKalkulusStøtter ytelseType) {
        return repository.hentKoblingerFor(koblingReferanser, ytelseType);
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
