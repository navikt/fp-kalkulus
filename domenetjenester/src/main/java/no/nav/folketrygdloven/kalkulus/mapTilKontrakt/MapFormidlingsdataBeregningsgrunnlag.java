package no.nav.folketrygdloven.kalkulus.mapTilKontrakt;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagGUIInput;
import no.nav.folketrygdloven.kalkulator.input.UtbetalingsgradGrunnlag;
import no.nav.folketrygdloven.kalkulator.modell.svp.PeriodeMedUtbetalingsgradDto;
import no.nav.folketrygdloven.kalkulator.modell.svp.AktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.UttakArbeidType;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.felles.v1.Periode;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.detaljert.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.detaljert.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.detaljert.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulus.typer.Beløp;

public class MapFormidlingsdataBeregningsgrunnlag {

    public static BeregningsgrunnlagGrunnlagDto mapMedBrevfelt(BeregningsgrunnlagGrunnlagDto dto, BeregningsgrunnlagGUIInput input) {
        if (!(input.getYtelsespesifiktGrunnlag() instanceof UtbetalingsgradGrunnlag)) {
            return dto;
        }
        BigDecimal grenseverdi = dto.getBeregningsgrunnlag().getGrunnbeløp() == null
                ? BigDecimal.ZERO
                : Beløp.safeVerdi(dto.getBeregningsgrunnlag().getGrunnbeløp()).multiply(BigDecimal.valueOf(6));

        UtbetalingsgradGrunnlag yg = input.getYtelsespesifiktGrunnlag();

        if (!dto.getBeregningsgrunnlagTilstand().erFør(BeregningsgrunnlagTilstand.FORESLÅTT)) {
            dto.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder()
                    .forEach(periodeDto -> oppdaterAndelerMedFormidlingsfelt(periodeDto, yg, grenseverdi));
        }

        return dto;
    }

    private static void oppdaterAndelerMedFormidlingsfelt(BeregningsgrunnlagPeriodeDto bgPeriode,
                                                          UtbetalingsgradGrunnlag yg, BigDecimal grenseverdi) {
        bgPeriode.getBeregningsgrunnlagPrStatusOgAndelList()
                .stream().filter(a -> a.getBruttoPrÅr() != null)
                .forEach(andel -> {
                    andel.setAvkortetFørGraderingPrÅr(andel.getAvkortetFørGraderingPrÅr());
                    andel.setAvkortetMotInntektstak(Beløp.fra(finnInntektstakForAndel(andel, bgPeriode, yg, grenseverdi)));
                });
    }

    static BigDecimal finnInntektstakForAndel(BeregningsgrunnlagPrStatusOgAndelDto andel,
                                              BeregningsgrunnlagPeriodeDto bgPeriode,
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

    private static BigDecimal finnInntektstakForYtelse(BeregningsgrunnlagPrStatusOgAndelDto andel,
                                                       BeregningsgrunnlagPeriodeDto inneværendeBGPeriode,
                                                       UtbetalingsgradGrunnlag yg,
                                                       BigDecimal grenseverdi) {
        if (!harSøktUtbetalingForAndel(inneværendeBGPeriode.getPeriode(), andel, yg)) {
            return BigDecimal.ZERO;
        }
        BigDecimal inntektIkkeSøktOm = finnInntektIkkeSøktOm(inneværendeBGPeriode, yg);
        BigDecimal inntektSøktOmAT = finnInntektSøktOm(inneværendeBGPeriode, yg, AktivitetStatus.ARBEIDSTAKER);
        BigDecimal inntektSøktOmFL = finnInntektSøktOm(inneværendeBGPeriode, yg, AktivitetStatus.FRILANSER);
        BigDecimal inntektSøktOmSN = finnInntektSøktOm(inneværendeBGPeriode, yg, AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE);
        BigDecimal inntektIkkeTilgjengeligForSN = inntektIkkeSøktOm.add(inntektSøktOmAT).add(inntektSøktOmFL).add(inntektSøktOmSN);
        BigDecimal grenseverdiTruketFraIkkeTilgjengeligInntekt = grenseverdi.subtract(inntektIkkeTilgjengeligForSN);
        return grenseverdiTruketFraIkkeTilgjengeligInntekt.compareTo(BigDecimal.ZERO) <= 0
                ? BigDecimal.ZERO
                : grenseverdiTruketFraIkkeTilgjengeligInntekt.min(Beløp.safeVerdi(andel.getBruttoPrÅr()));
    }

    private static BigDecimal finnInntektstakForNæring(BeregningsgrunnlagPrStatusOgAndelDto andel,
                                                       BeregningsgrunnlagPeriodeDto inneværendeBGPeriode,
                                                       UtbetalingsgradGrunnlag yg,
                                                       BigDecimal grenseverdi) {
        if (!harSøktUtbetalingForAndel(inneværendeBGPeriode.getPeriode(), andel, yg)) {
            return BigDecimal.ZERO;
        }
        BigDecimal inntektIkkeSøktOm = finnInntektIkkeSøktOm(inneværendeBGPeriode, yg);
        BigDecimal inntektSøktOmAT = finnInntektSøktOm(inneværendeBGPeriode, yg, AktivitetStatus.ARBEIDSTAKER);
        BigDecimal inntektSøktOmFL = finnInntektSøktOm(inneværendeBGPeriode, yg, AktivitetStatus.FRILANSER);
        BigDecimal inntektIkkeTilgjengeligForSN = inntektIkkeSøktOm.add(inntektSøktOmAT).add(inntektSøktOmFL);
        BigDecimal grenseverdiTruketFraIkkeTilgjengeligInntekt = grenseverdi.subtract(inntektIkkeTilgjengeligForSN);
        return grenseverdiTruketFraIkkeTilgjengeligInntekt.compareTo(BigDecimal.ZERO) <= 0
                ? BigDecimal.ZERO
                : grenseverdiTruketFraIkkeTilgjengeligInntekt.min(Beløp.safeVerdi(andel.getBruttoPrÅr()));
    }

    private static BigDecimal finnInntektstakForFrilans(BeregningsgrunnlagPrStatusOgAndelDto andel,
                                                        BeregningsgrunnlagPeriodeDto inneværendeBGPeriode,
                                                        UtbetalingsgradGrunnlag yg,
                                                        BigDecimal grenseverdi) {

        if (!harSøktUtbetalingForAndel(inneværendeBGPeriode.getPeriode(), andel, yg)) {
            return BigDecimal.ZERO;
        }
        BigDecimal inntektIkkeSøktOm = finnInntektIkkeSøktOm(inneværendeBGPeriode, yg);
        BigDecimal inntektSøktOmAT = finnInntektSøktOm(inneværendeBGPeriode, yg, AktivitetStatus.ARBEIDSTAKER);
        BigDecimal inntektIkkeTilgjengeligForFL = inntektIkkeSøktOm.add(inntektSøktOmAT);
        BigDecimal grenseverdiTruketFraIkkeTilgjengeligInntekt = grenseverdi.subtract(inntektIkkeTilgjengeligForFL);
        return grenseverdiTruketFraIkkeTilgjengeligInntekt.compareTo(BigDecimal.ZERO) <= 0
                ? BigDecimal.ZERO
                : grenseverdiTruketFraIkkeTilgjengeligInntekt.min(Beløp.safeVerdi(andel.getBruttoPrÅr()));
    }

    private static BigDecimal finnInntektSøktOm(BeregningsgrunnlagPeriodeDto inneværendeBGPeriode, UtbetalingsgradGrunnlag yg, AktivitetStatus status) {
        List<BeregningsgrunnlagPrStatusOgAndelDto> andelerSøktOm = inneværendeBGPeriode.getBeregningsgrunnlagPrStatusOgAndelList().stream()
                .filter(bga -> bga.getAktivitetStatus().equals(status))
                .filter(bga -> harSøktUtbetalingForAndel(inneværendeBGPeriode.getPeriode(), bga, yg))
                .collect(Collectors.toList());
        return finnBrutto(andelerSøktOm);
    }

    private static BigDecimal finnInntektstakForArbeidstaker(BeregningsgrunnlagPrStatusOgAndelDto andelÅVurdere,
                                                             BeregningsgrunnlagPeriodeDto inneværendeBGPeriode,
                                                             UtbetalingsgradGrunnlag yg,
                                                             BigDecimal grenseverdi) {
        boolean harSøkt = harSøktUtbetalingForAndel(inneværendeBGPeriode.getPeriode(), andelÅVurdere, yg);
        if (!harSøkt) {
            return BigDecimal.ZERO;
        }

        BigDecimal inntektIkkeSøktOm = finnInntektIkkeSøktOm(inneværendeBGPeriode, yg);
        BigDecimal grenseMinusAnnenInntekt = grenseverdi.subtract(inntektIkkeSøktOm);
        return grenseMinusAnnenInntekt.compareTo(BigDecimal.ZERO) <= 0
                ? BigDecimal.ZERO
                : grenseMinusAnnenInntekt.min(Beløp.safeVerdi(andelÅVurdere.getBruttoPrÅr()));
    }

    private static BigDecimal finnInntektIkkeSøktOm(BeregningsgrunnlagPeriodeDto inneværendeBGPeriode,
                                                    UtbetalingsgradGrunnlag yg) {
        List<BeregningsgrunnlagPrStatusOgAndelDto> alleAndelerUtenUtb = inneværendeBGPeriode.getBeregningsgrunnlagPrStatusOgAndelList().stream()
                .filter(bga -> !harSøktUtbetalingForAndel(inneværendeBGPeriode.getPeriode(), bga, yg))
                .collect(Collectors.toList());
        return finnBrutto(alleAndelerUtenUtb);
    }

    private static BigDecimal finnBrutto(List<BeregningsgrunnlagPrStatusOgAndelDto> andeler) {
        return andeler.stream()
                .filter(Objects::nonNull)
                .map(BeregningsgrunnlagPrStatusOgAndelDto::getBruttoPrÅr)
                .filter(Objects::nonNull)
                .map(Beløp::verdi)
                .filter(Objects::nonNull)
                .reduce(BigDecimal::add)
                .orElse(BigDecimal.ZERO);
    }

    private static boolean harSøktUtbetalingForAndel(Periode bgPeriode,
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
            String agBGOrgnr = bgAndel.getArbeidsgiver() == null ? null : bgAndel.getArbeidsgiver().getArbeidsgiverOrgnr();
            String agBGOAktor = bgAndel.getArbeidsgiver() == null ? null : bgAndel.getArbeidsgiver().getArbeidsgiverAktørId();
            String agIDUtbetaling = utbArbeid.getArbeidsgiver().map(no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver::getIdentifikator).orElse(null);

            var bgAndelRef = InternArbeidsforholdRefDto.ref(bgAndel.getBgAndelArbeidsforhold().getArbeidsforholdRef());
            var utbRef = utbArbeid.getInternArbeidsforholdRef() == null ? InternArbeidsforholdRefDto.nullRef() : utbArbeid.getInternArbeidsforholdRef();

            boolean agMatcher = agBGOAktor == null ? Objects.equals(agBGOrgnr, agIDUtbetaling) : Objects.equals(agBGOAktor, agIDUtbetaling);
            boolean refMatcher = bgAndelRef.gjelderFor(utbRef);
            return agMatcher && refMatcher;
        } else return false;
    }

    private static boolean harSøktUtbetalingIPeriode(List<PeriodeMedUtbetalingsgradDto> periodeMedUtbetalingsgrad, Periode periode) {
        return periodeMedUtbetalingsgrad.stream()
                .filter(utbPeriode -> utbPeriode.getPeriode().overlapper(Intervall.fraOgMedTilOgMed(periode.getFom(), periode.getTom())))
                .anyMatch(utbPeriode -> utbPeriode.getUtbetalingsgrad().compareTo(BigDecimal.ZERO) > 0);
    }
}
