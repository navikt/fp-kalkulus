package no.nav.folketrygdloven.kalkulus.iay.arbeid.v1;

import java.time.LocalDate;

import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.folketrygdloven.kalkulus.felles.v1.Aktør;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = JsonInclude.Include.ALWAYS, content = JsonInclude.Include.ALWAYS)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
public class ArbeidsgiverOpplysningerDto {

    @JsonProperty(value = "identifikator")
    @Valid
    @NotNull
    private Aktør aktør;

    @JsonProperty(value = "navn")
    @Valid
    @Pattern(regexp = "^[\\p{Graph}\\p{Space}\\p{Sc}\\p{L}\\p{M}\\p{N}]+$", message="'${validatedValue}' matcher ikke tillatt pattern '{regexp}'")
    private String navn;

    @JsonProperty(value = "fødselsdato")
    @Valid
    private LocalDate fødselsdato; // Fødselsdato for privatperson som arbeidsgiver

    public ArbeidsgiverOpplysningerDto() {
        // default ctor
    }

    public ArbeidsgiverOpplysningerDto(Aktør aktør, String navn, LocalDate fødselsdato) {
        this.aktør = aktør;
        this.navn = navn;
        this.fødselsdato = fødselsdato;
    }

    public ArbeidsgiverOpplysningerDto(Aktør aktør, String navn) {
        this.aktør = aktør;
        this.navn = navn;
    }

    public Aktør getAktør() {
        return aktør;
    }

    public String getNavn() {
        return navn;
    }

    public LocalDate getFødselsdato() {
        return fødselsdato;
    }

    @AssertTrue(message = "Aktør av typen orgnr skal ikke ha oppgitt fødselsdato")
    public boolean erGyldig() {
        if (aktør.getErOrganisasjon()) {
            return fødselsdato == null;
        }
        return true;
    }

}
