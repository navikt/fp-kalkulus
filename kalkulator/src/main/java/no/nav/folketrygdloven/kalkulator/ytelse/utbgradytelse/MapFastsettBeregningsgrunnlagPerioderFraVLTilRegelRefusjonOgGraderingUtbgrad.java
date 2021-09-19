package no.nav.folketrygdloven.kalkulator.ytelse.utbgradytelse;

import static no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.kodeverk.MapUttakArbeidTypeTilAktivitetStatusV2.mapAktivitetStatus;
import static no.nav.folketrygdloven.kalkulator.ytelse.utbgradytelse.FinnAndelsnrForAktivitetMedUtbgrad.finnAndelsnrIFørstePeriode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Gradering;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Arbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.AktivitetStatusV2;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.AndelGradering;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.AndelGraderingImpl;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.ArbeidsforholdOgInntektsmelding;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.PeriodeModell;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.SplittetPeriode;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.MapArbeidsforholdFraVLTilRegel;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.periodisering.FinnAnsettelsesPeriode;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.periodisering.FinnFørsteDagEtterBekreftetPermisjon;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.periodisering.MapFastsettBeregningsgrunnlagPerioderFraVLTilRegelRefusjonOgGradering;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.periodisering.MapPeriodisertBruttoBeregningsgrunnlag;
import no.nav.folketrygdloven.kalkulator.felles.FinnYrkesaktiviteterForBeregningTjeneste;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.input.UtbetalingsgradGrunnlag;
import no.nav.folketrygdloven.kalkulator.input.YtelsespesifiktGrunnlag;
import no.nav.folketrygdloven.kalkulator.modell.behandling.KoblingReferanse;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.AktivitetsAvtaleDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetFilterDto;
import no.nav.folketrygdloven.kalkulator.modell.svp.PeriodeMedUtbetalingsgradDto;
import no.nav.folketrygdloven.kalkulator.modell.svp.UtbetalingsgradArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.svp.UtbetalingsgradPrAktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.modell.uttak.UttakArbeidType;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;

public class MapFastsettBeregningsgrunnlagPerioderFraVLTilRegelRefusjonOgGraderingUtbgrad
        extends MapFastsettBeregningsgrunnlagPerioderFraVLTilRegelRefusjonOgGradering {

    /** Finner gyldige perioder for refusjon basert på perioder med utbetalingsgrad
     *
     *
     * @param startdatoPermisjon Startdato for permisjonen for ytelse søkt for
     * @param ytelsespesifiktGrunnlag Ytelsesspesifikt grunnlag
     * @param ya                      Yrkesaktivitet
     * @param beregningsgrunnlag Beregningsgrunnlag
     * @return Gyldige perioder for refusjon
     */
    @Override
    protected List<Intervall> finnGyldigeRefusjonPerioder(LocalDate startdatoPermisjon, YtelsespesifiktGrunnlag ytelsespesifiktGrunnlag, YrkesaktivitetDto ya, BeregningsgrunnlagDto beregningsgrunnlag) {

        if (ytelsespesifiktGrunnlag instanceof UtbetalingsgradGrunnlag) {
            var utbetalingsgradGrunnlag = (UtbetalingsgradGrunnlag) ytelsespesifiktGrunnlag;
            var perioderMedUtbetaling = utbetalingsgradGrunnlag.finnUtbetalingsgraderForArbeid(ya.getArbeidsgiver(), ya.getArbeidsforholdRef())
                    .stream()
                    .filter(p -> p.getUtbetalingsgrad() != null && p.getUtbetalingsgrad().compareTo(BigDecimal.ZERO) > 0)
                    .map(PeriodeMedUtbetalingsgradDto::getPeriode)
                    .collect(Collectors.toList());
            return perioderMedUtbetaling;
        }
        return ya.getAlleAktivitetsAvtaler().stream().filter(AktivitetsAvtaleDto::erAnsettelsesPeriode).map(AktivitetsAvtaleDto::getPeriode).collect(Collectors.toList());
    }

    @Override
    protected Optional<LocalDate> utledStartdatoPermisjon(Input input,
                                                          LocalDate skjæringstidspunktBeregning,
                                                          YrkesaktivitetDto ya,
                                                          Periode ansettelsesPeriode, Optional<InntektsmeldingDto> inntektsmelding) {
        Optional<LocalDate> førsteSøktePermisjonsdag = finnFørsteSøktePermisjonsdag(input.getBeregningsgrunnlagInput(), ya, ansettelsesPeriode);
        return førsteSøktePermisjonsdag.map(dato -> skjæringstidspunktBeregning.isAfter(dato) ? skjæringstidspunktBeregning : dato);
    }

    @Override
    protected Optional<LocalDate> finnFørsteSøktePermisjonsdag(BeregningsgrunnlagInput input, YrkesaktivitetDto ya, Periode ansettelsesPeriode) {
        var grunnlag = input.getBeregningsgrunnlagGrunnlag();
        LocalDate skjæringstidspunktBeregning = grunnlag.getBeregningsgrunnlag()
                .orElseThrow(() -> new IllegalStateException("Skal ha beregningsgrunnlag"))
                .getSkjæringstidspunkt();
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

    @Override
    protected PeriodeModell mapPeriodeModell(BeregningsgrunnlagInput input,
                                             BeregningsgrunnlagDto vlBeregningsgrunnlag,
                                             YrkesaktivitetFilterDto filter,
                                             LocalDate skjæringstidspunkt,
                                             List<SplittetPeriode> eksisterendePerioder,
                                             List<ArbeidsforholdOgInntektsmelding> regelInntektsmeldinger,
                                             List<no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.AndelGradering> regelAndelGraderinger) {
        return PeriodeModell.builder()
                .medSkjæringstidspunkt(skjæringstidspunkt)
                .medGrunnbeløp(vlBeregningsgrunnlag.getGrunnbeløp().getVerdi())
                .medInntektsmeldinger(regelInntektsmeldinger)
                .medAndelGraderinger(regelAndelGraderinger)
                .medEndringISøktYtelse(mapUtbetalingsgradPerioder(input, vlBeregningsgrunnlag, filter))
                .medEksisterendePerioder(eksisterendePerioder)
                .medPeriodisertBruttoBeregningsgrunnlag(MapPeriodisertBruttoBeregningsgrunnlag.map(vlBeregningsgrunnlag))
                .build();
    }

    private boolean erAnsattIPerioden(KoblingReferanse ref,
                                      UtbetalingsgradArbeidsforholdDto utbetalingsgradPrAktivitetDto,
                                      YrkesaktivitetFilterDto filter) {
        if (!utbetalingsgradPrAktivitetDto.getUttakArbeidType().equals(UttakArbeidType.ORDINÆRT_ARBEID)) {
            // Antar SN og FL er aktiv i hele uttaksperioden
            return true;
        }
        if (utbetalingsgradPrAktivitetDto.getArbeidsgiver().isEmpty()) {
            throw new IllegalArgumentException("Forventer arbeidsgiver for aktivitettype " + utbetalingsgradPrAktivitetDto.getUttakArbeidType());
        }
        Optional<YrkesaktivitetDto> yrkesaktivitet = FinnYrkesaktiviteterForBeregningTjeneste.finnAlleYrkesaktiviteterInkludertFjernetIOverstyring(ref, filter)
                .stream()
                .filter(ya -> ya.gjelderFor(utbetalingsgradPrAktivitetDto.getArbeidsgiver().get(), utbetalingsgradPrAktivitetDto.getInternArbeidsforholdRef()))
                .findFirst();
        if (yrkesaktivitet.isEmpty()) {
            return false;
        }
        Optional<Periode> ansettelsesPeriodeSomSlutterVedEllerEtterStp = FinnAnsettelsesPeriode.finnMinMaksPeriode(filter.getAnsettelsesPerioder(yrkesaktivitet.get()),
                ref.getSkjæringstidspunktBeregning());
        return ansettelsesPeriodeSomSlutterVedEllerEtterStp.isPresent();
    }

    private List<AndelGradering> mapUtbetalingsgradPerioder(BeregningsgrunnlagInput input, BeregningsgrunnlagDto vlBeregningsgrunnlag, YrkesaktivitetFilterDto filter) {
        List<UtbetalingsgradPrAktivitetDto> utbetalingsgradPrAktivitet = ((UtbetalingsgradGrunnlag) input.getYtelsespesifiktGrunnlag()).getUtbetalingsgradPrAktivitet();
        return mapTilrettelegginger(input.getKoblingReferanse(), utbetalingsgradPrAktivitet, vlBeregningsgrunnlag, filter);
    }

    private AndelGradering mapUttak(KoblingReferanse ref, YrkesaktivitetFilterDto filter, UtbetalingsgradPrAktivitetDto tilrettelegging, BeregningsgrunnlagDto vlBeregningsgrunnlag) {
        var tilretteleggingArbeidsforhold = tilrettelegging.getUtbetalingsgradArbeidsforhold();
        AktivitetStatusV2 tilretteleggingAktivitetStatus = mapAktivitetStatus(tilretteleggingArbeidsforhold.getUttakArbeidType());

        AndelGraderingImpl.Builder builder = no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.AndelGraderingImpl.builder()
                .medAktivitetStatus(tilretteleggingAktivitetStatus)
                .medAndelsnr(finnAndelsnrIFørstePeriode(vlBeregningsgrunnlag, tilretteleggingArbeidsforhold).orElse(null));

        mapArbeidsforholdMedPeriode(ref, filter, tilretteleggingArbeidsforhold)
                .ifPresent(builder::medArbeidsforhold);
        List<Gradering> graderinger = tilrettelegging.getPeriodeMedUtbetalingsgrad().stream()
                .filter(p -> !p.getPeriode().getTomDato().isBefore(vlBeregningsgrunnlag.getSkjæringstidspunkt()))
                .map(periode -> mapUttakPeriode(periode, vlBeregningsgrunnlag.getSkjæringstidspunkt()))
                .collect(Collectors.toList());
        builder.medGraderinger(graderinger);
        return builder.build();
    }

    private Optional<Arbeidsforhold> mapArbeidsforholdMedPeriode(KoblingReferanse ref, YrkesaktivitetFilterDto filter, UtbetalingsgradArbeidsforholdDto tilretteleggingArbeidsforhold) {
        return tilretteleggingArbeidsforhold.getArbeidsgiver().map(arbeidsgiver -> {
            InternArbeidsforholdRefDto tilretteleggingArbeidsforholdRef = tilretteleggingArbeidsforhold.getInternArbeidsforholdRef() == null ? InternArbeidsforholdRefDto.nullRef() : tilretteleggingArbeidsforhold.getInternArbeidsforholdRef();
            // Finner yrkesaktiviteter inkludert fjernet i overstyring siden vi kun er interessert i å lage nye arbeidsforhold for nye aktiviteter (Disse kan ikke fjernes)
            Optional<YrkesaktivitetDto> yrkesaktivitet = FinnYrkesaktiviteterForBeregningTjeneste.finnAlleYrkesaktiviteterInkludertFjernetIOverstyring(ref, filter)
                    .stream()
                    .filter(ya -> ya.gjelderFor(arbeidsgiver, tilretteleggingArbeidsforholdRef))
                    .findFirst();
            Arbeidsforhold arbeidsforhold = MapArbeidsforholdFraVLTilRegel.mapArbeidsforhold(arbeidsgiver, tilretteleggingArbeidsforholdRef);
            yrkesaktivitet.ifPresent(ya -> Arbeidsforhold.builder(arbeidsforhold)
                    .medAnsettelsesPeriode(FinnAnsettelsesPeriode.getMinMaksPeriode(filter.getAnsettelsesPerioder(ya),
                            ref.getSkjæringstidspunktBeregning())));
            return arbeidsforhold;
        });
    }

    private Gradering mapUttakPeriode(PeriodeMedUtbetalingsgradDto periodeMedUtbetalingsgrad, LocalDate skjæringstidspunkt) {
        Periode periode;
        Intervall utbetalingsgradPeriode = periodeMedUtbetalingsgrad.getPeriode();
        if (utbetalingsgradPeriode.getFomDato().isBefore(skjæringstidspunkt)) {
            periode = Periode.of(skjæringstidspunkt, utbetalingsgradPeriode.getTomDato());
        } else {
            periode = Periode.of(utbetalingsgradPeriode.getFomDato(), utbetalingsgradPeriode.getTomDato());
        }
        return new Gradering(periode, periodeMedUtbetalingsgrad.getUtbetalingsgrad());
    }

    // TODO: Denne bør vere private
    List<AndelGradering> mapTilrettelegginger(KoblingReferanse ref, List<UtbetalingsgradPrAktivitetDto> utbetalingsgradPrAktivitet, BeregningsgrunnlagDto vlBeregningsgrunnlag, YrkesaktivitetFilterDto filter) {
        return utbetalingsgradPrAktivitet.stream()
                .filter(dto -> erAnsattIPerioden(ref, dto.getUtbetalingsgradArbeidsforhold(),filter))
                .map(a -> mapUttak(ref, filter, a, vlBeregningsgrunnlag)).collect(Collectors.toList());
    }

}
