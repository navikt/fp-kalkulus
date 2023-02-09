package no.nav.folketrygdloven.kalkulator.steg.fordeling.tilkommetInntekt;

import java.util.List;
import java.util.function.Function;

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
                                                                             LocalDateTimeline<V> nyePerioderTidslinje,
                                                                             PeriodeÅrsak periodeÅrsak,
                                                                             PeriodeÅrsak avsluttetPeriodeÅrsak, List<Intervall> forlengelseperioder) {
        return splittPerioder(beregningsgrunnlag, nyePerioderTidslinje,
                SplittBGPerioder.splittPerioderOgSettÅrsakCombinator(periodeÅrsak),
                getSettAvsluttetPeriodeårsakMapper(nyePerioderTidslinje, forlengelseperioder, avsluttetPeriodeÅrsak)
        );
    }


    /**
     * Splitter opp og periodiserer beregningsgrunnlag på opppgitt periodetidslinje
     * Verdier i tidslinje må vere ulike på hver side av splitten (kjører compress før combine)
     *
     * @param beregningsgrunnlag     Beregningsgrunnlag
     * @param nyePerioderTidslinje   Tidslinje for nye perioder
     * @param nyePerioderCombinator  Combinator for left-joint mot eksisterende perioder
     * @param periodeTidslinjeMapper Mapper for preproseessering av splittet grunnlag (Kan brukes til å sette avsluttet periodeårsak for segmenter som tilstøter nye perioder)
     * @return Beregningsgrunnlag med splittet perioder
     */
    public static <V> BeregningsgrunnlagDto splittPerioder(BeregningsgrunnlagDto beregningsgrunnlag,
                                                           LocalDateTimeline<V> nyePerioderTidslinje,
                                                           LocalDateSegmentCombinator<BeregningsgrunnlagPeriodeDto, V, BeregningsgrunnlagPeriodeDto> nyePerioderCombinator,
                                                           Function<LocalDateTimeline<BeregningsgrunnlagPeriodeDto>, LocalDateTimeline<BeregningsgrunnlagPeriodeDto>> periodeTidslinjeMapper) {

        var eksisterendePerioder = beregningsgrunnlag.getBeregningsgrunnlagPerioder();

        var eksisterendePerioderTidslinje = new LocalDateTimeline<>(eksisterendePerioder.stream()
                .map(p -> new LocalDateSegment<>(new LocalDateInterval(p.getPeriode().getFomDato(), p.getPeriode().getTomDato()), p)).toList());

        var resultatPerioder = eksisterendePerioderTidslinje.combine(nyePerioderTidslinje.compress(), nyePerioderCombinator, LocalDateTimeline.JoinStyle.LEFT_JOIN);

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

    public static <V> Function<LocalDateTimeline<BeregningsgrunnlagPeriodeDto>, LocalDateTimeline<BeregningsgrunnlagPeriodeDto>> getSettAvsluttetPeriodeårsakMapper(LocalDateTimeline<V> nyePerioderTidslinje,
                                                                                                                                                                    List<Intervall> forlengelseperioder,
                                                                                                                                                                    PeriodeÅrsak avsluttetPeriodeårsak) {

        return (tidslinje) -> {
            var perioderTilVurderingTjeneste = new PerioderTilVurderingTjeneste(forlengelseperioder, tidslinje.toSegments().stream().map(s -> Intervall.fraOgMedTilOgMed(s.getFom(), s.getTom())).toList());
            var gamleperioder = tidslinje.disjoint(nyePerioderTidslinje);
            gamleperioder.stream()
                    .filter(perioderTilVurderingTjeneste::erTilVurdering)
                    .filter(s -> nyePerioderTidslinje.stream().anyMatch(it -> it.getTom().equals(s.getFom().minusDays(1))))
                    .forEach(p -> BeregningsgrunnlagPeriodeDto.oppdater(p.getValue()).leggTilPeriodeÅrsak(avsluttetPeriodeårsak));
            return tidslinje;

        };
    }

    public static <V> LocalDateSegmentCombinator<BeregningsgrunnlagPeriodeDto, V, BeregningsgrunnlagPeriodeDto> splittPerioderOgSettÅrsakCombinator(PeriodeÅrsak periodeårsak) {
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
