package no.nav.folketrygdloven.kalkulator.ytelse.utbgradytelse;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.periodisering.FinnAnsettelsesPeriode;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.periodisering.FinnFørsteDagEtterPermisjon;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.periodisering.refusjon.MapRefusjonPerioderFraVLTilRegel;
import no.nav.folketrygdloven.kalkulator.felles.frist.ArbeidsgiverRefusjonskravTjeneste;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.input.UtbetalingsgradGrunnlag;
import no.nav.folketrygdloven.kalkulator.input.YtelsespesifiktGrunnlag;
import no.nav.folketrygdloven.kalkulator.modell.iay.AktivitetsAvtaleDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.permisjon.PermisjonFilter;
import no.nav.folketrygdloven.kalkulator.modell.svp.PeriodeMedUtbetalingsgradDto;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;

public abstract class MapRefusjonPerioderFraVLTilRegelUtbgrad
        extends MapRefusjonPerioderFraVLTilRegel {

    public MapRefusjonPerioderFraVLTilRegelUtbgrad(ArbeidsgiverRefusjonskravTjeneste arbeidsgiverRefusjonskravTjeneste) {
        super(arbeidsgiverRefusjonskravTjeneste);
    }

    @Override
    protected Optional<LocalDate> utledStartdatoEtterPermisjon(LocalDate skjæringstidspunktBeregning,
                                                               InntektsmeldingDto inntektsmelding,
                                                               Set<YrkesaktivitetDto> yrkesaktiviteterForIM,
                                                               PermisjonFilter permisjonFilter,
                                                               YtelsespesifiktGrunnlag ytelsespesifiktGrunnlag) {
        var førsteSøktePermisjonsdag = finnFørsteSøktePermisjonsdag(
                yrkesaktiviteterForIM, permisjonFilter,
                skjæringstidspunktBeregning,
                (UtbetalingsgradGrunnlag) ytelsespesifiktGrunnlag);
        return førsteSøktePermisjonsdag.map(dato -> skjæringstidspunktBeregning.isAfter(dato) ? skjæringstidspunktBeregning : dato);
    }

    private Optional<LocalDate> finnFørsteSøktePermisjonsdag(Collection<YrkesaktivitetDto> yrkesaktiviteter,
                                                             PermisjonFilter permisjonFilter,
                                                             LocalDate skjæringstidspunkt,
                                                             UtbetalingsgradGrunnlag ytelsespesifiktGrunnlag) {
        var alleAnsattperioderForInntektsmeldingEtterStartAvBeregning = finnAnsattperioderForYrkesaktiviteter(yrkesaktiviteter, skjæringstidspunkt);
        var ansettelsesPeriode = FinnAnsettelsesPeriode.getMinMaksPeriode(alleAnsattperioderForInntektsmeldingEtterStartAvBeregning, skjæringstidspunkt);
        var førstedagEtterPermisjonOpt = FinnFørsteDagEtterPermisjon.finn(yrkesaktiviteter, ansettelsesPeriode, skjæringstidspunkt, permisjonFilter);
        if (førstedagEtterPermisjonOpt.isEmpty()) {
            return Optional.empty();
        }
        var utbetalingsgrader = yrkesaktiviteter.stream().map(ya -> ytelsespesifiktGrunnlag.finnUtbetalingsgraderForArbeid(ya.getArbeidsgiver(), ya.getArbeidsforholdRef()))
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
        Optional<LocalDate> førsteDatoMedUtbetalingOpt = utbetalingsgrader.stream()
                .filter(periodeMedUtbetalingsgradDto -> periodeMedUtbetalingsgradDto.getUtbetalingsgrad().compareTo(BigDecimal.ZERO) != 0)
                .map(PeriodeMedUtbetalingsgradDto::getPeriode)
                .map(Intervall::getFomDato)
                .min(Comparator.naturalOrder());

        if (førsteDatoMedUtbetalingOpt.isEmpty()) {
            return Optional.empty();
        }

        LocalDate førsteDagEtterPermisjon = førstedagEtterPermisjonOpt.get();
        LocalDate førsteDatoMedUtbetaling = førsteDatoMedUtbetalingOpt.get();
        return førsteDagEtterPermisjon.isAfter(førsteDatoMedUtbetaling) ? førstedagEtterPermisjonOpt : førsteDatoMedUtbetalingOpt;
    }

}
