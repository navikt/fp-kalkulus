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

    public Optional<Long> hentKoblingHvisFinnes(KoblingReferanse referanse, YtelseTyperKalkulusStøtter ytelseType) {
        return repository.hentFor(referanse, ytelseType);
    }

    public List<KoblingEntitet> hentKoblinger(Collection<KoblingReferanse> koblingReferanser, YtelseTyperKalkulusStøtter ytelseType) {
        return repository.hentKoblingerFor(koblingReferanser, ytelseType);
    }

    public List<KoblingEntitet> hentKoblinger(Collection<KoblingReferanse> koblingReferanser) {
        return repository.hentKoblingIdForKoblingReferanser(koblingReferanser);
    }


    // Burde ta i bruk skrivelås?
    public KoblingLås taSkrivesLås(KoblingReferanse referanse) {
        return taSkrivesLås(repository.hentKoblingIdForKoblingReferanse(referanse));
    }

    public KoblingLås taSkrivesLås(Long koblingId) {
        return låsRepository.taLås(koblingId);
    }


}
