package no.nav.folketrygdloven.kalkulus.kobling;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import no.nav.folketrygdloven.kalkulus.domene.entiteter.kobling.KoblingRelasjon;
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

    public List<KoblingRelasjon> finnOgOpprettKoblingRelasjoner(Map<UUID, List<UUID>> koblingrelasjoner) {
        var referanser = koblingrelasjoner.keySet().stream().map(KoblingReferanse::new)
                .collect(Collectors.toCollection(ArrayList::new));
        referanser.addAll(koblingrelasjoner.values().stream().flatMap(Collection::stream).map(KoblingReferanse::new).collect(Collectors.toList()));
        var alleKoblinger = hentKoblinger(referanser);
        var koblingRelasjonEniteter = koblingrelasjoner.entrySet().stream()
                .filter(e -> !e.getValue().isEmpty())
                .flatMap(e -> {
                    var koblingId = finnIdFraListe(e.getKey(), alleKoblinger);
                    return e.getValue().stream().map(v -> new KoblingRelasjon(koblingId, finnIdFraListe(v, alleKoblinger)));
                })
                .filter(r -> !r.getKoblingId().equals(r.getOriginalKoblingId())) // Filtrere ut koblinger mot seg selv
                .collect(Collectors.toList());
        koblingRelasjonEniteter.forEach(repository::lagre);
        return repository.hentRelasjonerFor(alleKoblinger.stream().map(KoblingEntitet::getId).collect(Collectors.toSet()));
    }

    private Long finnIdFraListe(UUID referanse, List<KoblingEntitet> alleKoblinger) {
        return alleKoblinger.stream()
                .filter(k -> k.getKoblingReferanse().getReferanse().equals(referanse))
                .map(KoblingEntitet::getId)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Forventer å finne kobling"));
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

    public List<KoblingRelasjon> hentKoblingRelasjoner(Collection<Long> koblingIder) {
        return repository.hentRelasjonerFor(koblingIder);
    }

    public Optional<AktørId> hentAktørIdForSak(Saksnummer saksnummer) {
        return repository.hentSisteKoblingForSaksnummer(saksnummer).map(KoblingEntitet::getAktørId);
    }

    public List<KoblingEntitet> hentKoblingerForSak(Saksnummer saksnummer) {
        return repository.hentAlleKoblingerForSaksnummer(saksnummer);
    }

    // Burde ta i bruk skrivelås?
    public KoblingLås taSkrivesLås(KoblingReferanse referanse) {
        return taSkrivesLås(repository.hentKoblingIdForKoblingReferanse(referanse));
    }

    public KoblingLås taSkrivesLås(Long koblingId) {
        return låsRepository.taLås(koblingId);
    }


}
