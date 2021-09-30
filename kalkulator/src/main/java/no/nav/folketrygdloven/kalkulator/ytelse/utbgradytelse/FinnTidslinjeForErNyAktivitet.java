package no.nav.folketrygdloven.kalkulator.ytelse.utbgradytelse;

import static no.nav.folketrygdloven.kalkulator.ytelse.utbgradytelse.AktivitetStatusMatcher.matcherStatusEllerIkkeYrkesaktiv;
import static no.nav.fpsak.tidsserie.LocalDateInterval.TIDENES_ENDE;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.svp.UtbetalingsgradArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.fpsak.tidsserie.LocalDateSegment;
import no.nav.fpsak.tidsserie.LocalDateTimeline;
import no.nav.fpsak.tidsserie.StandardCombinators;

class FinnTidslinjeForErNyAktivitet {


    /** Finner tidslinje for perioder der gitt arbeidsforhold har en matchende andel i beregningsgrunnlaget.
     * @param vlBeregningsgrunnlag Beregningsgrunnlag
     * @param utbetalingsgradArbeidsforhold Arbeidsforhold
     * @return Tidslinje som angir om aktivitet ikke eksisterer (er ny) i beregningsgrunnlaget
     */
    static LocalDateTimeline<Boolean> finnTidslinjeForNyAktivitet(BeregningsgrunnlagDto vlBeregningsgrunnlag,
                                                     UtbetalingsgradArbeidsforholdDto utbetalingsgradArbeidsforhold) {
        Arbeidsgiver tilretteleggingArbeidsgiver = utbetalingsgradArbeidsforhold.getArbeidsgiver().orElse(null);
        InternArbeidsforholdRefDto tilretteleggingArbeidsforholdRef = utbetalingsgradArbeidsforhold.getInternArbeidsforholdRef();

        var eksisterendeAndelSegmenter = vlBeregningsgrunnlag.getBeregningsgrunnlagPerioder().stream()
                .filter(p -> p.getBeregningsgrunnlagPrStatusOgAndelList()
                        .stream().anyMatch(a -> matcherStatusEllerIkkeYrkesaktiv(a.getAktivitetStatus(), utbetalingsgradArbeidsforhold.getUttakArbeidType()) &&
                                a.getBgAndelArbeidsforhold().map(bgAndelArbeidsforhold -> bgAndelArbeidsforhold.getArbeidsgiver().equals(tilretteleggingArbeidsgiver) &&
                                        bgAndelArbeidsforhold.getArbeidsforholdRef().gjelderFor(tilretteleggingArbeidsforholdRef)).orElse(true)))
                .map(p -> new LocalDateSegment<>(p.getBeregningsgrunnlagPeriodeFom(), p.getBeregningsgrunnlagPeriodeTom(), false))
                .collect(Collectors.toList());
        LocalDateTimeline<Boolean> eksisterendeAndelTidslinje = new LocalDateTimeline<>(eksisterendeAndelSegmenter);
        return new LocalDateTimeline<>(vlBeregningsgrunnlag.getSkjæringstidspunkt(), TIDENES_ENDE, true)
                .crossJoin(eksisterendeAndelTidslinje, StandardCombinators::coalesceRightHandSide);
    }

}
