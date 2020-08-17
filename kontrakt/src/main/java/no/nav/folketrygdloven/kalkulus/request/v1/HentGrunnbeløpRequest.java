package no.nav.folketrygdloven.kalkulus.request.v1;

import java.time.LocalDate;
import java.util.UUID;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

public class HentGrunnbeløpRequest {

    @JsonProperty(value = "dato", required = true)
    @Valid
    @NotNull
    private LocalDate dato;

    @JsonProperty(value = "eksternReferanse", required = true)
    @Valid
    @NotNull
    private UUID eksternReferanse;

    protected HentGrunnbeløpRequest() {
        // default ctor
    }

    public HentGrunnbeløpRequest(@Valid @NotNull LocalDate dato, @Valid @NotNull UUID eksternReferanse) {
        this.dato = dato;
        this.eksternReferanse = eksternReferanse;
    }

    public LocalDate getDato() {
        return dato;
    }

    public UUID getEksternReferanse() {
        return eksternReferanse;
    }
}
