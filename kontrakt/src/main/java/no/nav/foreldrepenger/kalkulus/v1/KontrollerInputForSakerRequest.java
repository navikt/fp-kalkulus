package no.nav.foreldrepenger.kalkulus.v1;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

public class KontrollerInputForSakerRequest {

    @JsonProperty(value = "saksnummer", required = true)
    @NotNull
    private List<@Valid Saksnummer> saksnummer;


    protected KontrollerInputForSakerRequest() {
        // default ctor
    }

    public KontrollerInputForSakerRequest(@NotNull List<@Valid Saksnummer> saksnummer) {
        this.saksnummer = saksnummer;
    }

    public List<Saksnummer> getSaksnummer() {
        return saksnummer;
    }

}
