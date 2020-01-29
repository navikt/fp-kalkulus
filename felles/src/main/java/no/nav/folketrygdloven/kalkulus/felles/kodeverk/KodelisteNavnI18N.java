package no.nav.folketrygdloven.kalkulus.felles.kodeverk;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonBackReference;

import no.nav.folketrygdloven.kalkulus.felles.diff.DiffIgnore;


public class KodelisteNavnI18N {

    private Long id;

    @JsonBackReference
    private Kodeliste kodeliste;

    @DiffIgnore
    private String navn;

    @DiffIgnore
    private String språk;


    KodelisteNavnI18N() {
        // Hibernate trenger default constructor.
    }

    public KodelisteNavnI18N(String språk, String navn) {
        this.språk = språk;
        this.navn = navn;
    }

    public Long getId() {
        return id;
    }

    public String getSpråk() {
        return språk;
    }

    public Kodeliste getKodeliste() {
        return kodeliste;
    }

    public String getNavn() {
        return navn;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof KodelisteNavnI18N)) return false;
        KodelisteNavnI18N that = (KodelisteNavnI18N) o;
        return Objects.equals(kodeliste, that.kodeliste) &&
            Objects.equals(språk, that.språk);
    }

    @Override
    public int hashCode() {
        return Objects.hash(kodeliste, språk);
    }

    @Override
    public String toString() {
        return "KodelisteNavnI18N{" +
            "kodeliste=" + kodeliste +
            ", navn='" + navn + '\'' +
            ", språk='" + språk + '\'' +
            '}';
    }
}
