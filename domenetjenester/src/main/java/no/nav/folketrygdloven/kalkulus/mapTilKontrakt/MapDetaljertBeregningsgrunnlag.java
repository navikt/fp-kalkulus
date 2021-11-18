package no.nav.folketrygdloven.kalkulus.mapTilKontrakt;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagGUIInput;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningAktivitetAggregatEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningAktivitetEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningAktivitetOverstyringEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningAktivitetOverstyringerEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningRefusjonOverstyringEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningRefusjonOverstyringerEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagGrunnlagEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndel;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.FaktaAggregatEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.FaktaAktørEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.FaktaArbeidsforholdEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.SammenligningsgrunnlagPrStatus;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.Beløp;
import no.nav.folketrygdloven.kalkulus.felles.v1.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulus.felles.v1.Periode;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.AndelKilde;
import no.nav.folketrygdloven.kalkulus.kodeverk.Inntektskategori;
import no.nav.folketrygdloven.kalkulus.response.v1.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.detaljert.BGAndelArbeidsforhold;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.detaljert.BeregningAktivitetAggregatDto;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.detaljert.BeregningAktivitetDto;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.detaljert.BeregningAktivitetOverstyringDto;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.detaljert.BeregningAktivitetOverstyringerDto;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.detaljert.BeregningRefusjonOverstyringDto;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.detaljert.BeregningRefusjonOverstyringerDto;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.detaljert.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.detaljert.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.detaljert.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.detaljert.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.detaljert.FaktaAggregatDto;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.detaljert.FaktaAktørDto;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.detaljert.FaktaArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.detaljert.Sammenligningsgrunnlag;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.detaljert.SammenligningsgrunnlagPrStatusDto;

public class MapDetaljertBeregningsgrunnlag {

    public static BeregningsgrunnlagGrunnlagDto mapMedBrevfelt(BeregningsgrunnlagGrunnlagEntitet grunnlag, BeregningsgrunnlagGUIInput input) {
        BeregningsgrunnlagGrunnlagDto dto = mapGrunnlag(grunnlag);
        BeregningsgrunnlagGrunnlagDto beregningsgrunnlagGrunnlagDto = MapFormidlingsdataBeregningsgrunnlag.mapMedBrevfelt(dto, input);
        return beregningsgrunnlagGrunnlagDto;
    }

    public static BeregningsgrunnlagGrunnlagDto mapGrunnlag(BeregningsgrunnlagGrunnlagEntitet beregningsgrunnlagGrunnlagEntitet) {
        BeregningsgrunnlagDto beregningsgrunnlag = beregningsgrunnlagGrunnlagEntitet.getBeregningsgrunnlag()
                .map(MapDetaljertBeregningsgrunnlag::map).orElse(null);

        return new BeregningsgrunnlagGrunnlagDto(
                beregningsgrunnlag,
                beregningsgrunnlagGrunnlagEntitet.getFaktaAggregat().map(MapDetaljertBeregningsgrunnlag::mapFaktaAggregat).orElse(null),
                mapBeregningAktivitetAggregat(beregningsgrunnlagGrunnlagEntitet.getRegisterAktiviteter()),
                beregningsgrunnlagGrunnlagEntitet.getSaksbehandletAktiviteter().map(MapDetaljertBeregningsgrunnlag::mapBeregningAktivitetAggregat).orElse(null),
                beregningsgrunnlagGrunnlagEntitet.getOverstyring().map(MapDetaljertBeregningsgrunnlag::mapOverstyrteAktiviteterAggregat).orElse(null),
                beregningsgrunnlagGrunnlagEntitet.getRefusjonOverstyringer().map(MapDetaljertBeregningsgrunnlag::mapRefusjonOverstyringAggregat).orElse(null),
                beregningsgrunnlagGrunnlagEntitet.erAktivt(),
                beregningsgrunnlagGrunnlagEntitet.getBeregningsgrunnlagTilstand());
    }

    private static FaktaAggregatDto mapFaktaAggregat(FaktaAggregatEntitet faktaAggregatEntitet) {
        return new FaktaAggregatDto(mapFaktaArbeidsforholdListe(faktaAggregatEntitet.getFaktaArbeidsforhold()),
                faktaAggregatEntitet.getFaktaAktør().map(MapDetaljertBeregningsgrunnlag::mapFaktaAktør).orElse(null));
    }

    private static List<FaktaArbeidsforholdDto> mapFaktaArbeidsforholdListe(List<FaktaArbeidsforholdEntitet> faktaArbeidsforhold) {
        return faktaArbeidsforhold.stream().map(MapDetaljertBeregningsgrunnlag::mapFaktaArbeidsforhold).collect(Collectors.toList());
    }

    private static FaktaArbeidsforholdDto mapFaktaArbeidsforhold(FaktaArbeidsforholdEntitet faktaArbeidsforholdEntitet) {
        return new FaktaArbeidsforholdDto(
                mapArbeidsgiver(faktaArbeidsforholdEntitet.getArbeidsgiver()),
                faktaArbeidsforholdEntitet.getArbeidsforholdRef() == null || faktaArbeidsforholdEntitet.getArbeidsforholdRef().getReferanse() == null ? null
                        : new InternArbeidsforholdRefDto(faktaArbeidsforholdEntitet.getArbeidsforholdRef().getReferanse()),
                faktaArbeidsforholdEntitet.getErTidsbegrensetVurdering(),
                faktaArbeidsforholdEntitet.getHarMottattYtelseVurdering(),
                faktaArbeidsforholdEntitet.getHarLønnsendringIBeregningsperiodenVurdering()
        );
    }

    private static FaktaAktørDto mapFaktaAktør(FaktaAktørEntitet faktaAktørEntitet) {
        return new FaktaAktørDto(faktaAktørEntitet.getErNyIArbeidslivetSNVurdering(),
                faktaAktørEntitet.getErNyoppstartetFLVurdering(),
                faktaAktørEntitet.getHarFLMottattYtelseVurdering(),
                faktaAktørEntitet.getSkalBeregnesSomMilitærVurdering(),
                faktaAktørEntitet.getSkalBesteberegnesVurdering(),
                faktaAktørEntitet.getMottarEtterlønnSluttpakkeVurdering());
    }

    private static BeregningRefusjonOverstyringerDto mapRefusjonOverstyringAggregat(BeregningRefusjonOverstyringerEntitet beregningRefusjonOverstyringerEntitet) {
        return new BeregningRefusjonOverstyringerDto(mapRefusjonOverstyringer(beregningRefusjonOverstyringerEntitet.getRefusjonOverstyringer()));
    }

    private static List<BeregningRefusjonOverstyringDto> mapRefusjonOverstyringer(List<BeregningRefusjonOverstyringEntitet> refusjonOverstyringer) {
        return refusjonOverstyringer.stream().map(MapDetaljertBeregningsgrunnlag::mapRefusjonOverstyring).collect(Collectors.toList());
    }

    private static BeregningRefusjonOverstyringDto mapRefusjonOverstyring(BeregningRefusjonOverstyringEntitet beregningRefusjonOverstyringEntitet) {
        return new BeregningRefusjonOverstyringDto(
                mapArbeidsgiver(beregningRefusjonOverstyringEntitet.getArbeidsgiver()),
                beregningRefusjonOverstyringEntitet.getFørsteMuligeRefusjonFom().orElse(null),
                beregningRefusjonOverstyringEntitet.getErFristUtvidet());
    }

    private static BeregningAktivitetOverstyringerDto mapOverstyrteAktiviteterAggregat(BeregningAktivitetOverstyringerEntitet beregningAktivitetOverstyringerEntitet) {
        return new BeregningAktivitetOverstyringerDto(mapOverstyringer(beregningAktivitetOverstyringerEntitet.getOverstyringer()));
    }

    private static List<BeregningAktivitetOverstyringDto> mapOverstyringer(List<BeregningAktivitetOverstyringEntitet> overstyringer) {
        return overstyringer.stream().map(MapDetaljertBeregningsgrunnlag::mapOverstyrtAktivitet).collect(Collectors.toList());
    }

    private static BeregningAktivitetOverstyringDto mapOverstyrtAktivitet(BeregningAktivitetOverstyringEntitet beregningAktivitetOverstyringEntitet) {
        return new BeregningAktivitetOverstyringDto(
                new Periode(beregningAktivitetOverstyringEntitet.getPeriode().getFomDato(), beregningAktivitetOverstyringEntitet.getPeriode().getTomDato()),
                beregningAktivitetOverstyringEntitet.getArbeidsgiver().map(MapDetaljertBeregningsgrunnlag::mapArbeidsgiver).orElse(null),
                beregningAktivitetOverstyringEntitet.getArbeidsforholdRef() == null
                        || beregningAktivitetOverstyringEntitet.getArbeidsforholdRef().getReferanse() == null ? null
                        : new InternArbeidsforholdRefDto(beregningAktivitetOverstyringEntitet.getArbeidsforholdRef().getReferanse()),
                        beregningAktivitetOverstyringEntitet.getOpptjeningAktivitetType(),
                        beregningAktivitetOverstyringEntitet.getHandling());
    }

    private static BeregningAktivitetAggregatDto mapBeregningAktivitetAggregat(BeregningAktivitetAggregatEntitet aktivitetAggregatEntitet) {
        return new BeregningAktivitetAggregatDto(
                mapBeregningAktiviteter(aktivitetAggregatEntitet.getBeregningAktiviteter()),
                aktivitetAggregatEntitet.getSkjæringstidspunktOpptjening());
    }

    private static List<BeregningAktivitetDto> mapBeregningAktiviteter(List<BeregningAktivitetEntitet> beregningAktiviteter) {
        return beregningAktiviteter.stream().map(MapDetaljertBeregningsgrunnlag::mapBeregningAktivitet).collect(Collectors.toList());
    }

    private static BeregningAktivitetDto mapBeregningAktivitet(BeregningAktivitetEntitet beregningAktivitetEntitet) {
        return new BeregningAktivitetDto(
                new Periode(beregningAktivitetEntitet.getPeriode().getFomDato(), beregningAktivitetEntitet.getPeriode().getTomDato()),
                beregningAktivitetEntitet.getArbeidsgiver() == null ? null : mapArbeidsgiver(beregningAktivitetEntitet.getArbeidsgiver()),
                beregningAktivitetEntitet.getArbeidsforholdRef() == null || beregningAktivitetEntitet.getArbeidsforholdRef().getReferanse() == null ? null
                        : new InternArbeidsforholdRefDto(beregningAktivitetEntitet.getArbeidsforholdRef().getReferanse()),
                        beregningAktivitetEntitet.getOpptjeningAktivitetType());
    }

    public static BeregningsgrunnlagDto map(BeregningsgrunnlagEntitet beregningsgrunnlagEntitet) {
        return new BeregningsgrunnlagDto(
                beregningsgrunnlagEntitet.getSkjæringstidspunkt(),
                mapAktivitetstatuser(beregningsgrunnlagEntitet),
                mapBeregningsgrunnlagPerioder(beregningsgrunnlagEntitet),
                mapSammenligningsgrunnlag(beregningsgrunnlagEntitet),
                mapSammenligningsgrunnlagPrStatusListe(beregningsgrunnlagEntitet),
                beregningsgrunnlagEntitet.getFaktaOmBeregningTilfeller(),
                beregningsgrunnlagEntitet.isOverstyrt(),
                beregningsgrunnlagEntitet.getGrunnbeløp() == null ? null : beregningsgrunnlagEntitet.getGrunnbeløp().getVerdi());
    }

    private static List<SammenligningsgrunnlagPrStatusDto> mapSammenligningsgrunnlagPrStatusListe(BeregningsgrunnlagEntitet beregningsgrunnlagEntitet) {
        if (beregningsgrunnlagEntitet.getSammenligningsgrunnlagPrStatusListe() == null) {
            return null;
        }
        return beregningsgrunnlagEntitet.getSammenligningsgrunnlagPrStatusListe().stream().map(MapDetaljertBeregningsgrunnlag::mapSammeligningsgrunnlagPrStatus)
                .collect(Collectors.toList());
    }

    private static SammenligningsgrunnlagPrStatusDto mapSammeligningsgrunnlagPrStatus(SammenligningsgrunnlagPrStatus sammenligningsgrunnlagPrStatus) {
        return new SammenligningsgrunnlagPrStatusDto(
                new Periode(sammenligningsgrunnlagPrStatus.getSammenligningsperiodeFom(), sammenligningsgrunnlagPrStatus.getSammenligningsperiodeTom()),
                sammenligningsgrunnlagPrStatus.getSammenligningsgrunnlagType(),
                mapFraBeløp(sammenligningsgrunnlagPrStatus.getRapportertPrÅr()),
                sammenligningsgrunnlagPrStatus.getAvvikPromilleNy().getVerdi());
    }

    private static Sammenligningsgrunnlag mapSammenligningsgrunnlag(BeregningsgrunnlagEntitet beregningsgrunnlagEntitet) {
        return beregningsgrunnlagEntitet.getSammenligningsgrunnlag().map(sg ->
                new Sammenligningsgrunnlag(
                        new Periode(sg.getSammenligningsperiodeFom(), sg.getSammenligningsperiodeTom()),
                        mapFraBeløp(sg.getRapportertPrÅr()),
                        sg.getAvvikPromilleNy().getVerdi())).orElse(null);
    }

    private static List<BeregningsgrunnlagPeriodeDto> mapBeregningsgrunnlagPerioder(BeregningsgrunnlagEntitet beregningsgrunnlagEntitet) {
        return beregningsgrunnlagEntitet.getBeregningsgrunnlagPerioder().stream().map(MapDetaljertBeregningsgrunnlag::mapPeriode).collect(Collectors.toList());
    }

    private static BeregningsgrunnlagPeriodeDto mapPeriode(BeregningsgrunnlagPeriode beregningsgrunnlagPeriode) {
        return new BeregningsgrunnlagPeriodeDto(
                mapAndeler(beregningsgrunnlagPeriode.getBeregningsgrunnlagPrStatusOgAndelList()),
                new Periode(beregningsgrunnlagPeriode.getBeregningsgrunnlagPeriodeFom(), beregningsgrunnlagPeriode.getBeregningsgrunnlagPeriodeTom()),
                mapFraBeløp(beregningsgrunnlagPeriode.getBruttoPrÅr()),
                mapFraBeløp(beregningsgrunnlagPeriode.getAvkortetPrÅr()),
                mapFraBeløp(beregningsgrunnlagPeriode.getRedusertPrÅr()),
                beregningsgrunnlagPeriode.getDagsats(),
                beregningsgrunnlagPeriode.getPeriodeÅrsaker());
    }

    private static List<BeregningsgrunnlagPrStatusOgAndelDto> mapAndeler(List<BeregningsgrunnlagPrStatusOgAndel> beregningsgrunnlagPrStatusOgAndelList) {
        return beregningsgrunnlagPrStatusOgAndelList.stream().map(MapDetaljertBeregningsgrunnlag::mapAndel).collect(Collectors.toList());
    }

    private static BeregningsgrunnlagPrStatusOgAndelDto mapAndel(BeregningsgrunnlagPrStatusOgAndel beregningsgrunnlagPrStatusOgAndel) {
        return new BeregningsgrunnlagPrStatusOgAndelDto(
                beregningsgrunnlagPrStatusOgAndel.getAndelsnr(),
                AktivitetStatus.fraKode(beregningsgrunnlagPrStatusOgAndel.getAktivitetStatus().getKode()),
                mapBeregningsperiode(beregningsgrunnlagPrStatusOgAndel),
                beregningsgrunnlagPrStatusOgAndel.getArbeidsforholdType(),
                mapFraBeløp(beregningsgrunnlagPrStatusOgAndel.getBruttoPrÅr()),
                mapFraBeløp(beregningsgrunnlagPrStatusOgAndel.getRedusertRefusjonPrÅr()),
                mapFraBeløp(beregningsgrunnlagPrStatusOgAndel.getRedusertBrukersAndelPrÅr()),
                beregningsgrunnlagPrStatusOgAndel.getDagsatsBruker(),
                beregningsgrunnlagPrStatusOgAndel.getDagsatsArbeidsgiver(),
                Inntektskategori.fraKode(beregningsgrunnlagPrStatusOgAndel.getInntektskategori().getKode()),
                mapBgAndelArbeidsforhold(beregningsgrunnlagPrStatusOgAndel),
                mapFraBeløp(beregningsgrunnlagPrStatusOgAndel.getOverstyrtPrÅr()),
                mapFraBeløp(beregningsgrunnlagPrStatusOgAndel.getAvkortetPrÅr()),
                mapFraBeløp(beregningsgrunnlagPrStatusOgAndel.getRedusertPrÅr()),
                mapFraBeløp(beregningsgrunnlagPrStatusOgAndel.getBeregnetPrÅr()),
                mapFraBeløp(beregningsgrunnlagPrStatusOgAndel.getBesteberegningPrÅr()),
                mapFraBeløp(beregningsgrunnlagPrStatusOgAndel.getFordeltPrÅr()),
                mapFraBeløp(beregningsgrunnlagPrStatusOgAndel.getMaksimalRefusjonPrÅr()),
                mapFraBeløp(beregningsgrunnlagPrStatusOgAndel.getAvkortetRefusjonPrÅr()),
                mapFraBeløp(beregningsgrunnlagPrStatusOgAndel.getAvkortetBrukersAndelPrÅr()),
                mapFraBeløp(beregningsgrunnlagPrStatusOgAndel.getPgiSnitt()),
                mapFraBeløp(beregningsgrunnlagPrStatusOgAndel.getPgi1()),
                mapFraBeløp(beregningsgrunnlagPrStatusOgAndel.getPgi2()),
                mapFraBeløp(beregningsgrunnlagPrStatusOgAndel.getPgi3()),
                mapFraBeløp(beregningsgrunnlagPrStatusOgAndel.getBruttoPrÅr()),
                beregningsgrunnlagPrStatusOgAndel.getFastsattAvSaksbehandler(),
                beregningsgrunnlagPrStatusOgAndel.getKilde().equals(AndelKilde.SAKSBEHANDLER_KOFAKBER) || beregningsgrunnlagPrStatusOgAndel.getKilde().equals(AndelKilde.SAKSBEHANDLER_FORDELING),
                beregningsgrunnlagPrStatusOgAndel.getOrginalDagsatsFraTilstøtendeYtelse());
    }

    private static Periode mapBeregningsperiode(BeregningsgrunnlagPrStatusOgAndel beregningsgrunnlagPrStatusOgAndel) {
        return beregningsgrunnlagPrStatusOgAndel.getBeregningsperiodeFom() == null ? null
                : new Periode(beregningsgrunnlagPrStatusOgAndel.getBeregningsperiodeFom(), beregningsgrunnlagPrStatusOgAndel.getBeregningsperiodeTom());
    }

    private static BGAndelArbeidsforhold mapBgAndelArbeidsforhold(BeregningsgrunnlagPrStatusOgAndel beregningsgrunnlagPrStatusOgAndel) {
        return beregningsgrunnlagPrStatusOgAndel.getBgAndelArbeidsforhold().map(MapDetaljertBeregningsgrunnlag::mapArbeidsforhold).orElse(null);
    }

    private static BGAndelArbeidsforhold mapArbeidsforhold(no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BGAndelArbeidsforhold bgAndelArbeidsforhold) {
        return new BGAndelArbeidsforhold(
                mapArbeidsgiver(bgAndelArbeidsforhold.getArbeidsgiver()),
                bgAndelArbeidsforhold.getArbeidsforholdRef().getReferanse(),
                mapFraBeløp(bgAndelArbeidsforhold.getGjeldendeRefusjonPrÅr()),
                bgAndelArbeidsforhold.getNaturalytelseBortfaltPrÅr().map(Beløp::getVerdi).orElse(null),
                bgAndelArbeidsforhold.getNaturalytelseTilkommetPrÅr().map(Beløp::getVerdi).orElse(null),
                bgAndelArbeidsforhold.getArbeidsperiodeFom(),
                bgAndelArbeidsforhold.getArbeidsperiodeTom().orElse(null));
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
