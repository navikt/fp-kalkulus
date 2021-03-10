package no.nav.folketrygdloven.kalkulator.ytelse.utbgradytelse;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.periodisering.FinnAnsettelsesPeriode;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.periodisering.FinnFørsteDagEtterBekreftetPermisjon;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.periodisering.refusjon.MapRefusjonPerioderFraVLTilRegel;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.input.UtbetalingsgradGrunnlag;
import no.nav.folketrygdloven.kalkulator.input.YtelsespesifiktGrunnlag;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.svp.PeriodeMedUtbetalingsgradDto;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;

public abstract class MapRefusjonPerioderFraVLTilRegelUtbgrad
        extends MapRefusjonPerioderFraVLTilRegel {

    /** Finner gyldige perioder for refusjon basert på perioder med utbetalingsgrad
     *
     *
     * @param startdatoPermisjon Startdato for permisjonen for ytelse søkt for
     * @param ytelsespesifiktGrunnlag Ytelsesspesifikt grunnlag
     * @param inntektsmelding                      inntektsmelding
     * @param beregningsgrunnlag
     * @return Gyldige perioder for refusjon
     */
    @Override
    protected List<Intervall> finnGyldigeRefusjonPerioder(LocalDate startdatoPermisjon,
                                                          YtelsespesifiktGrunnlag ytelsespesifiktGrunnlag,
                                                          InntektsmeldingDto inntektsmelding, BeregningsgrunnlagDto beregningsgrunnlag) {
        if (erMidlertidigInaktiv(beregningsgrunnlag)) {
            return Collections.emptyList();
        }
        if (ytelsespesifiktGrunnlag instanceof UtbetalingsgradGrunnlag) {
            var utbetalingsgradGrunnlag = (UtbetalingsgradGrunnlag) ytelsespesifiktGrunnlag;
            var perioderMedUtbetaling = utbetalingsgradGrunnlag.finnUtbetalingsgraderForArbeid(inntektsmelding.getArbeidsgiver(), inntektsmelding.getArbeidsforholdRef())
                    .stream()
                    .filter(p -> p.getUtbetalingsgrad() != null && p.getUtbetalingsgrad().compareTo(BigDecimal.ZERO) > 0)
                    .map(PeriodeMedUtbetalingsgradDto::getPeriode)
                    .collect(Collectors.toList());
            return perioderMedUtbetaling;
        }
        return List.of();
    }

    private boolean erMidlertidigInaktiv(BeregningsgrunnlagDto beregningsgrunnlag) {
        return beregningsgrunnlag.getAktivitetStatuser().stream().anyMatch(a -> a.getAktivitetStatus().equals(AktivitetStatus.MIDLERTIDIG_INAKTIV));
    }

    @Override
    protected Optional<LocalDate> utledStartdatoPermisjon(Input input,
                                                          LocalDate skjæringstidspunktBeregning,
                                                          InntektsmeldingDto inntektsmelding,
                                                          Set<YrkesaktivitetDto> yrkesaktiviteter) {
        Optional<LocalDate> førsteSøktePermisjonsdag = yrkesaktiviteter.stream()
                .map(ya -> finnFørsteSøktePermisjonsdag(input.getBeregningsgrunnlagInput(), ya))             .filter(Optional::isPresent)
                .map(Optional::get)
                .min(Comparator.naturalOrder());
        return førsteSøktePermisjonsdag.map(dato -> skjæringstidspunktBeregning.isAfter(dato) ? skjæringstidspunktBeregning : dato);
    }

    private Optional<LocalDate> finnFørsteSøktePermisjonsdag(BeregningsgrunnlagInput input, YrkesaktivitetDto ya) {
        LocalDate skjæringstidspunktBeregning = input.getBeregningsgrunnlag().getSkjæringstidspunkt();
        Periode ansettelsesPeriode = FinnAnsettelsesPeriode.getMinMaksPeriode(ya.getAlleAnsettelsesperioder(), skjæringstidspunktBeregning);
        Optional<LocalDate> førstedagEtterBekreftetPermisjonOpt = FinnFørsteDagEtterBekreftetPermisjon.finn(input.getIayGrunnlag(), ya, ansettelsesPeriode, skjæringstidspunktBeregning);
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
