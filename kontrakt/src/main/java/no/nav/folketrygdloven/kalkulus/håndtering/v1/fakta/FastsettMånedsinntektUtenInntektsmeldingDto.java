package no.nav.folketrygdloven.kalkulus.håndtering.v1.fakta;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

public class FastsettMånedsinntektUtenInntektsmeldingDto {

    @JsonProperty("andelListe")
    @Valid
    @NotNull
    private List<FastsettMånedsinntektUtenInntektsmeldingAndelDto> andelListe;

    public FastsettMånedsinntektUtenInntektsmeldingDto() {
    }

    public FastsettMånedsinntektUtenInntektsmeldingDto(List<FastsettMånedsinntektUtenInntektsmeldingAndelDto> andelListe) {
        this.andelListe = andelListe;
    }

    public List<FastsettMånedsinntektUtenInntektsmeldingAndelDto> getAndelListe() {
        return andelListe;
    }

    public void setAndelListe(List<FastsettMånedsinntektUtenInntektsmeldingAndelDto> andelListe) {
        this.andelListe = andelListe;
    }
}
