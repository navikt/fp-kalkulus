package no.nav.folketrygdloven.kalkulator.modell.typer;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Stillingsprosent slik det er oppgitt i arbeidsavtalen
 */
public class Stillingsprosent implements Serializable {

    public static final Stillingsprosent ZERO = new Stillingsprosent(0);
    public static final Stillingsprosent HUNDRED = new Stillingsprosent(100);
    private static final Logger log = LoggerFactory.getLogger(Stillingsprosent.class);
    private static final RoundingMode AVRUNDINGSMODUS = RoundingMode.HALF_EVEN;
    private BigDecimal verdi;

    public Stillingsprosent(BigDecimal verdi) {
        this.verdi = verdi == null ? null : fiksNegativOgMax(verdi);
        validerRange(this.verdi);
    }

    // Beleilig å kunne opprette gjennom int
    public Stillingsprosent(Integer verdi) {
        this(new BigDecimal(verdi));
    }

    // Beleilig å kunne opprette gjennom string
    public Stillingsprosent(String verdi) {
        this(new BigDecimal(verdi));
    }

    private static void validerRange(BigDecimal verdi) {
        if (verdi == null) {
            return;
        }
        check(verdi.compareTo(BigDecimal.ZERO) >= 0, "Prosent må være >= 0"); //$NON-NLS-1$
    }

    private static void check(boolean check, String message, Object... params) {
        if (!check) {
            throw new IllegalArgumentException(String.format(message, params));
        }
    }

    public BigDecimal getVerdi() {
        return verdi;
    }

    private BigDecimal skalertVerdi() {
        return verdi.setScale(2, AVRUNDINGSMODUS);
    }


    private BigDecimal fiksNegativOgMax(BigDecimal verdi) {
        var maxVerdi = new BigDecimal(500);
        if (null != verdi && verdi.compareTo(BigDecimal.ZERO) < 0) {
            log.info("[IAY] Prosent (yrkesaktivitet, permisjon) kan ikke være mindre enn 0, absolutt verdi brukes isteden. Verdi fra AA-reg: {}", verdi);
            verdi = verdi.abs();
        }
        if (null != verdi && verdi.compareTo(maxVerdi) > 0) {
            log.info("[IAY] Prosent (yrkesaktivitet, permisjon) kan ikke være mer enn 500, avkortet verdi brukes isteden. Verdi fra AA-reg: {}", verdi);
            verdi = maxVerdi;
        }
        return verdi;
    }

    public boolean erNulltall() {
        return verdi != null && verdi.intValue() == 0;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj == null || !getClass().equals(obj.getClass())) {
            return false;
        }
        Stillingsprosent other = (Stillingsprosent) obj;
        return Objects.equals(skalertVerdi(), other.skalertVerdi());
    }

    @Override
    public int hashCode() {
        return Objects.hash(skalertVerdi());
    }

    @Override
    public String toString() {
        return "Stillingsprosent{" +
                "verdi=" + verdi +
                ", skalertVerdi=" + skalertVerdi() +
                '}';
    }
}
