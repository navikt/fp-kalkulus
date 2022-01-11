package no.nav.folketrygdloven.kalkulus.opptjening.v1;

import java.math.BigDecimal;

import javax.validation.Valid;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.folketrygdloven.kalkulus.felles.v1.Periode;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
public class OppgittArbeidsforholdDto {

    @JsonProperty("periode")
    @NotNull
    @Valid
    private Periode periode;

    @JsonProperty("inntekt")
    @NotNull
    @DecimalMin(value = "0.00", message = "verdien ${validatedValue} må være >= {value}")
    @DecimalMax(value = "1000000000.00", message = "verdien ${validatedValue} må være <= {value}")
    @Digits(integer = 10, fraction = 2)
    @Valid
    private BigDecimal inntekt;


    public OppgittArbeidsforholdDto() {
        // Json deserilaisering
    }

    public OppgittArbeidsforholdDto(@NotNull Periode periode, @NotNull BigDecimal inntekt) {
        this.periode = periode;
        this.inntekt = inntekt;
    }

    public Periode getPeriode() {
        return periode;
    }

    public BigDecimal getInntekt() {
        return inntekt;
    }

}
