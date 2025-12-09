package no.nav.foreldrepenger.kalkulus.request;

import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import no.nav.foreldrepenger.kalkulus.typer.Saksnummer;

/**
 * Spesifikasjon for å unikt identifisere en kobling.
 */
public record EnkelFpkalkulusRequestDto(@Valid @NotNull UUID behandlingUuid,
                                        @Valid @NotNull Saksnummer saksnummer) {}
