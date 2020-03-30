package no.nav.folketrygdloven.kalkulus.beregning.v1;

import java.math.BigDecimal;
import java.util.Objects;

import javax.validation.Valid;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.folketrygdloven.kalkulus.felles.v1.Periode;


//TODO(OJR) ønsker kalkulus skal eie grunnbeløp, da trenger vi ikke denne lengre
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = Visibility.NONE, getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE, isGetterVisibility = Visibility.NONE, creatorVisibility = Visibility.NONE)
@JsonInclude(value = Include.NON_ABSENT, content = Include.NON_EMPTY)
public class GrunnbeløpDto {

    @JsonProperty(value = "periode", required = true)
    @Valid
    @NotNull
    private Periode periode;

    @JsonProperty(value = "gSnitt", required = true)
    @Valid
    @DecimalMin(value = "0.00", message = "verdien ${validatedValue} må være >= {value}")
    @DecimalMax(value = "200000.00", message = "verdien ${validatedValue} må være <= {value}")
    @Digits(integer = 6, fraction = 2)
    private BigDecimal gSnitt;

    @JsonProperty(value = "gVerdi", required = true)
    @Valid
    @DecimalMin(value = "0.00", message = "verdien ${validatedValue} må være >= {value}")
    @DecimalMax(value = "200000.00", message = "verdien ${validatedValue} må være <= {value}")
    @Digits(integer = 6, fraction = 2)
    private BigDecimal gVerdi;

    public GrunnbeløpDto() {
        // default ctor
    }

    public GrunnbeløpDto(@Valid @NotNull Periode periode, @Valid @NotNull BigDecimal gSnitt, @Valid @NotNull BigDecimal gVerdi) {
        this.periode = periode;
        this.gSnitt = gSnitt;
        this.gVerdi = gVerdi;
    }

    public Periode getPeriode() {
        return periode;
    }

    public BigDecimal getgSnitt() {
        return gSnitt;
    }

    public BigDecimal getgVerdi() {
        return gVerdi;
    }

    @Override
    public String toString() {
        return "GrunnbeløpDto{" +
                "periode=" + periode +
                ", gSnitt=" + gSnitt +
                ", gVerdi=" + gVerdi +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GrunnbeløpDto that = (GrunnbeløpDto) o;
        return Objects.equals(periode, that.periode) &&
                Objects.equals(gSnitt, that.gSnitt) &&
                Objects.equals(gVerdi, that.gVerdi);
    }

    @Override
    public int hashCode() {
        return Objects.hash(periode, gSnitt, gVerdi);
    }
}
