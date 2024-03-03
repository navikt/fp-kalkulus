package no.nav.folketrygdloven.kalkulus.mapTilKontrakt;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.input.UtbetalingsgradGrunnlag;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.svp.AktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.svp.PeriodeMedUtbetalingsgradDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.UttakArbeidType;
import no.nav.folketrygdloven.kalkulus.typer.Utbetalingsgrad;

/**
 * Fallback for å utlede avkortet før gradering.
 */
class FinnInntektstak {


    static Beløp finnInntektstakForAndel(BeregningsgrunnlagPrStatusOgAndelDto andel,
                                              BeregningsgrunnlagPeriodeDto bgPeriode,
                                              UtbetalingsgradGrunnlag yg,
                                              Beløp grenseverdi) {
        if (andel.getAktivitetStatus().erArbeidstaker()) {
            return finnInntektstakForArbeidstaker(andel, bgPeriode, yg, grenseverdi);
        }
        if (andel.getAktivitetStatus().erFrilanser()) {
            return finnInntektstakForFrilans(andel, bgPeriode, yg, grenseverdi);
        }
        if (andel.getAktivitetStatus().erSelvstendigNæringsdrivende()) {
            return finnInntektstakForNæring(andel, bgPeriode, yg, grenseverdi);
        }
        if (andel.getAktivitetStatus().erDagpenger() || andel.getAktivitetStatus().equals(AktivitetStatus.ARBEIDSAVKLARINGSPENGER)) {
            return finnInntektstakForYtelse(andel, bgPeriode, yg, grenseverdi);
        }
        return Beløp.ZERO;
    }


    private static Beløp finnInntektstakForYtelse(BeregningsgrunnlagPrStatusOgAndelDto andel,
                                                       BeregningsgrunnlagPeriodeDto inneværendeBGPeriode,
                                                       UtbetalingsgradGrunnlag yg,
                                                       Beløp grenseverdi) {
        if (!harSøktUtbetalingForAndel(inneværendeBGPeriode.getPeriode(), andel, yg)) {
            return Beløp.ZERO;
        }
        var inntektIkkeSøktOm = finnInntektIkkeSøktOm(inneværendeBGPeriode, yg);
        var inntektSøktOmAT = finnInntektSøktOm(inneværendeBGPeriode, yg, AktivitetStatus.ARBEIDSTAKER);
        var inntektSøktOmFL = finnInntektSøktOm(inneværendeBGPeriode, yg, AktivitetStatus.FRILANSER);
        var inntektSøktOmSN = finnInntektSøktOm(inneværendeBGPeriode, yg, AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE);
        var inntektIkkeTilgjengeligForSN = inntektIkkeSøktOm.adder(inntektSøktOmAT).adder(inntektSøktOmFL).adder(inntektSøktOmSN);
        var grenseverdiTruketFraIkkeTilgjengeligInntekt = grenseverdi.subtraher(inntektIkkeTilgjengeligForSN);
        return grenseverdiTruketFraIkkeTilgjengeligInntekt.compareTo(Beløp.ZERO) <= 0
                ? Beløp.ZERO
                : grenseverdiTruketFraIkkeTilgjengeligInntekt.min(andel.getBruttoPrÅr());
    }

    private static Beløp finnInntektstakForNæring(BeregningsgrunnlagPrStatusOgAndelDto andel,
                                                       BeregningsgrunnlagPeriodeDto inneværendeBGPeriode,
                                                       UtbetalingsgradGrunnlag yg,
                                                  Beløp grenseverdi) {
        if (!harSøktUtbetalingForAndel(inneværendeBGPeriode.getPeriode(), andel, yg)) {
            return Beløp.ZERO;
        }
        var inntektIkkeSøktOm = finnInntektIkkeSøktOm(inneværendeBGPeriode, yg);
        var inntektSøktOmAT = finnInntektSøktOm(inneværendeBGPeriode, yg, AktivitetStatus.ARBEIDSTAKER);
        var inntektSøktOmFL = finnInntektSøktOm(inneværendeBGPeriode, yg, AktivitetStatus.FRILANSER);
        var inntektIkkeTilgjengeligForSN = inntektIkkeSøktOm.adder(inntektSøktOmAT).adder(inntektSøktOmFL);
        var grenseverdiTruketFraIkkeTilgjengeligInntekt = grenseverdi.subtraher(inntektIkkeTilgjengeligForSN);
        return grenseverdiTruketFraIkkeTilgjengeligInntekt.compareTo(Beløp.ZERO) <= 0
                ? Beløp.ZERO
                : grenseverdiTruketFraIkkeTilgjengeligInntekt.min(andel.getBruttoPrÅr());
    }

    private static Beløp finnInntektstakForFrilans(BeregningsgrunnlagPrStatusOgAndelDto andel,
                                                        BeregningsgrunnlagPeriodeDto inneværendeBGPeriode,
                                                        UtbetalingsgradGrunnlag yg,
                                                   Beløp grenseverdi) {

        if (!harSøktUtbetalingForAndel(inneværendeBGPeriode.getPeriode(), andel, yg)) {
            return Beløp.ZERO;
        }
        var inntektIkkeSøktOm = finnInntektIkkeSøktOm(inneværendeBGPeriode, yg);
        var inntektSøktOmAT = finnInntektSøktOm(inneværendeBGPeriode, yg, AktivitetStatus.ARBEIDSTAKER);
        var inntektIkkeTilgjengeligForFL = inntektIkkeSøktOm.adder(inntektSøktOmAT);
        var grenseverdiTruketFraIkkeTilgjengeligInntekt = grenseverdi.subtraher(inntektIkkeTilgjengeligForFL);
        return grenseverdiTruketFraIkkeTilgjengeligInntekt.compareTo(Beløp.ZERO) <= 0
                ? Beløp.ZERO
                : grenseverdiTruketFraIkkeTilgjengeligInntekt.min(andel.getBruttoPrÅr());
    }

    private static Beløp finnInntektSøktOm(BeregningsgrunnlagPeriodeDto inneværendeBGPeriode, UtbetalingsgradGrunnlag yg, AktivitetStatus status) {
        var andelerSøktOm = inneværendeBGPeriode.getBeregningsgrunnlagPrStatusOgAndelList().stream()
                .filter(bga -> bga.getAktivitetStatus().equals(status))
                .filter(bga -> harSøktUtbetalingForAndel(inneværendeBGPeriode.getPeriode(), bga, yg))
                .collect(Collectors.toList());
        return finnBrutto(andelerSøktOm);
    }


    private static Beløp finnInntektstakForArbeidstaker(BeregningsgrunnlagPrStatusOgAndelDto andelÅVurdere,
                                                             BeregningsgrunnlagPeriodeDto inneværendeBGPeriode,
                                                             UtbetalingsgradGrunnlag yg,
                                                        Beløp grenseverdi) {
        boolean harSøkt = harSøktUtbetalingForAndel(inneværendeBGPeriode.getPeriode(), andelÅVurdere, yg);
        if (!harSøkt) {
            return Beløp.ZERO;
        }

        var inntektIkkeSøktOm = finnInntektIkkeSøktOm(inneværendeBGPeriode, yg);
        var grenseMinusAnnenInntekt = grenseverdi.subtraher(inntektIkkeSøktOm);
        return grenseMinusAnnenInntekt.compareTo(Beløp.ZERO) <= 0
                ? Beløp.ZERO
                : grenseMinusAnnenInntekt.min(andelÅVurdere.getBruttoPrÅr());
    }

    private static Beløp finnInntektIkkeSøktOm(BeregningsgrunnlagPeriodeDto inneværendeBGPeriode,
                                                    UtbetalingsgradGrunnlag yg) {
        var alleAndelerUtenUtb = inneværendeBGPeriode.getBeregningsgrunnlagPrStatusOgAndelList().stream()
                .filter(bga -> !harSøktUtbetalingForAndel(inneværendeBGPeriode.getPeriode(), bga, yg))
                .collect(Collectors.toList());
        return finnBrutto(alleAndelerUtenUtb);
    }

    private static Beløp finnBrutto(List<BeregningsgrunnlagPrStatusOgAndelDto> andeler) {
        return andeler.stream()
                .filter(Objects::nonNull)
                .map(BeregningsgrunnlagPrStatusOgAndelDto::getBruttoPrÅr)
                .filter(Objects::nonNull)
                .reduce(Beløp::adder)
                .orElse(Beløp.ZERO);
    }

    private static boolean harSøktUtbetalingForAndel(Intervall bgPeriode,
                                                     BeregningsgrunnlagPrStatusOgAndelDto bgAndel,
                                                     UtbetalingsgradGrunnlag yg) {
        return yg.getUtbetalingsgradPrAktivitet().stream()
                .filter(utb -> matcherMedBGAndel(utb.getUtbetalingsgradArbeidsforhold(), bgAndel))
                .anyMatch(utb -> harSøktUtbetalingIPeriode(utb.getPeriodeMedUtbetalingsgrad(), bgPeriode));
    }

    private static boolean matcherMedBGAndel(AktivitetDto utbArbeid,
                                             BeregningsgrunnlagPrStatusOgAndelDto bgAndel) {
        if (bgAndel.getAktivitetStatus().erFrilanser()) {
            return utbArbeid.getUttakArbeidType().equals(UttakArbeidType.FRILANS);
        } else if (bgAndel.getAktivitetStatus().erSelvstendigNæringsdrivende()) {
            return utbArbeid.getUttakArbeidType().equals(UttakArbeidType.SELVSTENDIG_NÆRINGSDRIVENDE);
        } else if (bgAndel.getAktivitetStatus().equals(AktivitetStatus.DAGPENGER)) {
            return utbArbeid.getUttakArbeidType().equals(UttakArbeidType.DAGPENGER);
        } else if (bgAndel.getAktivitetStatus().erArbeidstaker()) {
            var agIdentifikator = bgAndel.getArbeidsgiver().map(Arbeidsgiver::getIdentifikator).orElse(null);
            var agIDUtbetaling = utbArbeid.getArbeidsgiver().map(no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver::getIdentifikator).orElse(null);

            var bgAndelRef = InternArbeidsforholdRefDto.ref(bgAndel.getBgAndelArbeidsforhold().get().getArbeidsforholdRef().getReferanse());
            var utbRef = utbArbeid.getInternArbeidsforholdRef() == null ? InternArbeidsforholdRefDto.nullRef() : utbArbeid.getInternArbeidsforholdRef();

            boolean agMatcher = Objects.equals(agIdentifikator, agIDUtbetaling);
            boolean refMatcher = bgAndelRef.gjelderFor(utbRef);
            return agMatcher && refMatcher;
        } else return false;
    }

    private static boolean harSøktUtbetalingIPeriode(List<PeriodeMedUtbetalingsgradDto> periodeMedUtbetalingsgrad, Intervall periode) {
        return periodeMedUtbetalingsgrad.stream()
                .filter(utbPeriode -> utbPeriode.getPeriode().overlapper(Intervall.fraOgMedTilOgMed(periode.getFomDato(), periode.getTomDato())))
                .anyMatch(utbPeriode -> utbPeriode.getUtbetalingsgrad().compareTo(Utbetalingsgrad.ZERO) > 0);
    }

}
