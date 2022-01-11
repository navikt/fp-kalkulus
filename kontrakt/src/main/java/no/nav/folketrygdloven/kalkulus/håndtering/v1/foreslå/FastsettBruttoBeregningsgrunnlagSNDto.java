package no.nav.folketrygdloven.kalkulus.håndtering.v1.foreslå;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class FastsettBruttoBeregningsgrunnlagSNDto {

    @JsonProperty("bruttoBeregningsgrunnlag")
    @Valid
    @Min(0)
    @Max(Long.MAX_VALUE)
    private Integer bruttoBeregningsgrunnlag;

    @JsonCreator
    public FastsettBruttoBeregningsgrunnlagSNDto(@JsonProperty("bruttoBeregningsgrunnlag") Integer bruttoBeregningsgrunnlag) {
        this.bruttoBeregningsgrunnlag = bruttoBeregningsgrunnlag;
    }

    public Integer getBruttoBeregningsgrunnlag() {
        return bruttoBeregningsgrunnlag;
    }
}
