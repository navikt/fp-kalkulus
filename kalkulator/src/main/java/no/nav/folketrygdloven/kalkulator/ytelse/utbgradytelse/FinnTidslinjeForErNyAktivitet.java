package no.nav.folketrygdloven.kalkulator.ytelse.utbgradytelse;

import static no.nav.folketrygdloven.kalkulator.ytelse.utbgradytelse.AktivitetStatusMatcher.matcherStatusEllerIkkeYrkesaktiv;
import static no.nav.fpsak.tidsserie.LocalDateInterval.TIDENES_ENDE;

import java.util.Optional;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.modell.uttak.UttakArbeidType;
import no.nav.fpsak.tidsserie.LocalDateSegment;
import no.nav.fpsak.tidsserie.LocalDateTimeline;
import no.nav.fpsak.tidsserie.StandardCombinators;

public class FinnTidslinjeForErNyAktivitet {


    /** Finner tidslinje for perioder der gitt arbeidsforhold har en matchende andel i beregningsgrunnlaget.
     * @param vlBeregningsgrunnlag Beregningsgrunnlag
     * @param uttakArbeidType
     * @param internArbeidsforholdRef
     * @param arbeidsgiver
     * @return Tidslinje som angir om aktivitet ikke eksisterer (er ny) i beregningsgrunnlaget
     */
    public static LocalDateTimeline<Boolean> finnTidslinjeForNyAktivitet(BeregningsgrunnlagDto vlBeregningsgrunnlag,
                                                                  UttakArbeidType uttakArbeidType,
                                                                  InternArbeidsforholdRefDto internArbeidsforholdRef,
                                                                  Optional<Arbeidsgiver> arbeidsgiver) {
        Arbeidsgiver tilretteleggingArbeidsgiver = arbeidsgiver.orElse(null);

        var eksisterendeAndelSegmenter = vlBeregningsgrunnlag.getBeregningsgrunnlagPerioder().stream()
                .filter(p -> p.getBeregningsgrunnlagPrStatusOgAndelList()
                        .stream().anyMatch(a -> matcherStatusEllerIkkeYrkesaktiv(a.getAktivitetStatus(), uttakArbeidType) &&
                                a.getBgAndelArbeidsforhold().map(bgAndelArbeidsforhold -> bgAndelArbeidsforhold.getArbeidsgiver().equals(tilretteleggingArbeidsgiver) &&
                                        bgAndelArbeidsforhold.getArbeidsforholdRef().gjelderFor(internArbeidsforholdRef)).orElse(true)))
                .map(p -> new LocalDateSegment<>(p.getBeregningsgrunnlagPeriodeFom(), p.getBeregningsgrunnlagPeriodeTom(), false))
                .collect(Collectors.toList());
        LocalDateTimeline<Boolean> eksisterendeAndelTidslinje = new LocalDateTimeline<>(eksisterendeAndelSegmenter);
        return new LocalDateTimeline<>(vlBeregningsgrunnlag.getSkjæringstidspunkt(), TIDENES_ENDE, true)
                .crossJoin(eksisterendeAndelTidslinje, StandardCombinators::coalesceRightHandSide);
    }

}
