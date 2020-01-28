package no.nav.folketrygdloven.kalkulator.modell.virksomhet;

import java.io.Serializable;
import java.util.Objects;

import no.nav.folketrygdloven.kalkulator.modell.diff.IndexKey;
import no.nav.folketrygdloven.kalkulator.modell.diff.TraverseValue;
import no.nav.folketrygdloven.kalkulator.modell.typer.AktørId;

/**
 * En arbeidsgiver (enten virksomhet eller personlig arbeidsgiver).
 */
public class ArbeidsgiverMedNavn implements Serializable, TraverseValue, IndexKey {

    private String arbeidsgiverOrgnr;
    private AktørId arbeidsgiverAktørId;

    private String navn;

    public String getNavn() {
        return navn;
    }

    public void setNavn(String navn) {
        this.navn = navn;
    }

    @SuppressWarnings("unused")
    private ArbeidsgiverMedNavn() {
        // for JPA
    }

    protected ArbeidsgiverMedNavn(String arbeidsgiverOrgnr, AktørId arbeidsgiverAktørId) {
        if (arbeidsgiverAktørId == null && arbeidsgiverOrgnr == null) {
            throw new IllegalArgumentException("Utvikler-feil: arbeidsgiver uten hverken orgnr eller aktørId");
        } else if (arbeidsgiverAktørId != null && arbeidsgiverOrgnr != null) {
            throw new IllegalArgumentException("Utvikler-feil: arbeidsgiver med både orgnr og aktørId");
        }
        this.arbeidsgiverOrgnr = arbeidsgiverOrgnr;
        this.arbeidsgiverAktørId = arbeidsgiverAktørId;
    }

    @Override
    public String getIndexKey() {
        return getAktørId() != null
            ? IndexKey.createKey("arbeidsgiverAktørId", getAktørId())
            : IndexKey.createKey("virksomhet", getOrgnr());
    }

    public static ArbeidsgiverMedNavn virksomhet(String arbeidsgiverOrgnr) {
        return new ArbeidsgiverMedNavn(arbeidsgiverOrgnr, null);
    }

    public static ArbeidsgiverMedNavn virksomhet(OrgNummer arbeidsgiverOrgnr) {
        return new ArbeidsgiverMedNavn(arbeidsgiverOrgnr.getId(), null);
    }

    public static ArbeidsgiverMedNavn person(AktørId arbeidsgiverAktørId) {
        return new ArbeidsgiverMedNavn(null, arbeidsgiverAktørId);
    }

    /**
     * Virksomhets orgnr. Leser bør ta høyde for at dette kan være juridisk orgnr (istdf. virksomhets orgnr).
     */
    public String getOrgnr() {
        return arbeidsgiverOrgnr;
    }

    /**
     * Hvis arbeidsgiver er en privatperson, returner aktørId for person.
     */
    public AktørId getAktørId() {
        return arbeidsgiverAktørId;
    }

    /**
     * Returneer ident for arbeidsgiver. Kan være Org nummer eller Aktør id (dersom arbeidsgiver er en enkelt person -
     * f.eks. for Frilans el.)
     */
    public String getIdentifikator() {
        if (arbeidsgiverAktørId != null) {
            return getAktørId().getId();
        }
        return getOrgnr();
    }

    /**
     * Return true hvis arbeidsgiver er en {@link Virksomhet}, false hvis en Person.
     */
    public boolean getErVirksomhet() {
        return getOrgnr() != null;
    }

    /**
     * Return true hvis arbeidsgiver er en {@link AktørId}, ellers false.
     */
    public boolean erAktørId() {
        return getAktørId() != null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || !(o instanceof ArbeidsgiverMedNavn))
            return false;
        ArbeidsgiverMedNavn that = (ArbeidsgiverMedNavn) o;
        return Objects.equals(getOrgnr(), that.getOrgnr()) &&
            Objects.equals(getAktørId(), that.getAktørId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getOrgnr(), getAktørId());
    }

    @Override
    public String toString() {
        return "Arbeidsgiver{" +
            "virksomhet=" + getOrgnr() +
            ", arbeidsgiverAktørId='" + getAktørId() + '\'' +
            '}';
    }

    public static ArbeidsgiverMedNavn fra(ArbeidsgiverMedNavn arbeidsgiver) {
        if (arbeidsgiver == null) return null;
        return new ArbeidsgiverMedNavn(arbeidsgiver.getOrgnr(), arbeidsgiver.getAktørId());
    }

    public static ArbeidsgiverMedNavn fra(Virksomhet virksomhet) {
        return fra(virksomhet(virksomhet.getOrgnr()));
    }

    public static ArbeidsgiverMedNavn fra(AktørId aktørId) {
        return fra(person(aktørId));
    }
}
