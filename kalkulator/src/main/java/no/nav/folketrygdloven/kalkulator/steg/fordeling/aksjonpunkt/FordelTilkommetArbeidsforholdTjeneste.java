package no.nav.folketrygdloven.kalkulator.steg.fordeling.aksjonpunkt;

import java.time.LocalDate;
import java.util.Objects;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.modell.virksomhet.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.AktivitetStatus;

public final class FordelTilkommetArbeidsforholdTjeneste {

    private FordelTilkommetArbeidsforholdTjeneste() {
        // Skjuler default
    }

    public static boolean erNyAktivitet(BeregningsgrunnlagPrStatusOgAndelDto andel,
                                        BeregningAktivitetAggregatDto aktivitetAggregat,
                                        LocalDate skjæringstidspunkt) {
        if (andel.getLagtTilAvSaksbehandler() || andel.getAktivitetStatus().equals(AktivitetStatus.BRUKERS_ANDEL)) {
            return false;
        }
        if (andel.getBgAndelArbeidsforhold().isEmpty() || andel.getArbeidsgiver().isEmpty() || andel.getArbeidsforholdRef().isEmpty()) {
            return erNyAktivitetSomIkkeErArbeid(andel, aktivitetAggregat, skjæringstidspunkt);
        }
        return erNyttArbeidsforhold(andel.getArbeidsgiver().get(), andel.getArbeidsforholdRef().get(), aktivitetAggregat, skjæringstidspunkt);
    }

    private static Boolean erNyAktivitetSomIkkeErArbeid(BeregningsgrunnlagPrStatusOgAndelDto andel, BeregningAktivitetAggregatDto aktivitetAggregat, LocalDate skjæringstidspunkt) {
        if (!andel.getAktivitetStatus().erArbeidstaker()) {
            var beregningAktiviteter = aktivitetAggregat.getBeregningAktiviteter();
            return beregningAktiviteter.stream()
                    .filter(beregningAktivitet -> erIkkeAktivDagenFørSkjæringstidspunktet(skjæringstidspunkt, beregningAktivitet))
                    .noneMatch(b -> MapAktivitetstatusTilOpptjeningAktivitetType.map(andel.getAktivitetStatus()).equals(b.getOpptjeningAktivitetType()));
        }
        return false;
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
