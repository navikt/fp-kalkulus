package no.nav.foreldrepenger.kalkulus.kontrakt.request;

import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import no.nav.foreldrepenger.kalkulus.kontrakt.typer.Saksnummer;

/**
 * Spesifikasjon for å hente hvilke steg en kobling eller dens originalkobling har vært innom.
 */
public record EnkelGrunnlagTilstanderRequestDto(@Valid @NotNull Saksnummer saksnummer,
                                                @Valid @NotNull UUID behandlingUuid,
                                                @Valid UUID originalBehandlingUuid) {}
