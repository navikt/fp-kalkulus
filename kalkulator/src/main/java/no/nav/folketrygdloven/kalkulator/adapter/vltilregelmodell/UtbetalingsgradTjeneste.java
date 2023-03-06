package no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell;

import static no.nav.folketrygdloven.kalkulator.ytelse.utbgradytelse.AktivitetStatusMatcher.matcherStatus;
import static no.nav.folketrygdloven.kalkulator.ytelse.utbgradytelse.AktivitetStatusMatcher.matcherStatusUtenIkkeYrkesaktiv;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

import no.nav.folketrygdloven.kalkulator.input.UtbetalingsgradGrunnlag;
import no.nav.folketrygdloven.kalkulator.input.YtelsespesifiktGrunnlag;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.svp.PeriodeMedUtbetalingsgradDto;
import no.nav.folketrygdloven.kalkulator.modell.svp.UtbetalingsgradPrAktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;

public class UtbetalingsgradTjeneste {

    public static BigDecimal finnGradertBruttoForAndel(BeregningsgrunnlagPrStatusOgAndelDto andel,
                                                       Intervall periode,
                                                       YtelsespesifiktGrunnlag ytelsesSpesifiktGrunnlag) {
        BigDecimal utbetalingsgrad = finnUtbetalingsgradForAndel(andel, periode, ytelsesSpesifiktGrunnlag, false);
        return andel.getBruttoInkludertNaturalYtelser()
                .divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_EVEN)
                .multiply(utbetalingsgrad);
    }


    public static BigDecimal finnUtbetalingsgradForAndel(BeregningsgrunnlagPrStatusOgAndelDto andel,
                                                         Intervall periode,
                                                         YtelsespesifiktGrunnlag ytelsesSpesifiktGrunnlag,
                                                         boolean skalIgnorereIkkeYrkesaktivStatus) {
        if (ytelsesSpesifiktGrunnlag instanceof UtbetalingsgradGrunnlag) {
            if (andel.getAktivitetStatus().erArbeidstaker() && andel.getBgAndelArbeidsforhold().isPresent()) {
                return finnUtbetalingsgradForArbeid(andel.getBgAndelArbeidsforhold().get().getArbeidsgiver(), andel.getBgAndelArbeidsforhold().get().getArbeidsforholdRef(), periode, ytelsesSpesifiktGrunnlag, skalIgnorereIkkeYrkesaktivStatus);
            } else {
                return finnUtbetalingsgradForStatus(andel.getAktivitetStatus(), periode, ytelsesSpesifiktGrunnlag);
            }
        }
        return BigDecimal.valueOf(100);
    }


    public static BigDecimal finnUtbetalingsgradForStatus(AktivitetStatus status,
                                                          Intervall periode,
                                                          YtelsespesifiktGrunnlag ytelsesSpesifiktGrunnlag) {
        if (ytelsesSpesifiktGrunnlag instanceof UtbetalingsgradGrunnlag utbetalingsgradGrunnlag) {
            return finnUtbetalingsgradForStatus(status, periode, utbetalingsgradGrunnlag);
        }
        return BigDecimal.valueOf(100);
    }

    public static BigDecimal finnUtbetalingsgradForStatus(AktivitetStatus status, Intervall periode, UtbetalingsgradGrunnlag utbetalingsgradGrunnlag) {
        if (status.erArbeidstaker()) {
            throw new IllegalStateException("Bruk Arbeidsforhold-mapper");
        }
        return finnPerioderForStatus(status, utbetalingsgradGrunnlag)
                .stream()
                .flatMap(utb -> utb.getPeriodeMedUtbetalingsgrad().stream())
                .filter(p -> p.getPeriode().inkluderer(periode.getFomDato()))
                .map(PeriodeMedUtbetalingsgradDto::getUtbetalingsgrad)
                .findFirst()
                .orElse(BigDecimal.ZERO);
    }

    public static Optional<UtbetalingsgradPrAktivitetDto> finnPerioderForStatus(AktivitetStatus status, UtbetalingsgradGrunnlag utbetalingsgradGrunnlag) {
        return utbetalingsgradGrunnlag.getUtbetalingsgradPrAktivitet().stream()
                .filter(ubtGrad -> matcherStatus(status, ubtGrad.getUtbetalingsgradArbeidsforhold().getUttakArbeidType())).findFirst();
    }

    public static BigDecimal finnUtbetalingsgradForArbeid(Arbeidsgiver arbeidsgiver,
                                                          InternArbeidsforholdRefDto arbeidsforholdRef,
                                                          Intervall periode,
                                                          YtelsespesifiktGrunnlag ytelsesSpesifiktGrunnlag,
                                                          boolean skalIgnorereIkkeYrkesaktivStatus) {
        if (ytelsesSpesifiktGrunnlag instanceof UtbetalingsgradGrunnlag utbetalingsgradGrunnlag) {
            return finnUtbetalingsgradForArbeid(arbeidsgiver, arbeidsforholdRef, periode, utbetalingsgradGrunnlag, skalIgnorereIkkeYrkesaktivStatus);
        }
        return BigDecimal.valueOf(100);
    }

    public static BigDecimal finnUtbetalingsgradForArbeid(Arbeidsgiver arbeidsgiver, InternArbeidsforholdRefDto arbeidsforholdRef, Intervall periode, UtbetalingsgradGrunnlag utbetalingsgradGrunnlag, boolean skalIgnorereIkkeYrkesaktivStatus) {
        return finnPerioderForArbeid(utbetalingsgradGrunnlag, arbeidsgiver, arbeidsforholdRef, skalIgnorereIkkeYrkesaktivStatus)
                .stream()
                .flatMap(utb -> utb.getPeriodeMedUtbetalingsgrad().stream())
                .filter(p -> p.getPeriode().inkluderer(periode.getFomDato()))
                .map(PeriodeMedUtbetalingsgradDto::getUtbetalingsgrad)
                .findFirst()
                .orElse(BigDecimal.ZERO);
    }


    public static List<UtbetalingsgradPrAktivitetDto> finnPerioderForArbeid(UtbetalingsgradGrunnlag utbetalingsgradGrunnlag, Arbeidsgiver arbeidsgiver, InternArbeidsforholdRefDto arbeidsforholdRef, boolean skalIgnorereIkkeYrkesaktivStatus) {
        return utbetalingsgradGrunnlag.getUtbetalingsgradPrAktivitet().stream()
                .filter(utbGrad -> erArbeidstaker(utbGrad, skalIgnorereIkkeYrkesaktivStatus) &&
                        matcherArbeidsgiver(utbGrad, arbeidsgiver) &&
                        matcherArbeidsforholdReferanse(utbGrad, arbeidsforholdRef)).toList();
    }

    private static boolean erArbeidstaker(UtbetalingsgradPrAktivitetDto ubtGrad, boolean ignorerIkkeYrkesaktivStatus) {
        if (ignorerIkkeYrkesaktivStatus) {
            return matcherStatusUtenIkkeYrkesaktiv(AktivitetStatus.ARBEIDSTAKER, ubtGrad.getUtbetalingsgradArbeidsforhold().getUttakArbeidType());
        }
        return matcherStatus(AktivitetStatus.ARBEIDSTAKER, ubtGrad.getUtbetalingsgradArbeidsforhold().getUttakArbeidType());
    }

    private static boolean matcherArbeidsforholdReferanse(UtbetalingsgradPrAktivitetDto utbGrad, InternArbeidsforholdRefDto arbeidsforholdRef) {
        return utbGrad.getUtbetalingsgradArbeidsforhold().getInternArbeidsforholdRef().gjelderFor(arbeidsforholdRef);
    }

    private static Boolean matcherArbeidsgiver(UtbetalingsgradPrAktivitetDto utbGrad, Arbeidsgiver arbeidsgiver) {
        return utbGrad.getUtbetalingsgradArbeidsforhold().getArbeidsgiver()
                .map(Arbeidsgiver::getIdentifikator)
                .map(id -> arbeidsgiver.getIdentifikator().equals(id))
                .orElse(false);
    }

}
