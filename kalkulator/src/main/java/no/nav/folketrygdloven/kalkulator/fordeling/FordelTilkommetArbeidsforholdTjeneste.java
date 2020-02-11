package no.nav.folketrygdloven.kalkulator.fordeling;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.opptjening.OpptjeningAktivitetType;

import java.time.LocalDate;
import java.util.Objects;

public final class FordelTilkommetArbeidsforholdTjeneste {

    private FordelTilkommetArbeidsforholdTjeneste() {
        // Skjuler default
    }

    public static boolean erNyttArbeidsforhold(BeregningsgrunnlagPrStatusOgAndelDto andel,
                                               BeregningAktivitetAggregatDto aktivitetAggregat,
                                               LocalDate skjæringstidspunkt) {
        if (!andel.getBgAndelArbeidsforhold().isPresent()) {
            return false;
        }
        BGAndelArbeidsforholdDto arbeidsforhold = andel.getBgAndelArbeidsforhold().get();
        var beregningAktiviteter = aktivitetAggregat.getBeregningAktiviteter();
        return beregningAktiviteter.stream()
                .filter(beregningAktivitet -> !beregningAktivitet.getPeriode().getTomDato().isBefore(skjæringstidspunkt.minusDays(1)))
                .filter(beregningAktivitet -> !beregningAktivitet.getPeriode().getFomDato().isAfter(skjæringstidspunkt.minusDays(1)))
                .noneMatch(
                        beregningAktivitet -> matcherReferanse(arbeidsforhold, beregningAktivitet) && matcherArbeidsgiver(arbeidsforhold, beregningAktivitet));
    }

    private static boolean matcherReferanse(BGAndelArbeidsforholdDto arbeidsforhold, BeregningAktivitetDto beregningAktivitet) {
        String andelRef = arbeidsforhold.getArbeidsforholdRef().getReferanse();
        String aktivitetRef = beregningAktivitet.getArbeidsforholdRef().getReferanse();
        return Objects.equals(andelRef, aktivitetRef);
    }

    private static boolean matcherArbeidsgiver(BGAndelArbeidsforholdDto arbeidsforhold, BeregningAktivitetDto beregningAktivitet) {
        return Objects.equals(arbeidsforhold.getArbeidsgiver(), beregningAktivitet.getArbeidsgiver());
    }

    public static boolean erNyFLSNAndel(BeregningsgrunnlagPrStatusOgAndelDto andel, BeregningAktivitetAggregatDto aktivitetAggregat, LocalDate skjæringstidspunkt) {
        if (andel.getAktivitetStatus().erFrilanser()) {
            return erNyAndelMedType(aktivitetAggregat, skjæringstidspunkt, OpptjeningAktivitetType.FRILANS);
        }
        if (andel.getAktivitetStatus().erSelvstendigNæringsdrivende()) {
            return erNyAndelMedType(aktivitetAggregat, skjæringstidspunkt, OpptjeningAktivitetType.NÆRING);
        }
        return false;
    }

    private static boolean erNyAndelMedType(BeregningAktivitetAggregatDto beregningAktivitetAggregat, LocalDate skjæringstidspunkt, OpptjeningAktivitetType opptjeningAktivitetType) {
        return beregningAktivitetAggregat.getBeregningAktiviteter().stream()
                .filter(beregningAktivitet -> !beregningAktivitet.getPeriode().getTomDato().isBefore(skjæringstidspunkt.minusDays(1)))
                .noneMatch(
                        beregningAktivitet -> opptjeningAktivitetType.equals(beregningAktivitet.getOpptjeningAktivitetType()));
    }

}
