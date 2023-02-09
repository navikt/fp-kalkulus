package no.nav.folketrygdloven.kalkulator.steg.fordeling.tilkommetInntekt;

import java.util.function.Function;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.PeriodeÅrsak;
import no.nav.fpsak.tidsserie.LocalDateInterval;
import no.nav.fpsak.tidsserie.LocalDateSegment;
import no.nav.fpsak.tidsserie.LocalDateSegmentCombinator;
import no.nav.fpsak.tidsserie.LocalDateTimeline;

public class SplittBGPerioder {
    private SplittBGPerioder() {
        // skjul public constructor
    }

    public static <V> BeregningsgrunnlagDto splittPerioderOgSettPeriodeårsak(BeregningsgrunnlagDto beregningsgrunnlag,
                                                                             LocalDateTimeline<V> nyePerioderTidslinje,
                                                                             PeriodeÅrsak periodeÅrsak, PeriodeÅrsak avsluttetPeriodeårsak) {
        return splittPerioder(beregningsgrunnlag, nyePerioderTidslinje,
                SplittBGPerioder.splittPerioderOgSettÅrsakCombinator(periodeÅrsak, avsluttetPeriodeårsak),
                getSettAvsluttetPeriodeårsakMapper(periodeÅrsak, avsluttetPeriodeårsak)
                );
    }


    public static <V> BeregningsgrunnlagDto splittPerioder(BeregningsgrunnlagDto beregningsgrunnlag,
                                                           LocalDateTimeline<V> nyePerioderTidslinje,
                                                           LocalDateSegmentCombinator<BeregningsgrunnlagPeriodeDto, V, BeregningsgrunnlagPeriodeDto> nyePerioderCombinator,
                                                           Function<LocalDateTimeline<BeregningsgrunnlagPeriodeDto>, LocalDateTimeline<BeregningsgrunnlagPeriodeDto>> periodeTidslinjeMapper) {

        var eksisterendePerioder = beregningsgrunnlag.getBeregningsgrunnlagPerioder();

        var eksisterendePerioderTidslinje = new LocalDateTimeline<>(eksisterendePerioder.stream()
                .map(p -> new LocalDateSegment<>(new LocalDateInterval(p.getPeriode().getFomDato(), p.getPeriode().getTomDato()), p)).toList());

        var resultatPerioder = eksisterendePerioderTidslinje.combine(nyePerioderTidslinje, nyePerioderCombinator, LocalDateTimeline.JoinStyle.LEFT_JOIN);

        var nyttBg = BeregningsgrunnlagDto.builder(beregningsgrunnlag).fjernAllePerioder().build();

        periodeTidslinjeMapper.apply(resultatPerioder)
                .toSegments()
                .forEach(s -> {
                    if (s.getValue() != null) {
                        BeregningsgrunnlagPeriodeDto.oppdater(s.getValue()).build(nyttBg);
                    }
                });

        return nyttBg;
    }

    public static Function<LocalDateTimeline<BeregningsgrunnlagPeriodeDto>, LocalDateTimeline<BeregningsgrunnlagPeriodeDto>> getSettAvsluttetPeriodeårsakMapper(PeriodeÅrsak periodeÅrsak, PeriodeÅrsak avsluttetPeriodeårsak) {
        return (tidslinje) -> {
            var segmenterMedPeriodeÅrsak = tidslinje.stream().filter(s -> s.getValue().getPeriodeÅrsaker().stream().anyMatch(periodeÅrsak::equals))
                    .toList();
            tidslinje.stream()
                    .filter(s -> s.getValue().getPeriodeÅrsaker().stream().noneMatch(periodeÅrsak::equals))
                    .filter(s -> segmenterMedPeriodeÅrsak.stream().anyMatch(sp -> sp.getTom().equals(s.getFom().minusDays(1))))
                    .forEach(p -> BeregningsgrunnlagPeriodeDto.oppdater(p.getValue()).leggTilPeriodeÅrsak(avsluttetPeriodeårsak));
            return tidslinje;

        };
    }

    public static <V> LocalDateSegmentCombinator<BeregningsgrunnlagPeriodeDto, V, BeregningsgrunnlagPeriodeDto> splittPerioderOgSettÅrsakCombinator(PeriodeÅrsak periodeårsak, PeriodeÅrsak avluttetPeriodeårsak) {
        return (di, lhs, rhs) -> {
            if (lhs != null && rhs != null) {
                var nyPeriode = BeregningsgrunnlagPeriodeDto.kopier(lhs.getValue())
                        .leggTilPeriodeÅrsak(periodeårsak)
                        .medBeregningsgrunnlagPeriode(di.getFomDato(), di.getTomDato())
                        .build();
                return new LocalDateSegment<>(di, nyPeriode);
            } else if (lhs != null) {
                var nyPeriode = BeregningsgrunnlagPeriodeDto.kopier(lhs.getValue())
                        .medBeregningsgrunnlagPeriode(di.getFomDato(), di.getTomDato())
                        .build();
                return new LocalDateSegment<>(di, nyPeriode);
            }
            return null;
        };
    }

}
