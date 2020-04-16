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

import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.OppgittPeriodeInntekt;
import no.nav.folketrygdloven.kalkulator.modell.svp.PeriodeMedUtbetalingsgradDto;
import no.nav.folketrygdloven.kalkulator.modell.svp.UtbetalingsgradArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.svp.UtbetalingsgradPrAktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.modell.uttak.UttakArbeidType;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagGrunnlagEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndel;

public class UtbetalingsgradMapperFRISINN {

    public static final int VIRKEDAGER_I_ET_ÅR = 260;

    /**
     * Finnner utbetalingsgrader for FRISINN
     *
     * @param iayGrunnlag InntektArbeidYtelseGrunnlag
     * @param beregningsgrunnlagGrunnlagEntitet Aktivt beregningsgrunnlag
     * @return Liste med utbetalingsgrader for FRISINN
     */
    public static List<UtbetalingsgradPrAktivitetDto> map(InntektArbeidYtelseGrunnlagDto iayGrunnlag,
                                                          Optional<BeregningsgrunnlagGrunnlagEntitet> beregningsgrunnlagGrunnlagEntitet) {
        Optional<LocalDate> stpOpt = finnSkjæringstidspunkt(beregningsgrunnlagGrunnlagEntitet);
        if (stpOpt.isEmpty()) {
            return Collections.emptyList();
        }
        List<BeregningsgrunnlagPrStatusOgAndel> andeler = finnAlleAndelerIFørstePeriode(beregningsgrunnlagGrunnlagEntitet);
        List<UtbetalingsgradPrAktivitetDto> utbetalingsgradPrAktivitetListe = new ArrayList<>();

        mapUtbetalingsgradNæring(iayGrunnlag, andeler, utbetalingsgradPrAktivitetListe);
        mapUtbetalingsgradFrilans(iayGrunnlag, andeler, utbetalingsgradPrAktivitetListe);
        return utbetalingsgradPrAktivitetListe;
    }

    private static void mapUtbetalingsgradFrilans(InntektArbeidYtelseGrunnlagDto iayGrunnlag,
                                                  List<BeregningsgrunnlagPrStatusOgAndel> andeler,
                                                  List<UtbetalingsgradPrAktivitetDto> utbetalingsgradPrAktivitetListe) {
        Optional<BeregningsgrunnlagPrStatusOgAndel> næringAndel = finnFrilansandel(andeler);
        næringAndel.filter(a -> a.getBruttoPrÅr() != null)
                .map(a -> mapUtbetalingsgraderForFrilans(iayGrunnlag, a))
                .ifPresent(utbetalingsgradPrAktivitetListe::add);
    }

    private static void mapUtbetalingsgradNæring(InntektArbeidYtelseGrunnlagDto iayGrunnlag, List<BeregningsgrunnlagPrStatusOgAndel> andeler, List<UtbetalingsgradPrAktivitetDto> utbetalingsgradPrAktivitetListe) {
        Optional<BeregningsgrunnlagPrStatusOgAndel> næringAndel = finnNæringsandel(andeler);
        næringAndel.filter(a -> a.getBruttoPrÅr() != null)
                .map(a -> mapUtbetalingsgraderForNæring(iayGrunnlag, a))
                .ifPresent(utbetalingsgradPrAktivitetListe::add);
    }

    private static UtbetalingsgradPrAktivitetDto mapUtbetalingsgraderForNæring(InntektArbeidYtelseGrunnlagDto iayGrunnlag, BeregningsgrunnlagPrStatusOgAndel a) {
        UtbetalingsgradArbeidsforholdDto snAktivitet = new UtbetalingsgradArbeidsforholdDto(null, InternArbeidsforholdRefDto.nullRef(), UttakArbeidType.SELVSTENDIG_NÆRINGSDRIVENDE);
        BigDecimal totalInntektVedStp = a.getBruttoPrÅr();
        List<PeriodeMedUtbetalingsgradDto> perioderMedUtbetalingsgrad = finnNæringsInntekter(iayGrunnlag).stream()
                .map(inntekt -> mapTilPeriodeMedUtbetalingsgrad(inntekt, totalInntektVedStp))
                .collect(Collectors.toList());
        return new UtbetalingsgradPrAktivitetDto(snAktivitet, perioderMedUtbetalingsgrad);
    }

    private static UtbetalingsgradPrAktivitetDto mapUtbetalingsgraderForFrilans(InntektArbeidYtelseGrunnlagDto iayGrunnlag, BeregningsgrunnlagPrStatusOgAndel a) {
        UtbetalingsgradArbeidsforholdDto snAktivitet = new UtbetalingsgradArbeidsforholdDto(null,
                InternArbeidsforholdRefDto.nullRef(), UttakArbeidType.FRILANS);
        BigDecimal totalInntektVedStp = a.getBruttoPrÅr();
        List<PeriodeMedUtbetalingsgradDto> perioderMedUtbetalingsgrad = finnFrilansInntekter(iayGrunnlag).stream()
                .map(inntekt -> mapTilPeriodeMedUtbetalingsgrad(inntekt, totalInntektVedStp))
                .collect(Collectors.toList());
        return new UtbetalingsgradPrAktivitetDto(snAktivitet, perioderMedUtbetalingsgrad);
    }

    private static List<OppgittPeriodeInntekt> finnFrilansInntekter(InntektArbeidYtelseGrunnlagDto iayGrunnlag) {
        if (iayGrunnlag.getOppgittOpptjening().isEmpty() || iayGrunnlag.getOppgittOpptjening().get().getFrilans().isEmpty()) {
            return Collections.emptyList();
        }
        return iayGrunnlag.getOppgittOpptjening().get().getFrilans().get().getOppgittFrilansInntekt()
                .stream().map(i -> (OppgittPeriodeInntekt) i)
                .collect(Collectors.toList());
    }

    private static List<OppgittPeriodeInntekt> finnNæringsInntekter(InntektArbeidYtelseGrunnlagDto iayGrunnlag) {
        if (iayGrunnlag.getOppgittOpptjening().isEmpty()) {
            return Collections.emptyList();
    }
        return iayGrunnlag.getOppgittOpptjening().get().getEgenNæring()
                .stream().map(i -> (OppgittPeriodeInntekt) i)
                .collect(Collectors.toList());
    }

    private static PeriodeMedUtbetalingsgradDto mapTilPeriodeMedUtbetalingsgrad(OppgittPeriodeInntekt oppgittPeriodeInntekt,
                                                                                BigDecimal totalInntektVedStp) {
        BigDecimal løpendeÅrsinntekt = finnEffektivÅrsinntektForLøpenedeInntekt(oppgittPeriodeInntekt);
        BigDecimal bortfaltInntekt = totalInntektVedStp.subtract(løpendeÅrsinntekt).max(BigDecimal.ZERO);
        BigDecimal utbetalingsgrad = totalInntektVedStp.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ZERO : bortfaltInntekt.divide(totalInntektVedStp, 2,RoundingMode.HALF_UP);
        return new PeriodeMedUtbetalingsgradDto(oppgittPeriodeInntekt.getPeriode(), utbetalingsgrad.multiply(BigDecimal.valueOf(100)));
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
     * Finner effektiv årsinntekt fra oppgitt inntekt
     *
     * @param oppgittInntekt oppgitt inntektsinformasjon
     * @return effektiv årsinntekt fra inntekt
     */
    public static BigDecimal finnEffektivÅrsinntektForLøpenedeInntekt(OppgittPeriodeInntekt oppgittInntekt) {
        BigDecimal dagsats = finnEffektivDagsatsIPeriode(oppgittInntekt);
        return dagsats.multiply(BigDecimal.valueOf(VIRKEDAGER_I_ET_ÅR));
    }

    /**
     * Finner opptjent inntekt pr dag i periode
     *
     * @param oppgittInntekt Informasjon om oppgitt inntekt
     * @return dagsats i periode
     */
    private static BigDecimal finnEffektivDagsatsIPeriode(OppgittPeriodeInntekt oppgittInntekt) {
        Intervall periode = oppgittInntekt.getPeriode();
        long dagerIRapportertPeriode = Virkedager.beregnAntallVirkedager(periode.getFomDato(), periode.getTomDato());
        return oppgittInntekt.getInntekt().divide(BigDecimal.valueOf(dagerIRapportertPeriode), RoundingMode.HALF_UP);
    }
}
