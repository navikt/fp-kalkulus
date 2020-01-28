package no.nav.folketrygdloven.kalkulator.modell.kodeverk;

import java.util.Objects;

import javax.persistence.MappedSuperclass;

import no.nav.folketrygdloven.kalkulator.modell.diff.DiffIgnore;


/**
 * Superklasse-entitet for tabeller som fungerer som internt kodeverk/ oppslag/ referansedata.
 * Disse tabellene har en felles struktur som inneholder
 * <ul>
 * <li>kode</li>
 * <li>navn - for visning</li>
 * <li>beskrivelse - for dokumentasjon av koden</li>
 * </ul>
 * <p>
 * Bruk: Subklass denne med en en klasse som mapper til en spesifikk tabell.
 */
@MappedSuperclass
public abstract class KodeverkTabell implements BasisKodeverdi {

    private String kode;

    @DiffIgnore
    private String beskrivelse;

    /**
     * Navn registrert i databasen.
     */
    @DiffIgnore
    private String navn;

    protected KodeverkTabell() {
        // Hibernate
    }

    protected KodeverkTabell(String kode) {
        Objects.requireNonNull(kode, "kode");
        this.kode = kode;
    }

    @Override
    public String getIndexKey() {
        return kode;
    }

    @Override
    public String getKode() {
        return kode;
    }

    @Override
    public String getNavn() {
        return navn;
    }

    public String getBeskrivelse() {
        return beskrivelse;
    }

    @Override
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        } else if (object == null || !(object instanceof KodeverkTabell)) {
            return false;
        }
        KodeverkTabell other = (KodeverkTabell) object;
        return Objects.equals(getKode(), other.getKode());
    }

    @Override
    public int hashCode() {
        return Objects.hash(kode);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "<kode=" + getKode() + ">"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }
}
