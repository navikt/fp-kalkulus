package no.nav.foreldrepenger.kalkulus.kontrakt.request;

import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningSteg;

/**
 * Responsobjekt som viser en koblings besteberegningsgrunnlag
 */
public record TilgjengeligeTilstanderDto(@Valid @NotNull TilgjengeligeTilstandDto behandlingMedTilstander, @Valid TilgjengeligeTilstandDto originalbehandlingMedTilstander){

    public record TilgjengeligeTilstandDto(@Valid @NotNull UUID behandlingUuid,
                                         @NotNull @Size(max=15) List<@Valid BeregningSteg> tilgjengeligeSteg){}
}

