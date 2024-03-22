package no.nav.folketrygdloven.fpkalkulus.kontrakt;

import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

/**
 * Spesifikasjon for å hente et beregningsgrunnlag for en behandling.
 */
public record HentBeregningsgrunnlagRequestDto(@Valid @NotNull UUID behandlingUuid) {}
