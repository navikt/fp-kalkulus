package no.nav.folketrygdloven.kalkulator.steg.fordeling.tilkommetInntekt;

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
                                                                             PeriodeÅrsak periodeÅrsak) {
        return splittPerioder(beregningsgrunnlag, nyePerioderTidslinje, SplittBGPerioder.splittPerioderOgSettÅrsakCombinator(periodeÅrsak));
    }


    public static <V> BeregningsgrunnlagDto splittPerioder(BeregningsgrunnlagDto beregningsgrunnlag,
                                                           LocalDateTimeline<V> nyePerioderTidslinje,
                                                           LocalDateSegmentCombinator<BeregningsgrunnlagPeriodeDto, V, BeregningsgrunnlagPeriodeDto.Builder> nyePerioderCombinator) {

        var eksisterendePerioder = beregningsgrunnlag.getBeregningsgrunnlagPerioder();

        var eksisterendePerioderTidslinje = new LocalDateTimeline<>(eksisterendePerioder.stream()
                .map(p -> new LocalDateSegment<>(new LocalDateInterval(p.getPeriode().getFomDato(), p.getPeriode().getTomDato()), p)).toList());


        var resultatPerioder = eksisterendePerioderTidslinje.combine(nyePerioderTidslinje, nyePerioderCombinator, LocalDateTimeline.JoinStyle.LEFT_JOIN);

        var nyttBg = BeregningsgrunnlagDto.builder(beregningsgrunnlag).fjernAllePerioder().build();

        resultatPerioder.toSegments()
                .forEach(s -> {
                    if (s.getValue() != null) {
                        s.getValue().build(nyttBg);
                    }
                });

        return nyttBg;
    }

    public static <V> LocalDateSegmentCombinator<BeregningsgrunnlagPeriodeDto, V, BeregningsgrunnlagPeriodeDto.Builder> splittPerioderOgSettÅrsakCombinator(PeriodeÅrsak periodeårsak) {
        return (di, lhs, rhs) -> {
            if (lhs != null && rhs != null) {
                var nyPeriode = BeregningsgrunnlagPeriodeDto.kopier(lhs.getValue())
                        .leggTilPeriodeÅrsak(periodeårsak)
                        .medBeregningsgrunnlagPeriode(di.getFomDato(), di.getTomDato());
                return new LocalDateSegment<>(di, nyPeriode);
            } else if (lhs != null) {
                var nyPeriode = BeregningsgrunnlagPeriodeDto.kopier(lhs.getValue())
                        .medBeregningsgrunnlagPeriode(di.getFomDato(), di.getTomDato());
                return new LocalDateSegment<>(di, nyPeriode);
            }
            return null;
        };
    }

}
