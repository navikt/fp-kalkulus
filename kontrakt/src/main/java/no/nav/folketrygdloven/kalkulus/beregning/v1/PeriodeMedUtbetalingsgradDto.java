package no.nav.folketrygdloven.kalkulus.beregning.v1;

import java.math.BigDecimal;

import javax.validation.Valid;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;

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
}

