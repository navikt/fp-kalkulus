package no.nav.folketrygdloven.kalkulus.håndtering.v1;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_ABSENT;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.folketrygdloven.kalkulus.håndtering.v1.fakta.FaktaBeregningLagreDto;
import no.nav.folketrygdloven.kalkulus.håndtering.v1.fordeling.FastsettBeregningsgrunnlagAndelDto;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = NON_ABSENT, content = NON_EMPTY)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
public class OverstyrBeregningsgrunnlagHåndteringDto extends HåndterBeregningDto {

    public static final String IDENT_TYPE = "OVERSTYR_BEREGNINGSGRUNNLAG";

    @JsonProperty("fakta")
    @Valid
    private FaktaBeregningLagreDto fakta;

    @JsonProperty("overstyrteAndeler")
    @Valid
    @NotNull
    private List<FastsettBeregningsgrunnlagAndelDto> overstyrteAndeler;

    public OverstyrBeregningsgrunnlagHåndteringDto() {
        // default ctor
    }

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

    @Override
    public String getIdentType() {
        return IDENT_TYPE;
    }
}
