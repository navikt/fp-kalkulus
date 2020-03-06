package no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import java.math.BigDecimal;
import java.util.Objects;

import javax.validation.Valid;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = JsonInclude.Include.NON_ABSENT, content = JsonInclude.Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = NONE, setterVisibility = NONE, isGetterVisibility = NONE, creatorVisibility = NONE)
public class AndelMedBeløpDto extends FaktaOmBeregningAndelDto {

    @Valid
    @JsonProperty("fastsattBelopPrMnd")
    @Digits(integer = 8, fraction = 2)
    @DecimalMin("0.00")
    @DecimalMax("10000000.00")
    private BigDecimal fastsattBelopPrMnd;

    public BigDecimal getFastsattBelopPrMnd() {
        return fastsattBelopPrMnd;
    }

    public void setFastsattBelopPrMnd(BigDecimal fastsattBelopPrMnd) {
        this.fastsattBelopPrMnd = fastsattBelopPrMnd;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        AndelMedBeløpDto that = (AndelMedBeløpDto) o;
        return Objects.equals(fastsattBelopPrMnd, that.fastsattBelopPrMnd);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), fastsattBelopPrMnd);
    }
}
