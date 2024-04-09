package no.nav.folketrygdloven.fpkalkulus.kontrakt;

import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import jakarta.validation.constraints.Size;

import no.nav.folketrygdloven.kalkulus.felles.v1.KalkulatorInputDto;
import no.nav.folketrygdloven.kalkulus.håndtering.v1.HåndterBeregningDto;

/**
 * Spesifikasjon for å løse avklaringsbehov for en behandling.
 */
public record HåndterBeregningRequestDto(@Valid @NotNull UUID behandlingUuid,
                                         @Valid @NotNull KalkulatorInputDto kalkulatorInput,
                                         @Valid @NotNull @Size(min = 1) List<HåndterBeregningDto> håndterBeregningDtoList) {}
