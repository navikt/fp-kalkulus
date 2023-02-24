package no.nav.folketrygdloven.kalkulator.steg.fordeling.tilkommetInntekt;

import static no.nav.folketrygdloven.kalkulus.kodeverk.ArbeidType.FRILANSER_OPPDRAGSTAKER_MED_MER;
import static no.nav.fpsak.tidsserie.LocalDateInterval.TIDENES_ENDE;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import no.nav.folketrygdloven.kalkulator.modell.iay.InntektspostDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDto;
import no.nav.fpsak.tidsserie.LocalDateInterval;
import no.nav.fpsak.tidsserie.LocalDateSegment;
import no.nav.fpsak.tidsserie.LocalDateTimeline;
import no.nav.fpsak.tidsserie.StandardCombinators;

class FrilansInntektTidslinjeUtil {


    /**
     * Utleder utvidet tidslinje for frilansinntekt
     * Utvidet betyr her at vi tar med alle perioder som ligger innenfor to kalendermåneder før og to kalendermåneder etter inntektsperioden og i tillegg 2 kalendermåneder før dagens dato.
     * <p>
     * Eksempel:
     * En inntekt i februar utvides til å markere perioden 1. desember - 30. april
     *
     * @param skjæringstidspunkt Skjæringstidspunkt beregning
     * @param yrkesaktiviteter   Alle yrkesaktiviteter
     * @param inntektposter      Alle inntektsposter
     * @return Utvidet frilansinntekt tidslinje
     */
    public static LocalDateTimeline<Boolean> finnFrilansInntektTidslinje(LocalDate skjæringstidspunkt, Collection<YrkesaktivitetDto> yrkesaktiviteter, Collection<InntektspostDto> inntektposter) {
        var frilansInntektSegmenter = finnUtvidetInntektperiodeForFrilans(skjæringstidspunkt, yrkesaktiviteter, inntektposter);
        frilansInntektSegmenter.add(dagensDatoMinusToMånederGodkjent());

        return new LocalDateTimeline<>(frilansInntektSegmenter, StandardCombinators::alwaysTrueForMatch);
    }

    /**
     * Filterer bort frilansaktiviteter dersom perioden ikke har inntekt
     * se no.nav.folketrygdloven.kalkulator.steg.fordeling.tilkommetInntekt.FrilansInntektTidslinjeUtil#finnFrilansInntektTidslinje(java.time.LocalDate, java.util.Collection, java.util.Collection) for utledning av tidlinjen for inntekt.
     *
     * @param di  Interval
     * @param lhs Segment med inntektsforhold
     * @param rhs Segment med godkjent periode for inntekt
     * @return Filtrert segment
     */
    static LocalDateSegment<Set<TilkommetInntektsforholdTjeneste.Inntektsforhold>> kunFrilansMedInntekt(LocalDateInterval di,
                                                                                                        LocalDateSegment<Set<TilkommetInntektsforholdTjeneste.Inntektsforhold>> lhs,
                                                                                                        LocalDateSegment<Boolean> rhs) {
        if (lhs != null) {
            var inntektsforhold = lhs.getValue();
            var erFrilanser = inntektsforhold.stream().anyMatch(a -> a.aktivitetStatus().erFrilanser());
            if (erFrilanser) {
                if (rhs != null && rhs.getValue()) {
                    return new LocalDateSegment<>(di, inntektsforhold);
                } else {
                    return new LocalDateSegment<>(di, inntektsforhold.stream().filter(a -> !a.aktivitetStatus().erFrilanser()).collect(Collectors.toSet()));
                }
            }
            return new LocalDateSegment<>(di, lhs.getValue());
        }
        // Skal ikkje havne her ved LEFT_JOIN
        return null;
    }

    private static ArrayList<LocalDateSegment<Boolean>> finnUtvidetInntektperiodeForFrilans(LocalDate skjæringstidspunkt, Collection<YrkesaktivitetDto> yrkesaktiviteter, Collection<InntektspostDto> inntektposter) {
        return yrkesaktiviteter
                .stream()
                .filter(ya -> ya.getArbeidType().equals(FRILANSER_OPPDRAGSTAKER_MED_MER))
                .flatMap(ya -> finnUtvidetInntektssegmenterForAktivitet(skjæringstidspunkt, inntektposter, ya))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private static Stream<LocalDateSegment<Boolean>> finnUtvidetInntektssegmenterForAktivitet(LocalDate skjæringstidspunkt, Collection<InntektspostDto> inntektposter, YrkesaktivitetDto ya) {
        var posterForFrilansAktivitet = finnInntektsposterForAktivitetEtterStp(skjæringstidspunkt, inntektposter, ya);
        return posterForFrilansAktivitet.stream().map(FrilansInntektTidslinjeUtil::plussMinusToMånederGodkjent);
    }

    private static List<InntektspostDto> finnInntektsposterForAktivitetEtterStp(LocalDate skjæringstidspunkt, Collection<InntektspostDto> inntektposter, YrkesaktivitetDto ya) {
        return inntektposter.stream()
                .filter(i -> Objects.equals(i.getInntekt().getArbeidsgiver(), ya.getArbeidsgiver()))
                .filter(i -> i.getPeriode().getTomDato().isAfter(skjæringstidspunkt)).toList();
    }

    private static LocalDateSegment<Boolean> plussMinusToMånederGodkjent(InntektspostDto p) {
        return new LocalDateSegment<>(
                p.getPeriode().getFomDato().minusMonths(2).withDayOfMonth(1),
                p.getPeriode().getTomDato().plusMonths(3).withDayOfMonth(1).minusDays(1), Boolean.TRUE);
    }

    private static LocalDateSegment<Boolean> dagensDatoMinusToMånederGodkjent() {
        return new LocalDateSegment<>(LocalDate.now().minusMonths(2).withDayOfMonth(1), TIDENES_ENDE, Boolean.TRUE);
    }

}
