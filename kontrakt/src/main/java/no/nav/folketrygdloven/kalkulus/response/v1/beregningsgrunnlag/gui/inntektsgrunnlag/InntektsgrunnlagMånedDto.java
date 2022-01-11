package no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.inntektsgrunnlag;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import java.time.LocalDate;
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
public class InntektsgrunnlagMånedDto {

    @Valid
    @NotNull
    @JsonProperty(value = "fom")
    private LocalDate fom;

    @Valid
    @NotNull
    @JsonProperty(value = "tom")
    private LocalDate tom;

    @Valid
    @JsonProperty(value = "inntekter")
    @Size(max = 100)
    private List<InntektsgrunnlagInntektDto> inntekter;

    public InntektsgrunnlagMånedDto() {
    }

    public InntektsgrunnlagMånedDto(@Valid @NotNull LocalDate fom,
                                    @Valid @NotNull LocalDate tom,
                                    @Valid @NotNull @Min(0) @Max(Integer.MAX_VALUE) List<InntektsgrunnlagInntektDto> inntekter) {
        this.fom = fom;
        this.tom = tom;
        this.inntekter = inntekter;
    }

    public LocalDate getFom() {
        return fom;
    }

    public LocalDate getTom() {
        return tom;
    }

    public List<InntektsgrunnlagInntektDto> getInntekter() {
        return inntekter;
    }
}
