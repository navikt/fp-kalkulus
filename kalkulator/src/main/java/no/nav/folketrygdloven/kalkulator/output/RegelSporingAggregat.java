package no.nav.folketrygdloven.kalkulator.output;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RegelSporingAggregat {

    private List<RegelSporingGrunnlag> regelsporingerGrunnlag = new ArrayList<>();

    private List<RegelSporingPeriode> regelsporingPerioder = new ArrayList<>();

    public RegelSporingAggregat(List<RegelSporingPeriode> regelsporingPerioder) {
        this.regelsporingPerioder = regelsporingPerioder;
    }

    public RegelSporingAggregat(List<RegelSporingGrunnlag> regelsporingerGrunnlag,
                                List<RegelSporingPeriode> regelsporingPerioder) {
        this.regelsporingerGrunnlag.addAll(regelsporingerGrunnlag);
        this.regelsporingPerioder = regelsporingPerioder;
    }

    public RegelSporingAggregat(RegelSporingGrunnlag... regelsporingerGrunnlag) {
        this.regelsporingerGrunnlag.addAll(Arrays.asList(regelsporingerGrunnlag));
    }

    public List<RegelSporingGrunnlag> getRegelsporingerGrunnlag() {
        return regelsporingerGrunnlag;
    }

    public List<RegelSporingPeriode> getRegelsporingPerioder() {
        return regelsporingPerioder;
    }


    public static RegelSporingAggregat konkatiner(RegelSporingAggregat sporing1, RegelSporingAggregat sporing2) {
        if (sporing1 == null) {
            return sporing2;
        }
        if (sporing2 == null) {
            return sporing1;
        }
        return new RegelSporingAggregat(
                Stream.concat(
                        sporing1.getRegelsporingerGrunnlag().stream(),
                        sporing2.getRegelsporingerGrunnlag().stream()).collect(Collectors.toList()),
                Stream.concat(
                        sporing1.getRegelsporingPerioder().stream(),
                        sporing2.getRegelsporingPerioder().stream()).collect(Collectors.toList())
        );
    }

}
