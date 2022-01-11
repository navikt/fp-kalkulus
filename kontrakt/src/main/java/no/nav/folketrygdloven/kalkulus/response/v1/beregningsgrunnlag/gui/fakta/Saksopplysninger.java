package no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.fakta;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = JsonInclude.Include.NON_ABSENT, content = JsonInclude.Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = NONE, setterVisibility = NONE, isGetterVisibility = NONE, creatorVisibility = NONE)
public class Saksopplysninger {

    @Valid
    @JsonProperty(value = "arbeidsforholdMedLønnsendring")
    @Size
    private List<ArbeidsforholdDto> arbeidsforholdMedLønnsendring;

    @Valid
    @JsonProperty(value = "kortvarigeArbeidsforhold")
    @Size
    private List<ArbeidsforholdDto> kortvarigeArbeidsforhold;

    public List<ArbeidsforholdDto> getArbeidsforholdMedLønnsendring() {
        return arbeidsforholdMedLønnsendring;
    }

    public void setArbeidsforholdMedLønnsendring(List<ArbeidsforholdDto> arbeidsforholdMedLønnsendring) {
        this.arbeidsforholdMedLønnsendring = arbeidsforholdMedLønnsendring;
    }

    public List<ArbeidsforholdDto> getKortvarigeArbeidsforhold() {
        return kortvarigeArbeidsforhold;
    }

    public void setKortvarigeArbeidsforhold(List<ArbeidsforholdDto> kortvarigeArbeidsforhold) {
        this.kortvarigeArbeidsforhold = kortvarigeArbeidsforhold;
    }
}
