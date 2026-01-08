package no.nav.foreldrepenger.kalkulus.kontrakt.response.beregningsgrunnlag.besteberegning;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import no.nav.foreldrepenger.kalkulus.kontrakt.typer.Beløp;
import no.nav.foreldrepenger.kalkulus.kontrakt.typer.InternArbeidsforholdRefDto;
import no.nav.foreldrepenger.kalkulus.kontrakt.typer.Periode;
import no.nav.folketrygdloven.kalkulus.kodeverk.OpptjeningAktivitetType;
import no.nav.foreldrepenger.kalkulus.kontrakt.typer.Arbeidsgiver;

/**
 * Responsobjekt som viser en koblings besteberegningsgrunnlag
 */
public record BesteberegningGrunnlagDto(@NotNull @Size(max=6) List<@Valid BesteberegningMånedDto> seksBesteMåneder, @Valid @NotNull Beløp avvikFørsteOgTredjeLedd){

    public record BesteberegningMånedDto(@Valid @NotNull Periode periode, @NotNull @Size(max=100) List<@Valid BesteberegningInntektDto> inntekter){}

    public record BesteberegningInntektDto(@Valid @NotNull OpptjeningAktivitetType opptjeningAktiviteterDto, @Valid @NotNull Beløp inntekt,
                                           @Valid Arbeidsgiver arbeidsgiver, @Valid InternArbeidsforholdRefDto internArbeidsforholdRefDto){}
}

