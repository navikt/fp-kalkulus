package no.nav.folketrygdloven.kalkulus.domene.entiteter.forlengelse;

import java.util.Objects;

import org.hibernate.annotations.Immutable;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import no.nav.folketrygdloven.kalkulus.felles.diff.IndexKey;
import no.nav.folketrygdloven.kalkulus.felles.jpa.BaseEntitet;
import no.nav.folketrygdloven.kalkulus.felles.jpa.IntervallEntitet;

/**
 * Definerer en relasjon mellom kobling og original kobling.
 */
@Entity(name = "Forlengelseperiode")
@Table(name = "FORLENGELSE_PERIODE")
@Immutable
public class ForlengelseperiodeEntitet extends BaseEntitet implements IndexKey {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_FORLENGELSE_PERIODE")
    private Long id;

    @Embedded
    private IntervallEntitet periode;

    @Version
    @Column(name = "versjon", nullable = false)
    private long versjon;

    public ForlengelseperiodeEntitet() {
    }

    public ForlengelseperiodeEntitet(IntervallEntitet periode) {
        this.periode = periode;
    }

    public IntervallEntitet getPeriode() {
        return periode;
    }

    @Override
    public String getIndexKey() {
        return IndexKey.createKey(periode);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ForlengelseperiodeEntitet that = (ForlengelseperiodeEntitet) o;
        return Objects.equals(periode, that.periode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(periode);
    }
}
