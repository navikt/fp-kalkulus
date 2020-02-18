package no.nav.folketrygdloven.kalkulus.håndtering.v1;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.folketrygdloven.kalkulus.håndtering.v1.fakta.FaktaBeregningLagreDto;
import no.nav.folketrygdloven.kalkulus.håndtering.v1.fordeling.FastsettBeregningsgrunnlagAndelDto;

public class OverstyrBeregningsgrunnlagHåndteringDto extends HåndterBeregningDto {

    @JsonProperty("fakta")
    @Valid
    private FaktaBeregningLagreDto fakta;

    @JsonProperty("overstyrteAndeler")
    @Valid
    @NotNull
    private List<FastsettBeregningsgrunnlagAndelDto> overstyrteAndeler;

    public OverstyrBeregningsgrunnlagHåndteringDto(@Valid FaktaBeregningLagreDto fakta, @Valid @NotNull List<FastsettBeregningsgrunnlagAndelDto> overstyrteAndeler) {
        this.fakta = fakta;
        this.overstyrteAndeler = overstyrteAndeler;
    }

    public FaktaBeregningLagreDto getFakta() {
        return fakta;
    }

    public List<FastsettBeregningsgrunnlagAndelDto> getOverstyrteAndeler() {
        return overstyrteAndeler;
    }

}
