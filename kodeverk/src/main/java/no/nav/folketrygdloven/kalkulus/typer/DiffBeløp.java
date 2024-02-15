package no.nav.folketrygdloven.kalkulus.typer;

import java.math.BigDecimal;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;

/*
 * Beløp med fortegn
 */
public record DiffBeløp(@JsonValue @Valid
                        @DecimalMin(value = "-10000000000.00")
                        @DecimalMax(value = "1000000000.00")
                        @Digits(integer = 10, fraction = 2)
                        BigDecimal verdi) implements Comparable<DiffBeløp> {

    public static BigDecimal safeVerdi(DiffBeløp beløp) {
        return beløp == null ? null : beløp.verdi();
    }

    public static DiffBeløp fra(DiffBeløp beløp) {
        return beløp == null || beløp.verdi() == null ? null : DiffBeløp.fra(beløp.verdi());
    }

    @JsonCreator
    public static DiffBeløp fra(BigDecimal beløp) {
        return beløp != null ? new DiffBeløp(beløp) : null;
    }

    public static DiffBeløp fra(long beløp) {
        return DiffBeløp.fra(BigDecimal.valueOf(beløp));
    }

    public static DiffBeløp fra(int beløp) {
        return DiffBeløp.fra(BigDecimal.valueOf(beløp));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o instanceof DiffBeløp ob) {
            return Objects.equals(this.verdi(), ob.verdi()) || (this.verdi() != null && ob.verdi() != null && this.compareTo(ob) == 0);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(verdi);
    }

    @Override
    public int compareTo(DiffBeløp beløp) {
        return this.verdi.compareTo(beløp.verdi());
    }

}
