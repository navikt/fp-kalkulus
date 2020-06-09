package no.nav.folketrygdloven.kalkulator.fordeling;

import java.time.LocalDate;
import java.util.Objects;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.opptjening.OpptjeningAktivitetType;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.modell.virksomhet.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.ArbeidType;

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
        return erNyttArbeidsforhold(andel.getArbeidsgiver().get(), andel.getArbeidsforholdRef().get(), aktivitetAggregat, skjæringstidspunkt) && !erAutomatiskFordelt(andel);
    }

    private static boolean erAutomatiskFordelt(BeregningsgrunnlagPrStatusOgAndelDto andel) {
        return andel.getInntektskategori() != null && andel.getFordeltPrÅr() != null;
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

    public static boolean erNyttArbeidsforhold(YrkesaktivitetDto yrkesaktivitetDto,
                                               BeregningAktivitetAggregatDto aktivitetAggregat,
                                               LocalDate skjæringstidspunkt) {
        if (!ArbeidType.ORDINÆRT_ARBEIDSFORHOLD.equals(yrkesaktivitetDto.getArbeidType())) {
            return false;
        }
        var beregningAktiviteter = aktivitetAggregat.getBeregningAktiviteter();
        return beregningAktiviteter.stream()
                .filter(beregningAktivitet -> !beregningAktivitet.getPeriode().getTomDato().isBefore(skjæringstidspunkt.minusDays(1)))
                .filter(beregningAktivitet -> !beregningAktivitet.getPeriode().getFomDato().isAfter(skjæringstidspunkt.minusDays(1)))
                .noneMatch(
                        beregningAktivitet -> yrkesaktivitetDto.getArbeidsforholdRef().gjelderFor(beregningAktivitet.getArbeidsforholdRef()) && matcherArbeidsgiver(yrkesaktivitetDto.getArbeidsgiver(), beregningAktivitet));
    }

    public static boolean erNyttArbeidsforhold(Arbeidsgiver arbeidsgiver,
                                               InternArbeidsforholdRefDto arbeidsforholdRef,
                                               BeregningAktivitetAggregatDto aktivitetAggregat,
                                               LocalDate skjæringstidspunkt) {
        var beregningAktiviteter = aktivitetAggregat.getBeregningAktiviteter();
        return beregningAktiviteter.stream()
                .filter(beregningAktivitet -> erIkkeAktivDagenFørSkjæringstidspunktet(skjæringstidspunkt, beregningAktivitet))
                .noneMatch(
                        beregningAktivitet -> matcherReferanse(arbeidsforholdRef, beregningAktivitet) && matcherArbeidsgiver(arbeidsgiver, beregningAktivitet));
    }

    private static boolean erIkkeAktivDagenFørSkjæringstidspunktet(LocalDate skjæringstidspunkt, BeregningAktivitetDto beregningAktivitet) {
        return !beregningAktivitet.getPeriode().getTomDato().isBefore(skjæringstidspunkt.minusDays(1)) &&
                !beregningAktivitet.getPeriode().getFomDato().isAfter(skjæringstidspunkt.minusDays(1));
    }

    private static boolean matcherReferanse(InternArbeidsforholdRefDto internArbeidsforholdRef, BeregningAktivitetDto beregningAktivitet) {
        String aktivitetRef = beregningAktivitet.getArbeidsforholdRef().getReferanse();
        return Objects.equals(internArbeidsforholdRef.getReferanse(), aktivitetRef);
    }

    private static boolean matcherArbeidsgiver(Arbeidsgiver arbeidsgiver, BeregningAktivitetDto beregningAktivitet) {
        return Objects.equals(arbeidsgiver, beregningAktivitet.getArbeidsgiver());
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
