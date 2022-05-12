package no.nav.folketrygdloven.kalkulator.ytelse.k9;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.felles.frist.ArbeidsgiverRefusjonskravTjeneste;
import no.nav.folketrygdloven.kalkulator.input.UtbetalingsgradGrunnlag;
import no.nav.folketrygdloven.kalkulator.input.YtelsespesifiktGrunnlag;
import no.nav.folketrygdloven.kalkulator.modell.iay.AktivitetsAvtaleDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.permisjon.PermisjonFilter;
import no.nav.folketrygdloven.kalkulator.modell.svp.PeriodeMedUtbetalingsgradDto;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulator.ytelse.utbgradytelse.MapRefusjonPerioderFraVLTilRegelUtbgrad;
import no.nav.fpsak.tidsserie.LocalDateSegment;
import no.nav.fpsak.tidsserie.LocalDateTimeline;
import no.nav.fpsak.tidsserie.StandardCombinators;

public abstract class MapRefusjonPerioderFraVLTilRegelK9 extends MapRefusjonPerioderFraVLTilRegelUtbgrad {

    public MapRefusjonPerioderFraVLTilRegelK9(ArbeidsgiverRefusjonskravTjeneste arbeidsgiverRefusjonskravTjeneste) {
        super(arbeidsgiverRefusjonskravTjeneste);
    }

    /** Finner gyldige perioder for refusjon basert på perioder med utbetalingsgrad og ansettelse
     *
     *
     * @param startdatoPermisjon Startdato for permisjonen for ytelse søkt for
     * @param ytelsespesifiktGrunnlag Ytelsesspesifikt grunnlag
     * @param im inntektsmelding for refusjonskrav
     * @param relaterteYrkesaktiviteter Relaterte yrkesaktiviteter
     * @param permisjonFilter Permisjonsfilter
     * @return Gyldige perioder for refusjon
     */
    @Override
    protected List<Intervall> finnGyldigeRefusjonPerioder(LocalDate startdatoPermisjon,
                                                          YtelsespesifiktGrunnlag ytelsespesifiktGrunnlag,
                                                          InntektsmeldingDto im,
                                                          List<AktivitetsAvtaleDto> ansattperioder,
                                                          Set<YrkesaktivitetDto> relaterteYrkesaktiviteter,
                                                          PermisjonFilter permisjonFilter) {
        if (im.getRefusjonOpphører() != null && im.getRefusjonOpphører().isBefore(startdatoPermisjon)) {
            // Refusjon opphører før det utledede startpunktet, blir aldri refusjon
            return Collections.emptyList();
        }
        if (ytelsespesifiktGrunnlag instanceof UtbetalingsgradGrunnlag) {
            var utbetalingTidslinje = finnUtbetalingTidslinje((UtbetalingsgradGrunnlag) ytelsespesifiktGrunnlag, im);
            var ansettelseTidslinje = finnAnsettelseTidslinje(ansattperioder);
            var permisjonTidslinje = finnPermisjontidslinje(relaterteYrkesaktiviteter, permisjonFilter);
            return utbetalingTidslinje.intersection(ansettelseTidslinje).disjoint(permisjonTidslinje)
                    .getLocalDateIntervals()
                    .stream()
                    .map(i -> Intervall.fraOgMedTilOgMed(i.getFomDato(), i.getTomDato()))
                    .collect(Collectors.toList());
        }
        throw new IllegalStateException("Forventet utbetalingsgrader men fant ikke UtbetalingsgradGrunnlag.");
    }

    private LocalDateTimeline<Boolean> finnPermisjontidslinje(Set<YrkesaktivitetDto> yrkesaktiviteterRelatertTilInntektsmelding, PermisjonFilter permisjonFilter) {
        return yrkesaktiviteterRelatertTilInntektsmelding.stream()
                .map(permisjonFilter::finnTidslinjeForPermisjonOver14Dager)
                .reduce((t1, t2) -> t1.combine(t2, StandardCombinators::alwaysTrueForMatch, LocalDateTimeline.JoinStyle.CROSS_JOIN))
                .orElse(LocalDateTimeline.empty());
    }

    private LocalDateTimeline<Boolean> finnAnsettelseTidslinje(List<AktivitetsAvtaleDto> ansattperioder) {
        var segmenterMedAnsettelse = ansattperioder.stream()
                .map(AktivitetsAvtaleDto::getPeriode)
                .map(p -> new LocalDateTimeline<>(List.of(new LocalDateSegment<>(p.getFomDato(), p.getTomDato(), true))))
                .collect(Collectors.toList());

        var timeline = new LocalDateTimeline<Boolean>(List.of());

        for (LocalDateTimeline<Boolean> localDateSegments : segmenterMedAnsettelse) {
            timeline = timeline.combine(localDateSegments, StandardCombinators::coalesceRightHandSide, LocalDateTimeline.JoinStyle.CROSS_JOIN);
        }

        return timeline.compress();
    }

    private LocalDateTimeline<Boolean> finnUtbetalingTidslinje(UtbetalingsgradGrunnlag ytelsespesifiktGrunnlag, InntektsmeldingDto im) {
        var segmenterMedUtbetaling = ytelsespesifiktGrunnlag.finnUtbetalingsgraderForArbeid(im.getArbeidsgiver(), im.getArbeidsforholdRef())
                .stream()
                .filter(p -> p.getUtbetalingsgrad() != null && p.getUtbetalingsgrad().compareTo(BigDecimal.ZERO) > 0)
                .map(PeriodeMedUtbetalingsgradDto::getPeriode)
                .map(p -> new LocalDateTimeline<>(List.of(new LocalDateSegment<>(p.getFomDato(), p.getTomDato(), true))))
                .collect(Collectors.toList());

        var timeline = new LocalDateTimeline<Boolean>(List.of());

        for (LocalDateTimeline<Boolean> localDateSegments : segmenterMedUtbetaling) {
            timeline = timeline.combine(localDateSegments, StandardCombinators::coalesceRightHandSide, LocalDateTimeline.JoinStyle.CROSS_JOIN);
        }

        return timeline.compress();
    }


}
