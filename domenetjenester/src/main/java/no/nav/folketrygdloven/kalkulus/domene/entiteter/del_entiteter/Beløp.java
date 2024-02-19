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
 * Beløp representerer kombinasjon av kroner og øre på standardisert format
 */
@Embeddable
public class Beløp implements Serializable, IndexKey, TraverseValue, Comparable<Beløp> {
    public static final Beløp ZERO = new Beløp(BigDecimal.ZERO);
    private static final RoundingMode AVRUNDINGSMODUS = RoundingMode.HALF_EVEN;

    @Column(name = "beloep", scale = 2)
    @ChangeTracked
    private BigDecimal verdi;

    protected Beløp() {
        // for hibernate
    }

    public Beløp(BigDecimal verdi) {
        this.verdi = verdi;
    }

    public static Beløp fraKalkulatorBeløp(no.nav.folketrygdloven.kalkulus.typer.Beløp beløp) {
        var verdi =  no.nav.folketrygdloven.kalkulus.typer.Beløp.safeVerdi(beløp);
        return verdi != null ? new Beløp(verdi) : null;
    }

    public static no.nav.folketrygdloven.kalkulus.typer.Beløp tilKalkulatorBeløp(Beløp beløp) {
        return beløp != null ? no.nav.folketrygdloven.kalkulus.typer.Beløp.fra(beløp.getVerdi()) : null;
    }


    // Beleilig å kunne opprette gjennom int
    public Beløp(Integer verdi) {
        this.verdi = verdi == null ? null : new BigDecimal(verdi);
    }

    // Beleilig å kunne opprette gjennom string
    public Beløp(String verdi) {
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
        Beløp other = (Beløp) obj;
        return Objects.equals(skalertVerdi(), other.skalertVerdi());
    }

    @Override
    public int hashCode() {
        return Objects.hash(skalertVerdi());
    }

    @Override
    public String toString() {
        return "Beløp{" +
            "verdi=" + verdi +
            ", skalertVerdi=" + skalertVerdi() +
            '}';
    }

    @Override
    public int compareTo(Beløp annetBeløp) {
        return verdi.compareTo(annetBeløp.getVerdi());
    }

    public boolean erNullEllerNulltall() {
        return verdi == null || erNulltall();
    }

    public boolean erNulltall() {
        return verdi != null && compareTo(Beløp.ZERO) == 0;
    }

    public Beløp multipliser(int multiplicand) {
        return new Beløp(this.verdi.multiply(BigDecimal.valueOf(multiplicand)));
    }

    public Beløp adder(Beløp augend) {
        return new Beløp(this.verdi.add(augend.getVerdi()));
    }
}
