package no.nav.folketrygdloven.kalkulus.mapTilKontrakt;

import static no.nav.folketrygdloven.kalkulator.ytelse.utbgradytelse.AktivitetStatusMatcher.matcherStatusEllerIkkeYrkesaktiv;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.input.UtbetalingsgradGrunnlag;
import no.nav.folketrygdloven.kalkulator.modell.svp.PeriodeMedUtbetalingsgradDto;
import no.nav.folketrygdloven.kalkulator.modell.svp.UtbetalingsgradPrAktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagGrunnlagEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndel;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.Beløp;
import no.nav.folketrygdloven.kalkulus.felles.jpa.IntervallEntitet;
import no.nav.folketrygdloven.kalkulus.felles.v1.Periode;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.Inntektskategori;
import no.nav.folketrygdloven.kalkulus.response.v1.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.brev.BGAndelArbeidsforhold;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.brev.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.brev.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.brev.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.brev.BeregningsgrunnlagPrStatusOgAndelDto;

public class MapBrevBeregningsgrunnlag {


    public static BeregningsgrunnlagGrunnlagDto mapGrunnlag(BeregningsgrunnlagGrunnlagEntitet beregningsgrunnlagGrunnlagEntitet,
                                                            UtbetalingsgradGrunnlag utbetalingsgradGrunnlag) {
        BeregningsgrunnlagDto beregningsgrunnlag = beregningsgrunnlagGrunnlagEntitet.getBeregningsgrunnlag()
                .map(bg -> map(bg, utbetalingsgradGrunnlag)).orElse(null);
        return new BeregningsgrunnlagGrunnlagDto(beregningsgrunnlag);
    }

    public static BeregningsgrunnlagDto map(BeregningsgrunnlagEntitet beregningsgrunnlagEntitet, UtbetalingsgradGrunnlag utbetalingsgradGrunnlag) {
        return new BeregningsgrunnlagDto(
                beregningsgrunnlagEntitet.getSkjæringstidspunkt(),
                mapAktivitetstatuser(beregningsgrunnlagEntitet),
                mapBeregningsgrunnlagPerioder(beregningsgrunnlagEntitet, utbetalingsgradGrunnlag),
                beregningsgrunnlagEntitet.getGrunnbeløp() == null ? null : beregningsgrunnlagEntitet.getGrunnbeløp().getVerdi());
    }

    private static List<BeregningsgrunnlagPeriodeDto> mapBeregningsgrunnlagPerioder(BeregningsgrunnlagEntitet beregningsgrunnlagEntitet, UtbetalingsgradGrunnlag utbetalingsgradGrunnlag) {
        return beregningsgrunnlagEntitet.getBeregningsgrunnlagPerioder().stream().map(p -> mapPeriode(p, utbetalingsgradGrunnlag)).collect(Collectors.toList());
    }

    private static BeregningsgrunnlagPeriodeDto mapPeriode(BeregningsgrunnlagPeriode beregningsgrunnlagPeriode, UtbetalingsgradGrunnlag utbetalingsgradGrunnlag) {
        return new BeregningsgrunnlagPeriodeDto(
                mapAndeler(finnAndelerSomHarSøkt(beregningsgrunnlagPeriode, utbetalingsgradGrunnlag)),
                new Periode(beregningsgrunnlagPeriode.getBeregningsgrunnlagPeriodeFom(), beregningsgrunnlagPeriode.getBeregningsgrunnlagPeriodeTom()),
                mapFraBeløp(beregningsgrunnlagPeriode.getBruttoPrÅr()),
                mapFraBeløp(beregningsgrunnlagPeriode.getAvkortetPrÅr()),
                beregningsgrunnlagPeriode.getDagsats());
    }

    private static List<BeregningsgrunnlagPrStatusOgAndel> finnAndelerSomHarSøkt(BeregningsgrunnlagPeriode beregningsgrunnlagPeriode,
                                                                                 UtbetalingsgradGrunnlag utbetalingsgradGrunnlag) {
        return beregningsgrunnlagPeriode.getBeregningsgrunnlagPrStatusOgAndelList().stream()
                .filter(a -> harSøktUtbetaling(beregningsgrunnlagPeriode, utbetalingsgradGrunnlag, a))
                .collect(Collectors.toList());
    }

    private static boolean harSøktUtbetaling(BeregningsgrunnlagPeriode beregningsgrunnlagPeriode, UtbetalingsgradGrunnlag utbetalingsgradGrunnlag, BeregningsgrunnlagPrStatusOgAndel a) {
        return finnUtbetalingsgradForAndel(a, beregningsgrunnlagPeriode.getPeriode(), utbetalingsgradGrunnlag).compareTo(BigDecimal.ZERO) > 0;
    }

    private static List<BeregningsgrunnlagPrStatusOgAndelDto> mapAndeler(List<BeregningsgrunnlagPrStatusOgAndel> beregningsgrunnlagPrStatusOgAndelList) {
        return beregningsgrunnlagPrStatusOgAndelList.stream().map(MapBrevBeregningsgrunnlag::mapAndel).collect(Collectors.toList());
    }

    private static BeregningsgrunnlagPrStatusOgAndelDto mapAndel(BeregningsgrunnlagPrStatusOgAndel andel) {
        return new BeregningsgrunnlagPrStatusOgAndelDto(
                andel.getAndelsnr(),
                AktivitetStatus.fraKode(andel.getAktivitetStatus().getKode()),
                andel.getArbeidsforholdType(),
                andel.getBeregningsperiodeFom() == null ? null : new Periode(andel.getBeregningsperiodeFom(), andel.getBeregningsperiodeTom()),
                mapFraBeløp(andel.getBruttoPrÅr()),
                andel.getDagsatsBruker(),
                andel.getDagsatsArbeidsgiver(),
                Inntektskategori.fraKode(andel.getInntektskategori().getKode()),
                mapBgAndelArbeidsforhold(andel),
                mapFraBeløp(andel.getAvkortetFørGraderingPrÅr()),
                mapFraBeløp(andel.getAvkortetPrÅr()),
                mapFraBeløp(andel.getOverstyrtPrÅr()),
                mapFraBeløp(andel.getRedusertPrÅr()),
                mapFraBeløp(andel.getBeregnetPrÅr()),
                andel.getFastsattAvSaksbehandler());
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
        return beregningsgrunnlagEntitet.getAktivitetStatuser().stream().map(a -> AktivitetStatus.fraKode(a.getAktivitetStatus().getKode()))
                .collect(Collectors.toList());
    }

    private static Arbeidsgiver mapArbeidsgiver(no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.Arbeidsgiver a) {
        return new Arbeidsgiver(a.getOrgnr(), a.getAktørId() != null ? a.getAktørId().getId() : null);
    }

    private static BigDecimal finnUtbetalingsgradForAndel(BeregningsgrunnlagPrStatusOgAndel andel,
                                                          IntervallEntitet periode,
                                                          UtbetalingsgradGrunnlag utbetalingsgradGrunnlag) {
        if (andel.getAktivitetStatus().erArbeidstaker() && andel.getBgAndelArbeidsforhold().isPresent()) {
            return mapUtbetalingsgradForArbeid(andel.getBgAndelArbeidsforhold().get(), periode, utbetalingsgradGrunnlag);
        } else {
            return finnUtbetalingsgradForStatus(andel.getAktivitetStatus(), periode, utbetalingsgradGrunnlag);
        }
    }


    private static BigDecimal finnUtbetalingsgradForStatus(AktivitetStatus status,
                                                           IntervallEntitet periode,
                                                           UtbetalingsgradGrunnlag utbetalingsgradGrunnlag) {
        if (status.erArbeidstaker()) {
            throw new IllegalStateException("Bruk Arbeidsforhold-mapper");
        }
        return utbetalingsgradGrunnlag.getUtbetalingsgradPrAktivitet().stream()
                .filter(ubtGrad -> matcherStatusEllerIkkeYrkesaktiv(status, ubtGrad.getUtbetalingsgradArbeidsforhold().getUttakArbeidType()))
                .flatMap(utb -> utb.getPeriodeMedUtbetalingsgrad().stream())
                .filter(p -> p.getPeriode().inkluderer(periode.getFomDato()))
                .map(PeriodeMedUtbetalingsgradDto::getUtbetalingsgrad)
                .findFirst()
                .orElse(BigDecimal.ZERO);
    }


    static BigDecimal mapUtbetalingsgradForArbeid(no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BGAndelArbeidsforhold arbeidsforhold,
                                                  IntervallEntitet periode,
                                                  UtbetalingsgradGrunnlag utbetalingsgradGrunnlag) {
        return utbetalingsgradGrunnlag.getUtbetalingsgradPrAktivitet().stream()
                .filter(MapBrevBeregningsgrunnlag::erArbeidstakerEllerIkkeYrkesaktiv)
                .filter(utbGrad -> matcherArbeidsgiver(arbeidsforhold, utbGrad)
                        && matcherArbeidsforholdReferanse(arbeidsforhold, utbGrad))
                .flatMap(utb -> utb.getPeriodeMedUtbetalingsgrad().stream())
                .filter(p -> p.getPeriode().inkluderer(periode.getFomDato()))
                .map(PeriodeMedUtbetalingsgradDto::getUtbetalingsgrad)
                .findFirst()
                .orElse(BigDecimal.ZERO);
    }

    private static boolean erArbeidstakerEllerIkkeYrkesaktiv(UtbetalingsgradPrAktivitetDto ubtGrad) {
        return matcherStatusEllerIkkeYrkesaktiv(AktivitetStatus.ARBEIDSTAKER, ubtGrad.getUtbetalingsgradArbeidsforhold().getUttakArbeidType());
    }

    private static boolean matcherArbeidsforholdReferanse(no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BGAndelArbeidsforhold arbeidsforhold, UtbetalingsgradPrAktivitetDto utbGrad) {
        return utbGrad.getUtbetalingsgradArbeidsforhold().getInternArbeidsforholdRef().gjelderFor(InternArbeidsforholdRefDto.ref(arbeidsforhold.getArbeidsforholdRef().getReferanse()));
    }

    private static Boolean matcherArbeidsgiver(no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BGAndelArbeidsforhold arbeidsforhold, UtbetalingsgradPrAktivitetDto utbGrad) {
        return utbGrad.getUtbetalingsgradArbeidsforhold().getArbeidsgiver()
                .map(no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver::getIdentifikator)
                .map(id -> id.equals(arbeidsforhold.getArbeidsgiver().getIdentifikator()))
                .orElse(false);
    }

    private static BigDecimal mapFraBeløp(Beløp beløp) {
        return beløp == null ? null : beløp.getVerdi();
    }

}
