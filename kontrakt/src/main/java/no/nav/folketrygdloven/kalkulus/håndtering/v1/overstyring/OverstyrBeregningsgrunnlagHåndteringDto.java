package no.nav.folketrygdloven.kalkulus.håndtering.v1.overstyring;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_ABSENT;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.folketrygdloven.kalkulus.håndtering.v1.HåndterBeregningDto;
import no.nav.folketrygdloven.kalkulus.håndtering.v1.fakta.FaktaBeregningLagreDto;
import no.nav.folketrygdloven.kalkulus.håndtering.v1.fordeling.FastsettBeregningsgrunnlagAndelDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.HåndteringKode;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = NON_ABSENT, content = NON_EMPTY)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
public class OverstyrBeregningsgrunnlagHåndteringDto extends HåndterBeregningDto {

    public static final String IDENT_TYPE = "6015";

    @JsonProperty("fakta")
    @Valid
    private FaktaBeregningLagreDto fakta;

    @JsonProperty("overstyrteAndeler")
    @Valid
    @Size(min = 1)
    private List<FastsettBeregningsgrunnlagAndelDto> overstyrteAndeler;

    @JsonCreator
    public OverstyrBeregningsgrunnlagHåndteringDto(@JsonProperty("fakta") @Valid FaktaBeregningLagreDto fakta, @JsonProperty("overstyrteAndeler") @Valid @NotNull List<FastsettBeregningsgrunnlagAndelDto> overstyrteAndeler) {
        super(new HåndteringKode(IDENT_TYPE));
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
