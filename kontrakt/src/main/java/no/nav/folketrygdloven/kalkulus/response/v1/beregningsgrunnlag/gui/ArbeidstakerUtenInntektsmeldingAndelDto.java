package no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import java.math.BigDecimal;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = JsonInclude.Include.NON_ABSENT, content = JsonInclude.Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = NONE, setterVisibility = NONE, isGetterVisibility = NONE, creatorVisibility = NONE)
public class ArbeidstakerUtenInntektsmeldingAndelDto extends FaktaOmBeregningAndelDto {

    @Valid
    @JsonProperty(value = "mottarYtelse")
    private Boolean mottarYtelse;

    @Valid
    @JsonProperty(value = "inntektPrMnd")
    @Digits(integer = 8, fraction = 2)
    @DecimalMin("0.00")
    @DecimalMax("10000000.00")
    private BigDecimal inntektPrMnd;

    public Boolean getMottarYtelse() {
        return mottarYtelse;
    }

    public void setMottarYtelse(boolean mottarYtelse) {
        this.mottarYtelse = mottarYtelse;
    }

    public BigDecimal getInntektPrMnd() {
        return inntektPrMnd;
    }

    public void setInntektPrMnd(BigDecimal inntektPrMnd) {
        this.inntektPrMnd = inntektPrMnd;
    }
}
