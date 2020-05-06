package no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell;

import static no.nav.folketrygdloven.kalkulator.ytelse.utbgradytelse.AktivitetStatusMapper.mapAktivitetStatus;

import java.math.BigDecimal;
import java.math.RoundingMode;

import no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.UtbetalingsgradGrunnlag;
import no.nav.folketrygdloven.kalkulator.input.YtelsespesifiktGrunnlag;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.svp.PeriodeMedUtbetalingsgradDto;
import no.nav.folketrygdloven.kalkulator.modell.svp.UtbetalingsgradPrAktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.virksomhet.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.AktivitetStatus;

public class UtbetalingsgradTjeneste {

    public static BigDecimal finnGradertBruttoForAndel(BeregningsgrunnlagPrStatusOgAndelDto andel,
                                                       Intervall periode,
                                                       YtelsespesifiktGrunnlag ytelsesSpesifiktGrunnlag) {
        BigDecimal utbetalingsgrad = finnUtbetalingsgradForAndel(andel, periode, ytelsesSpesifiktGrunnlag);
        return andel.getBruttoInkludertNaturalYtelser()
                .divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_EVEN)
                .multiply(utbetalingsgrad);
    }


    static BigDecimal finnUtbetalingsgradForAndel(BeregningsgrunnlagPrStatusOgAndelDto andel,
                                                  Intervall periode,
                                                  YtelsespesifiktGrunnlag ytelsesSpesifiktGrunnlag) {
        if (ytelsesSpesifiktGrunnlag instanceof UtbetalingsgradGrunnlag) {
            if (andel.getAktivitetStatus().erArbeidstaker() && andel.getBgAndelArbeidsforhold().isPresent()) {
                return mapUtbetalingsgradForArbeid(andel.getBgAndelArbeidsforhold().get(), periode, ytelsesSpesifiktGrunnlag);
            } else {
                return finnUtbetalingsgradForStatus(andel.getAktivitetStatus(), periode, ytelsesSpesifiktGrunnlag);
            }
        }
        return BigDecimal.valueOf(100);
    }


    private static BigDecimal finnUtbetalingsgradForStatus(AktivitetStatus status,
                                                           Intervall periode,
                                                           YtelsespesifiktGrunnlag ytelsesSpesifiktGrunnlag) {
        if (ytelsesSpesifiktGrunnlag instanceof UtbetalingsgradGrunnlag) {
            if (status.erArbeidstaker()) {
                throw new IllegalStateException("Bruk Arbeidsforhold-mapper");
            }
            UtbetalingsgradGrunnlag utbetalingsgradGrunnlag = (UtbetalingsgradGrunnlag) ytelsesSpesifiktGrunnlag;
            return utbetalingsgradGrunnlag.getUtbetalingsgradPrAktivitet().stream()
                    .filter(ubtGrad -> mapAktivitetStatus(ubtGrad.getUtbetalingsgradArbeidsforhold().getUttakArbeidType()).equals(status))
                    .flatMap(utb -> utb.getPeriodeMedUtbetalingsgrad().stream())
                    .filter(p -> p.getPeriode().inkluderer(periode.getFomDato()))
                    .map(PeriodeMedUtbetalingsgradDto::getUtbetalingsgrad)
                    .findFirst()
                    .orElse(BigDecimal.ZERO);
        }
        return BigDecimal.valueOf(100);
    }

    static BigDecimal mapUtbetalingsgradForArbeid(BGAndelArbeidsforholdDto arbeidsforhold,
                                                  Intervall periode,
                                                  YtelsespesifiktGrunnlag ytelsesSpesifiktGrunnlag) {
        if (ytelsesSpesifiktGrunnlag instanceof UtbetalingsgradGrunnlag) {
            UtbetalingsgradGrunnlag utbetalingsgradGrunnlag = (UtbetalingsgradGrunnlag) ytelsesSpesifiktGrunnlag;
            return utbetalingsgradGrunnlag.getUtbetalingsgradPrAktivitet().stream()
                    .filter(ubtGrad -> mapAktivitetStatus(ubtGrad.getUtbetalingsgradArbeidsforhold().getUttakArbeidType()).equals(AktivitetStatus.ARBEIDSTAKER))
                    .filter(utbGrad -> matcherArbeidsgiver(arbeidsforhold, utbGrad)
                            && matcherArbeidsforholdReferanse(arbeidsforhold, utbGrad))
                    .flatMap(utb -> utb.getPeriodeMedUtbetalingsgrad().stream())
                    .filter(p -> p.getPeriode().inkluderer(periode.getFomDato()))
                    .map(PeriodeMedUtbetalingsgradDto::getUtbetalingsgrad)
                    .findFirst()
                    .orElse(BigDecimal.ZERO);
        }
        return BigDecimal.valueOf(100);
    }

    private static boolean matcherArbeidsforholdReferanse(BGAndelArbeidsforholdDto arbeidsforhold, UtbetalingsgradPrAktivitetDto utbGrad) {
        return utbGrad.getUtbetalingsgradArbeidsforhold().getInternArbeidsforholdRef().gjelderFor(arbeidsforhold.getArbeidsforholdRef());
    }

    private static Boolean matcherArbeidsgiver(BGAndelArbeidsforholdDto arbeidsforhold, UtbetalingsgradPrAktivitetDto utbGrad) {
        return utbGrad.getUtbetalingsgradArbeidsforhold().getArbeidsgiver()
                .map(Arbeidsgiver::getIdentifikator)
                .map(id -> arbeidsforhold.getArbeidsgiver().getIdentifikator().equals(id))
        .orElse(false);
    }

}
