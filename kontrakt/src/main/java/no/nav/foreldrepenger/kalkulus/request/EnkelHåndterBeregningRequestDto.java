package no.nav.foreldrepenger.kalkulus.request;

import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import no.nav.foreldrepenger.kalkulus.request.håndtering.HåndterBeregningDto;
import no.nav.foreldrepenger.kalkulus.request.input.KalkulatorInputDto;
import no.nav.foreldrepenger.kalkulus.typer.Saksnummer;

/**
 * Spesifikasjon for å løse avklaringsbehov for en behandling.
 */
public record EnkelHåndterBeregningRequestDto(@Valid @NotNull UUID behandlingUuid,
                                              @Valid @NotNull Saksnummer saksnummer,
                                              @Valid @NotNull KalkulatorInputDto kalkulatorInput,
                                              @NotNull @Size(min = 1) List<@Valid HåndterBeregningDto> håndterBeregningDtoList) {}
