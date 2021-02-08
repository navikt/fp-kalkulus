package no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.inntektsgrunnlag;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = JsonInclude.Include.NON_ABSENT, content = JsonInclude.Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = NONE, setterVisibility = NONE, isGetterVisibility = NONE, creatorVisibility = NONE)
public class InntektsgrunnlagDto {

    @Valid
    @NotNull
    @JsonProperty(value = "måneder")
    @Size(max = 12)
    private List<InntektsgrunnlagMånedDto> måneder;

    public InntektsgrunnlagDto() {
    }

    public InntektsgrunnlagDto(@Valid @NotNull @Min(0) @Max(12) List<InntektsgrunnlagMånedDto> måneder) {
        this.måneder = måneder;
    }

    public List<InntektsgrunnlagMånedDto> getMåneder() {
        return måneder;
    }
}
