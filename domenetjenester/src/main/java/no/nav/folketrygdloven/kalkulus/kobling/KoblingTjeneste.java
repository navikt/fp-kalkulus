package no.nav.folketrygdloven.kalkulus.kobling;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.folketrygdloven.kalkulus.typer.AktørId;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.KoblingReferanse;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.Saksnummer;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.kobling.KoblingEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.kobling.KoblingLås;
import no.nav.folketrygdloven.kalkulus.kodeverk.YtelseTyperKalkulusStøtterKontrakt;
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

    public List<KoblingEntitet> finnEllerOpprett(List<KoblingReferanse> referanser, YtelseTyperKalkulusStøtterKontrakt ytelseTyperKalkulusStøtter, AktørId aktørId, Saksnummer saksnummer) {
        var eksisterendeKoblinger = hentKoblinger(referanser, ytelseTyperKalkulusStøtter);
        var alleKoblinger = new ArrayList<>(eksisterendeKoblinger);
        if (eksisterendeKoblinger.size() != referanser.size()) {
            List<KoblingEntitet> nyeKoblinger = referanser.stream()
                    .filter(ref -> eksisterendeKoblinger.stream().map(KoblingEntitet::getKoblingReferanse)
                            .map(KoblingReferanse::getReferanse)
                            .noneMatch(koblingRef -> koblingRef.equals(ref.getReferanse())))
                    .map(ref -> new KoblingEntitet(ref, ytelseTyperKalkulusStøtter, saksnummer, aktørId))
                    .collect(Collectors.toList());
            nyeKoblinger.forEach(kobling -> repository.lagre(kobling));
            alleKoblinger.addAll(nyeKoblinger);
        }
        return alleKoblinger;
    }

    public Optional<KoblingEntitet> hentFor(KoblingReferanse referanse) {
        return repository.hentForKoblingReferanse(referanse);
    }

    public Optional<Long> hentKoblingHvisFinnes(KoblingReferanse referanse, YtelseTyperKalkulusStøtterKontrakt ytelseType) {
        return repository.hentFor(referanse, ytelseType);
    }

    public List<KoblingEntitet> hentKoblinger(Collection<KoblingReferanse> koblingReferanser, YtelseTyperKalkulusStøtterKontrakt ytelseType) {
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
