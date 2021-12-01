package no.nav.folketrygdloven.kalkulus.domene.entiteter.kobling;

import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Version;

import no.nav.folketrygdloven.kalkulus.felles.diff.IndexKey;
import no.nav.folketrygdloven.kalkulus.felles.jpa.BaseEntitet;

/**
 * Definerer en relasjon mellom kobling og original kobling.
 */
@Entity(name = "KoblingRelasjon")
@Table(name = "KOBLING_RELASJON")
public class KoblingRelasjon extends BaseEntitet implements IndexKey {


    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_KOBLING_RELASJON")
    private Long id;

    /**
     * Refererer til en kobling med lik saksnummer, aktørid og skjæringstidspunkt som original kobling
     */
    @Column(name = "kobling_id", nullable = false)
    private Long koblingId;

    /**
     * Refererer til koblingens originale kobling
     */
    @Column(name = "original_kobling_id", nullable = false)
    private Long originalKoblingId;

    @Version
    @Column(name = "versjon", nullable = false)
    private long versjon;


    public KoblingRelasjon() {
    }

    public KoblingRelasjon(Long koblingId, Long originalKoblingId) {
        this.koblingId = koblingId;
        this.originalKoblingId = originalKoblingId;
    }

    public Long getKoblingId() {
        return koblingId;
    }

    public Long getOriginalKoblingId() {
        return originalKoblingId;
    }


    @Override
    public String getIndexKey() {
        return IndexKey.createKey(koblingId, originalKoblingId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        KoblingRelasjon that = (KoblingRelasjon) o;
        return Objects.equals(koblingId, that.koblingId) &&
                Objects.equals(originalKoblingId, that.originalKoblingId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(koblingId, originalKoblingId);
    }

    @Override
    public String toString() {
        return "KoblingRelasjon{" +
                "koblingId=" + koblingId +
                ", originalKoblingId=" + originalKoblingId +
                '}';
    }
}
