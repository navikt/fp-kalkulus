package no.nav.folketrygdloven.kalkulus.kopiering;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.diff.DiffForKopieringDto;
import no.nav.folketrygdloven.kalkulator.modell.diff.DiffForKopieringResult;
import no.nav.folketrygdloven.kalkulator.modell.diff.TraverseGraph;
import no.nav.folketrygdloven.kalkulator.modell.diff.TraverseGraphConfig;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.fpsak.tidsserie.LocalDateSegment;
import no.nav.fpsak.tidsserie.LocalDateTimeline;

public class BeregningsgrunnlagDiffSjekker {

    private BeregningsgrunnlagDiffSjekker() {
        // Skjul
    }

    public static boolean harSignifikantDiffIAktiviteter(BeregningAktivitetAggregatDto aktivt, BeregningAktivitetAggregatDto forrige) {
        return !getDiff(aktivt, forrige).isEmpty();
    }


    static boolean harSignifikantDiffIBeregningsgrunnlag(BeregningsgrunnlagDto aktivt, BeregningsgrunnlagDto forrige) {
        return !getDiff(aktivt, forrige).isEmpty();
    }

    /**
     * Finner perioder uten diff mellom aktivt og forrige beregningsgrunnlag
     * Antar ikke at fom og tom for periodene er like, men sjekker på innhold på periodenivå og lenger ned i treet
     *
     * @param aktivt  Aktivt beregningsgrunnlag
     * @param forrige Forrige beregningsgrunnlag
     * @return Intervaller som ikke har diff
     */
    public static Set<Intervall> finnPerioderUtenDiff(BeregningsgrunnlagDto aktivt, BeregningsgrunnlagDto forrige) {
        if (harDiffIFelterPåBeregningsgrunnlag(aktivt, forrige)) {
            return Set.of();
        }
        List<BeregningsgrunnlagPeriodeDto> aktivePerioder = aktivt.getBeregningsgrunnlagPerioder();
        List<BeregningsgrunnlagPeriodeDto> forrigePerioder = forrige.getBeregningsgrunnlagPerioder();
        return finnPerioderUtenDiff(aktivePerioder, forrigePerioder);
    }

    private static boolean harDiffIFelterPåBeregningsgrunnlag(BeregningsgrunnlagDto aktivt, BeregningsgrunnlagDto forrige) {
        // Sjekker om det kun er diff på periodenivå
        return !getDiffUtenPerioder(aktivt, forrige).isEmpty();
    }


    public static DiffForKopieringResult getDiff(Object dtoObject1, Object dtoObject2) {
        var config = new TraverseGraphConfig();
        config.setIgnoreNulls(false);
        config.setOnlyCheckTrackedFields(true);

        config.addLeafClasses(Beløp.class);
        config.addLeafClasses(InternArbeidsforholdRefDto.class);
        config.addLeafClasses(Arbeidsgiver.class);

        var diffDto = new DiffForKopieringDto(new TraverseGraph(config));

        return diffDto.diff(dtoObject1, dtoObject2);
    }

    private static DiffForKopieringResult getDiffUtenPerioder(BeregningsgrunnlagDto dtoObject1, BeregningsgrunnlagDto dtoObject2) {
        var config = new TraverseGraphConfig();
        config.setIgnoreNulls(false);
        config.setOnlyCheckTrackedFields(true);
        config.setInclusionFilter(obj -> !(obj instanceof BeregningsgrunnlagPeriodeDto));

        config.addLeafClasses(no.nav.folketrygdloven.kalkulus.felles.v1.Beløp.class);
        config.addLeafClasses(InternArbeidsforholdRefDto.class);
        config.addLeafClasses(Arbeidsgiver.class);

        var diffEntity = new DiffForKopieringDto(new TraverseGraph(config));

        return diffEntity.diff(dtoObject1, dtoObject2);
    }

    private static Set<Intervall> finnPerioderUtenDiff(List<BeregningsgrunnlagPeriodeDto> aktivePerioder, List<BeregningsgrunnlagPeriodeDto> forrigePerioder) {
        var aktivtidslinje = lagPeriodeTidslinje(aktivePerioder);
        var forrigeTidslinje = lagPeriodeTidslinje(forrigePerioder);

        var ïkkeDiffTidslinje = finnTidslinjeUtenDiff(aktivtidslinje, forrigeTidslinje);

        return ïkkeDiffTidslinje.toSegments()
                .stream()
                .filter(LocalDateSegment::getValue)
                .map(s -> Intervall.fraOgMedTilOgMed(s.getFom(), s.getTom()))
                .collect(Collectors.toSet());

    }

    private static LocalDateTimeline<Boolean> finnTidslinjeUtenDiff(LocalDateTimeline<BeregningsgrunnlagPeriodeDto> aktivtidslinje, LocalDateTimeline<BeregningsgrunnlagPeriodeDto> forrigeTidslinje) {
        return aktivtidslinje.crossJoin(forrigeTidslinje, (di, lhs, rhs) -> {
            if (lhs == null || rhs == null) {
                return new LocalDateSegment<>(di, false);
            } else {
                return new LocalDateSegment<>(di, getDiff(lhs.getValue(), rhs.getValue()).isEmpty());
            }
        });
    }

    private static LocalDateTimeline<BeregningsgrunnlagPeriodeDto> lagPeriodeTidslinje(List<BeregningsgrunnlagPeriodeDto> aktivePerioder) {
        return aktivePerioder.stream()
                .map(p -> new LocalDateSegment<>(p.getBeregningsgrunnlagPeriodeFom(), p.getBeregningsgrunnlagPeriodeTom(), p))
                .collect(Collectors.collectingAndThen(Collectors.toList(), LocalDateTimeline::new));
    }

}
