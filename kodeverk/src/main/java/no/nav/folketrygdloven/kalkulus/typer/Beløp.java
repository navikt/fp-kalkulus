package no.nav.folketrygdloven.kalkulus.typer;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;

public record Beløp(@JsonValue @Valid
                    @DecimalMin(value = "0.00")
                    @DecimalMax(value = "1000000000.00")
                    @Digits(integer = 10, fraction = 2)
                    BigDecimal verdi) implements Comparable<Beløp> {

    public static final Beløp ZERO = Beløp.fra(BigDecimal.ZERO);

    public boolean erNullEller0() {
        return verdi == null || this.compareTo(ZERO) == 0;
    }

    public Beløp multipliser(int operand) {
        return new Beløp(this.verdi.multiply(BigDecimal.valueOf(operand)));
    }

    public Beløp multipliser(Beløp operand) {
        return Beløp.fra(this.verdi.multiply(operand.verdi()));
    }

    public Beløp adder(Beløp operand) {
        return new Beløp(this.verdi.add(operand.verdi()));
    }

    public static BigDecimal safeVerdi(Beløp beløp) {
        return beløp == null ? null : beløp.verdi();
    }

    public static Beløp fra(Beløp beløp) {
        return beløp == null || beløp.verdi() == null ? null : Beløp.fra(beløp.verdi());
    }

    @JsonCreator
    public static Beløp fra(BigDecimal beløp) {
        return beløp != null ? new Beløp(beløp) : null;
    }

    public static Beløp fra(long beløp) {
        return Beløp.fra(BigDecimal.valueOf(beløp));
    }

    public static Beløp fra(int beløp) {
        return Beløp.fra(BigDecimal.valueOf(beløp));
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        return o instanceof Beløp ob && (Objects.equals(this.verdi(), ob.verdi()) || (this.verdi() != null && ob.verdi() != null && this.compareTo(ob) == 0));

    }

    // TODO diskutere og avklare om bruke skalertverdi i hashcode/equals vs compareTo == 0
    private BigDecimal skalertVerdi() {
        return verdi == null ? null : verdi.setScale(2, RoundingMode.HALF_EVEN);
    }

    @Override
    public int hashCode() {
        return Objects.hash(verdi);
    }

    @Override
    public int compareTo(Beløp beløp) {
        return this.verdi.compareTo(beløp.verdi());
    }

}
