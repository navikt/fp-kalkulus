package no.nav.folketrygdloven.kalkulator.ytelse.utbgradytelse;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.Optional;
import java.util.Set;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.periodisering.FinnAnsettelsesPeriode;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.periodisering.FinnFørsteDagEtterPermisjon;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.periodisering.refusjon.MapRefusjonPerioderFraVLTilRegel;
import no.nav.folketrygdloven.kalkulator.felles.frist.ArbeidsgiverRefusjonskravTjeneste;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.input.UtbetalingsgradGrunnlag;
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
                                                          Set<YrkesaktivitetDto> yrkesaktiviteter, PermisjonFilter permisjonFilter) {
        Optional<LocalDate> førsteSøktePermisjonsdag = yrkesaktiviteter.stream()
                .map(ya -> finnFørsteSøktePermisjonsdag(input.getBeregningsgrunnlagInput(), ya, permisjonFilter)).filter(Optional::isPresent)
                .map(Optional::get)
                .min(Comparator.naturalOrder());
        return førsteSøktePermisjonsdag.map(dato -> skjæringstidspunktBeregning.isAfter(dato) ? skjæringstidspunktBeregning : dato);
    }

    private Optional<LocalDate> finnFørsteSøktePermisjonsdag(BeregningsgrunnlagInput input, YrkesaktivitetDto ya, PermisjonFilter permisjonFilter) {
        LocalDate skjæringstidspunktBeregning = input.getBeregningsgrunnlag().getSkjæringstidspunkt();
        Periode ansettelsesPeriode = FinnAnsettelsesPeriode.getMinMaksPeriode(ya.getAlleAnsettelsesperioder(), skjæringstidspunktBeregning);
        Optional<LocalDate> førstedagEtterBekreftetPermisjonOpt = FinnFørsteDagEtterPermisjon.finn(ya, ansettelsesPeriode, skjæringstidspunktBeregning, permisjonFilter);
        if (førstedagEtterBekreftetPermisjonOpt.isEmpty()) {
            return Optional.empty();
        }
        UtbetalingsgradGrunnlag utbetalingsgradGrunnlag = input.getYtelsespesifiktGrunnlag();
        Optional<LocalDate> førsteDatoMedUtbetalingOpt = utbetalingsgradGrunnlag.finnUtbetalingsgraderForArbeid(ya.getArbeidsgiver(), ya.getArbeidsforholdRef())
                .stream()
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
