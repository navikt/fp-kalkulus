package no.nav.folketrygdloven.kalkulus.håndtering.v1.foreslå;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

public class FastsettBruttoBeregningsgrunnlagSNforNyIArbeidslivetDto {

    @JsonProperty("bruttoBeregningsgrunnlag")
    @Valid
    @NotNull
    private Integer bruttoBeregningsgrunnlag;

    public FastsettBruttoBeregningsgrunnlagSNforNyIArbeidslivetDto(Integer bruttoBeregningsgrunnlag) {
        this.bruttoBeregningsgrunnlag = bruttoBeregningsgrunnlag;
    }

    public Integer getBruttoBeregningsgrunnlag() {
        return bruttoBeregningsgrunnlag;
    }
}
