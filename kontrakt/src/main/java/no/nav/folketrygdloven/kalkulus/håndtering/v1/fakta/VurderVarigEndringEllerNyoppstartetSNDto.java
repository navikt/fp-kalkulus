package no.nav.folketrygdloven.kalkulus.håndtering.v1.fakta;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

public class VurderVarigEndringEllerNyoppstartetSNDto {

    @JsonProperty("erVarigEndretNaering")
    @Valid
    @NotNull
    private boolean erVarigEndretNaering;

    @JsonProperty("bruttoBeregningsgrunnlag")
    @Valid
    @NotNull
    private Integer bruttoBeregningsgrunnlag;

    public VurderVarigEndringEllerNyoppstartetSNDto(boolean erVarigEndretNaering, Integer bruttoBeregningsgrunnlag) {
        this.erVarigEndretNaering = erVarigEndretNaering;
        this.bruttoBeregningsgrunnlag = bruttoBeregningsgrunnlag;
    }

    public boolean getErVarigEndretNaering() {
        return erVarigEndretNaering;
    }

    public Integer getBruttoBeregningsgrunnlag() {
        return bruttoBeregningsgrunnlag;
    }
}
