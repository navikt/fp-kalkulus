package no.nav.folketrygdloven.kalkulator.ytelse.svp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import no.nav.folketrygdloven.kalkulator.FagsakYtelseTypeRef;
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

@FagsakYtelseTypeRef("SVP")
@ApplicationScoped
public class MapRefusjonPerioderFraVLTilRegelSVP extends MapRefusjonPerioderFraVLTilRegelUtbgrad {

    @Inject
    public MapRefusjonPerioderFraVLTilRegelSVP(ArbeidsgiverRefusjonskravTjeneste arbeidsgiverRefusjonskravTjeneste) {
        super(arbeidsgiverRefusjonskravTjeneste);
    }

    /**
     * Finner gyldige perioder for refusjon basert på perioder med utbetalingsgrad og inntektsmeldingsdata
     *
     * @param startdatoPermisjon      Startdato for permisjonen for ytelse søkt for
     * @param ytelsespesifiktGrunnlag Ytelsesspesifikt grunnlag
     * @param inntektsmelding         inntektsmelding
     * @param ansattperioder          Ansettelsesperioder
     * @param yrkesaktiviteter        Yrkesaktiviteter relatert til inntektsmelding
     * @param permisjonFilter         Permisjonsfilter
     * @return Gyldige perioder for refusjon
     */
    @Override
    protected List<Intervall> finnGyldigeRefusjonPerioder(LocalDate startdatoPermisjon,
                                                          YtelsespesifiktGrunnlag ytelsespesifiktGrunnlag,
                                                          InntektsmeldingDto inntektsmelding,
                                                          List<AktivitetsAvtaleDto> ansattperioder,
                                                          Set<YrkesaktivitetDto> yrkesaktiviteter,
                                                          PermisjonFilter permisjonFilter) {
        if (inntektsmelding.getRefusjonOpphører() != null && inntektsmelding.getRefusjonOpphører().isBefore(startdatoPermisjon)) {
            // Refusjon opphører før det utledede startpunktet, blir aldri refusjon
            return Collections.emptyList();
        }
        if (ytelsespesifiktGrunnlag instanceof UtbetalingsgradGrunnlag utbetalingsgradGrunnlag) {
            var utbetalingTidslinje = finnUtbetalingTidslinje(utbetalingsgradGrunnlag, inntektsmelding);
            var permisjontidslinje = finnPermisjontidslinje(yrkesaktiviteter, permisjonFilter);
            return utbetalingTidslinje.disjoint(permisjontidslinje)
                    .getLocalDateIntervals()
                    .stream()
                    .map(it -> Intervall.fraOgMedTilOgMed(it.getFomDato(), it.getTomDato()))
                    .toList();
        }
        return List.of();
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

    private LocalDateTimeline<Boolean> finnPermisjontidslinje(Set<YrkesaktivitetDto> yrkesaktiviteterRelatertTilInntektsmelding, PermisjonFilter permisjonFilter) {
        return yrkesaktiviteterRelatertTilInntektsmelding.stream()
                .map(permisjonFilter::finnTidslinjeForPermisjonOver14Dager)
                .reduce((t1, t2) -> t1.combine(t2, StandardCombinators::alwaysTrueForMatch, LocalDateTimeline.JoinStyle.INNER_JOIN))
                .orElse(LocalDateTimeline.empty());
    }

}
