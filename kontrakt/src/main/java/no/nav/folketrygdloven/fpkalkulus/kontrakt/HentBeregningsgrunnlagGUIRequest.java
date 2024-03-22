package no.nav.folketrygdloven.fpkalkulus.kontrakt;

import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import no.nav.folketrygdloven.kalkulus.felles.v1.KalkulatorInputDto;

/**
 * Spesifikasjon for å hente beregningsgrunnlag GUI dto for en behandling.
 */
public record HentBeregningsgrunnlagGUIRequest(@Valid @NotNull UUID behandlingUuid,
                                               @Valid @NotNull KalkulatorInputDto kalkulatorInput) {}
