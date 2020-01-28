package no.nav.folketrygdloven.kalkulator;

import static no.nav.folketrygdloven.kalkulator.GrunnbeløpTestKonstanter.GRUNNBELØP_2013;
import static no.nav.folketrygdloven.kalkulator.GrunnbeløpTestKonstanter.GRUNNBELØP_2014;
import static no.nav.folketrygdloven.kalkulator.GrunnbeløpTestKonstanter.GRUNNBELØP_2015;
import static no.nav.folketrygdloven.kalkulator.GrunnbeløpTestKonstanter.GRUNNBELØP_2016;
import static no.nav.folketrygdloven.kalkulator.GrunnbeløpTestKonstanter.GRUNNBELØP_2017;
import static no.nav.folketrygdloven.kalkulator.GrunnbeløpTestKonstanter.GRUNNBELØP_2018;
import static no.nav.folketrygdloven.kalkulator.GrunnbeløpTestKonstanter.GRUNNBELØP_2019;
import static no.nav.folketrygdloven.kalkulator.GrunnbeløpTestKonstanter.GSNITT_2013;
import static no.nav.folketrygdloven.kalkulator.GrunnbeløpTestKonstanter.GSNITT_2014;
import static no.nav.folketrygdloven.kalkulator.GrunnbeløpTestKonstanter.GSNITT_2015;
import static no.nav.folketrygdloven.kalkulator.GrunnbeløpTestKonstanter.GSNITT_2016;
import static no.nav.folketrygdloven.kalkulator.GrunnbeløpTestKonstanter.GSNITT_2017;
import static no.nav.folketrygdloven.kalkulator.GrunnbeløpTestKonstanter.GSNITT_2018;
import static no.nav.folketrygdloven.kalkulator.GrunnbeløpTestKonstanter.GSNITT_2019;

import java.time.LocalDate;
import java.util.List;

import no.nav.folketrygdloven.beregningsgrunnlag.Grunnbeløp;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;

public class GrunnbeløpMock {

    public static List<Grunnbeløp> GRUNNBELØPSATSER = List.of(
        new Grunnbeløp(LocalDate.of(2013, 5, 1), LocalDate.of(2014, 4, 30), GRUNNBELØP_2013, GSNITT_2013),
        new Grunnbeløp(LocalDate.of(2014, 5, 1), LocalDate.of(2015, 4, 30), GRUNNBELØP_2014, GSNITT_2014),
        new Grunnbeløp(LocalDate.of(2015, 5, 1), LocalDate.of(2016, 4, 30), GRUNNBELØP_2015, GSNITT_2015),
        new Grunnbeløp(LocalDate.of(2016, 5, 1), LocalDate.of(2017, 4, 30), GRUNNBELØP_2016, GSNITT_2016),
        new Grunnbeløp(LocalDate.of(2017, 5, 1), LocalDate.of(2018, 4, 30), GRUNNBELØP_2017, GSNITT_2017),
        new Grunnbeløp(LocalDate.of(2018, 5, 1), LocalDate.of(2019, 4, 30), GRUNNBELØP_2018, GSNITT_2018),
        new Grunnbeløp(LocalDate.of(2019, 5, 1), LocalDate.of(2020, 4, 30), GRUNNBELØP_2019, GSNITT_2019)
    );

    public static Long finnGrunnbeløp(LocalDate dato) {
        return finnGyldigGrunnbeløpPåDato(dato)
            .getGVerdi();
    }

    public static Long finnGrunnbeløpsnitt(LocalDate dato) {
        return finnGSNITTPåDato(dato)
            .getGSnitt();
    }

    private static Grunnbeløp finnGyldigGrunnbeløpPåDato(LocalDate dato) {
        return GRUNNBELØPSATSER.stream().filter(gb -> Periode.of(gb.getFom(), gb.getTom()).inneholder(dato))
            .findFirst().orElseThrow();
    }

    private static Grunnbeløp finnGSNITTPåDato(LocalDate dato) {
        int year = dato.getYear();
        return GRUNNBELØPSATSER.stream().filter(gb -> year == gb.getFom().getYear())
            .findFirst().orElseThrow();
    }
}
