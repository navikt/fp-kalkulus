package no.nav.foreldrepenger.kalkulus.kontrakt.request.håndtering.refusjon;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = JsonInclude.Include.NON_ABSENT, content = JsonInclude.Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = NONE, setterVisibility = NONE, isGetterVisibility = NONE, creatorVisibility = NONE)
public class RefusjonskravForSentDto {

    // TODO Refusjon: Flytte denne til refusjon-mappa
    // TODO Refusjon: Når denne skal puttes inn i den andre dto-en så må vi sjekke om all annotasjonen er riktig

    @Valid
    @JsonProperty(value = "arbeidsgiverIdent")
    @Pattern(regexp = "^[\\p{Graph}\\p{Space}\\p{Sc}\\p{L}\\p{M}\\p{N}]+$", message="'${validatedValue}' matcher ikke tillatt pattern '{regexp}'")
    @NotNull
    private String arbeidsgiverIdent;

    @Valid
    @JsonProperty(value = "erRefusjonskravGyldig")
    private Boolean erRefusjonskravGyldig;

    public String getArbeidsgiverIdent() {
        return arbeidsgiverIdent;
    }

    public void setArbeidsgiverIdent(String arbeidsgiverIdent) {
        this.arbeidsgiverIdent = arbeidsgiverIdent;
    }

    public Boolean getErRefusjonskravGyldig() {
        return erRefusjonskravGyldig;
    }

    public void setErRefusjonskravGyldig(Boolean erRefusjonskravGyldig) {
        this.erRefusjonskravGyldig = erRefusjonskravGyldig;
    }
}
