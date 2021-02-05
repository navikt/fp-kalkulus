package no.nav.folketrygdloven.kalkulator.steg.fordeling.aksjonpunkt;

import java.time.LocalDate;
import java.util.Objects;

import no.nav.folketrygdloven.kalkulator.felles.BeregningstidspunktTjeneste;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.AndelKilde;
import no.nav.folketrygdloven.kalkulus.kodeverk.FagsakYtelseType;

public final class FordelTilkommetArbeidsforholdTjeneste {

    private FordelTilkommetArbeidsforholdTjeneste() {
        // Skjuler default
    }

    public static boolean erAktivitetLagtTilIPeriodisering(BeregningsgrunnlagPrStatusOgAndelDto andel) {
        return andel.getKilde().equals(AndelKilde.PROSESS_PERIODISERING) || andel.getKilde().equals(AndelKilde.PROSESS_PERIODISERING_TILKOMMET_INNTEKT);
    }

    public static boolean erNyttArbeidsforhold(Arbeidsgiver arbeidsgiver,
                                               InternArbeidsforholdRefDto arbeidsforholdRef,
                                               BeregningAktivitetAggregatDto aktivitetAggregat,
                                               LocalDate skjæringstidspunkt) {
        var beregningAktiviteter = aktivitetAggregat.getBeregningAktiviteter();
        return beregningAktiviteter.stream()
                .filter(beregningAktivitet -> erIkkeAktivPåSisteAktivitetsdato(skjæringstidspunkt, beregningAktivitet))
                .noneMatch(
                        beregningAktivitet -> arbeidsforholdRef.gjelderFor(beregningAktivitet.getArbeidsforholdRef()) && matcherArbeidsgiver(arbeidsgiver, beregningAktivitet));
    }

    private static boolean erIkkeAktivPåSisteAktivitetsdato(LocalDate skjæringstidspunkt, BeregningAktivitetDto beregningAktivitet) {
        LocalDate beregningstidspunkt = BeregningstidspunktTjeneste.finnBeregningstidspunkt(skjæringstidspunkt);
        return !beregningAktivitet.getPeriode().getTomDato().isBefore(beregningstidspunkt) &&
                !beregningAktivitet.getPeriode().getFomDato().isAfter(beregningstidspunkt);
    }

    private static boolean matcherArbeidsgiver(Arbeidsgiver arbeidsgiver, BeregningAktivitetDto beregningAktivitet) {
        return Objects.equals(arbeidsgiver, beregningAktivitet.getArbeidsgiver());
    }

}
