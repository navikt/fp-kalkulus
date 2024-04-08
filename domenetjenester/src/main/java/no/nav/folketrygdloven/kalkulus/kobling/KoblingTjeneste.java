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

import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.KoblingReferanse;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.Saksnummer;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.kobling.KoblingEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.kobling.KoblingRelasjon;
import no.nav.folketrygdloven.kalkulus.kodeverk.FagsakYtelseType;
import no.nav.folketrygdloven.kalkulus.tjeneste.kobling.KoblingRepository;
import no.nav.folketrygdloven.kalkulus.typer.AktørId;

@ApplicationScoped
public class KoblingTjeneste {

    private KoblingRepository repository;

    KoblingTjeneste() {
    }

    @Inject
    public KoblingTjeneste(KoblingRepository repository) {
        this.repository = repository;
    }

    public KoblingEntitet finnEllerOpprett(KoblingReferanse referanse, FagsakYtelseType ytelseType, AktørId aktørId, Saksnummer saksnummer) {
        var eksisterendeKobling = repository.hentKoblingFor(referanse);
        if (eksisterendeKobling.isPresent()) {
            return eksisterendeKobling.get();
        }
        var nyKobling = new KoblingEntitet(referanse, ytelseType, saksnummer, aktørId);
        repository.lagre(nyKobling);
        return nyKobling;
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

    public List<KoblingEntitet> hentKoblinger(Collection<KoblingReferanse> koblingReferanser, FagsakYtelseType ytelseType) {
        return repository.hentKoblingerFor(koblingReferanser, ytelseType);
    }

    public List<KoblingEntitet> hentKoblinger(Collection<KoblingReferanse> koblingReferanser) {
        return repository.hentKoblingIdForKoblingReferanser(koblingReferanser);
    }

    public List<KoblingRelasjon> hentKoblingRelasjoner(Collection<Long> koblingIder) {
        return repository.hentRelasjonerFor(koblingIder);
    }

}
