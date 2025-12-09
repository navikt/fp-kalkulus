package no.nav.foreldrepenger.kalkulus.request;

import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningSteg;
import no.nav.folketrygdloven.kalkulus.kodeverk.FagsakYtelseType;
import no.nav.foreldrepenger.kalkulus.request.input.KalkulatorInputDto;
import no.nav.foreldrepenger.kalkulus.typer.PersonIdent;
import no.nav.foreldrepenger.kalkulus.typer.Saksnummer;

/**
 * Spesifikasjon for å beregne basert på input.
 */
public record EnkelBeregnRequestDto(@Valid @NotNull Saksnummer saksnummer,
                                    @Valid @NotNull UUID behandlingUuid,
                                    @Valid @NotNull PersonIdent aktør,
                                    @Valid @NotNull FagsakYtelseType ytelseSomSkalBeregnes,
                                    @Valid @NotNull BeregningSteg stegType,
                                    @Valid @NotNull KalkulatorInputDto kalkulatorInput,
                                    @Valid UUID originalBehandlingUuid) {}
