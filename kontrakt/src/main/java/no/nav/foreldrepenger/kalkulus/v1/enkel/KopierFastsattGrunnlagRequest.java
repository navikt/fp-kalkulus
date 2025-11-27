package no.nav.foreldrepenger.kalkulus.v1.enkel;

import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import no.nav.foreldrepenger.kalkulus.v1.Saksnummer;

/**
 * Spesifikasjon for å kopiere et fastsatt grunnlag fra originalBehandlingUuid til behandlingUuid.
 */
public record KopierFastsattGrunnlagRequest(@Valid @NotNull Saksnummer saksnummer,
                                            @Valid @NotNull UUID behandlingUuid,
                                            @Valid @NotNull UUID originalBehandlingUuid) {}
