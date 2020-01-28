package no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.periodisering;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetFilterDto;
import no.nav.folketrygdloven.kalkulator.regelmodell.Periode;

public class ErFjernetIOverstyrt {
    private ErFjernetIOverstyrt() {
        // skjul public constructor
    }

    public static boolean erFjernetIOverstyrt(YrkesaktivitetFilterDto filter,
                                              YrkesaktivitetDto yrkesaktivitet,
                                              BeregningAktivitetAggregatDto aktivitetAggregatEntitet,
                                              LocalDate skjæringstidspunktBeregning) {

        List<Periode> ansettelsesPerioder = filter.getAnsettelsesPerioder(yrkesaktivitet).stream()
            .map(aa -> new Periode(aa.getPeriode().getFomDato(), aa.getPeriode().getTomDato()))
            .filter(periode -> !periode.getTom().isBefore(skjæringstidspunktBeregning.minusDays(1)))
            .collect(Collectors.toList());
        if (erAktivDagenFørSkjæringstidspunktet(skjæringstidspunktBeregning, ansettelsesPerioder)) {
            return erFjernet(yrkesaktivitet, aktivitetAggregatEntitet);
        }
        return false;
    }

    private static boolean erFjernet(YrkesaktivitetDto yrkesaktivitet, BeregningAktivitetAggregatDto beregningAktivitetAggregat) {
        return beregningAktivitetAggregat.getBeregningAktiviteter().stream()
            .noneMatch(beregningAktivitet -> beregningAktivitet.gjelderFor(yrkesaktivitet.getArbeidsgiver(), yrkesaktivitet.getArbeidsforholdRef()));
    }

    private static boolean erAktivDagenFørSkjæringstidspunktet(LocalDate skjæringstidspunktBeregning, List<Periode> ansettelsesPerioder) {
        return ansettelsesPerioder.stream().anyMatch(periode -> periode.inneholder(skjæringstidspunktBeregning.minusDays(1)));
    }
}
