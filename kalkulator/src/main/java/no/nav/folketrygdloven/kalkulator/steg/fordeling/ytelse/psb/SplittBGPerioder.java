package no.nav.folketrygdloven.kalkulator.steg.fordeling.ytelse.psb;

import java.util.List;

import no.nav.folketrygdloven.kalkulator.avklaringsbehov.PerioderTilVurderingTjeneste;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
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
                                                                             List<Intervall> forlengelseperioder,
                                                                             LocalDateTimeline<V> nyePerioderTidslinje,
                                                                             PeriodeÅrsak periodeårsak) {

        var perioderTilVurderingTjeneste = new PerioderTilVurderingTjeneste(forlengelseperioder, beregningsgrunnlag);
        var eksisterendePerioder = beregningsgrunnlag.getBeregningsgrunnlagPerioder().stream()
                .filter(p -> perioderTilVurderingTjeneste.erTilVurdering(p.getPeriode())).toList();

        var eksisterendePerioderTidslinje = new LocalDateTimeline<>(eksisterendePerioder.stream()
                .map(p -> new LocalDateSegment<>(new LocalDateInterval(p.getPeriode().getFomDato(), p.getPeriode().getTomDato()), p)).toList());


        var resultatPerioder = eksisterendePerioderTidslinje.combine(nyePerioderTidslinje, splittPerioderOgSettÅrsak(periodeårsak), LocalDateTimeline.JoinStyle.LEFT_JOIN);

        var nyttBg = BeregningsgrunnlagDto.builder(beregningsgrunnlag).fjernAllePerioder().build();

        resultatPerioder.toSegments()
                .forEach(s -> {
                    if (s.getValue() != null) {
                        s.getValue().build(nyttBg);
                    }
                });

        return nyttBg;
    }

    private static <V> LocalDateSegmentCombinator<BeregningsgrunnlagPeriodeDto, V, BeregningsgrunnlagPeriodeDto.Builder> splittPerioderOgSettÅrsak(PeriodeÅrsak periodeårsak) {
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
