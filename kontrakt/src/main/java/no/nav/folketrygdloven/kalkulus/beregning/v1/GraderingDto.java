package no.nav.folketrygdloven.kalkulus.beregning.v1;

import java.math.BigDecimal;

import javax.validation.Valid;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.folketrygdloven.kalkulus.felles.v1.Periode;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = Include.NON_ABSENT, content = Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
public class GraderingDto {


    @JsonProperty(value = "periode")
    @Valid
    @NotNull
    private Periode periode;

    @JsonProperty(value = "arbeidstidProsent")
    @Valid
    @DecimalMin(value = "0.00", message = "stillingsprosent ${validatedValue} må være >= {value}")
    @DecimalMax(value = "100.00", message = "stillingsprosent ${validatedValue} må være <= {value}")
    private BigDecimal arbeidstidProsent;

    protected GraderingDto() {
        // default ctor
    }

    public GraderingDto(@Valid @NotNull Periode periode, @Valid @DecimalMin(value = "0.00", message = "stillingsprosent ${validatedValue} må være >= {value}") @DecimalMax(value = "100.00", message = "stillingsprosent ${validatedValue} må være <= {value}") BigDecimal arbeidstidProsent) {
        this.periode = periode;
        this.arbeidstidProsent = arbeidstidProsent;
    }

    public Periode getPeriode() {
        return periode;
    }

    public BigDecimal getArbeidstidProsent() {
        return arbeidstidProsent;
    }
}
