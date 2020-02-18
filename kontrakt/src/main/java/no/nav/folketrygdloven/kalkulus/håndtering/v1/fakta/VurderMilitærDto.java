package no.nav.folketrygdloven.kalkulus.håndtering.v1.fakta;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

public class VurderMilitærDto {

    @JsonProperty("harMilitaer")
    @Valid
    @NotNull
    private Boolean harMilitaer;

    public VurderMilitærDto(@Valid @NotNull Boolean harMilitaer) {
        this.harMilitaer = harMilitaer;
    }

    public Boolean getHarMilitaer() {
        return harMilitaer;
    }
}
