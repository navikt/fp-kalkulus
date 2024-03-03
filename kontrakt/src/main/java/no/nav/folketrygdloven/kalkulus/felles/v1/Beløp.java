package no.nav.folketrygdloven.kalkulus.felles.v1;

import java.math.BigDecimal;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import no.nav.folketrygdloven.kalkulus.kodeverk.TempAvledeKode;

public record Beløp(@JsonValue
                    @Valid @NotNull
                    @DecimalMin(value = "-10000000000.00")
                    @DecimalMax(value = "1000000000.00")
                    @Digits(integer = 10, fraction = 2)
                    BigDecimal verdi) implements Comparable<Beløp> {

    public static final Beløp ZERO = Beløp.fra(BigDecimal.ZERO);

    public Beløp {
        Objects.requireNonNull(verdi);
    }

    // Bruk denne som JsonCreator etter overgang + avklaring av ft-kalkulus sin lagret input
    public static Beløp fra(BigDecimal beløp) {
        return beløp != null ? new Beløp(beløp) : null;
    }

    /*
     * Midlertidig fram til konsumenter er konvertert og ft-kalkulus har ryddet lagret input
     * Trenger da ikke lenger delegating
     */
    @Deprecated(since = "2024-03-03", forRemoval = true)
    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static Beløp fraGenerell(Object beløp) {
        if (beløp == null) {
            return null;
        }
        var asBigDecimal = TempAvledeKode.getBeløp(beløp);
        return Beløp.fra(asBigDecimal);
    }

    public static Beløp fra(int beløp) {
        return Beløp.fra(BigDecimal.valueOf(beløp));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        return o instanceof Beløp ob &&
                (Objects.equals(this.verdi(), ob.verdi()) || (this.verdi() != null && ob.verdi() != null && this.compareTo(ob) == 0));

    }

    @Override
    public int hashCode() {
        return Objects.hash(verdi);
    }


    @Override
    public String toString() {
        return verdi != null ? verdi.toString() : null;
    }

    @Override
    public int compareTo(Beløp beløp) {
        return this.verdi.compareTo(beløp.verdi());
    }

}
