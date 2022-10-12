package no.nav.folketrygdloven.kalkulus.mapTilKontrakt;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.input.UtbetalingsgradGrunnlag;
import no.nav.folketrygdloven.kalkulator.modell.svp.AktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.svp.PeriodeMedUtbetalingsgradDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.modell.uttak.UttakArbeidType;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndel;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.Beløp;
import no.nav.folketrygdloven.kalkulus.felles.jpa.IntervallEntitet;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;

/**
 * Fallback for å utlede avkortet før gradering.
 */
class FinnInntektstak {


    static BigDecimal finnInntektstakForAndel(BeregningsgrunnlagPrStatusOgAndel andel,
                                              BeregningsgrunnlagPeriode bgPeriode,
                                              UtbetalingsgradGrunnlag yg,
                                              BigDecimal grenseverdi) {
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
        return BigDecimal.ZERO;
    }


    private static BigDecimal finnInntektstakForYtelse(BeregningsgrunnlagPrStatusOgAndel andel,
                                                       BeregningsgrunnlagPeriode inneværendeBGPeriode,
                                                       UtbetalingsgradGrunnlag yg,
                                                       BigDecimal grenseverdi) {
        if (!harSøktUtbetalingForAndel(inneværendeBGPeriode.getPeriode(), andel, yg)) {
            return BigDecimal.ZERO;
        }
        var inntektIkkeSøktOm = finnInntektIkkeSøktOm(inneværendeBGPeriode, yg);
        var inntektSøktOmAT = finnInntektSøktOm(inneværendeBGPeriode, yg, AktivitetStatus.ARBEIDSTAKER);
        var inntektSøktOmFL = finnInntektSøktOm(inneværendeBGPeriode, yg, AktivitetStatus.FRILANSER);
        var inntektSøktOmSN = finnInntektSøktOm(inneværendeBGPeriode, yg, AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE);
        var inntektIkkeTilgjengeligForSN = inntektIkkeSøktOm.add(inntektSøktOmAT).add(inntektSøktOmFL).add(inntektSøktOmSN);
        var grenseverdiTruketFraIkkeTilgjengeligInntekt = grenseverdi.subtract(inntektIkkeTilgjengeligForSN);
        return grenseverdiTruketFraIkkeTilgjengeligInntekt.compareTo(BigDecimal.ZERO) <= 0
                ? BigDecimal.ZERO
                : grenseverdiTruketFraIkkeTilgjengeligInntekt.min(andel.getBruttoPrÅr().getVerdi());
    }

    private static BigDecimal finnInntektstakForNæring(BeregningsgrunnlagPrStatusOgAndel andel,
                                                       BeregningsgrunnlagPeriode inneværendeBGPeriode,
                                                       UtbetalingsgradGrunnlag yg,
                                                       BigDecimal grenseverdi) {
        if (!harSøktUtbetalingForAndel(inneværendeBGPeriode.getPeriode(), andel, yg)) {
            return BigDecimal.ZERO;
        }
        var inntektIkkeSøktOm = finnInntektIkkeSøktOm(inneværendeBGPeriode, yg);
        var inntektSøktOmAT = finnInntektSøktOm(inneværendeBGPeriode, yg, AktivitetStatus.ARBEIDSTAKER);
        var inntektSøktOmFL = finnInntektSøktOm(inneværendeBGPeriode, yg, AktivitetStatus.FRILANSER);
        var inntektIkkeTilgjengeligForSN = inntektIkkeSøktOm.add(inntektSøktOmAT).add(inntektSøktOmFL);
        var grenseverdiTruketFraIkkeTilgjengeligInntekt = grenseverdi.subtract(inntektIkkeTilgjengeligForSN);
        return grenseverdiTruketFraIkkeTilgjengeligInntekt.compareTo(BigDecimal.ZERO) <= 0
                ? BigDecimal.ZERO
                : grenseverdiTruketFraIkkeTilgjengeligInntekt.min(andel.getBruttoPrÅr().getVerdi());
    }

    private static BigDecimal finnInntektstakForFrilans(BeregningsgrunnlagPrStatusOgAndel andel,
                                                        BeregningsgrunnlagPeriode inneværendeBGPeriode,
                                                        UtbetalingsgradGrunnlag yg,
                                                        BigDecimal grenseverdi) {

        if (!harSøktUtbetalingForAndel(inneværendeBGPeriode.getPeriode(), andel, yg)) {
            return BigDecimal.ZERO;
        }
        var inntektIkkeSøktOm = finnInntektIkkeSøktOm(inneværendeBGPeriode, yg);
        var inntektSøktOmAT = finnInntektSøktOm(inneværendeBGPeriode, yg, AktivitetStatus.ARBEIDSTAKER);
        var inntektIkkeTilgjengeligForFL = inntektIkkeSøktOm.add(inntektSøktOmAT);
        var grenseverdiTruketFraIkkeTilgjengeligInntekt = grenseverdi.subtract(inntektIkkeTilgjengeligForFL);
        return grenseverdiTruketFraIkkeTilgjengeligInntekt.compareTo(BigDecimal.ZERO) <= 0
                ? BigDecimal.ZERO
                : grenseverdiTruketFraIkkeTilgjengeligInntekt.min(andel.getBruttoPrÅr().getVerdi());
    }

    private static BigDecimal finnInntektSøktOm(BeregningsgrunnlagPeriode inneværendeBGPeriode, UtbetalingsgradGrunnlag yg, AktivitetStatus status) {
        var andelerSøktOm = inneværendeBGPeriode.getBeregningsgrunnlagPrStatusOgAndelList().stream()
                .filter(bga -> bga.getAktivitetStatus().equals(status))
                .filter(bga -> harSøktUtbetalingForAndel(inneværendeBGPeriode.getPeriode(), bga, yg))
                .collect(Collectors.toList());
        return finnBrutto(andelerSøktOm);
    }


    private static BigDecimal finnInntektstakForArbeidstaker(BeregningsgrunnlagPrStatusOgAndel andelÅVurdere,
                                                             BeregningsgrunnlagPeriode inneværendeBGPeriode,
                                                             UtbetalingsgradGrunnlag yg,
                                                             BigDecimal grenseverdi) {
        boolean harSøkt = harSøktUtbetalingForAndel(inneværendeBGPeriode.getPeriode(), andelÅVurdere, yg);
        if (!harSøkt) {
            return BigDecimal.ZERO;
        }

        var inntektIkkeSøktOm = finnInntektIkkeSøktOm(inneværendeBGPeriode, yg);
        var grenseMinusAnnenInntekt = grenseverdi.subtract(inntektIkkeSøktOm);
        return grenseMinusAnnenInntekt.compareTo(BigDecimal.ZERO) <= 0
                ? BigDecimal.ZERO
                : grenseMinusAnnenInntekt.min(andelÅVurdere.getBruttoPrÅr().getVerdi());
    }

    private static BigDecimal finnInntektIkkeSøktOm(BeregningsgrunnlagPeriode inneværendeBGPeriode,
                                                    UtbetalingsgradGrunnlag yg) {
        var alleAndelerUtenUtb = inneværendeBGPeriode.getBeregningsgrunnlagPrStatusOgAndelList().stream()
                .filter(bga -> !harSøktUtbetalingForAndel(inneværendeBGPeriode.getPeriode(), bga, yg))
                .collect(Collectors.toList());
        return finnBrutto(alleAndelerUtenUtb);
    }

    private static BigDecimal finnBrutto(List<BeregningsgrunnlagPrStatusOgAndel> andeler) {
        return andeler.stream()
                .filter(Objects::nonNull)
                .map(BeregningsgrunnlagPrStatusOgAndel::getBruttoPrÅr)
                .filter(Objects::nonNull)
                .map(Beløp::getVerdi)
                .reduce(BigDecimal::add)
                .orElse(BigDecimal.ZERO);
    }

    private static boolean harSøktUtbetalingForAndel(IntervallEntitet bgPeriode,
                                                     BeregningsgrunnlagPrStatusOgAndel bgAndel,
                                                     UtbetalingsgradGrunnlag yg) {
        return yg.getUtbetalingsgradPrAktivitet().stream()
                .filter(utb -> matcherMedBGAndel(utb.getUtbetalingsgradArbeidsforhold(), bgAndel))
                .anyMatch(utb -> harSøktUtbetalingIPeriode(utb.getPeriodeMedUtbetalingsgrad(), bgPeriode));
    }

    private static boolean matcherMedBGAndel(AktivitetDto utbArbeid,
                                             BeregningsgrunnlagPrStatusOgAndel bgAndel) {
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

    private static boolean harSøktUtbetalingIPeriode(List<PeriodeMedUtbetalingsgradDto> periodeMedUtbetalingsgrad, IntervallEntitet periode) {
        return periodeMedUtbetalingsgrad.stream()
                .filter(utbPeriode -> utbPeriode.getPeriode().overlapper(Intervall.fraOgMedTilOgMed(periode.getFomDato(), periode.getTomDato())))
                .anyMatch(utbPeriode -> utbPeriode.getUtbetalingsgrad().compareTo(BigDecimal.ZERO) > 0);
    }

}
