package no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.inntektsgrunnlag;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import java.math.BigDecimal;

import javax.validation.Valid;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.folketrygdloven.kalkulus.kodeverk.InntektAktivitetType;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = JsonInclude.Include.NON_ABSENT, content = JsonInclude.Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = NONE, setterVisibility = NONE, isGetterVisibility = NONE, creatorVisibility = NONE)
public class InntektsgrunnlagInntektDto {

    @Valid
    @NotNull
    @JsonProperty(value = "inntektAktivitetType")
    private InntektAktivitetType inntektAktivitetType;

    @Valid
    @JsonProperty("beløp")
    @Digits(integer = 8, fraction = 2)
    @DecimalMin("0.00")
    @DecimalMax("10000000.00")
    private BigDecimal beløp;

    public InntektsgrunnlagInntektDto() {
    }

    public InntektsgrunnlagInntektDto(@Valid @NotNull InntektAktivitetType inntektAktivitetType,
                                      @Valid @Digits(integer = 8, fraction = 2) @DecimalMin("0.00") @DecimalMax("10000000.00") BigDecimal beløp) {
        this.inntektAktivitetType = inntektAktivitetType;
        this.beløp = beløp;
    }

    public BigDecimal getBeløp() {
        return beløp;
    }

    public InntektAktivitetType getInntektAktivitetType() {
        return inntektAktivitetType;
    }
}
