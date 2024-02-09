package no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;


@JsonAutoDetect(getterVisibility= JsonAutoDetect.Visibility.NONE, setterVisibility= JsonAutoDetect.Visibility.NONE, fieldVisibility= JsonAutoDetect.Visibility.ANY)
public class BeregningsgrunnlagPrStatusOgAndelYtelseDto extends BeregningsgrunnlagPrStatusOgAndelDto {
    @Valid
    @JsonProperty("belopFraMeldekortPrMnd")
    @Digits(integer = 8, fraction = 2)
    @DecimalMin("0.00")
    @DecimalMax("10000000.00")
    private BigDecimal belopFraMeldekortPrMnd;

    @Valid
    @JsonProperty("belopFraMeldekortPrAar")
    @Digits(integer = 8, fraction = 2)
    @DecimalMin("0.00")
    @DecimalMax("10000000.00")
    private BigDecimal belopFraMeldekortPrAar;

    @Valid
    @JsonProperty("oppjustertGrunnlag")
    @Digits(integer = 8, fraction = 2)
    @DecimalMin("0.00")
    @DecimalMax("10000000.00")
    private BigDecimal oppjustertGrunnlag;

    public BeregningsgrunnlagPrStatusOgAndelYtelseDto() {
        super();
    }

    public BigDecimal getBelopFraMeldekortPrMnd() {
        return belopFraMeldekortPrMnd;
    }

    public BigDecimal getBelopFraMeldekortPrAar() {
        return belopFraMeldekortPrAar;
    }

    public void setBelopFraMeldekortPrMnd(BigDecimal belopFraMeldekortPrMnd) {
        this.belopFraMeldekortPrMnd = belopFraMeldekortPrMnd;
    }

    public void setBelopFraMeldekortPrAar(BigDecimal belopFraMeldekortPrAar) {
        this.belopFraMeldekortPrAar = belopFraMeldekortPrAar;
    }

    public BigDecimal getOppjustertGrunnlag() {
        return oppjustertGrunnlag;
    }

    public void setOppjustertGrunnlag(BigDecimal oppjustertGrunnlag) {
        this.oppjustertGrunnlag = oppjustertGrunnlag;
    }
}
