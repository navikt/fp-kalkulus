package no.nav.folketrygdloven.kalkulus.håndtering.v1.foreslå;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class FastsettBruttoBeregningsgrunnlagDto {

    @JsonProperty("bruttoBeregningsgrunnlag")
    @Valid
    @Min(0)
    @Max(178956970)
    private Integer bruttoBeregningsgrunnlag;

    @JsonCreator
    public FastsettBruttoBeregningsgrunnlagDto(@JsonProperty("bruttoBeregningsgrunnlag") Integer bruttoBeregningsgrunnlag) {
        this.bruttoBeregningsgrunnlag = bruttoBeregningsgrunnlag;
    }

    public Integer getBruttoBeregningsgrunnlag() {
        return bruttoBeregningsgrunnlag;
    }
}
