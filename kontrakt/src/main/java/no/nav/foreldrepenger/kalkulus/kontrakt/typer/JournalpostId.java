package no.nav.foreldrepenger.kalkulus.kontrakt.typer;

import com.fasterxml.jackson.annotation.JsonValue;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record JournalpostId(
    @JsonValue
    @Pattern(regexp = "^[\\p{L}\\p{N}_\\.\\-:|]+$", message = "JournalpostId [${validatedValue}] matcher ikke tillatt pattern")
    @NotNull String journalpostId) {

}
