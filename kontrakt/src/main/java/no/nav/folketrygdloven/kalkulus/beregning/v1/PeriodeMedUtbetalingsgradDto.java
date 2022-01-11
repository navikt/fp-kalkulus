package no.nav.folketrygdloven.kalkulus.beregning.v1;

import java.math.BigDecimal;
import java.util.Objects;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.folketrygdloven.kalkulus.felles.v1.Periode;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = Visibility.NONE, getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE, isGetterVisibility = Visibility.NONE, creatorVisibility = Visibility.NONE)
@JsonInclude(value = Include.NON_ABSENT, content = Include.NON_EMPTY)
public class PeriodeMedUtbetalingsgradDto {

    @JsonProperty(value = "periode", required = true)
    @NotNull
    @Valid
    private Periode periode;

    @JsonProperty(value = "utbetalingsgrad", required = true)
    @Valid
    @DecimalMin(value = "0.00", message = "utbetalingsgrad ${validatedValue} må være >= {value}")
    @DecimalMax(value = "100.00", message = "utbetalingsgrad ${validatedValue} må være <= {value}")
    @Digits(integer = 3, fraction = 2)
    private BigDecimal utbetalingsgrad;

    public PeriodeMedUtbetalingsgradDto() {
    }

    public PeriodeMedUtbetalingsgradDto(@NotNull @Valid Periode periode, @Valid @DecimalMin(value = "0.00", message = "utbetalingsgrad ${validatedValue} må være >= {value}") @DecimalMax(value = "100.00", message = "utbetalingsgrad ${validatedValue} må være <= {value}") BigDecimal utbetalingsgrad) {
        this.periode = periode;
        this.utbetalingsgrad = utbetalingsgrad;
    }

    public Periode getPeriode() {
        return periode;
    }

    public BigDecimal getUtbetalingsgrad() {
        return utbetalingsgrad;
    }

    @Override
    public String toString() {
        return "PeriodeMedUtbetalingsgradDto{" +
                "periode=" + periode +
                ", utbetalingsgrad=" + utbetalingsgrad +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PeriodeMedUtbetalingsgradDto that = (PeriodeMedUtbetalingsgradDto) o;
        return Objects.equals(periode, that.periode) &&
                Objects.equals(utbetalingsgrad, that.utbetalingsgrad);
    }

    @Override
    public int hashCode() {
        return Objects.hash(periode, utbetalingsgrad);
    }
}

