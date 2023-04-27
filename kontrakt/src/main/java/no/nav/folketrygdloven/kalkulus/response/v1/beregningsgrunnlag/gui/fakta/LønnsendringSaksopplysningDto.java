package no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.fakta;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import no.nav.folketrygdloven.kalkulus.kodeverk.LønnsendringScenario;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = JsonInclude.Include.NON_ABSENT, content = JsonInclude.Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = NONE, setterVisibility = NONE, isGetterVisibility = NONE, creatorVisibility = NONE)
public class LønnsendringSaksopplysningDto {

    @Valid
    @JsonProperty(value = "sisteLønnsendringsdato")
    @NotNull
    private LocalDate sisteLønnsendringsdato;

    @Valid
    @JsonProperty(value = "lønnsendringScenario")
    @NotNull
    private LønnsendringScenario lønnsendringScenario;


    @Valid
    @JsonProperty(value = "arbeidsforhold")
    @NotNull
    private ArbeidsforholdDto arbeidsforhold;

    public LønnsendringSaksopplysningDto() {
    }

    public LønnsendringSaksopplysningDto(LocalDate sisteLønnsendringsdato, LønnsendringScenario lønnsendringScenario, ArbeidsforholdDto arbeidsforhold) {
        this.sisteLønnsendringsdato = sisteLønnsendringsdato;
        this.lønnsendringScenario = lønnsendringScenario;
        this.arbeidsforhold = arbeidsforhold;
    }

    public LocalDate getSisteLønnsendringsdato() {
        return sisteLønnsendringsdato;
    }

    public LønnsendringScenario getLønnsendringScenario() {
        return lønnsendringScenario;
    }

    public ArbeidsforholdDto getArbeidsforhold() {
        return arbeidsforhold;
    }
}
