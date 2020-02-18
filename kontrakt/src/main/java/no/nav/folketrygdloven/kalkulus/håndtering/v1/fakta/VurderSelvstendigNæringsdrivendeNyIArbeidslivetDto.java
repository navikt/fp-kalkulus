package no.nav.folketrygdloven.kalkulus.håndtering.v1.fakta;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

public class VurderSelvstendigNæringsdrivendeNyIArbeidslivetDto {

    @JsonProperty("erNyIArbeidslivet")
    @Valid
    @NotNull
    private Boolean erNyIArbeidslivet;

    public VurderSelvstendigNæringsdrivendeNyIArbeidslivetDto(Boolean erNyIArbeidslivet) { // NOSONAR
        this.erNyIArbeidslivet = erNyIArbeidslivet;
    }

    public void setErNyIArbeidslivet(Boolean erNyIArbeidslivet) {
        this.erNyIArbeidslivet = erNyIArbeidslivet;
    }

    public Boolean erNyIArbeidslivet() {
        return erNyIArbeidslivet;
    }
}
