package no.nav.folketrygdloven.fpkalkulus.kontrakt;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import no.nav.folketrygdloven.kalkulus.felles.v1.Saksnummer;

import java.util.UUID;

/**
 * Spesifikasjon for å unikt identifisere en kobling.
 */
public record EnkelFpkalkulusRequestDto(@Valid @NotNull UUID behandlingUuid,
                                        @Valid @NotNull Saksnummer saksnummer) {}
