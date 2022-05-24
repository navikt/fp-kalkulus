package no.nav.folketrygdloven.kalkulator.modell.iay.permisjon;

import static no.nav.fpsak.tidsserie.LocalDateInterval.TIDENES_BEGYNNELSE;
import static no.nav.fpsak.tidsserie.LocalDateInterval.TIDENES_ENDE;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.AktivitetsAvtaleDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.opptjening.OpptjeningAktiviteterDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulator.tid.TimelineWeekendCompressor;
import no.nav.folketrygdloven.kalkulus.kodeverk.OpptjeningAktivitetType;
import no.nav.fpsak.tidsserie.LocalDateSegment;
import no.nav.fpsak.tidsserie.LocalDateTimeline;
import no.nav.fpsak.tidsserie.StandardCombinators;


public class PermisjonFilter {

    private final Map<OpptjeningAktivitetType, LocalDateTimeline<Boolean>> tidslinjePerYtelse;
    private final Collection<YrkesaktivitetDto> yrkesaktiviteter;
    private LocalDate fom;

    public PermisjonFilter(BeregningAktivitetAggregatDto beregningAktivitetAggregatDto,
                           Collection<YrkesaktivitetDto> yrkesaktiviteter) {
        this.tidslinjePerYtelse = utledYtelsesTidslinjer(beregningAktivitetAggregatDto.getBeregningAktiviteter().stream().map(AktivitetFraOpptjening::new).toList());
        this.yrkesaktiviteter = yrkesaktiviteter;
    }

    public PermisjonFilter(Collection<OpptjeningAktiviteterDto.OpptjeningPeriodeDto> opptjeningperioder, Collection<YrkesaktivitetDto> yrkesaktiviteter) {
        this.tidslinjePerYtelse = utledYtelsesTidslinjer(opptjeningperioder.stream().map(AktivitetFraOpptjening::new).toList());
        this.yrkesaktiviteter = yrkesaktiviteter;
    }

    public void medFom(LocalDate fom) {
        this.fom = fom;
    }

    public LocalDateTimeline<Boolean> tidslinjeForPermisjoner(Arbeidsgiver arbeidsgiver, InternArbeidsforholdRefDto arbeidsforholdRef) {
        var relevantYrkesaktivitet = yrkesaktiviteter
                .stream()
                .filter(ya -> ya.gjelderFor(
                        arbeidsgiver,
                        arbeidsforholdRef))
                .findFirst();
        return relevantYrkesaktivitet.map(this::finnTidslinjeForPermisjonOver14Dager)
                .orElse(LocalDateTimeline.empty());
    }

    public LocalDateTimeline<Boolean> finnTidslinjeForPermisjonOver14Dager(YrkesaktivitetDto yrkesaktivitet) {

        // Permisjoner på yrkesaktivitet
        LocalDateTimeline<Boolean> aktivPermisjonTidslinje = PermisjonPerYrkesaktivitet.utledPermisjonPerYrkesaktivitet(yrkesaktivitet, tidslinjePerYtelse);

        // Vurder kun permisjonsperioder over aktivitetens lengde og fra gitt dato
        LocalDateTimeline<Boolean> tidslinjeTilVurdering = new LocalDateTimeline<>(List.of(new LocalDateSegment<>(fom != null ? fom : TIDENES_BEGYNNELSE, TIDENES_ENDE, Boolean.TRUE)));
        tidslinjeTilVurdering = tidslinjeTilVurdering.intersection(aktivPermisjonTidslinje.compress());
        var aktivitetsTidslinje = new LocalDateTimeline<>(yrkesaktivitet.getAlleAnsettelsesperioder().stream()
                .map(AktivitetsAvtaleDto::getPeriode)
                .map(p -> new LocalDateSegment<>(p.getFomDato(), p.getTomDato(), Boolean.TRUE))
                .toList(), StandardCombinators::alwaysTrueForMatch);
        tidslinjeTilVurdering = tidslinjeTilVurdering.intersection(aktivitetsTidslinje.compress());

        // Legg til mellomliggende periode dersom helg mellom permisjonsperioder
        tidslinjeTilVurdering = komprimerForHelg(tidslinjeTilVurdering);

        // Underkjent vurderingsstatus dersom sammenhengende permisjonsperiode > 14 dager
        var permisjonOver14Dager = tidslinjeTilVurdering.compress().stream()
                .filter(segment -> segment.getValue() == Boolean.TRUE && segment.getLocalDateInterval().days() > 14)
                .collect(Collectors.toSet());

        return new LocalDateTimeline<>(permisjonOver14Dager);
    }

    private Map<OpptjeningAktivitetType, LocalDateTimeline<Boolean>> utledYtelsesTidslinjer(Collection<AktivitetFraOpptjening> aktiviteter) {
        var gruppertPåYtelse = aktiviteter.stream()
                .filter(op -> OpptjeningAktivitetType.YTELSE.contains(op.opptjeningAktivitetType()))
                .collect(Collectors.groupingBy(AktivitetFraOpptjening::opptjeningAktivitetType));
        var timelinePerYtelse = new HashMap<OpptjeningAktivitetType, LocalDateTimeline<Boolean>>();

        for (Map.Entry<OpptjeningAktivitetType, List<AktivitetFraOpptjening>> entry : gruppertPåYtelse.entrySet()) {
            var segmenter = entry.getValue()
                    .stream()
                    .map(it -> new LocalDateSegment<>(it.periode().getFomDato(), it.periode().getTomDato(), true))
                    .collect(Collectors.toSet());
            var timeline = new LocalDateTimeline<>(segmenter, StandardCombinators::alwaysTrueForMatch);
            timeline = komprimerForHelg(timeline);
            timelinePerYtelse.put(entry.getKey(), timeline);
        }

        return timelinePerYtelse;
    }

    private static LocalDateTimeline<Boolean> komprimerForHelg(LocalDateTimeline<Boolean> tidslinje) {
        var factory = new TimelineWeekendCompressor.CompressorFactory<Boolean>(Objects::equals, (i, lhs, rhs) -> new LocalDateSegment<>(i, lhs.getValue()));
        TimelineWeekendCompressor<Boolean> compressor = tidslinje.toSegments().stream()
                .collect(factory::get, TimelineWeekendCompressor::accept, TimelineWeekendCompressor::combine);
        return new LocalDateTimeline<>(compressor.getSegmenter());
    }

    private record AktivitetFraOpptjening(OpptjeningAktivitetType opptjeningAktivitetType, Intervall periode) {

        private AktivitetFraOpptjening(BeregningAktivitetDto aktivitet) {
            this(aktivitet.getOpptjeningAktivitetType(), aktivitet.getPeriode());
        }

        private AktivitetFraOpptjening(OpptjeningAktiviteterDto.OpptjeningPeriodeDto aktivitet) {
            this(aktivitet.getOpptjeningAktivitetType(), aktivitet.getPeriode());
        }
    }

}
