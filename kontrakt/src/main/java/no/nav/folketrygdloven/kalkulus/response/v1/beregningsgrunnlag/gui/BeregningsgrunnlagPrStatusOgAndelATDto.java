package no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_ABSENT;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

import java.math.BigDecimal;

import javax.validation.Valid;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;


@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = NON_ABSENT, content = NON_EMPTY)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
public class BeregningsgrunnlagPrStatusOgAndelATDto extends BeregningsgrunnlagPrStatusOgAndelDto {
    public static final String DTO_TYPE = "AT";

    @Valid
    @JsonProperty("bortfaltNaturalytelse")
    @Digits(integer = 8, fraction = 2)
    @DecimalMin("0.00")
    @DecimalMax("10000000.00")
    private BigDecimal bortfaltNaturalytelse;

    public BeregningsgrunnlagPrStatusOgAndelATDto() {
        super();
    }

    public BigDecimal getBortfaltNaturalytelse() {
        return bortfaltNaturalytelse;
    }

    public void setBortfaltNaturalytelse(BigDecimal bortfaltNaturalytelse) {
        this.bortfaltNaturalytelse = bortfaltNaturalytelse;
    }
}
