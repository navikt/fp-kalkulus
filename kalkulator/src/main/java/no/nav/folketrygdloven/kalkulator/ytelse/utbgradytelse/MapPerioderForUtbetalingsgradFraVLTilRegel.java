package no.nav.folketrygdloven.kalkulator.ytelse.utbgradytelse;

import static no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.kodeverk.MapUttakArbeidTypeTilAktivitetStatusV2.mapAktivitetStatus;
import static no.nav.folketrygdloven.kalkulator.ytelse.utbgradytelse.FinnTidslinjeForErNyAktivitet.finnTidslinjeForNyAktivitet;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Arbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.AktivitetStatusV2;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.utbetalingsgrad.AndelUtbetalingsgrad;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.utbetalingsgrad.PeriodeModellUtbetalingsgrad;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.utbetalingsgrad.Utbetalingsgrad;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.SplittetPeriode;
import no.nav.folketrygdloven.kalkulator.KonfigurasjonVerdi;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.MapArbeidsforholdFraVLTilRegel;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.periodisering.FinnAnsettelsesPeriode;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.periodisering.MapPeriodisertBruttoBeregningsgrunnlag;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.periodisering.MapSplittetPeriodeFraVLTilRegel;
import no.nav.folketrygdloven.kalkulator.felles.FinnYrkesaktiviteterForBeregningTjeneste;
import no.nav.folketrygdloven.kalkulator.felles.inntektgradering.SimulerTilkomneAktiviteterTjeneste;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.input.UtbetalingsgradGrunnlag;
import no.nav.folketrygdloven.kalkulator.modell.behandling.KoblingReferanse;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetFilterDto;
import no.nav.folketrygdloven.kalkulator.modell.svp.AktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.svp.PeriodeMedUtbetalingsgradDto;
import no.nav.folketrygdloven.kalkulator.modell.svp.UtbetalingsgradPrAktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.StatusOgArbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.uttak.UttakArbeidType;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.fpsak.tidsserie.LocalDateSegment;
import no.nav.fpsak.tidsserie.LocalDateTimeline;


public class MapPerioderForUtbetalingsgradFraVLTilRegel {

    public static PeriodeModellUtbetalingsgrad map(BeregningsgrunnlagInput input,
                                                   BeregningsgrunnlagDto beregningsgrunnlag) {
        var iayGrunnlag = input.getIayGrunnlag();
        var filter = new YrkesaktivitetFilterDto(iayGrunnlag.getArbeidsforholdInformasjon(), iayGrunnlag.getAktørArbeidFraRegister());
        LocalDate skjæringstidspunkt = beregningsgrunnlag.getSkjæringstidspunkt();
        var eksisterendePerioder = beregningsgrunnlag.getBeregningsgrunnlagPerioder().stream()
                .map(MapSplittetPeriodeFraVLTilRegel::map).collect(Collectors.toList());

        return mapPeriodeModell(input,
                beregningsgrunnlag,
                filter,
                skjæringstidspunkt,
                eksisterendePerioder
        );
    }


    private static PeriodeModellUtbetalingsgrad mapPeriodeModell(BeregningsgrunnlagInput input,
                                                    BeregningsgrunnlagDto vlBeregningsgrunnlag,
                                                    YrkesaktivitetFilterDto filter,
                                                    LocalDate skjæringstidspunkt,
                                                    List<SplittetPeriode> eksisterendePerioder) {
        return PeriodeModellUtbetalingsgrad.builder()
                .medSkjæringstidspunkt(skjæringstidspunkt)
                .medGrunnbeløp(vlBeregningsgrunnlag.getGrunnbeløp().getVerdi())
                .medEndringISøktYtelse(mapUtbetalingsgradPerioder(input, vlBeregningsgrunnlag, filter))
                .medEksisterendePerioder(eksisterendePerioder)
                .medPeriodisertBruttoBeregningsgrunnlag(MapPeriodisertBruttoBeregningsgrunnlag.map(vlBeregningsgrunnlag))
                .build();
    }

    private static boolean erAnsattIPerioden(KoblingReferanse ref,
                                             AktivitetDto utbetalingsgradPrAktivitetDto,
                                             YrkesaktivitetFilterDto filter) {
        if (!utbetalingsgradPrAktivitetDto.getUttakArbeidType().equals(UttakArbeidType.ORDINÆRT_ARBEID)) {
            // Antar SN, FL og IKKE_YREKSAKTIV er aktiv i hele uttaksperioden
            return true;
        }
        if (utbetalingsgradPrAktivitetDto.getArbeidsgiver().isEmpty()) {
            throw new IllegalArgumentException("Forventer arbeidsgiver for aktivitettype " + utbetalingsgradPrAktivitetDto.getUttakArbeidType());
        }
        Optional<YrkesaktivitetDto> yrkesaktivitet = FinnYrkesaktiviteterForBeregningTjeneste.finnAlleYrkesaktiviteterInkludertFjernetIOverstyring(filter, ref.getSkjæringstidspunktBeregning())
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

    private static List<AndelUtbetalingsgrad> mapUtbetalingsgradPerioder(BeregningsgrunnlagInput input, BeregningsgrunnlagDto vlBeregningsgrunnlag, YrkesaktivitetFilterDto filter) {
        List<UtbetalingsgradPrAktivitetDto> utbetalingsgradPrAktivitet = ((UtbetalingsgradGrunnlag) input.getYtelsespesifiktGrunnlag()).getUtbetalingsgradPrAktivitet();
        var tilkomneAktiviteterTidslinje = SimulerTilkomneAktiviteterTjeneste.utledTilkommetAktivitetPerioder(input);
        return mapTilrettelegginger(input.getKoblingReferanse(), utbetalingsgradPrAktivitet, vlBeregningsgrunnlag, filter, tilkomneAktiviteterTidslinje);
    }

    private static AndelUtbetalingsgrad mapUttak(KoblingReferanse ref,
                                                 YrkesaktivitetFilterDto filter,
                                                 UtbetalingsgradPrAktivitetDto tilrettelegging,
                                                 BeregningsgrunnlagDto vlBeregningsgrunnlag,
                                                 List<UtbetalingsgradPrAktivitetDto> allePerioder,
                                                 LocalDateTimeline<Set<StatusOgArbeidsgiver>> tilkomneAktiviteterTidslinje) {
        var tilretteleggingArbeidsforhold = tilrettelegging.getUtbetalingsgradArbeidsforhold();
        AktivitetStatusV2 tilretteleggingAktivitetStatus = mapAktivitetStatus(tilretteleggingArbeidsforhold, allePerioder);

        AndelUtbetalingsgrad.Builder builder = no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.utbetalingsgrad.AndelUtbetalingsgrad.builder()
                .medAktivitetStatus(tilretteleggingAktivitetStatus)
                .medNyAktivitetTidslinje(finnTidslinjeForNyAktivitet(vlBeregningsgrunnlag, tilretteleggingArbeidsforhold.getUttakArbeidType(), tilretteleggingArbeidsforhold.getInternArbeidsforholdRef(), tilretteleggingArbeidsforhold.getArbeidsgiver()));

        mapArbeidsforholdMedPeriode(ref, filter, tilretteleggingArbeidsforhold)
                .ifPresent(builder::medArbeidsforhold);
        List<Utbetalingsgrad> utbetalingsgrader = tilrettelegging.getPeriodeMedUtbetalingsgrad().stream()
                .filter(p -> !p.getPeriode().getTomDato().isBefore(vlBeregningsgrunnlag.getSkjæringstidspunkt()))
                .map(periode -> mapUttakPeriode(periode, vlBeregningsgrunnlag.getSkjæringstidspunkt(), erTilkommetIPeriode(tilrettelegging.getUtbetalingsgradArbeidsforhold(), tilkomneAktiviteterTidslinje, periode)))
                .collect(Collectors.toList());
        builder.medUtbetalingsgrader(utbetalingsgrader);
        return builder.build();
    }

    private static boolean erTilkommetIPeriode(AktivitetDto utbetalingsgradArbeidsforhold, LocalDateTimeline<Set<StatusOgArbeidsgiver>> tilkomneAktiviteterTidslinje, PeriodeMedUtbetalingsgradDto periode) {
        return tilkomneAktiviteterTidslinje.stream()
                .filter(interval -> interval.overlapper(LocalDateSegment.emptySegment(periode.getPeriode().getFomDato(), periode.getPeriode().getTomDato())))
                .anyMatch(interval -> interval.getValue().stream()
                        .anyMatch(aktivitet -> Objects.equals(aktivitet.arbeidsgiver(), utbetalingsgradArbeidsforhold.getArbeidsgiver().orElse(null))
                        && AktivitetStatusMatcher.matcherStatus(aktivitet.aktivitetStatus(), utbetalingsgradArbeidsforhold.getUttakArbeidType())));
    }

    private static Optional<Arbeidsforhold> mapArbeidsforholdMedPeriode(KoblingReferanse ref, YrkesaktivitetFilterDto filter, AktivitetDto tilretteleggingArbeidsforhold) {
        return tilretteleggingArbeidsforhold.getArbeidsgiver().map(arbeidsgiver -> {
            InternArbeidsforholdRefDto tilretteleggingArbeidsforholdRef = tilretteleggingArbeidsforhold.getInternArbeidsforholdRef() == null ? InternArbeidsforholdRefDto.nullRef() : tilretteleggingArbeidsforhold.getInternArbeidsforholdRef();
            // Finner yrkesaktiviteter inkludert fjernet i overstyring siden vi kun er interessert i å lage nye arbeidsforhold for nye aktiviteter (Disse kan ikke fjernes)
            Optional<YrkesaktivitetDto> yrkesaktivitet = FinnYrkesaktiviteterForBeregningTjeneste.finnAlleYrkesaktiviteterInkludertFjernetIOverstyring(filter, ref.getSkjæringstidspunktBeregning())
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

    private static Utbetalingsgrad mapUttakPeriode(PeriodeMedUtbetalingsgradDto periodeMedUtbetalingsgrad, LocalDate skjæringstidspunkt, boolean erTilkommetIPerioden) {
        Periode periode;
        Intervall utbetalingsgradPeriode = periodeMedUtbetalingsgrad.getPeriode();
        if (utbetalingsgradPeriode.getFomDato().isBefore(skjæringstidspunkt)) {
            periode = Periode.of(skjæringstidspunkt, utbetalingsgradPeriode.getTomDato());
        } else {
            periode = Periode.of(utbetalingsgradPeriode.getFomDato(), utbetalingsgradPeriode.getTomDato());
        }
        var aktivitetsgrad = periodeMedUtbetalingsgrad.getAktivitetsgrad();
        if (erTilkommetIPerioden && aktivitetsgrad.isPresent() && KonfigurasjonVerdi.get("GRADERING_MOT_INNTEKT", false)) {
            return new Utbetalingsgrad(periode, BigDecimal.valueOf(100).subtract(aktivitetsgrad.get()));
        }
        return new Utbetalingsgrad(periode, periodeMedUtbetalingsgrad.getUtbetalingsgrad());
    }

    // TODO: Denne bør vere private
    static List<AndelUtbetalingsgrad> mapTilrettelegginger(KoblingReferanse ref,
                                                                   List<UtbetalingsgradPrAktivitetDto> utbetalingsgradPrAktivitet,
                                                                   BeregningsgrunnlagDto vlBeregningsgrunnlag,
                                                                   YrkesaktivitetFilterDto filter,
                                                                   LocalDateTimeline<Set<StatusOgArbeidsgiver>> tilkomneAktiviteterTidslinje) {
        return utbetalingsgradPrAktivitet.stream()
                .filter(dto -> erAnsattIPerioden(ref, dto.getUtbetalingsgradArbeidsforhold(),filter))
                .map(a -> mapUttak(ref, filter, a, vlBeregningsgrunnlag, utbetalingsgradPrAktivitet, tilkomneAktiviteterTidslinje)).collect(Collectors.toList());
    }

}
