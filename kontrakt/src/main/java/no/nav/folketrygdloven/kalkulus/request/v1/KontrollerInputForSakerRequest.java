package no.nav.folketrygdloven.kalkulus.request.v1;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

public class KontrollerInputForSakerRequest {

    @JsonProperty(value = "saksnummer", required = true)
    @Valid
    @NotNull
    private List<String> saksnummer;


    protected KontrollerInputForSakerRequest() {
        // default ctor
    }

    public KontrollerInputForSakerRequest(@Valid @NotNull List<String> saksnummer) {
        this.saksnummer = saksnummer;
    }

    public List<String> getSaksnummer() {
        return saksnummer;
    }

}
