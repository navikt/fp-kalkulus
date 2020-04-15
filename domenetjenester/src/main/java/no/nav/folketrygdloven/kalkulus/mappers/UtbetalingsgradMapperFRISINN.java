package no.nav.folketrygdloven.kalkulus.mappers;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.modell.svp.PeriodeMedUtbetalingsgradDto;
import no.nav.folketrygdloven.kalkulator.modell.svp.UtbetalingsgradArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.svp.UtbetalingsgradPrAktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.modell.uttak.UttakArbeidType;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagGrunnlagEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndel;
import no.nav.folketrygdloven.kalkulus.felles.v1.Periode;
import no.nav.folketrygdloven.kalkulus.iay.v1.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulus.opptjening.v1.OppgittEgenNæringDto;
import no.nav.folketrygdloven.kalkulus.opptjening.v1.OppgittPeriodeInntekt;

public class UtbetalingsgradMapperFRISINN {


    public static final BigDecimal MND_I_1_ÅR = BigDecimal.valueOf(12);

    /**
     * Finnner utbetalingsgrader for FRISINN
     *
     * @param iayGrunnlag InntektArbeidYtelseGrunnlag
     * @param beregningsgrunnlagGrunnlagEntitet Aktivt beregningsgrunnlag
     * @param idag Dagens dato
     * @return Liste med utbetalingsgrader for FRISINN
     */
    public static List<UtbetalingsgradPrAktivitetDto> map(InntektArbeidYtelseGrunnlagDto iayGrunnlag,
                                                          Optional<BeregningsgrunnlagGrunnlagEntitet> beregningsgrunnlagGrunnlagEntitet,
                                                          LocalDate idag) {
        Optional<LocalDate> stpOpt = finnSkjæringstidspunkt(beregningsgrunnlagGrunnlagEntitet);
        if (stpOpt.isEmpty()) {
            return Collections.emptyList();
        }
        LocalDate skjæringstidspunkt = stpOpt.get();
        List<BeregningsgrunnlagPrStatusOgAndel> andeler = finnAlleAndelerIFørstePeriode(beregningsgrunnlagGrunnlagEntitet);
        List<Intervall> inntektPerioder = periodiserMånedsvisFraSkjæringstidspunktTilNå(skjæringstidspunkt, idag);
        List<UtbetalingsgradPrAktivitetDto> utbetalingsgradPrAktivitetListe = new ArrayList<>();

        mapUtbetalingsgradNæring(iayGrunnlag, andeler, inntektPerioder, utbetalingsgradPrAktivitetListe);
        mapUtbetalingsgradFrilans(iayGrunnlag, andeler, inntektPerioder, utbetalingsgradPrAktivitetListe);
        return utbetalingsgradPrAktivitetListe;
    }

    private static void mapUtbetalingsgradFrilans(InntektArbeidYtelseGrunnlagDto iayGrunnlag,
                                                  List<BeregningsgrunnlagPrStatusOgAndel> andeler,
                                                  List<Intervall> inntektPerioder,
                                                  List<UtbetalingsgradPrAktivitetDto> utbetalingsgradPrAktivitetListe) {
        Optional<BeregningsgrunnlagPrStatusOgAndel> næringAndel = finnFrilansandel(andeler);
        næringAndel.filter(a -> a.getBruttoPrÅr() != null)
                .map(a -> mapUtbetalingsgraderForFrilans(iayGrunnlag, inntektPerioder, a))
                .ifPresent(utbetalingsgradPrAktivitetListe::add);
    }

    private static void mapUtbetalingsgradNæring(InntektArbeidYtelseGrunnlagDto iayGrunnlag, List<BeregningsgrunnlagPrStatusOgAndel> andeler, List<Intervall> inntektPerioder, List<UtbetalingsgradPrAktivitetDto> utbetalingsgradPrAktivitetListe) {
        Optional<BeregningsgrunnlagPrStatusOgAndel> næringAndel = finnNæringsandel(andeler);
        næringAndel.filter(a -> a.getBruttoPrÅr() != null)
                .map(a -> mapUtbetalingsgraderForNæring(iayGrunnlag, inntektPerioder, a))
                .ifPresent(utbetalingsgradPrAktivitetListe::add);
    }

    private static UtbetalingsgradPrAktivitetDto mapUtbetalingsgraderForNæring(InntektArbeidYtelseGrunnlagDto iayGrunnlag, List<Intervall> inntektPerioder, BeregningsgrunnlagPrStatusOgAndel a) {
        UtbetalingsgradArbeidsforholdDto snAktivitet = new UtbetalingsgradArbeidsforholdDto(null, InternArbeidsforholdRefDto.nullRef(), UttakArbeidType.SELVSTENDIG_NÆRINGSDRIVENDE);
        BigDecimal totalInntektVedStp = a.getBruttoPrÅr();
        List<PeriodeMedUtbetalingsgradDto> perioderMedUtbetalingsgrad = inntektPerioder.stream()
                .map(månedsperiode -> mapTilPeriodeMedUtbetalingsgrad(finnNæringsInntekter(iayGrunnlag), totalInntektVedStp, månedsperiode))
                .collect(Collectors.toList());
        return new UtbetalingsgradPrAktivitetDto(snAktivitet, perioderMedUtbetalingsgrad);
    }

    private static UtbetalingsgradPrAktivitetDto mapUtbetalingsgraderForFrilans(InntektArbeidYtelseGrunnlagDto iayGrunnlag, List<Intervall> inntektPerioder, BeregningsgrunnlagPrStatusOgAndel a) {
        UtbetalingsgradArbeidsforholdDto snAktivitet = new UtbetalingsgradArbeidsforholdDto(null,
                InternArbeidsforholdRefDto.nullRef(), UttakArbeidType.FRILANS);
        BigDecimal totalInntektVedStp = a.getBruttoPrÅr();
        List<PeriodeMedUtbetalingsgradDto> perioderMedUtbetalingsgrad = inntektPerioder.stream()
                .map(månedsperiode -> mapTilPeriodeMedUtbetalingsgrad(finnFrilansInntekter(iayGrunnlag), totalInntektVedStp, månedsperiode))
                .collect(Collectors.toList());
        return new UtbetalingsgradPrAktivitetDto(snAktivitet, perioderMedUtbetalingsgrad);
    }

    private static List<OppgittPeriodeInntekt> finnFrilansInntekter(InntektArbeidYtelseGrunnlagDto iayGrunnlag) {
        if (iayGrunnlag.getOppgittOpptjening() == null || iayGrunnlag.getOppgittOpptjening().getFrilans() == null || iayGrunnlag.getOppgittOpptjening().getFrilans().getOppgittFrilansInntekt() == null) {
            return Collections.emptyList();
        }
        return iayGrunnlag.getOppgittOpptjening().getFrilans().getOppgittFrilansInntekt()
                .stream().map(i -> (OppgittPeriodeInntekt) i)
                .collect(Collectors.toList());
    }

    private static List<OppgittPeriodeInntekt> finnNæringsInntekter(InntektArbeidYtelseGrunnlagDto iayGrunnlag) {
        if (iayGrunnlag.getOppgittOpptjening() == null || iayGrunnlag.getOppgittOpptjening().getEgenNæring() == null) {
            return Collections.emptyList();
    }
        return iayGrunnlag.getOppgittOpptjening().getEgenNæring()
                .stream().map(i -> (OppgittPeriodeInntekt) i)
                .collect(Collectors.toList());
    }



    private static PeriodeMedUtbetalingsgradDto mapTilPeriodeMedUtbetalingsgrad(List<OppgittPeriodeInntekt> oppgittPeriodeInntekter, BigDecimal totalInntektVedStp, Intervall månedsperiode) {
        BigDecimal løpendeInntekt = finnTotalLøpendeInntektIPeriode(oppgittPeriodeInntekter, månedsperiode);
        BigDecimal løpendeÅrsinntekt = løpendeInntekt.multiply(MND_I_1_ÅR);
        BigDecimal bortfaltInntekt = totalInntektVedStp.subtract(løpendeÅrsinntekt).max(BigDecimal.ZERO);
        BigDecimal utbetalingsgrad = totalInntektVedStp.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ZERO : bortfaltInntekt.divide(totalInntektVedStp, 2,RoundingMode.HALF_UP);
        return new PeriodeMedUtbetalingsgradDto(månedsperiode, utbetalingsgrad.multiply(BigDecimal.valueOf(100)));
    }

    private static List<BeregningsgrunnlagPrStatusOgAndel> finnAlleAndelerIFørstePeriode(Optional<BeregningsgrunnlagGrunnlagEntitet> beregningsgrunnlagGrunnlagEntitet) {
        return beregningsgrunnlagGrunnlagEntitet.flatMap(BeregningsgrunnlagGrunnlagEntitet::getBeregningsgrunnlag)
                    .map(bg -> bg.getBeregningsgrunnlagPerioder().get(0))
                    .stream()
                    .flatMap(p -> p.getBeregningsgrunnlagPrStatusOgAndelList().stream())
                    .collect(Collectors.toList());
    }

    private static Optional<LocalDate> finnSkjæringstidspunkt(Optional<BeregningsgrunnlagGrunnlagEntitet> beregningsgrunnlagGrunnlagEntitet) {
        return beregningsgrunnlagGrunnlagEntitet.flatMap(BeregningsgrunnlagGrunnlagEntitet::getBeregningsgrunnlag)
                    .map(BeregningsgrunnlagEntitet::getSkjæringstidspunkt);
    }

    private static Optional<BeregningsgrunnlagPrStatusOgAndel> finnNæringsandel(List<BeregningsgrunnlagPrStatusOgAndel> andeler) {
        return andeler.stream().filter(a -> a.getAktivitetStatus().erSelvstendigNæringsdrivende()).findFirst();
    }

    private static Optional<BeregningsgrunnlagPrStatusOgAndel> finnFrilansandel(List<BeregningsgrunnlagPrStatusOgAndel> andeler) {
        return andeler.stream().filter(a -> a.getAktivitetStatus().erFrilanser()).findFirst();
    }

    /**
     * Lager månedsperioder fra skjæringstidspunkt til dagens dato.
     *
     * Første periode går fra skjæringstidspunkt og ut måneden. De resterende periodene går fra start til slutt i måneden.
     *
     * @param skjæringstidspunkt Skjæringstidspunkt
     * @param idag
     * @return Liste med oppstykket periode fra skjæringstidspunkt til dagens dato
     */
    private static List<Intervall> periodiserMånedsvisFraSkjæringstidspunktTilNå(LocalDate skjæringstidspunkt, LocalDate idag) {
        List<Intervall> inntektPerioder = new ArrayList<>();
        inntektPerioder.add(Intervall.fraOgMedTilOgMed(skjæringstidspunkt.withDayOfMonth(1), skjæringstidspunkt.withDayOfMonth(skjæringstidspunkt.lengthOfMonth())));
        LocalDate førsteDagIMåned = skjæringstidspunkt.withDayOfMonth(skjæringstidspunkt.lengthOfMonth()).plusDays(1);
        while (førsteDagIMåned.getMonthValue() <= idag.getMonthValue()) {
            inntektPerioder.add(Intervall.fraOgMedTilOgMed(førsteDagIMåned, førsteDagIMåned.withDayOfMonth(førsteDagIMåned.lengthOfMonth())));
            førsteDagIMåned = førsteDagIMåned.plusMonths(1);
        }
        return inntektPerioder;
    }

    /**
     * Finner total inntekt fra næringsvirksomhet i periode
     *
     * @param oppgittPeriodeInntekter Liste med oppgitt inntekt
     * @param månedsperiode Periode på 1 mnd
     * @return total inntekt fra næring i periode
     */
    private static BigDecimal  finnTotalLøpendeInntektIPeriode(List<OppgittPeriodeInntekt> oppgittPeriodeInntekter, Intervall månedsperiode) {
        return oppgittPeriodeInntekter.stream()
                .filter(e -> overlapperMedMåned(månedsperiode, e))
                .map(oppgittEgenNæringDto -> finnEffektivInntektIPeriodeForNæring(månedsperiode, oppgittEgenNæringDto)).reduce(BigDecimal::add)
                .orElse(BigDecimal.ZERO);
    }

    private static boolean overlapperMedMåned(Intervall månedsperiode, OppgittPeriodeInntekt e) {
        return Intervall.fraOgMedTilOgMed(e.getPeriode().getFom(), e.getPeriode().getTom()).overlapper(månedsperiode);
    }

    /**
     * Finner inntekten fra gitt næring i periode basert på overlappende dager mellom måned og oppgitt periode med inntekt
     *
     * @param månedsperiode periode
     * @param oppgittInntekt oppgitt inntektsinformasjon
     * @return total inntekt fra oppgitt næring i periode
     */
    private static BigDecimal finnEffektivInntektIPeriodeForNæring(Intervall månedsperiode, OppgittPeriodeInntekt oppgittInntekt) {
        Periode oppgittNæringPeriode = oppgittInntekt.getPeriode();
        BigDecimal dagsats = finnEffektivDagsatsIPeriode(oppgittInntekt, oppgittNæringPeriode);
        long overlappendeDager = finnAntallOverlappendeDagerIMåned(månedsperiode, oppgittNæringPeriode);
        return dagsats.multiply(BigDecimal.valueOf(overlappendeDager));
    }

    /**
     * Finner antall overlappende dager i perioder
     *
     * @param p Månedsperiode
     * @param periode periode fra næring
     * @return
     */
    private static long finnAntallOverlappendeDagerIMåned(Intervall p, Periode periode) {
        LocalDate størsteFom = periode.getFom().isAfter(p.getFomDato()) ? periode.getFom() : p.getFomDato();
        LocalDate minsteTom = periode.getTom().isBefore(p.getTomDato()) ? periode.getTom() : p.getTomDato();
        return ChronoUnit.DAYS.between(størsteFom, minsteTom.plusDays(1));
    }

    /**
     * Finner opptjent inntekt pr dag i periode fra gitt næringsvirksomhet
     *
     * @param oppgittInntekt Informasjon om oppgitt inntekt
     * @param periode Periode
     * @return dagsats fra næring
     */
    private static BigDecimal finnEffektivDagsatsIPeriode(OppgittPeriodeInntekt oppgittInntekt, Periode periode) {
        long dagerIRapportertPeriode = ChronoUnit.DAYS.between(periode.getFom(), periode.getTom().plusDays(1));
        return oppgittInntekt.getInntekt().divide(BigDecimal.valueOf(dagerIRapportertPeriode), RoundingMode.HALF_UP);
    }
}
