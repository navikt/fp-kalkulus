package no.nav.folketrygdloven.kalkulus.mapTilKontrakt;

import static no.nav.folketrygdloven.kalkulus.felles.tid.AbstractIntervall.TIDENES_ENDE;
import static no.nav.folketrygdloven.kalkulus.mapTilKontrakt.MapDetaljertBeregningsgrunnlag.mapSammenligningsgrunnlag;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagGrunnlagEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndel;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.Beløp;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.KoblingReferanse;
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


    public static BeregningsgrunnlagGrunnlagDto mapGrunnlag(KoblingReferanse koblingReferanse, BeregningsgrunnlagGrunnlagEntitet beregningsgrunnlagGrunnlagEntitet, boolean erForlengelse) {
        BeregningsgrunnlagDto beregningsgrunnlag = beregningsgrunnlagGrunnlagEntitet.getBeregningsgrunnlag()
                .map(MapBrevBeregningsgrunnlag::map).orElse(null);
        return new BeregningsgrunnlagGrunnlagDto(koblingReferanse.getReferanse(), beregningsgrunnlag, erForlengelse);
    }

    public static BeregningsgrunnlagDto map(BeregningsgrunnlagEntitet beregningsgrunnlagEntitet) {
        return new BeregningsgrunnlagDto(
                beregningsgrunnlagEntitet.getSkjæringstidspunkt(),
                mapAktivitetstatuser(beregningsgrunnlagEntitet),
                mapBeregningsgrunnlagPerioder(beregningsgrunnlagEntitet),
                mapSammenligningsgrunnlag(beregningsgrunnlagEntitet),
                beregningsgrunnlagEntitet.getGrunnbeløp() == null ? null : beregningsgrunnlagEntitet.getGrunnbeløp().getVerdi());
    }

    private static List<BeregningsgrunnlagPeriodeDto> mapBeregningsgrunnlagPerioder(BeregningsgrunnlagEntitet beregningsgrunnlagEntitet) {
        return beregningsgrunnlagEntitet.getBeregningsgrunnlagPerioder().stream().map(MapBrevBeregningsgrunnlag::mapPeriode).collect(Collectors.toList());
    }

    private static BeregningsgrunnlagPeriodeDto mapPeriode(BeregningsgrunnlagPeriode beregningsgrunnlagPeriode) {
        return new BeregningsgrunnlagPeriodeDto(
                mapAndeler(beregningsgrunnlagPeriode.getBeregningsgrunnlagPrStatusOgAndelList(), beregningsgrunnlagPeriode.getPeriode().getTomDato().equals(TIDENES_ENDE)),
                new Periode(beregningsgrunnlagPeriode.getBeregningsgrunnlagPeriodeFom(), beregningsgrunnlagPeriode.getBeregningsgrunnlagPeriodeTom()),
                mapFraBeløp(beregningsgrunnlagPeriode.getBruttoPrÅr()),
                mapFraBeløp(beregningsgrunnlagPeriode.getAvkortetPrÅr()),
                beregningsgrunnlagPeriode.getDagsats());
    }

    private static List<BeregningsgrunnlagPrStatusOgAndelDto> mapAndeler(List<BeregningsgrunnlagPrStatusOgAndel> beregningsgrunnlagPrStatusOgAndelList, boolean erSistePeriode) {
        return beregningsgrunnlagPrStatusOgAndelList.stream().map(a -> mapAndel(a, erSistePeriode)).collect(Collectors.toList());
    }

    private static BeregningsgrunnlagPrStatusOgAndelDto mapAndel(BeregningsgrunnlagPrStatusOgAndel andel, boolean erSistePeriode) {
        return new BeregningsgrunnlagPrStatusOgAndelDto(
                andel.getAndelsnr(),
                AktivitetStatus.fraKode(andel.getAktivitetStatus().getKode()),
                andel.getArbeidsforholdType(),
                andel.getBeregningsperiodeFom() == null ? null : new Periode(andel.getBeregningsperiodeFom(), andel.getBeregningsperiodeTom()),
                mapFraBeløp(andel.getBruttoPrÅr()),
                andel.getDagsatsBruker(),
                andel.getDagsatsArbeidsgiver(),
                finnUgradertDagsatsBruker(andel, erSistePeriode),
                finnUgradertDagsatsArbeidsgiver(andel, erSistePeriode),
                Inntektskategori.fraKode(andel.getInntektskategori().getKode()),
                mapBgAndelArbeidsforhold(andel),
                mapFraBeløp(andel.getAvkortetFørGraderingPrÅr()),
                mapFraBeløp(andel.getAvkortetPrÅr()),
                mapFraBeløp(andel.getOverstyrtPrÅr()),
                mapFraBeløp(andel.getRedusertPrÅr()),
                mapFraBeløp(andel.getBeregnetPrÅr()),
                andel.getFastsattAvSaksbehandler());
    }

    private static Long finnUgradertDagsatsBruker(BeregningsgrunnlagPrStatusOgAndel andel, boolean erSistePeriode) {
        if (andel.getDagsatsBruker() == null) {
            return null;
        }
        if (andel.getAvkortetPrÅr().getVerdi().compareTo(BigDecimal.ZERO) == 0) {
            return 0L;
        }
        var andelTilBruker = andel.getAvkortetBrukersAndelPrÅr().getVerdi().divide(andel.getAvkortetPrÅr().getVerdi(), 10, RoundingMode.HALF_UP);
        // Grunnet en feil i mapping har vi ikke lagret avkortetFørGradering for andeler i siste periode for en del behandlinger før 16.12.2021
        var avkortetFørGraderingPrÅr = erSistePeriode ? BigDecimal.ZERO : andel.getAvkortetFørGraderingPrÅr().getVerdi();
        return avkortetFørGraderingPrÅr.multiply(andelTilBruker)
                .divide(BigDecimal.valueOf(260), 10, RoundingMode.HALF_UP).longValue();
    }

    private static Long finnUgradertDagsatsArbeidsgiver(BeregningsgrunnlagPrStatusOgAndel andel, boolean erSistePeriode) {
        if (andel.getDagsatsArbeidsgiver() == null) {
            return null;
        }
        if (andel.getAvkortetPrÅr().getVerdi().compareTo(BigDecimal.ZERO) == 0) {
            return 0L;
        }
        var andelTilArbeidsgiver = andel.getAvkortetRefusjonPrÅr().getVerdi().divide(andel.getAvkortetPrÅr().getVerdi(), 10, RoundingMode.HALF_UP);
        // Grunnet en feil i mapping har vi ikke lagret avkortetFørGradering for andeler i siste periode for en del behandlinger før 16.12.2021
        var avkortetFørGraderingPrÅr = erSistePeriode ? BigDecimal.ZERO : andel.getAvkortetFørGraderingPrÅr().getVerdi();
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
        return beregningsgrunnlagEntitet.getAktivitetStatuser().stream().map(a -> AktivitetStatus.fraKode(a.getAktivitetStatus().getKode()))
                .collect(Collectors.toList());
    }

    private static Arbeidsgiver mapArbeidsgiver(no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.Arbeidsgiver a) {
        return new Arbeidsgiver(a.getOrgnr(), a.getAktørId() != null ? a.getAktørId().getId() : null);
    }

    private static BigDecimal mapFraBeløp(Beløp beløp) {
        return beløp == null ? null : beløp.getVerdi();
    }

}
