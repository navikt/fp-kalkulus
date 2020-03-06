package no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui;

import java.math.BigDecimal;

import javax.validation.Valid;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonProperty;


@JsonAutoDetect(getterVisibility=Visibility.NONE, setterVisibility=Visibility.NONE, fieldVisibility=Visibility.ANY)
public class BeregningsgrunnlagPrStatusOgAndelATDto extends BeregningsgrunnlagPrStatusOgAndelDto {

    @Valid
    @JsonProperty("bortfaltNaturalytelse")
    @Digits(integer = 8, fraction = 2)
    @DecimalMin("0.00")
    @DecimalMax("10000000.00")
    private BigDecimal bortfaltNaturalytelse;


    public BeregningsgrunnlagPrStatusOgAndelATDto() {
        super();
        // trengs for deserialisering av JSON
    }

    public BigDecimal getBortfaltNaturalytelse() {
        return bortfaltNaturalytelse;
    }

    public void setBortfaltNaturalytelse(BigDecimal bortfaltNaturalytelse) {
        this.bortfaltNaturalytelse = bortfaltNaturalytelse;
    }
}
