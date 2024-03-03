package no.nav.folketrygdloven.kalkulus.mapTilKontrakt;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.guitjenester.ModellTyperMapper;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagGUIInput;
import no.nav.folketrygdloven.kalkulator.input.UtbetalingsgradGrunnlag;
import no.nav.folketrygdloven.kalkulator.konfig.KonfigTjeneste;
import no.nav.folketrygdloven.kalkulator.modell.svp.AktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.svp.PeriodeMedUtbetalingsgradDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.felles.v1.Periode;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulus.kodeverk.UttakArbeidType;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.detaljert.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.detaljert.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.detaljert.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulus.typer.Utbetalingsgrad;

public class MapFormidlingsdataBeregningsgrunnlag {

    public static BeregningsgrunnlagGrunnlagDto mapMedBrevfelt(BeregningsgrunnlagGrunnlagDto dto, BeregningsgrunnlagGUIInput input) {
        if (!(input.getYtelsespesifiktGrunnlag() instanceof UtbetalingsgradGrunnlag)) {
            return dto;
        }
        var grenseverdi = Optional.ofNullable(dto.getBeregningsgrunnlag().getGrunnbeløp())
                .map(ModellTyperMapper::beløpFraDto)
                .map(g -> g.multipliser(KonfigTjeneste.getAntallGØvreGrenseverdi()))
                .orElse(Beløp.ZERO);

        UtbetalingsgradGrunnlag yg = input.getYtelsespesifiktGrunnlag();

        if (!dto.getBeregningsgrunnlagTilstand().erFør(BeregningsgrunnlagTilstand.FORESLÅTT)) {
            dto.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder()
                    .forEach(periodeDto -> oppdaterAndelerMedFormidlingsfelt(periodeDto, yg, grenseverdi));
        }

        return dto;
    }

    private static void oppdaterAndelerMedFormidlingsfelt(BeregningsgrunnlagPeriodeDto bgPeriode,
                                                          UtbetalingsgradGrunnlag yg, Beløp grenseverdi) {
        bgPeriode.getBeregningsgrunnlagPrStatusOgAndelList()
                .stream().filter(a -> a.getBruttoPrÅr() != null)
                .forEach(andel -> {
                    andel.setAvkortetFørGraderingPrÅr(andel.getAvkortetFørGraderingPrÅr());
                    andel.setAvkortetMotInntektstak(ModellTyperMapper.beløpTilDto(finnInntektstakForAndel(andel, bgPeriode, yg, grenseverdi)));
                });
    }

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
                : grenseverdiTruketFraIkkeTilgjengeligInntekt.min(ModellTyperMapper.beløpFraDto(andel.getBruttoPrÅr()));
    }

    private static Beløp finnInntektstakForNæring(BeregningsgrunnlagPrStatusOgAndelDto andel,
                                                       BeregningsgrunnlagPeriodeDto inneværendeBGPeriode,
                                                       UtbetalingsgradGrunnlag yg,
                                                       Beløp grenseverdi) {
        if (!harSøktUtbetalingForAndel(inneværendeBGPeriode.getPeriode(), andel, yg)) {
            return Beløp.ZERO.ZERO;
        }
        var inntektIkkeSøktOm = finnInntektIkkeSøktOm(inneværendeBGPeriode, yg);
        var inntektSøktOmAT = finnInntektSøktOm(inneværendeBGPeriode, yg, AktivitetStatus.ARBEIDSTAKER);
        var inntektSøktOmFL = finnInntektSøktOm(inneværendeBGPeriode, yg, AktivitetStatus.FRILANSER);
        var inntektIkkeTilgjengeligForSN = inntektIkkeSøktOm.adder(inntektSøktOmAT).adder(inntektSøktOmFL);
        var grenseverdiTruketFraIkkeTilgjengeligInntekt = grenseverdi.subtraher(inntektIkkeTilgjengeligForSN);
        return grenseverdiTruketFraIkkeTilgjengeligInntekt.compareTo(Beløp.ZERO) <= 0
                ? Beløp.ZERO.ZERO
                : grenseverdiTruketFraIkkeTilgjengeligInntekt.min(ModellTyperMapper.beløpFraDto(andel.getBruttoPrÅr()));
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
                : grenseverdiTruketFraIkkeTilgjengeligInntekt.min(ModellTyperMapper.beløpFraDto(andel.getBruttoPrÅr()));
    }

    private static Beløp finnInntektSøktOm(BeregningsgrunnlagPeriodeDto inneværendeBGPeriode, UtbetalingsgradGrunnlag yg, AktivitetStatus status) {
        List<BeregningsgrunnlagPrStatusOgAndelDto> andelerSøktOm = inneværendeBGPeriode.getBeregningsgrunnlagPrStatusOgAndelList().stream()
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
                : grenseMinusAnnenInntekt.min(ModellTyperMapper.beløpFraDto(andelÅVurdere.getBruttoPrÅr()));
    }

    private static Beløp finnInntektIkkeSøktOm(BeregningsgrunnlagPeriodeDto inneværendeBGPeriode,
                                                    UtbetalingsgradGrunnlag yg) {
        List<BeregningsgrunnlagPrStatusOgAndelDto> alleAndelerUtenUtb = inneværendeBGPeriode.getBeregningsgrunnlagPrStatusOgAndelList().stream()
                .filter(bga -> !harSøktUtbetalingForAndel(inneværendeBGPeriode.getPeriode(), bga, yg))
                .collect(Collectors.toList());
        return finnBrutto(alleAndelerUtenUtb);
    }

    private static Beløp finnBrutto(List<BeregningsgrunnlagPrStatusOgAndelDto> andeler) {
        return andeler.stream()
                .filter(Objects::nonNull)
                .map(BeregningsgrunnlagPrStatusOgAndelDto::getBruttoPrÅr)
                .filter(Objects::nonNull)
                .map(ModellTyperMapper::beløpFraDto)
                .filter(Objects::nonNull)
                .reduce(Beløp::adder)
                .orElse(Beløp.ZERO);
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
                .anyMatch(utbPeriode -> utbPeriode.getUtbetalingsgrad().compareTo(Utbetalingsgrad.ZERO) > 0);
    }
}
