package no.nav.foreldrepenger.kalkulus.kontrakt.request;

import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import no.nav.foreldrepenger.kalkulus.kontrakt.request.håndtering.HåndterBeregningDto;
import no.nav.foreldrepenger.kalkulus.kontrakt.request.input.KalkulatorInputDto;
import no.nav.foreldrepenger.kalkulus.kontrakt.typer.Saksnummer;

/**
 * Spesifikasjon for å løse avklaringsbehov for en behandling.
 */
public record EnkelHåndterBeregningRequestDto(
    @Valid @NotNull UUID behandlingUuid,
    @Valid @NotNull Saksnummer saksnummer,
    @Valid @NotNull KalkulatorInputDto kalkulatorInput,
    @Valid @NotNull HåndterBeregningDto håndterBeregningDto) {}
