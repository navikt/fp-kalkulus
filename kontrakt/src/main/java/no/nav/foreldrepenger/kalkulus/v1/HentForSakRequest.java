package no.nav.foreldrepenger.kalkulus.v1;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

public class HentForSakRequest {

    @JsonProperty(value = "saksnummer", required = true)
    @Valid
    @NotNull
    private Saksnummer saksnummer;


    protected HentForSakRequest() {
        // default ctor
    }

    public HentForSakRequest(@Valid @NotNull Saksnummer saksnummer) {
        this.saksnummer = saksnummer;
    }

    public Saksnummer getSaksnummer() {
        return saksnummer;
    }

}
