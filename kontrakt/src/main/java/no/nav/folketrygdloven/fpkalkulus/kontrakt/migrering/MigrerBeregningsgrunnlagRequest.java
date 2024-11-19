package no.nav.folketrygdloven.fpkalkulus.kontrakt.migrering;

import jakarta.validation.Valid;

import jakarta.validation.constraints.NotNull;

import no.nav.folketrygdloven.fpkalkulus.kontrakt.BeregnRequestDto;

public record MigrerBeregningsgrunnlagRequest(@Valid @NotNull BeregnRequestDto koblingData, @Valid @NotNull BeregningsgrunnlagGrunnlagMigreringDto grunnlag) {}
