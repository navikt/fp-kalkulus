package no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = JsonInclude.Include.NON_ABSENT, content = JsonInclude.Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = NONE, setterVisibility = NONE, isGetterVisibility = NONE, creatorVisibility = NONE)
public class PgiDto {

    @JsonProperty(value = "beløp")
    private BigDecimal beløp;

    @JsonProperty(value = "årstall")
    private Integer årstall;

    public PgiDto(BigDecimal beløp, Integer årstall) {
        this.beløp = beløp;
        this.årstall = årstall;
    }

    public BigDecimal getBeløp() {
        return beløp;
    }

    public Integer getÅrstall() {
        return årstall;
    }
}
