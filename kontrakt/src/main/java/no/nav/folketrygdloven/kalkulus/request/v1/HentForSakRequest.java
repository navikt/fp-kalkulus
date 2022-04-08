package no.nav.folketrygdloven.kalkulus.request.v1;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public class HentForSakRequest {

    @JsonProperty(value = "saksnummer", required = true)
    @Pattern(regexp = "^[A-Za-z0-9_.\\-:]+$", message = "'${validatedValue}' matcher ikke tillatt pattern '{value}'")
    @Valid
    @NotNull
    private String saksnummer;


    protected HentForSakRequest() {
        // default ctor
    }

    public HentForSakRequest(@Valid @NotNull String saksnummer) {
        this.saksnummer = saksnummer;
    }

    public String getSaksnummer() {
        return saksnummer;
    }

}
