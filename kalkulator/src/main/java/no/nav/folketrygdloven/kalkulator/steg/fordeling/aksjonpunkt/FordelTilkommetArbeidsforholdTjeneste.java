package no.nav.folketrygdloven.kalkulator.steg.fordeling.aksjonpunkt;

import java.time.LocalDate;
import java.util.Objects;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.modell.virksomhet.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.AndelKilde;

public final class FordelTilkommetArbeidsforholdTjeneste {

    private FordelTilkommetArbeidsforholdTjeneste() {
        // Skjuler default
    }

    public static boolean erAktivitetLagtTilIPeriodisering(BeregningsgrunnlagPrStatusOgAndelDto andel) {
        return andel.getKilde().equals(AndelKilde.PROSESS_PERIODISERING);
    }

    public static boolean erNyttArbeidsforhold(Arbeidsgiver arbeidsgiver,
                                               InternArbeidsforholdRefDto arbeidsforholdRef,
                                               BeregningAktivitetAggregatDto aktivitetAggregat,
                                               LocalDate skjæringstidspunkt) {
        var beregningAktiviteter = aktivitetAggregat.getBeregningAktiviteter();
        return beregningAktiviteter.stream()
                .filter(beregningAktivitet -> erIkkeAktivDagenFørSkjæringstidspunktet(skjæringstidspunkt, beregningAktivitet))
                .noneMatch(
                        beregningAktivitet -> arbeidsforholdRef.gjelderFor(beregningAktivitet.getArbeidsforholdRef()) && matcherArbeidsgiver(arbeidsgiver, beregningAktivitet));
    }

    private static boolean erIkkeAktivDagenFørSkjæringstidspunktet(LocalDate skjæringstidspunkt, BeregningAktivitetDto beregningAktivitet) {
        return !beregningAktivitet.getPeriode().getTomDato().isBefore(skjæringstidspunkt.minusDays(1)) &&
                !beregningAktivitet.getPeriode().getFomDato().isAfter(skjæringstidspunkt.minusDays(1));
    }

    private static boolean matcherArbeidsgiver(Arbeidsgiver arbeidsgiver, BeregningAktivitetDto beregningAktivitet) {
        return Objects.equals(arbeidsgiver, beregningAktivitet.getArbeidsgiver());
    }

}
