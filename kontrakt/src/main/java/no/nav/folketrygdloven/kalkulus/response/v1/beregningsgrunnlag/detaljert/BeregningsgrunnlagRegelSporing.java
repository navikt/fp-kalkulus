package no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.detaljert;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagRegelType;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = Include.NON_ABSENT, content = Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = NONE, setterVisibility = NONE, isGetterVisibility = NONE, creatorVisibility = NONE)
public class BeregningsgrunnlagRegelSporing {

    @JsonProperty(value = "regelEvaluering")
    @Valid
    private String regelEvaluering;

    @JsonProperty(value = "regelInput")
    @Valid
    @NotNull
    private String regelInput;

    @JsonProperty(value = "regelType")
    @Valid
    @NotNull
    private BeregningsgrunnlagRegelType regelType;

    public BeregningsgrunnlagRegelSporing() {
    }

    public BeregningsgrunnlagRegelSporing(String regelEvaluering, @NotNull String regelInput, @NotNull BeregningsgrunnlagRegelType regelType) {
        this.regelEvaluering = regelEvaluering;
        this.regelInput = regelInput;
        this.regelType = regelType;
    }

    public String getRegelEvaluering() {
        return regelEvaluering;
    }

    public String getRegelInput() {
        return regelInput;
    }

    public BeregningsgrunnlagRegelType getRegelType() {
        return regelType;
    }
}
