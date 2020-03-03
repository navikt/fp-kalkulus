package no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = JsonInclude.Include.NON_ABSENT, content = JsonInclude.Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = NONE, setterVisibility = NONE, isGetterVisibility = NONE, creatorVisibility = NONE)
public class RefusjonskravSomKommerForSentDto {

    @JsonProperty(value = "arbeidsgiverId")
    private String arbeidsgiverId;

    @JsonProperty(value = "arbeidsgiverVisningsnavn")
    private String arbeidsgiverVisningsnavn;

    @JsonProperty(value = "erRefusjonskravGyldig")
    private Boolean erRefusjonskravGyldig;

    public String getArbeidsgiverId() {
        return arbeidsgiverId;
    }

    public void setArbeidsgiverId(String arbeidsgiverId) {
        this.arbeidsgiverId = arbeidsgiverId;
    }

    public String getArbeidsgiverVisningsnavn() {
        return arbeidsgiverVisningsnavn;
    }

    public void setArbeidsgiverVisningsnavn(String arbeidsgiverVisningsnavn) {
        this.arbeidsgiverVisningsnavn = arbeidsgiverVisningsnavn;
    }

    public Boolean getErRefusjonskravGyldig() {
        return erRefusjonskravGyldig;
    }

    public void setErRefusjonskravGyldig(Boolean erRefusjonskravGyldig) {
        this.erRefusjonskravGyldig = erRefusjonskravGyldig;
    }
}
