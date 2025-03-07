package no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import no.nav.folketrygdloven.kalkulus.felles.diff.ChangeTracked;
import no.nav.folketrygdloven.kalkulus.felles.diff.IndexKey;
import no.nav.folketrygdloven.kalkulus.felles.diff.TraverseValue;

/**
 * Promille representerer en promillesats på standarisert format
 */
@Embeddable
public class Prosent implements Serializable, IndexKey, TraverseValue {
    public static final Prosent ZERO = new Prosent(BigDecimal.ZERO);
    private static final RoundingMode AVRUNDINGSMODUS = RoundingMode.HALF_EVEN;

    @Column(name = "prosent", scale = 10)
    @ChangeTracked
    private BigDecimal verdi;

    protected Prosent() {
        // for hibernate
    }

    public Prosent(BigDecimal verdi) {
        this.verdi = verdi;
    }


    // Beleilig å kunne opprette gjennom int
    public Prosent(Integer verdi) {
        this.verdi = verdi == null ? null : new BigDecimal(verdi);
    }

    // Beleilig å kunne opprette gjennom string
    public Prosent(String verdi) {
        this.verdi = verdi == null ? null : new BigDecimal(verdi);
    }

    private BigDecimal skalertVerdi() {
        return verdi == null ? null : verdi.setScale(2, AVRUNDINGSMODUS);
    }

    @Override
    public String getIndexKey() {
        BigDecimal skalertVerdi = skalertVerdi();
        return skalertVerdi != null ? skalertVerdi.toString() : null;
    }

    public BigDecimal getVerdi() {
        return verdi;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj == null || !getClass().equals(obj.getClass())) {
            return false;
        }
        Prosent other = (Prosent) obj;
        return Objects.equals(skalertVerdi(), other.skalertVerdi());
    }

    @Override
    public int hashCode() {
        return Objects.hash(skalertVerdi());
    }

    @Override
    public String toString() {
        return "Prosent{" +
            "verdi=" + verdi +
            ", skalertVerdi=" + skalertVerdi() +
            '}';
    }

    public int compareTo(Prosent annetBeløp) {
        return verdi.compareTo(annetBeløp.getVerdi());
    }

    public boolean erNullEllerNulltall() {
        return verdi == null || erNulltall();
    }

    public boolean erNulltall() {
        return verdi != null && compareTo(Prosent.ZERO) == 0;
    }


}
