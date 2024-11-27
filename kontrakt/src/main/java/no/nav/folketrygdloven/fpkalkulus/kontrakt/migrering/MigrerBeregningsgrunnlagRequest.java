package no.nav.folketrygdloven.fpkalkulus.kontrakt.migrering;

import jakarta.validation.Valid;

import jakarta.validation.constraints.NotNull;

import no.nav.folketrygdloven.fpkalkulus.kontrakt.FpkalkulusYtelser;
import no.nav.folketrygdloven.kalkulus.felles.v1.PersonIdent;
import no.nav.folketrygdloven.kalkulus.felles.v1.Saksnummer;

import java.util.UUID;

public record MigrerBeregningsgrunnlagRequest(@Valid @NotNull Saksnummer saksnummer,
                                              @Valid @NotNull UUID behandlingUuid,
                                              @Valid @NotNull PersonIdent aktør,
                                              @Valid @NotNull FpkalkulusYtelser ytelseSomSkalBeregnes,
                                              @Valid UUID originalBehandlingUuid,
                                              @Valid @NotNull BeregningsgrunnlagGrunnlagMigreringDto grunnlag) {}
