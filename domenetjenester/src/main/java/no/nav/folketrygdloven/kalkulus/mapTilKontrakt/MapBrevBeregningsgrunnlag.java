package no.nav.folketrygdloven.kalkulus.mapTilKontrakt;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagGUIInput;
import no.nav.folketrygdloven.kalkulator.input.UtbetalingsgradGrunnlag;
import no.nav.folketrygdloven.kalkulator.input.YtelsespesifiktGrunnlag;
import no.nav.folketrygdloven.kalkulator.konfig.DefaultKonfig;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagAktivitetStatus;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagGrunnlagEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndel;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.SammenligningsgrunnlagPrStatus;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.Beløp;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.KoblingReferanse;
import no.nav.folketrygdloven.kalkulus.felles.v1.Periode;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.mapFraEntitet.SammenligningTypeMapper;
import no.nav.folketrygdloven.kalkulus.response.v1.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.brev.BGAndelArbeidsforhold;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.brev.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.brev.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.brev.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.brev.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.detaljert.SammenligningsgrunnlagPrStatusDto;

public class MapBrevBeregningsgrunnlag {


    public static BeregningsgrunnlagGrunnlagDto mapGrunnlag(KoblingReferanse koblingReferanse,
                                                            BeregningsgrunnlagGrunnlagEntitet beregningsgrunnlagGrunnlagEntitet,
                                                            BeregningsgrunnlagGUIInput input,
                                                            boolean erForlengelse) {
        BeregningsgrunnlagDto beregningsgrunnlag = beregningsgrunnlagGrunnlagEntitet.getBeregningsgrunnlag()
                .map(gr -> MapBrevBeregningsgrunnlag.map(gr, input.getYtelsespesifiktGrunnlag())).orElse(null);
        return new BeregningsgrunnlagGrunnlagDto(koblingReferanse.getReferanse(), beregningsgrunnlag, erForlengelse);
    }

    public static BeregningsgrunnlagDto map(BeregningsgrunnlagEntitet beregningsgrunnlagEntitet, YtelsespesifiktGrunnlag ytelsespesifiktGrunnlag) {
        return new BeregningsgrunnlagDto(
                beregningsgrunnlagEntitet.getSkjæringstidspunkt(),
                mapAktivitetstatuser(beregningsgrunnlagEntitet),
                mapBeregningsgrunnlagPerioder(beregningsgrunnlagEntitet, ytelsespesifiktGrunnlag),
                mapSammenligningsgrunnlagPrStatusListe(beregningsgrunnlagEntitet),
                beregningsgrunnlagEntitet.getGrunnbeløp() == null ? null : beregningsgrunnlagEntitet.getGrunnbeløp().getVerdi());
    }

    private static List<SammenligningsgrunnlagPrStatusDto> mapSammenligningsgrunnlagPrStatusListe(BeregningsgrunnlagEntitet beregningsgrunnlagEntitet) {
        if (beregningsgrunnlagEntitet.getSammenligningsgrunnlagPrStatusListe().isEmpty() && beregningsgrunnlagEntitet.getSammenligningsgrunnlag().isPresent()) {
            // Mapper gammel sammenligningsgrunnlag over til nytt frem til vi har migrert
            var gammeltSG = beregningsgrunnlagEntitet.getSammenligningsgrunnlag().get();
            return Collections.singletonList(new SammenligningsgrunnlagPrStatusDto(
                    new Periode(gammeltSG.getSammenligningsperiodeFom(), gammeltSG.getSammenligningsperiodeTom()),
                    SammenligningTypeMapper.finnSammenligningtypeFraAktivitetstatus(beregningsgrunnlagEntitet),
                    mapFraBeløp(gammeltSG.getRapportertPrÅr()),
                    gammeltSG.getAvvikPromilleNy().getVerdi()));
        }
        return beregningsgrunnlagEntitet.getSammenligningsgrunnlagPrStatusListe().stream().map(MapBrevBeregningsgrunnlag::mapSammeligningsgrunnlagPrStatus)
                .collect(Collectors.toList());
    }

    private static SammenligningsgrunnlagPrStatusDto mapSammeligningsgrunnlagPrStatus(SammenligningsgrunnlagPrStatus sammenligningsgrunnlagPrStatus) {
        return new SammenligningsgrunnlagPrStatusDto(
                new Periode(sammenligningsgrunnlagPrStatus.getSammenligningsperiodeFom(), sammenligningsgrunnlagPrStatus.getSammenligningsperiodeTom()),
                sammenligningsgrunnlagPrStatus.getSammenligningsgrunnlagType(),
                mapFraBeløp(sammenligningsgrunnlagPrStatus.getRapportertPrÅr()),
                sammenligningsgrunnlagPrStatus.getGjeldendeAvvik().getVerdi());
    }

    private static List<BeregningsgrunnlagPeriodeDto> mapBeregningsgrunnlagPerioder(BeregningsgrunnlagEntitet beregningsgrunnlagEntitet, YtelsespesifiktGrunnlag ytelsespesifiktGrunnlag) {
        return beregningsgrunnlagEntitet.getBeregningsgrunnlagPerioder().stream().map(p -> MapBrevBeregningsgrunnlag.mapPeriode(p, ytelsespesifiktGrunnlag)).collect(Collectors.toList());
    }

    private static BeregningsgrunnlagPeriodeDto mapPeriode(BeregningsgrunnlagPeriode beregningsgrunnlagPeriode, YtelsespesifiktGrunnlag ytelsespesifiktGrunnlag) {
        return new BeregningsgrunnlagPeriodeDto(
                mapAndeler(beregningsgrunnlagPeriode.getBeregningsgrunnlagPrStatusOgAndelList(), beregningsgrunnlagPeriode, ytelsespesifiktGrunnlag),
                new Periode(beregningsgrunnlagPeriode.getBeregningsgrunnlagPeriodeFom(), beregningsgrunnlagPeriode.getBeregningsgrunnlagPeriodeTom()),
                mapFraBeløp(beregningsgrunnlagPeriode.getBruttoPrÅr()),
                mapFraBeløp(beregningsgrunnlagPeriode.getAvkortetPrÅr()),
                beregningsgrunnlagPeriode.getDagsats(), beregningsgrunnlagPeriode.getInntektgraderingsprosentBrutto() != null ? beregningsgrunnlagPeriode.getInntektgraderingsprosentBrutto().getVerdi() : null);
    }

    private static List<BeregningsgrunnlagPrStatusOgAndelDto> mapAndeler(List<BeregningsgrunnlagPrStatusOgAndel> beregningsgrunnlagPrStatusOgAndelList, BeregningsgrunnlagPeriode periode, YtelsespesifiktGrunnlag ytelsespesifiktGrunnlag) {
        return beregningsgrunnlagPrStatusOgAndelList.stream().map(a -> mapAndel(a, periode, ytelsespesifiktGrunnlag)).collect(Collectors.toList());
    }

    private static BeregningsgrunnlagPrStatusOgAndelDto mapAndel(BeregningsgrunnlagPrStatusOgAndel andel,
                                                                 BeregningsgrunnlagPeriode periode,
                                                                 YtelsespesifiktGrunnlag ytelsespesifiktGrunnlag) {
        return new BeregningsgrunnlagPrStatusOgAndelDto(
                andel.getAndelsnr(),
                andel.getAktivitetStatus(),
                andel.getArbeidsforholdType(),
                andel.getBeregningsperiodeFom() == null ? null : new Periode(andel.getBeregningsperiodeFom(), andel.getBeregningsperiodeTom()),
                mapFraBeløp(andel.getBruttoPrÅr()),
                andel.getDagsatsBruker(),
                andel.getDagsatsArbeidsgiver(),
                finnUgradertDagsatsBruker(andel, periode, ytelsespesifiktGrunnlag),
                finnUgradertDagsatsArbeidsgiver(andel, periode, ytelsespesifiktGrunnlag),
                andel.getInntektskategori(),
                mapBgAndelArbeidsforhold(andel),
                mapFraBeløp(andel.getAvkortetFørGraderingPrÅr()),
                mapFraBeløp(andel.getAvkortetPrÅr()),
                mapFraBeløp(andel.getOverstyrtPrÅr()),
                mapFraBeløp(andel.getRedusertPrÅr()),
                mapFraBeløp(andel.getBeregnetPrÅr()),
                andel.getFastsattAvSaksbehandler());
    }

    private static Long finnUgradertDagsatsBruker(BeregningsgrunnlagPrStatusOgAndel andel,
                                                  BeregningsgrunnlagPeriode periode,
                                                  YtelsespesifiktGrunnlag ytelsespesifiktGrunnlag) {
        if (andel.getDagsatsBruker() == null) {
            return null;
        }
        if (andel.getAvkortetPrÅr().getVerdi().compareTo(BigDecimal.ZERO) == 0) {
            return 0L;
        }
        var andelTilBruker = andel.getAvkortetBrukersAndelPrÅr().getVerdi().divide(andel.getAvkortetPrÅr().getVerdi(), 10, RoundingMode.HALF_UP);
        BigDecimal avkortetFørGraderingPrÅr = finnAvkortetFørGradering(andel, periode, ytelsespesifiktGrunnlag);
        return avkortetFørGraderingPrÅr.
                multiply(andelTilBruker)
                .divide(BigDecimal.valueOf(260), 10, RoundingMode.HALF_UP).longValue();
    }

    private static BigDecimal finnAvkortetFørGradering(BeregningsgrunnlagPrStatusOgAndel andel, BeregningsgrunnlagPeriode periode, YtelsespesifiktGrunnlag ytelsespesifiktGrunnlag) {
        BigDecimal avkortetFørGraderingPrÅr;
        if (andel.getAvkortetFørGraderingPrÅr() != null) {
            // Grunnet en feil i mapping har vi ikke lagret avkortetFørGradering for andeler i siste periode for en del behandlinger før 16.12.2021
            avkortetFørGraderingPrÅr = andel.getAvkortetFørGraderingPrÅr().getVerdi();
        } else if (ytelsespesifiktGrunnlag instanceof UtbetalingsgradGrunnlag utbetalingsgradGrunnlag) {
            // Fallback for saker der avkortetFørGraderingPrÅr ikke er satt
            var grenseverdi = periode.getBeregningsgrunnlag().getGrunnbeløp().getVerdi().multiply(new DefaultKonfig().getAntallGØvreGrenseverdi());
            avkortetFørGraderingPrÅr = FinnInntektstak.finnInntektstakForAndel(andel, periode, utbetalingsgradGrunnlag, grenseverdi);
        } else {
            avkortetFørGraderingPrÅr = andel.getAvkortetPrÅr().getVerdi();
        }
        return avkortetFørGraderingPrÅr;
    }

    private static Long finnUgradertDagsatsArbeidsgiver(BeregningsgrunnlagPrStatusOgAndel andel,
                                                        BeregningsgrunnlagPeriode periode,
                                                        YtelsespesifiktGrunnlag ytelsespesifiktGrunnlag) {
        if (andel.getDagsatsArbeidsgiver() == null) {
            return null;
        }
        if (andel.getAvkortetPrÅr().getVerdi().compareTo(BigDecimal.ZERO) == 0) {
            return 0L;
        }
        var andelTilArbeidsgiver = andel.getAvkortetRefusjonPrÅr().getVerdi().divide(andel.getAvkortetPrÅr().getVerdi(), 10, RoundingMode.HALF_UP);
        var avkortetFørGraderingPrÅr = finnAvkortetFørGradering(andel, periode, ytelsespesifiktGrunnlag);
        return avkortetFørGraderingPrÅr.multiply(andelTilArbeidsgiver)
                .divide(BigDecimal.valueOf(260), 10, RoundingMode.HALF_UP).longValue();

    }

    private static BGAndelArbeidsforhold mapBgAndelArbeidsforhold(BeregningsgrunnlagPrStatusOgAndel beregningsgrunnlagPrStatusOgAndel) {
        return beregningsgrunnlagPrStatusOgAndel.getBgAndelArbeidsforhold().map(MapBrevBeregningsgrunnlag::mapArbeidsforhold).orElse(null);
    }

    private static BGAndelArbeidsforhold mapArbeidsforhold(no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BGAndelArbeidsforhold bgAndelArbeidsforhold) {
        return new BGAndelArbeidsforhold(
                mapArbeidsgiver(bgAndelArbeidsforhold.getArbeidsgiver()),
                bgAndelArbeidsforhold.getArbeidsforholdRef().getReferanse(),
                mapFraBeløp(bgAndelArbeidsforhold.getGjeldendeRefusjonPrÅr()));
    }

    private static List<AktivitetStatus> mapAktivitetstatuser(BeregningsgrunnlagEntitet beregningsgrunnlagEntitet) {
        return beregningsgrunnlagEntitet.getAktivitetStatuser().stream().map(BeregningsgrunnlagAktivitetStatus::getAktivitetStatus)
                .collect(Collectors.toList());
    }

    private static Arbeidsgiver mapArbeidsgiver(no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.Arbeidsgiver a) {
        return new Arbeidsgiver(a.getOrgnr(), a.getAktørId() != null ? a.getAktørId().getId() : null);
    }

    private static BigDecimal mapFraBeløp(Beløp beløp) {
        return beløp == null ? null : beløp.getVerdi();
    }

}
