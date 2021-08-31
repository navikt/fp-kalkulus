package no.nav.folketrygdloven.kalkulus.beregning.input;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.KoblingReferanse;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.kobling.KoblingEntitet;

public class Resultat<T> {

    private HentInputResponsKode kode;
    private List<KoblingEntitet> koblinger;
    private Map<Long, T> resultatPrKobling;
    private Map<Long, LocalDate> skjæringstidspunktPrKobling;

    public Resultat(HentInputResponsKode kode) {
        this.kode = kode;
    }

    public Resultat(HentInputResponsKode kode, Map<Long, T> resultatPrKobling) {
        this.kode = kode;
        this.resultatPrKobling = resultatPrKobling;
    }

    public Resultat(HentInputResponsKode kode, Map<Long, T> resultatPrKobling, List<KoblingEntitet> koblinger) {
        this.kode = kode;
        this.resultatPrKobling = resultatPrKobling;
        this.koblinger = koblinger;
    }

    public static <T> Resultat<T> forGyldigInputMedData(Map<Long, T> resultatPrKobling) {
        return new Resultat<>(HentInputResponsKode.GYLDIG_INPUT, resultatPrKobling);
    }

    public static <T> Resultat<T> forGyldigInputMedData(Map<Long, T> resultatPrKobling, List<KoblingEntitet> koblinger) {
        return new Resultat<>(HentInputResponsKode.GYLDIG_INPUT, resultatPrKobling, koblinger);
    }

    public HentInputResponsKode getKode() {
        return kode;
    }

    public Map<Long, T> getResultatPrKobling() {
        return resultatPrKobling;
    }

    public Optional<Map<Long, T>> getResultatPrKoblingHvisFinnes() {
        return Optional.ofNullable(resultatPrKobling);
    }


    public Map<UUID, LocalDate> getSkjæringstidspunktPrReferanse() {
        return finnPrReferanse(skjæringstidspunktPrKobling);
    }

    public Map<Long, LocalDate> getSkjæringstidspunktPrKobling() {
        return skjæringstidspunktPrKobling;
    }

    public Resultat<T> medSkjæringstidspunktPrKobling(Map<Long, LocalDate> skjæringstidspunktPrKobling) {
        this.skjæringstidspunktPrKobling = skjæringstidspunktPrKobling;
        return this;
    }

    public Map<UUID, T> getResultatPrReferanse() {
        return finnPrReferanse(resultatPrKobling);
    }

    private <T> Map<UUID, T> finnPrReferanse(Map<Long, T> respons) {
        return respons.entrySet().stream().collect(
                Collectors.toMap(
                        e -> finnKoblingUUIDForKoblingId(e.getKey()),
                        Map.Entry::getValue
                )
        );
    }

    private UUID finnKoblingUUIDForKoblingId(Long koblingId) {
        return koblinger.stream().filter(k -> k.getId().equals(koblingId)).findFirst().map(KoblingEntitet::getKoblingReferanse)
                .map(KoblingReferanse::getReferanse).orElse(null);
    }
}
