package no.nav.folketrygdloven.kalkulator.modell.typer;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

import no.nav.folketrygdloven.kalkulator.modell.diff.ChangeTracked;
import no.nav.folketrygdloven.kalkulator.modell.diff.IndexKey;
import no.nav.folketrygdloven.kalkulator.modell.diff.TraverseValue;

/**
 * Antall timer slik det er oppgitt i arbeidsavtalen
 */
public class AntallTimer implements Serializable, IndexKey, TraverseValue {

    private static final RoundingMode AVRUNDINGSMODUS = RoundingMode.HALF_EVEN;

    @ChangeTracked
    private BigDecimal verdi;

    protected AntallTimer() {
        // for hibernate
    }

    public AntallTimer(BigDecimal verdi) {
        this.verdi = verdi;
    }

    // Beleilig å kunne opprette gjennom int
    public AntallTimer(Integer verdi) {
        this.verdi = verdi == null ? null : new BigDecimal(verdi);
    }

    // Beleilig å kunne opprette gjennom string
    public AntallTimer(String verdi) {
        this.verdi = verdi == null ? null : new BigDecimal(verdi);
    }

    public BigDecimal getVerdi() {
        return verdi;
    }

    public BigDecimal getSkalertVerdi() {
        return skalertVerdi();
    }

    private BigDecimal skalertVerdi() {
        if(verdi == null) {
            return null;
        }
        return verdi.setScale(2, AVRUNDINGSMODUS);
    }

    @Override
    public String getIndexKey() {
        return IndexKey.createKey(skalertVerdi());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj == null || !getClass().equals(obj.getClass())) {
            return false;
        }
        AntallTimer other = (AntallTimer) obj;
        return Objects.equals(skalertVerdi(), other.skalertVerdi());
    }

    @Override
    public int hashCode() {
        return Objects.hash(skalertVerdi());
    }

    @Override
    public String toString() {
        return "AntallTimer{" +
            "verdi=" + verdi +
            ", skalertVerdi=" + skalertVerdi() +
            '}';
    }
}
