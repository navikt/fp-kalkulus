package no.nav.folketrygdloven.kalkulus.domene.entiteter.forlengelse;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.hibernate.annotations.Immutable;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import no.nav.folketrygdloven.kalkulus.felles.diff.IndexKey;
import no.nav.folketrygdloven.kalkulus.felles.jpa.BaseEntitet;
import no.nav.folketrygdloven.kalkulus.felles.jpa.IntervallEntitet;

/**
 * Definerer en relasjon mellom kobling og original kobling.
 */
@Entity(name = "Forlengelseperioder")
@Table(name = "FORLENGELSE_PERIODER")
public class ForlengelseperioderEntitet extends BaseEntitet implements IndexKey {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_FORLENGELSE_PERIODER")
    private Long id;

    /**
     * Refererer til en kobling
     */
    @Column(name = "kobling_id", nullable = false)
    private Long koblingId;

    @Column(name = "aktiv", nullable = false)
    private boolean aktiv = true;

    @Immutable
    @JoinColumn(name = "FORLENGELSE_PERIODER_ID", nullable = false, updatable = false)
    @OneToMany(cascade = {CascadeType.PERSIST, CascadeType.REFRESH})
    private List<ForlengelseperiodeEntitet> forlengelseperioder = new ArrayList<>();

    @Version
    @Column(name = "versjon", nullable = false)
    private long versjon;


    public ForlengelseperioderEntitet() {
    }

    public ForlengelseperioderEntitet(Long koblingId, List<ForlengelseperiodeEntitet> forlengelseperioder) {
        this.koblingId = koblingId;
        this.forlengelseperioder = forlengelseperioder;
    }

    public Long getKoblingId() {
        return koblingId;
    }


    public List<ForlengelseperiodeEntitet> getForlengelseperioder() {
        return forlengelseperioder;
    }

    public List<IntervallEntitet> getForlengelseintervaller() {
        return forlengelseperioder.stream().map(ForlengelseperiodeEntitet::getPeriode).toList();
    }

    public void setAktiv(boolean aktiv) {
        this.aktiv = aktiv;
    }

    @Override
    public String getIndexKey() {
        return IndexKey.createKey(koblingId, forlengelseperioder);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ForlengelseperioderEntitet that = (ForlengelseperioderEntitet) o;
        return aktiv == that.aktiv && koblingId.equals(that.koblingId) && forlengelseperioder.equals(that.forlengelseperioder);
    }

    @Override
    public String toString() {
        return "ForlengelseperioderEntitet{" +
                "koblingId=" + koblingId +
                ", aktiv=" + aktiv +
                ", forlengelseperioder=" + forlengelseperioder +
                '}';
    }

    @Override
    public int hashCode() {
        return Objects.hash(koblingId, aktiv, forlengelseperioder);
    }
}
