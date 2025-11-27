package no.nav.foreldrepenger.kalkulus.v1.enkel;

import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import no.nav.foreldrepenger.kalkulus.v1.HåndterBeregningDto;
import no.nav.foreldrepenger.kalkulus.v1.KalkulatorInputDto;
import no.nav.foreldrepenger.kalkulus.v1.Saksnummer;

/**
 * Spesifikasjon for å løse avklaringsbehov for en behandling.
 */
public record EnkelHåndterBeregningRequestDto(@Valid @NotNull UUID behandlingUuid,
                                              @Valid @NotNull Saksnummer saksnummer,
                                              @Valid @NotNull KalkulatorInputDto kalkulatorInput,
                                              @NotNull @Size(min = 1) List<@Valid HåndterBeregningDto> håndterBeregningDtoList) {}
