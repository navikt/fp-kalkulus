package no.nav.folketrygdloven.kalkulus.beregning.input;

import java.util.Map;

public class Resultat<T> {

    private HentInputResponsKode kode;
    private Map<Long, T> resultatPrKobling;

    public Resultat(HentInputResponsKode kode) {
        this.kode = kode;
    }

    public Resultat(HentInputResponsKode kode, Map<Long, T> resultatPrKobling) {
        this.kode = kode;
        this.resultatPrKobling = resultatPrKobling;
    }

    public static <T> Resultat<T> forGyldigInputMedData(Map<Long, T> resultatPrKobling) {
        return new Resultat<>(HentInputResponsKode.GYLDIG_INPUT, resultatPrKobling);
    }

    public HentInputResponsKode getKode() {
        return kode;
    }

    public Map<Long, T> getResultatPrKobling() {
        return resultatPrKobling;
    }
}
