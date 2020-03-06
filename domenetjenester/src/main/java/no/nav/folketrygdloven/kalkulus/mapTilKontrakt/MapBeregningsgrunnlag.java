package no.nav.folketrygdloven.kalkulus.mapTilKontrakt;

import java.util.List;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndel;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.SammenligningsgrunnlagPrStatus;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.FaktaOmBeregningTilfelle;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.PeriodeÅrsak;
import no.nav.folketrygdloven.kalkulus.felles.v1.Periode;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.Inntektskategori;
import no.nav.folketrygdloven.kalkulus.kodeverk.OpptjeningAktivitetType;
import no.nav.folketrygdloven.kalkulus.kodeverk.SammenligningsgrunnlagType;
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
                beregningsgrunnlagEntitet.getFaktaOmBeregningTilfeller().stream().map(MapBeregningsgrunnlag::mapTilfelle).collect(Collectors.toList()),
                beregningsgrunnlagEntitet.isOverstyrt()
                );
    }

    private static no.nav.folketrygdloven.kalkulus.kodeverk.FaktaOmBeregningTilfelle mapTilfelle(FaktaOmBeregningTilfelle faktaOmBeregningTilfelle) {
        return new no.nav.folketrygdloven.kalkulus.kodeverk.FaktaOmBeregningTilfelle(faktaOmBeregningTilfelle.getKode());
    }

    private static List<SammenligningsgrunnlagPrStatusDto> mapSammenligningsgrunnlagPrStatusListe(BeregningsgrunnlagEntitet beregningsgrunnlagEntitet) {
        if (beregningsgrunnlagEntitet.getSammenligningsgrunnlagPrStatusListe() == null) {
            return null;
        }
        return beregningsgrunnlagEntitet.getSammenligningsgrunnlagPrStatusListe().stream().map(MapBeregningsgrunnlag::mapSammeligningsgrunnlagPrStatus).collect(Collectors.toList());
    }

    private static SammenligningsgrunnlagPrStatusDto mapSammeligningsgrunnlagPrStatus(SammenligningsgrunnlagPrStatus sammenligningsgrunnlagPrStatus) {
        return new SammenligningsgrunnlagPrStatusDto(
                new Periode(sammenligningsgrunnlagPrStatus.getSammenligningsperiodeFom(), sammenligningsgrunnlagPrStatus.getSammenligningsperiodeTom()),
                new SammenligningsgrunnlagType(sammenligningsgrunnlagPrStatus.getSammenligningsgrunnlagType().getKode()),
                sammenligningsgrunnlagPrStatus.getRapportertPrÅr(),
                sammenligningsgrunnlagPrStatus.getAvvikPromilleNy()
        );
    }

    private static Sammenligningsgrunnlag mapSammenligningsgrunnlag(BeregningsgrunnlagEntitet beregningsgrunnlagEntitet) {
        if (beregningsgrunnlagEntitet.getSammenligningsgrunnlag() == null) {
            return null;
        }
        no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.Sammenligningsgrunnlag sammenligningsgrunnlag = beregningsgrunnlagEntitet.getSammenligningsgrunnlag();
        return new Sammenligningsgrunnlag(
                new Periode(sammenligningsgrunnlag.getSammenligningsperiodeFom(), sammenligningsgrunnlag.getSammenligningsperiodeTom()),
                sammenligningsgrunnlag.getRapportertPrÅr(),
                sammenligningsgrunnlag.getAvvikPromilleNy()
        );
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
                beregningsgrunnlagPeriode.getPeriodeÅrsaker().stream().map(MapBeregningsgrunnlag::mapPeriodeÅrsak).collect(Collectors.toList()));
    }

    private static no.nav.folketrygdloven.kalkulus.kodeverk.PeriodeÅrsak mapPeriodeÅrsak(PeriodeÅrsak periodeÅrsak) {
        return new no.nav.folketrygdloven.kalkulus.kodeverk.PeriodeÅrsak(periodeÅrsak.getKode());
    }

    private static List<BeregningsgrunnlagPrStatusOgAndelDto> mapAndeler(List<BeregningsgrunnlagPrStatusOgAndel> beregningsgrunnlagPrStatusOgAndelList) {
        return beregningsgrunnlagPrStatusOgAndelList.stream().map(MapBeregningsgrunnlag::mapAndel).collect(Collectors.toList());
    }

    private static BeregningsgrunnlagPrStatusOgAndelDto mapAndel(BeregningsgrunnlagPrStatusOgAndel beregningsgrunnlagPrStatusOgAndel) {
        return new BeregningsgrunnlagPrStatusOgAndelDto(
                new AktivitetStatus(beregningsgrunnlagPrStatusOgAndel.getAktivitetStatus().getKode()),
                new Periode(beregningsgrunnlagPrStatusOgAndel.getBeregningsgrunnlagPeriode().getPeriode().getFomDato(), beregningsgrunnlagPrStatusOgAndel.getBeregningsgrunnlagPeriode().getPeriode().getTomDato()),
                new OpptjeningAktivitetType(beregningsgrunnlagPrStatusOgAndel.getArbeidsforholdType().getKode()),
                beregningsgrunnlagPrStatusOgAndel.getBruttoPrÅr(),
                beregningsgrunnlagPrStatusOgAndel.getRedusertRefusjonPrÅr(),
                beregningsgrunnlagPrStatusOgAndel.getRedusertBrukersAndelPrÅr(),
                beregningsgrunnlagPrStatusOgAndel.getDagsatsBruker(),
                beregningsgrunnlagPrStatusOgAndel.getDagsatsArbeidsgiver(),
                new Inntektskategori(beregningsgrunnlagPrStatusOgAndel.getInntektskategori().getKode()),
                mapBgAndelArbeidsforhold(beregningsgrunnlagPrStatusOgAndel)
                );
    }

    private static BGAndelArbeidsforhold mapBgAndelArbeidsforhold(BeregningsgrunnlagPrStatusOgAndel beregningsgrunnlagPrStatusOgAndel) {
        return beregningsgrunnlagPrStatusOgAndel.getBgAndelArbeidsforhold().map(MapBeregningsgrunnlag::mapArbeidsforhold).orElse(null);
    }

    private static BGAndelArbeidsforhold mapArbeidsforhold(no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BGAndelArbeidsforhold bgAndelArbeidsforhold) {
        return new BGAndelArbeidsforhold(
                new Arbeidsgiver(bgAndelArbeidsforhold.getArbeidsgiver().getOrgnr(), bgAndelArbeidsforhold.getArbeidsgiver().getAktørId() != null ? bgAndelArbeidsforhold.getArbeidsgiver().getAktørId().getId() : null),
                bgAndelArbeidsforhold.getArbeidsforholdRef().getReferanse(),
                bgAndelArbeidsforhold.getRefusjonskravPrÅr(),
                bgAndelArbeidsforhold.getNaturalytelseBortfaltPrÅr().orElse(null),
                bgAndelArbeidsforhold.getNaturalytelseTilkommetPrÅr().orElse(null),
                bgAndelArbeidsforhold.getArbeidsperiodeFom(),
                bgAndelArbeidsforhold.getArbeidsperiodeTom().orElse(null)
        );
    }

    private static List<AktivitetStatus> mapAktivitetstatuser(BeregningsgrunnlagEntitet beregningsgrunnlagEntitet) {
        return beregningsgrunnlagEntitet.getAktivitetStatuser().stream().map(a -> new AktivitetStatus(a.getAktivitetStatus().getKode()))
                    .collect(Collectors.toList());
    }

}
