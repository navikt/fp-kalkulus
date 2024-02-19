package no.nav.folketrygdloven.kalkulus.typer;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

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

    @JsonCreator
    public static Beløp fra(BigDecimal beløp) {
        return beløp != null ? new Beløp(beløp) : null;
    }

    public boolean erNullEller0() {
        return verdi == null || this.compareTo(ZERO) == 0;
    }

    public Beløp multipliser(int operand) {
        return new Beløp(this.verdi.multiply(BigDecimal.valueOf(operand)));
    }

    public Beløp multipliser(BigDecimal operand) {
        return new Beløp(this.verdi.multiply(operand));
    }

    public Beløp multipliser(Beløp operand) {
        return Beløp.fra(this.verdi.multiply(operand.verdi()));
    }

    public Beløp adder(Beløp operand) {
        return new Beløp(this.verdi.add(operand.verdi()));
    }

    public Beløp subtraher(Beløp operand) {
        return new Beløp(this.verdi.subtract(operand.verdi()));
    }

    public Beløp min(Beløp val) {
        return (compareTo(val) <= 0 ? this : val);
    }

    public Beløp max(Beløp val) {
        return (compareTo(val) >= 0 ? this : val);
    }

    public Beløp map(Function<BigDecimal, BigDecimal> mapper) {
        return Beløp.fra(mapper.apply(verdi));
    }

    public Beløp filter(Predicate<BigDecimal> filter) {
        return filter.test(verdi) ? this : null;
    }

    public static BigDecimal safeVerdi(Beløp beløp) {
        return beløp == null ? null : beløp.verdi();
    }

    public static Beløp safeSum(Beløp lhs, Beløp rhs) {
        if (Beløp.safeVerdi(lhs) == null && Beløp.safeVerdi(rhs) == null) {
            return null;
        } else if (Beløp.safeVerdi(lhs) == null) {
            return rhs;
        } else {
            return Beløp.safeVerdi(rhs) == null ? lhs : lhs.adder(rhs);
        }
    }

    public static Beløp fra(Beløp beløp) {
        return Beløp.fra(safeVerdi(beløp));
    }

    public static Beløp fra(Long beløp) {
        return Beløp.fra(beløp != null ? BigDecimal.valueOf(beløp) : null);
    }

    public static Beløp fra(Integer beløp) {
        return Beløp.fra(beløp != null ? BigDecimal.valueOf(beløp) : null);
    }

    public static Beløp fra(long beløp) {
        return Beløp.fra(BigDecimal.valueOf(beløp));
    }

    public static Beløp fra(int beløp) {
        return Beløp.fra(BigDecimal.valueOf(beløp));
    }

    public void sjekkpositiv() {
        if (this.compareTo(Beløp.ZERO) < 0 ) {
            throw new IllegalArgumentException("Negativt beløp" + verdi);
        }
    }

    public int intValue() {
        return verdi.intValue();
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
