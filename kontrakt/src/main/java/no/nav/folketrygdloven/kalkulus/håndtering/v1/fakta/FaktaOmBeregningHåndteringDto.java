package no.nav.folketrygdloven.kalkulus.håndtering.v1.fakta;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.folketrygdloven.kalkulus.håndtering.v1.HåndterBeregningDto;

public class FaktaOmBeregningHåndteringDto extends HåndterBeregningDto {

    @JsonProperty("fakta")
    @Valid
    @NotNull
    private FaktaBeregningLagreDto fakta;

    public FaktaOmBeregningHåndteringDto(@Valid @NotNull FaktaBeregningLagreDto fakta) {
        this.fakta = fakta;
    }

    public FaktaBeregningLagreDto getFakta() {
        return fakta;
    }
}
