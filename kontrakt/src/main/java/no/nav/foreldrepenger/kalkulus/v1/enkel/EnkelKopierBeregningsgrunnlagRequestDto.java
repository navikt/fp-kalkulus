package no.nav.foreldrepenger.kalkulus.v1.enkel;

import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningSteg;
import no.nav.foreldrepenger.kalkulus.v1.KalkulatorInputDto;
import no.nav.foreldrepenger.kalkulus.v1.Saksnummer;

/**
 * Spesifikasjon for å kopiere grunnlag fra et gitt steg til en ny kobling.
 * Merk at kalkulatorInput her er input til behandlingUuid, ikke originalBehandlingUuid
 */
public record EnkelKopierBeregningsgrunnlagRequestDto(@Valid @NotNull Saksnummer saksnummer,
                                                      @Valid @NotNull UUID behandlingUuid,
                                                      @Valid @NotNull UUID originalBehandlingUuid,
                                                      @Valid @NotNull BeregningSteg steg,
                                                      @Valid KalkulatorInputDto kalkulatorInput) {}
