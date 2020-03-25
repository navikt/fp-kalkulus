package no.nav.folketrygdloven.kalkulator.kontrakt.v1;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

public class ArbeidsgiverOpplysningerDto {

    private final String identifikator;
    private final String navn;
    private LocalDate fødselsdato; // Fødselsdato for privatperson som arbeidsgiver

    public ArbeidsgiverOpplysningerDto(String identifikator, String navn, LocalDate fødselsdato) {
        this.identifikator = identifikator;
        this.navn = navn;
        this.fødselsdato = fødselsdato;
    }

    public ArbeidsgiverOpplysningerDto(String identifikator, String navn) {
        this.identifikator = identifikator;
        this.navn = navn;
    }

    public String getIdentifikator() {
        return identifikator;
    }

    public String getNavn() {
        return navn;
    }

    public LocalDate getFødselsdato() {
        return fødselsdato;
    }

    @Override
    public String toString() {
        return "ArbeidsgiverOpplysningerDto{" +
                "identifikator='" + identifikator + '\'' +
                ", navn='" + navn + '\'' +
                ", fødselsdato=" + fødselsdato +
                '}';
    }
}
