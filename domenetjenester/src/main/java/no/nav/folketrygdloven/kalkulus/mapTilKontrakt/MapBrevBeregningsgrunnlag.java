package no.nav.folketrygdloven.kalkulus.mapTilKontrakt;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.input.UtbetalingsgradGrunnlag;
import no.nav.folketrygdloven.kalkulator.input.YtelsespesifiktGrunnlag;
import no.nav.folketrygdloven.kalkulator.konfig.DefaultKonfig;
import no.nav.folketrygdloven.kalkulator.modell.behandling.KoblingReferanse;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagAktivitetStatusDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.TilkommetInntektDto;
import no.nav.folketrygdloven.kalkulus.felles.v1.Periode;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.response.v1.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.brev.BGAndelArbeidsforhold;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.brev.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.brev.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.brev.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.brev.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.brev.TilkommetInntektsforholdDto;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.detaljert.SammenligningsgrunnlagPrStatusDto;

public class MapBrevBeregningsgrunnlag {


    public static BeregningsgrunnlagGrunnlagDto mapGrunnlag(KoblingReferanse koblingReferanse,
                                                            no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto beregningsgrunnlagGrunnlagEntitet,
                                                            YtelsespesifiktGrunnlag ytelsespesifiktGrunnlag,
                                                            boolean erForlengelse) {
        BeregningsgrunnlagDto beregningsgrunnlag = beregningsgrunnlagGrunnlagEntitet.getBeregningsgrunnlag()
                .map(gr -> MapBrevBeregningsgrunnlag.map(gr, ytelsespesifiktGrunnlag)).orElse(null);
        return new BeregningsgrunnlagGrunnlagDto(koblingReferanse.getKoblingUuid(), beregningsgrunnlag, erForlengelse);
    }

    public static BeregningsgrunnlagDto map(no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto beregningsgrunnlagEntitet, YtelsespesifiktGrunnlag ytelsespesifiktGrunnlag) {
        return new BeregningsgrunnlagDto(
                beregningsgrunnlagEntitet.getSkjæringstidspunkt(),
                mapAktivitetstatuser(beregningsgrunnlagEntitet),
                mapBeregningsgrunnlagPerioder(beregningsgrunnlagEntitet, ytelsespesifiktGrunnlag),
                mapSammenligningsgrunnlagPrStatusListe(beregningsgrunnlagEntitet),
                beregningsgrunnlagEntitet.getGrunnbeløp() == null ? null : beregningsgrunnlagEntitet.getGrunnbeløp().getVerdi());
    }

    private static List<SammenligningsgrunnlagPrStatusDto> mapSammenligningsgrunnlagPrStatusListe(no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto beregningsgrunnlagEntitet) {
        return beregningsgrunnlagEntitet.getSammenligningsgrunnlagPrStatusListe().stream().map(MapBrevBeregningsgrunnlag::mapSammeligningsgrunnlagPrStatus)
                .collect(Collectors.toList());
    }

    private static SammenligningsgrunnlagPrStatusDto mapSammeligningsgrunnlagPrStatus(no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.SammenligningsgrunnlagPrStatusDto sammenligningsgrunnlagPrStatus) {
        return new SammenligningsgrunnlagPrStatusDto(
                new Periode(sammenligningsgrunnlagPrStatus.getSammenligningsperiodeFom(), sammenligningsgrunnlagPrStatus.getSammenligningsperiodeTom()),
                sammenligningsgrunnlagPrStatus.getSammenligningsgrunnlagType(),
                sammenligningsgrunnlagPrStatus.getRapportertPrÅr(),
                sammenligningsgrunnlagPrStatus.getAvvikPromilleNy());
    }

    private static List<BeregningsgrunnlagPeriodeDto> mapBeregningsgrunnlagPerioder(no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto bg, YtelsespesifiktGrunnlag ytelsespesifiktGrunnlag) {
        return bg.getBeregningsgrunnlagPerioder().stream().map(p -> MapBrevBeregningsgrunnlag.mapPeriode(p, ytelsespesifiktGrunnlag, bg.getGrunnbeløp().getVerdi())).collect(Collectors.toList());
    }

    private static BeregningsgrunnlagPeriodeDto mapPeriode(no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto beregningsgrunnlagPeriode,
                                                           YtelsespesifiktGrunnlag ytelsespesifiktGrunnlag, BigDecimal grunnbeløp) {
        return new BeregningsgrunnlagPeriodeDto(
                mapAndeler(beregningsgrunnlagPeriode.getBeregningsgrunnlagPrStatusOgAndelList(), beregningsgrunnlagPeriode, ytelsespesifiktGrunnlag, grunnbeløp),
                mapTilkomneInntektsforhold(beregningsgrunnlagPeriode.getTilkomneInntekter()),
                new Periode(beregningsgrunnlagPeriode.getBeregningsgrunnlagPeriodeFom(), beregningsgrunnlagPeriode.getBeregningsgrunnlagPeriodeTom()),
                beregningsgrunnlagPeriode.getBruttoPrÅr(),
                beregningsgrunnlagPeriode.getAvkortetPrÅr(),
                beregningsgrunnlagPeriode.getDagsats(),
                beregningsgrunnlagPeriode.getInntektgraderingsprosentBrutto(),
                beregningsgrunnlagPeriode.getTotalUtbetalingsgradFraUttak(),
                beregningsgrunnlagPeriode.getTotalUtbetalingsgradEtterReduksjonVedTilkommetInntekt(),
                UtledGraderingsdata.utledGraderingsfaktorInntekt(beregningsgrunnlagPeriode, ytelsespesifiktGrunnlag),
                UtledGraderingsdata.utledGraderingsfaktorTid(beregningsgrunnlagPeriode, ytelsespesifiktGrunnlag));
    }

    private static List<TilkommetInntektsforholdDto> mapTilkomneInntektsforhold(List<TilkommetInntektDto> tilkomneInntekter) {
        return tilkomneInntekter == null ? null : tilkomneInntekter.stream().map(t -> new TilkommetInntektsforholdDto(
                        t.getAktivitetStatus(),
                        t.getArbeidsgiver().map(no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver::getIdentifikator).orElse(null),
                        t.getArbeidsforholdRef() == null ? null : t.getArbeidsforholdRef().getReferanse(),
                        t.getBruttoInntektPrÅr().intValue(),
                        t.skalRedusereUtbetaling()))
                .toList();
    }

    private static List<BeregningsgrunnlagPrStatusOgAndelDto> mapAndeler(List<no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto> beregningsgrunnlagPrStatusOgAndelList, no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto periode, YtelsespesifiktGrunnlag ytelsespesifiktGrunnlag, BigDecimal grunnbeløp) {
        return beregningsgrunnlagPrStatusOgAndelList.stream().map(a -> mapAndel(a, periode, ytelsespesifiktGrunnlag, grunnbeløp)).collect(Collectors.toList());
    }

    private static BeregningsgrunnlagPrStatusOgAndelDto mapAndel(no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto andel,
                                                                 no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto periode,
                                                                 YtelsespesifiktGrunnlag ytelsespesifiktGrunnlag, BigDecimal grunnbeløp) {
        return new BeregningsgrunnlagPrStatusOgAndelDto(
                andel.getAndelsnr(),
                andel.getAktivitetStatus(),
                andel.getArbeidsforholdType(),
                andel.getBeregningsperiodeFom() == null ? null : new Periode(andel.getBeregningsperiodeFom(), andel.getBeregningsperiodeTom()),
                andel.getBruttoPrÅr(),
                andel.getDagsatsBruker(),
                andel.getDagsatsArbeidsgiver(),
                finnUgradertDagsatsBruker(andel, periode, ytelsespesifiktGrunnlag, grunnbeløp),
                finnUgradertDagsatsArbeidsgiver(andel, periode, ytelsespesifiktGrunnlag, grunnbeløp),
                andel.getFastsattInntektskategori().getInntektskategori(),
                mapBgAndelArbeidsforhold(andel),
                andel.getAvkortetFørGraderingPrÅr(),
                andel.getAvkortetPrÅr(),
                andel.getOverstyrtPrÅr(),
                andel.getRedusertPrÅr(),
                andel.getBeregnetPrÅr(),
                andel.getFastsattAvSaksbehandler());
    }

    private static Long finnUgradertDagsatsBruker(no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto andel,
                                                  no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto periode,
                                                  YtelsespesifiktGrunnlag ytelsespesifiktGrunnlag, BigDecimal grunnbeløp) {
        if (andel.getDagsatsBruker() == null) {
            return null;
        }
        if (andel.getAvkortetPrÅr().compareTo(BigDecimal.ZERO) == 0) {
            return 0L;
        }
        var andelTilBruker = andel.getAvkortetBrukersAndelPrÅr().divide(andel.getAvkortetPrÅr(), 10, RoundingMode.HALF_UP);
        BigDecimal avkortetFørGraderingPrÅr = finnAvkortetFørGradering(andel, periode, ytelsespesifiktGrunnlag, grunnbeløp);
        return avkortetFørGraderingPrÅr.
                multiply(andelTilBruker)
                .divide(BigDecimal.valueOf(260), 10, RoundingMode.HALF_UP).longValue();
    }

    private static BigDecimal finnAvkortetFørGradering(no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto andel,
                                                       no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto periode,
                                                       YtelsespesifiktGrunnlag ytelsespesifiktGrunnlag, BigDecimal grunnbeløp) {
        BigDecimal avkortetFørGraderingPrÅr;
        if (andel.getAvkortetFørGraderingPrÅr() != null) {
            // Grunnet en feil i mapping har vi ikke lagret avkortetFørGradering for andeler i siste periode for en del behandlinger før 16.12.2021
            avkortetFørGraderingPrÅr = andel.getAvkortetFørGraderingPrÅr();
        } else if (ytelsespesifiktGrunnlag instanceof UtbetalingsgradGrunnlag utbetalingsgradGrunnlag) {
            // Fallback for saker der avkortetFørGraderingPrÅr ikke er satt
            var grenseverdi = grunnbeløp.multiply(new DefaultKonfig().getAntallGØvreGrenseverdi());
            avkortetFørGraderingPrÅr = FinnInntektstak.finnInntektstakForAndel(andel, periode, utbetalingsgradGrunnlag, grenseverdi);
        } else {
            avkortetFørGraderingPrÅr = andel.getAvkortetPrÅr();
        }
        return avkortetFørGraderingPrÅr;
    }

    private static Long finnUgradertDagsatsArbeidsgiver(no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto andel,
                                                        no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto periode,
                                                        YtelsespesifiktGrunnlag ytelsespesifiktGrunnlag, BigDecimal grunnbeløp) {
        if (andel.getDagsatsArbeidsgiver() == null) {
            return null;
        }
        if (andel.getAvkortetPrÅr().compareTo(BigDecimal.ZERO) == 0) {
            return 0L;
        }
        var andelTilArbeidsgiver = andel.getAvkortetRefusjonPrÅr().divide(andel.getAvkortetPrÅr(), 10, RoundingMode.HALF_UP);
        var avkortetFørGraderingPrÅr = finnAvkortetFørGradering(andel, periode, ytelsespesifiktGrunnlag, grunnbeløp);
        return avkortetFørGraderingPrÅr.multiply(andelTilArbeidsgiver)
                .divide(BigDecimal.valueOf(260), 10, RoundingMode.HALF_UP).longValue();

    }

    private static BGAndelArbeidsforhold mapBgAndelArbeidsforhold(no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto beregningsgrunnlagPrStatusOgAndel) {
        return beregningsgrunnlagPrStatusOgAndel.getBgAndelArbeidsforhold().map(MapBrevBeregningsgrunnlag::mapArbeidsforhold).orElse(null);
    }

    private static BGAndelArbeidsforhold mapArbeidsforhold(BGAndelArbeidsforholdDto bgAndelArbeidsforhold) {
        return new BGAndelArbeidsforhold(
                mapArbeidsgiver(bgAndelArbeidsforhold.getArbeidsgiver()),
                bgAndelArbeidsforhold.getArbeidsforholdRef().getReferanse(),
                bgAndelArbeidsforhold.getGjeldendeRefusjonPrÅr());
    }

    private static List<AktivitetStatus> mapAktivitetstatuser(no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto beregningsgrunnlagEntitet) {
        return beregningsgrunnlagEntitet.getAktivitetStatuser().stream().map(BeregningsgrunnlagAktivitetStatusDto::getAktivitetStatus)
                .collect(Collectors.toList());
    }

    private static Arbeidsgiver mapArbeidsgiver(no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver a) {
        return new Arbeidsgiver(a.getOrgnr(), a.getAktørId() != null ? a.getAktørId().getId() : null);
    }

}
