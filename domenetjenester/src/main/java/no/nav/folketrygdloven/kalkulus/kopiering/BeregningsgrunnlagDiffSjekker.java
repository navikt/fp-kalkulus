package no.nav.folketrygdloven.kalkulus.kopiering;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
import no.nav.folketrygdloven.kalkulus.felles.v1.BeløpDto;

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


    private static DiffForKopieringResult getDiff(Object dtoObject1, Object dtoObject2) {
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

        config.addLeafClasses(BeløpDto.class);
        config.addLeafClasses(InternArbeidsforholdRefDto.class);
        config.addLeafClasses(Arbeidsgiver.class);

        var diffEntity = new DiffForKopieringDto(new TraverseGraph(config));

        return diffEntity.diff(dtoObject1, dtoObject2);
    }

    private static boolean erDiffMellomToPerioder(BeregningsgrunnlagPeriodeDto aktivPeriode, BeregningsgrunnlagPeriodeDto forrigePeriode) {
        return !aktivPeriode.getIndexKey().equals(forrigePeriode.getIndexKey()) || !getDiff(aktivPeriode, forrigePeriode).isEmpty();
    }

    private static Set<Intervall> finnPerioderUtenDiff(List<BeregningsgrunnlagPeriodeDto> aktivePerioder, List<BeregningsgrunnlagPeriodeDto> forrigePerioder) {
        var perioderUtenDiff = new HashSet<Intervall>();
        // begge listene er sorter på fom dato så det er mulig å benytte indeks her
        int i = 0;
        while (i < forrigePerioder.size() && i < aktivePerioder.size() && !erDiffMellomToPerioder(aktivePerioder.get(i), forrigePerioder.get(i))) {
            perioderUtenDiff.add(aktivePerioder.get(i).getPeriode());
            i++;
        }
        return perioderUtenDiff;
    }

}
