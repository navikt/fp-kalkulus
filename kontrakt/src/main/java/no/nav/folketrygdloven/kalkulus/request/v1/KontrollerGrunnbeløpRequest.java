package no.nav.folketrygdloven.kalkulus.request.v1;

import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonProperty;

public class KontrollerGrunnbeløpRequest {

    @JsonProperty(value = "koblinger", required = true)
    @Valid
    @NotNull
    @Size(min=1)
    private List<UUID> koblinger;


    @JsonProperty(value = "saksnummer", required = true)
    @NotNull
    @Pattern(regexp = "^[A-Za-z0-9_.\\-:]+$", message = "'${validatedValue}' matcher ikke tillatt pattern '{value}'")
    @Valid
    private String saksnummer;


    protected KontrollerGrunnbeløpRequest() {
        // default ctor
    }

    public KontrollerGrunnbeløpRequest(@Valid @NotNull @Size(min = 1) List<UUID> koblinger,
                                       @JsonProperty(value = "saksnummer", required = true) @NotNull @Pattern(regexp = "^[A-Za-z0-9_.\\-:]+$", message = "'${validatedValue}' matcher ikke tillatt pattern '{value}'") @Valid String saksnummer) {
        this.koblinger = koblinger;
        this.saksnummer = saksnummer;
    }

    public List<UUID> getKoblinger() {
        return koblinger;
    }

    public String getSaksnummer() {
        return saksnummer;
    }
}
