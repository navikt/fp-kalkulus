package no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.periodisering;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.gradering.AndelGradering;
import no.nav.folketrygdloven.kalkulator.gradering.AndelGradering.Gradering;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDto;
import no.nav.folketrygdloven.kalkulator.regelmodell.Periode;

class MapGraderingForYrkesaktivitet {
    private MapGraderingForYrkesaktivitet() {
        // skjul public constructor
    }

    static List<no.nav.folketrygdloven.kalkulator.regelmodell.Gradering> mapGraderingForYrkesaktivitet(Collection<AndelGradering> andelGraderinger, YrkesaktivitetDto ya) {
        List<Gradering> graderingList = andelGraderinger.stream()
            .filter(gradering -> gradering.gjelderFor(ya.getArbeidsgiver(), ya.getArbeidsforholdRef()))
            .flatMap(g -> g.getGraderinger().stream())
            .collect(Collectors.toList());
        return mapGraderingPerioder(graderingList);
    }

    private static List<no.nav.folketrygdloven.kalkulator.regelmodell.Gradering> mapGraderingPerioder(List<Gradering> graderingList) {
        return graderingList.stream()
            .map(gradering -> new no.nav.folketrygdloven.kalkulator.regelmodell.Gradering(
                Periode.of(gradering.getPeriode().getFomDato(), gradering.getPeriode().getTomDato()),
                gradering.getArbeidstidProsent()))
            .collect(Collectors.toList());
    }
}
