package no.nav.folketrygdloven.kalkulus.mapTilKontrakt;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndel;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.SammenligningsgrunnlagPrStatus;
import no.nav.folketrygdloven.kalkulus.felles.v1.Periode;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.Inntektskategori;
import no.nav.folketrygdloven.kalkulus.response.v1.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.fastsatt.BGAndelArbeidsforhold;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.fastsatt.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.fastsatt.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.fastsatt.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.fastsatt.Sammenligningsgrunnlag;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.fastsatt.SammenligningsgrunnlagPrStatusDto;

public class MapBeregningsgrunnlag {

    public static BeregningsgrunnlagDto map(BeregningsgrunnlagEntitet beregningsgrunnlagEntitet) {
        return new BeregningsgrunnlagDto(
            beregningsgrunnlagEntitet.getSkjæringstidspunkt(),
            mapAktivitetstatuser(beregningsgrunnlagEntitet),
            mapBeregningsgrunnlagPerioder(beregningsgrunnlagEntitet),
            mapSammenligningsgrunnlag(beregningsgrunnlagEntitet),
            mapSammenligningsgrunnlagPrStatusListe(beregningsgrunnlagEntitet),
            beregningsgrunnlagEntitet.getFaktaOmBeregningTilfeller(),
            beregningsgrunnlagEntitet.isOverstyrt());
    }

    private static List<SammenligningsgrunnlagPrStatusDto> mapSammenligningsgrunnlagPrStatusListe(BeregningsgrunnlagEntitet beregningsgrunnlagEntitet) {
        if (beregningsgrunnlagEntitet.getSammenligningsgrunnlagPrStatusListe() == null) {
            return null;
        }
        return beregningsgrunnlagEntitet.getSammenligningsgrunnlagPrStatusListe().stream().map(MapBeregningsgrunnlag::mapSammeligningsgrunnlagPrStatus)
            .collect(Collectors.toList());
    }

    private static SammenligningsgrunnlagPrStatusDto mapSammeligningsgrunnlagPrStatus(SammenligningsgrunnlagPrStatus sammenligningsgrunnlagPrStatus) {
        return new SammenligningsgrunnlagPrStatusDto(
            new Periode(sammenligningsgrunnlagPrStatus.getSammenligningsperiodeFom(), sammenligningsgrunnlagPrStatus.getSammenligningsperiodeTom()),
            sammenligningsgrunnlagPrStatus.getSammenligningsgrunnlagType(),
            sammenligningsgrunnlagPrStatus.getRapportertPrÅr(),
            sammenligningsgrunnlagPrStatus.getAvvikPromilleNy());
    }

    private static Sammenligningsgrunnlag mapSammenligningsgrunnlag(BeregningsgrunnlagEntitet beregningsgrunnlagEntitet) {
        return beregningsgrunnlagEntitet.getSammenligningsgrunnlag().map(sg -> new Sammenligningsgrunnlag(
            new Periode(sg.getSammenligningsperiodeFom(), sg.getSammenligningsperiodeTom()),
            sg.getRapportertPrÅr(),
            sg.getAvvikPromilleNy())).orElse(null);
    }

    private static List<BeregningsgrunnlagPeriodeDto> mapBeregningsgrunnlagPerioder(BeregningsgrunnlagEntitet beregningsgrunnlagEntitet) {
        return beregningsgrunnlagEntitet.getBeregningsgrunnlagPerioder().stream().map(MapBeregningsgrunnlag::mapPeriode).collect(Collectors.toList());
    }

    private static BeregningsgrunnlagPeriodeDto mapPeriode(BeregningsgrunnlagPeriode beregningsgrunnlagPeriode) {
        return new BeregningsgrunnlagPeriodeDto(
            mapAndeler(beregningsgrunnlagPeriode.getBeregningsgrunnlagPrStatusOgAndelList()),
            new Periode(beregningsgrunnlagPeriode.getBeregningsgrunnlagPeriodeFom(), beregningsgrunnlagPeriode.getBeregningsgrunnlagPeriodeTom()),
            beregningsgrunnlagPeriode.getBruttoPrÅr(),
            beregningsgrunnlagPeriode.getAvkortetPrÅr(),
            beregningsgrunnlagPeriode.getRedusertPrÅr(),
            beregningsgrunnlagPeriode.getDagsats(),
            beregningsgrunnlagPeriode.getPeriodeÅrsaker());
    }

    private static List<BeregningsgrunnlagPrStatusOgAndelDto> mapAndeler(List<BeregningsgrunnlagPrStatusOgAndel> beregningsgrunnlagPrStatusOgAndelList) {
        return beregningsgrunnlagPrStatusOgAndelList.stream().map(MapBeregningsgrunnlag::mapAndel).collect(Collectors.toList());
    }

    private static BeregningsgrunnlagPrStatusOgAndelDto mapAndel(BeregningsgrunnlagPrStatusOgAndel beregningsgrunnlagPrStatusOgAndel) {
        return new BeregningsgrunnlagPrStatusOgAndelDto(
            AktivitetStatus.fraKode(beregningsgrunnlagPrStatusOgAndel.getAktivitetStatus().getKode()),
            mapBeregningsperiode(beregningsgrunnlagPrStatusOgAndel.getBeregningsperiodeFom(), beregningsgrunnlagPrStatusOgAndel.getBeregningsperiodeTom()),
            beregningsgrunnlagPrStatusOgAndel.getArbeidsforholdType(),
            beregningsgrunnlagPrStatusOgAndel.getBruttoPrÅr(),
            beregningsgrunnlagPrStatusOgAndel.getRedusertRefusjonPrÅr(),
            beregningsgrunnlagPrStatusOgAndel.getRedusertBrukersAndelPrÅr(),
            beregningsgrunnlagPrStatusOgAndel.getDagsatsBruker(),
            beregningsgrunnlagPrStatusOgAndel.getDagsatsArbeidsgiver(),
            Inntektskategori.fraKode(beregningsgrunnlagPrStatusOgAndel.getInntektskategori().getKode()),
            mapBgAndelArbeidsforhold(beregningsgrunnlagPrStatusOgAndel));
    }

    private static Periode mapBeregningsperiode(LocalDate fom, LocalDate tom) {
        if (fom == null && tom == null) {
            return null;
        }
        return new Periode(fom, tom);
    }

    private static BGAndelArbeidsforhold mapBgAndelArbeidsforhold(BeregningsgrunnlagPrStatusOgAndel beregningsgrunnlagPrStatusOgAndel) {
        return beregningsgrunnlagPrStatusOgAndel.getBgAndelArbeidsforhold().map(MapBeregningsgrunnlag::mapArbeidsforhold).orElse(null);
    }

    private static BGAndelArbeidsforhold mapArbeidsforhold(no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BGAndelArbeidsforhold bgAndelArbeidsforhold) {
        return new BGAndelArbeidsforhold(
            new Arbeidsgiver(bgAndelArbeidsforhold.getArbeidsgiver().getOrgnr(),
                bgAndelArbeidsforhold.getArbeidsgiver().getAktørId() != null ? bgAndelArbeidsforhold.getArbeidsgiver().getAktørId().getId() : null),
            bgAndelArbeidsforhold.getArbeidsforholdRef().getReferanse(),
            bgAndelArbeidsforhold.getGjeldendeRefusjonPrÅr(),
            bgAndelArbeidsforhold.getNaturalytelseBortfaltPrÅr().orElse(null),
            bgAndelArbeidsforhold.getNaturalytelseTilkommetPrÅr().orElse(null),
            bgAndelArbeidsforhold.getArbeidsperiodeFom(),
            bgAndelArbeidsforhold.getArbeidsperiodeTom().orElse(null));
    }

    private static List<AktivitetStatus> mapAktivitetstatuser(BeregningsgrunnlagEntitet beregningsgrunnlagEntitet) {
        return beregningsgrunnlagEntitet.getAktivitetStatuser().stream().map(a -> AktivitetStatus.fraKode(a.getAktivitetStatus().getKode()))
            .collect(Collectors.toList());
    }

}
