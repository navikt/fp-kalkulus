package no.nav.folketrygdloven.kalkulator.modell.typer;

import java.io.Serializable;
import java.util.Objects;

import no.nav.folketrygdloven.kalkulus.felles.diff.IndexKey;
import no.nav.folketrygdloven.kalkulus.typer.AktørId;
import no.nav.folketrygdloven.kalkulus.typer.OrgNummer;

/**
 * En arbeidsgiver (enten virksomhet eller personlig arbeidsgiver).
 */
public class Arbeidsgiver implements Serializable, IndexKey {

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
    private Arbeidsgiver() {
        // for JPA
    }

    protected Arbeidsgiver(String arbeidsgiverOrgnr, AktørId arbeidsgiverAktørId) {
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

    public static Arbeidsgiver virksomhet(String arbeidsgiverOrgnr) {
        return new Arbeidsgiver(arbeidsgiverOrgnr, null);
    }

    public static Arbeidsgiver virksomhet(OrgNummer arbeidsgiverOrgnr) {
        return new Arbeidsgiver(arbeidsgiverOrgnr.getId(), null);
    }

    public static Arbeidsgiver person(AktørId arbeidsgiverAktørId) {
        return new Arbeidsgiver(null, arbeidsgiverAktørId);
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
     * Return true hvis arbeidsgiver er en virksomhet, false hvis en Person.
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
        if (o == null || !(o instanceof Arbeidsgiver))
            return false;
        Arbeidsgiver that = (Arbeidsgiver) o;
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

    public static Arbeidsgiver fra(Arbeidsgiver arbeidsgiver) {
        if (arbeidsgiver == null) return null;
        return new Arbeidsgiver(arbeidsgiver.getOrgnr(), arbeidsgiver.getAktørId());
    }

    public static Arbeidsgiver fra(AktørId aktørId) {
        return fra(person(aktørId));
    }
}
