package no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.periodisering;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.kalkulator.felles.BeregningstidspunktTjeneste;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.ArbeidsforholdOverstyringDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetFilterDto;

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
            .filter(periode -> !periode.getTom().isBefore(BeregningstidspunktTjeneste.finnBeregningstidspunkt(skjæringstidspunktBeregning)))
            .collect(Collectors.toList());
        if (erAktivDagenFørSkjæringstidspunktet(skjæringstidspunktBeregning, ansettelsesPerioder)) {
            return liggerIkkeIBGAktivitetAggregat(yrkesaktivitet, aktivitetAggregatEntitet) && varIkkeIPermisjonPåSkjæringstidspunkt(filter.getBekreftedePermisjonerForYrkesaktivitet(yrkesaktivitet), skjæringstidspunktBeregning);
        }
        return false;
    }

    private static boolean varIkkeIPermisjonPåSkjæringstidspunkt(Collection<ArbeidsforholdOverstyringDto> bekreftedePermisjonerForYa, LocalDate skjæringstidspunktBeregning) {
        return bekreftedePermisjonerForYa.stream()
                .filter(os -> os.getBekreftetPermisjon().isPresent())
                .noneMatch(os -> os.getBekreftetPermisjon().get().getPeriode().inkluderer(skjæringstidspunktBeregning));
    }

    private static boolean liggerIkkeIBGAktivitetAggregat(YrkesaktivitetDto yrkesaktivitet, BeregningAktivitetAggregatDto aktivitetAggregatEntitet) {
        return aktivitetAggregatEntitet.getBeregningAktiviteter().stream()
            .noneMatch(beregningAktivitet -> beregningAktivitet.gjelderFor(yrkesaktivitet.getArbeidsgiver(), yrkesaktivitet.getArbeidsforholdRef()));
    }

    private static boolean erAktivDagenFørSkjæringstidspunktet(LocalDate skjæringstidspunktBeregning,
                                                               List<Periode> ansettelsesPerioder) {
        return ansettelsesPerioder.stream()
                .anyMatch(periode -> periode.inneholder(BeregningstidspunktTjeneste.finnBeregningstidspunkt(skjæringstidspunktBeregning)));
    }
}
