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
    protected Optional<LocalDate> utledStartdatoPermisjon(Input input,
                                                          LocalDate skjæringstidspunktBeregning,
                                                          InntektsmeldingDto inntektsmelding,
                                                          Set<YrkesaktivitetDto> yrkesaktiviteterForIM, PermisjonFilter permisjonFilter) {
        var førsteSøktePermisjonsdag = finnFørsteSøktePermisjonsdag(input.getBeregningsgrunnlagInput(), yrkesaktiviteterForIM, permisjonFilter);
        return førsteSøktePermisjonsdag.map(dato -> skjæringstidspunktBeregning.isAfter(dato) ? skjæringstidspunktBeregning : dato);
    }

    private Optional<LocalDate> finnFørsteSøktePermisjonsdag(BeregningsgrunnlagInput input, Collection<YrkesaktivitetDto> yrkesaktiviteter, PermisjonFilter permisjonFilter) {
        LocalDate skjæringstidspunktBeregning = input.getBeregningsgrunnlag().getSkjæringstidspunkt();
        List<AktivitetsAvtaleDto> alleAnsattperioderForInntektsmeldingEtterStartAvBeregning = finnAnsattperioderForYrkesaktiviteter(yrkesaktiviteter, input.getSkjæringstidspunktOpptjening());
        Periode ansettelsesPeriode = FinnAnsettelsesPeriode.getMinMaksPeriode(alleAnsattperioderForInntektsmeldingEtterStartAvBeregning, input.getSkjæringstidspunktOpptjening());
        Optional<LocalDate> førstedagEtterBekreftetPermisjonOpt = FinnFørsteDagEtterPermisjon.finn(yrkesaktiviteter, ansettelsesPeriode,
                skjæringstidspunktBeregning, permisjonFilter);
        if (førstedagEtterBekreftetPermisjonOpt.isEmpty()) {
            return Optional.empty();
        }
        UtbetalingsgradGrunnlag utbetalingsgradGrunnlag = input.getYtelsespesifiktGrunnlag();
        var utbetalingsgrader = yrkesaktiviteter.stream().map(ya -> utbetalingsgradGrunnlag.finnUtbetalingsgraderForArbeid(ya.getArbeidsgiver(), ya.getArbeidsforholdRef()))
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

        LocalDate førsteDagEtterPermisjon = førstedagEtterBekreftetPermisjonOpt.get();
        LocalDate førsteDatoMedUtbetaling = førsteDatoMedUtbetalingOpt.get();
        return førsteDagEtterPermisjon.isAfter(førsteDatoMedUtbetaling) ? førstedagEtterBekreftetPermisjonOpt : førsteDatoMedUtbetalingOpt;
    }

}
