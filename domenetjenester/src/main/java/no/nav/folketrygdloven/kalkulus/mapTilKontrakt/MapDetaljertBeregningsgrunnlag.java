package no.nav.folketrygdloven.kalkulus.mapTilKontrakt;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BGAndelArbeidsforholdEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningAktivitetAggregatEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningAktivitetEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningAktivitetOverstyringEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningAktivitetOverstyringerEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningRefusjonOverstyringEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningRefusjonOverstyringerEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagAktivitetStatusEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagGrunnlagEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagPeriodeEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.FaktaAggregatEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.FaktaAktørEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.FaktaArbeidsforholdEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.SammenligningsgrunnlagPrStatusEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.Promille;
import no.nav.folketrygdloven.kalkulus.felles.v1.Beløp;
import no.nav.folketrygdloven.kalkulus.felles.v1.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulus.felles.v1.Periode;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.AndelKilde;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand;
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
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.detaljert.SammenligningsgrunnlagPrStatusDto;

public class MapDetaljertBeregningsgrunnlag {

    public static BeregningsgrunnlagGrunnlagDto map(BeregningsgrunnlagGrunnlagEntitet gr) {
        BeregningsgrunnlagDto beregningsgrunnlag = gr.getBeregningsgrunnlag().map(MapDetaljertBeregningsgrunnlag::mapGrunnlag).orElse(null);
        FaktaAggregatDto faktaAggregat = gr.getFaktaAggregat().map(MapDetaljertBeregningsgrunnlag::mapFaktaAggregat).orElse(null);
        BeregningAktivitetAggregatDto registerAktiviteter = mapBeregningAktivitetAggregat(gr.getRegisterAktiviteter());
        BeregningAktivitetAggregatDto saksbehandletAktiviteter = gr.getSaksbehandletAktiviteter()
            .map(MapDetaljertBeregningsgrunnlag::mapBeregningAktivitetAggregat)
            .orElse(null);
        BeregningAktivitetOverstyringerDto overstyringer = gr.getOverstyring()
            .map(MapDetaljertBeregningsgrunnlag::mapOverstyrteAktiviteterAggregat)
            .orElse(null);
        BeregningRefusjonOverstyringerDto refusjonOverstyringer = gr.getRefusjonOverstyringer()
            .map(MapDetaljertBeregningsgrunnlag::mapRefusjonOverstyringAggregat)
            .orElse(null);
        BeregningsgrunnlagTilstand beregningsgrunnlagTilstand = gr.getBeregningsgrunnlagTilstand();
        return new BeregningsgrunnlagGrunnlagDto(beregningsgrunnlag, faktaAggregat, registerAktiviteter, saksbehandletAktiviteter, overstyringer,
            refusjonOverstyringer, beregningsgrunnlagTilstand);
    }

    private static FaktaAggregatDto mapFaktaAggregat(FaktaAggregatEntitet faktaAggregatEntitet) {
        return new FaktaAggregatDto(mapFaktaArbeidsforholdListe(faktaAggregatEntitet.getFaktaArbeidsforhold()),
            faktaAggregatEntitet.getFaktaAktør().map(MapDetaljertBeregningsgrunnlag::mapFaktaAktør).orElse(null));
    }

    private static List<FaktaArbeidsforholdDto> mapFaktaArbeidsforholdListe(List<FaktaArbeidsforholdEntitet> faktaArbeidsforhold) {
        return faktaArbeidsforhold.stream().map(MapDetaljertBeregningsgrunnlag::mapFaktaArbeidsforhold).collect(Collectors.toList());
    }

    private static FaktaArbeidsforholdDto mapFaktaArbeidsforhold(FaktaArbeidsforholdEntitet faktaArbeidsforholdEntitet) {
        return new FaktaArbeidsforholdDto(mapArbeidsgiver(faktaArbeidsforholdEntitet.getArbeidsgiver()),
            faktaArbeidsforholdEntitet.getArbeidsforholdRef() == null
                || faktaArbeidsforholdEntitet.getArbeidsforholdRef().getReferanse() == null ? null : new InternArbeidsforholdRefDto(
                faktaArbeidsforholdEntitet.getArbeidsforholdRef().getReferanse()), faktaArbeidsforholdEntitet.getErTidsbegrensetVurdering(),
            faktaArbeidsforholdEntitet.getHarMottattYtelseVurdering(), faktaArbeidsforholdEntitet.getHarLønnsendringIBeregningsperiodenVurdering());
    }

    private static FaktaAktørDto mapFaktaAktør(FaktaAktørEntitet faktaAktørEntitet) {
        return new FaktaAktørDto(faktaAktørEntitet.getErNyIArbeidslivetSNVurdering(), faktaAktørEntitet.getErNyoppstartetFLVurdering(),
            faktaAktørEntitet.getHarFLMottattYtelseVurdering(), faktaAktørEntitet.getSkalBeregnesSomMilitærVurdering(),
            faktaAktørEntitet.getSkalBesteberegnesVurdering(), faktaAktørEntitet.getMottarEtterlønnSluttpakkeVurdering());
    }

    private static BeregningRefusjonOverstyringerDto mapRefusjonOverstyringAggregat(BeregningRefusjonOverstyringerEntitet beregningRefusjonOverstyringerEntitet) {
        return new BeregningRefusjonOverstyringerDto(mapRefusjonOverstyringer(beregningRefusjonOverstyringerEntitet.getRefusjonOverstyringer()));
    }

    private static List<BeregningRefusjonOverstyringDto> mapRefusjonOverstyringer(List<BeregningRefusjonOverstyringEntitet> refusjonOverstyringer) {
        return refusjonOverstyringer.stream().map(MapDetaljertBeregningsgrunnlag::mapRefusjonOverstyring).collect(Collectors.toList());
    }

    private static BeregningRefusjonOverstyringDto mapRefusjonOverstyring(BeregningRefusjonOverstyringEntitet beregningRefusjonOverstyringEntitet) {
        return new BeregningRefusjonOverstyringDto(mapArbeidsgiver(beregningRefusjonOverstyringEntitet.getArbeidsgiver()),
            beregningRefusjonOverstyringEntitet.getFørsteMuligeRefusjonFom().orElse(null), beregningRefusjonOverstyringEntitet.getErFristUtvidet());
    }

    private static BeregningAktivitetOverstyringerDto mapOverstyrteAktiviteterAggregat(BeregningAktivitetOverstyringerEntitet beregningAktivitetOverstyringerEntitet) {
        return new BeregningAktivitetOverstyringerDto(mapOverstyringer(beregningAktivitetOverstyringerEntitet.getOverstyringer()));
    }

    private static List<BeregningAktivitetOverstyringDto> mapOverstyringer(List<BeregningAktivitetOverstyringEntitet> overstyringer) {
        return overstyringer.stream().map(MapDetaljertBeregningsgrunnlag::mapOverstyrtAktivitet).collect(Collectors.toList());
    }

    private static BeregningAktivitetOverstyringDto mapOverstyrtAktivitet(BeregningAktivitetOverstyringEntitet beregningAktivitetOverstyringEntitet) {
        return new BeregningAktivitetOverstyringDto(new Periode(beregningAktivitetOverstyringEntitet.getPeriode().getFomDato(),
            beregningAktivitetOverstyringEntitet.getPeriode().getTomDato()),
            beregningAktivitetOverstyringEntitet.getArbeidsgiver().map(MapDetaljertBeregningsgrunnlag::mapArbeidsgiver).orElse(null),
            beregningAktivitetOverstyringEntitet.getArbeidsforholdRef() == null
                || beregningAktivitetOverstyringEntitet.getArbeidsforholdRef().getReferanse() == null ? null : new InternArbeidsforholdRefDto(
                beregningAktivitetOverstyringEntitet.getArbeidsforholdRef().getReferanse()),
            beregningAktivitetOverstyringEntitet.getOpptjeningAktivitetType(), beregningAktivitetOverstyringEntitet.getHandling());
    }

    private static BeregningAktivitetAggregatDto mapBeregningAktivitetAggregat(BeregningAktivitetAggregatEntitet aktivitetAggregatEntitet) {
        return new BeregningAktivitetAggregatDto(mapBeregningAktiviteter(aktivitetAggregatEntitet.getBeregningAktiviteter()),
            aktivitetAggregatEntitet.getSkjæringstidspunktOpptjening());
    }

    private static List<BeregningAktivitetDto> mapBeregningAktiviteter(List<BeregningAktivitetEntitet> beregningAktiviteter) {
        return beregningAktiviteter.stream().map(MapDetaljertBeregningsgrunnlag::mapBeregningAktivitet).collect(Collectors.toList());
    }

    private static BeregningAktivitetDto mapBeregningAktivitet(BeregningAktivitetEntitet beregningAktivitetEntitet) {
        return new BeregningAktivitetDto(
            new Periode(beregningAktivitetEntitet.getPeriode().getFomDato(), beregningAktivitetEntitet.getPeriode().getTomDato()),
            beregningAktivitetEntitet.getArbeidsgiver() == null ? null : mapArbeidsgiver(beregningAktivitetEntitet.getArbeidsgiver()),
            beregningAktivitetEntitet.getArbeidsforholdRef() == null
                || beregningAktivitetEntitet.getArbeidsforholdRef().getReferanse() == null ? null : new InternArbeidsforholdRefDto(
                beregningAktivitetEntitet.getArbeidsforholdRef().getReferanse()), beregningAktivitetEntitet.getOpptjeningAktivitetType());
    }

    private static BeregningsgrunnlagDto mapGrunnlag(BeregningsgrunnlagEntitet beregningsgrunnlagEntitet) {
        return new BeregningsgrunnlagDto(beregningsgrunnlagEntitet.getSkjæringstidspunkt(), mapAktivitetstatuser(beregningsgrunnlagEntitet),
            mapBeregningsgrunnlagPerioder(beregningsgrunnlagEntitet), mapSammenligningsgrunnlagPrStatusListe(beregningsgrunnlagEntitet),
            beregningsgrunnlagEntitet.getFaktaOmBeregningTilfeller(), beregningsgrunnlagEntitet.isOverstyrt(),
            beregningsgrunnlagEntitet.getGrunnbeløp() == null ? null : mapBeløp(beregningsgrunnlagEntitet.getGrunnbeløp()));
    }

    private static List<SammenligningsgrunnlagPrStatusDto> mapSammenligningsgrunnlagPrStatusListe(BeregningsgrunnlagEntitet beregningsgrunnlagEntitet) {
        return beregningsgrunnlagEntitet.getSammenligningsgrunnlagPrStatusListe()
            .stream()
            .map(MapDetaljertBeregningsgrunnlag::mapSammeligningsgrunnlagPrStatus)
            .collect(Collectors.toList());
    }

    private static SammenligningsgrunnlagPrStatusDto mapSammeligningsgrunnlagPrStatus(SammenligningsgrunnlagPrStatusEntitet sammenligningsgrunnlagPrStatus) {
        return new SammenligningsgrunnlagPrStatusDto(
            new Periode(sammenligningsgrunnlagPrStatus.getSammenligningsperiodeFom(), sammenligningsgrunnlagPrStatus.getSammenligningsperiodeTom()),
            sammenligningsgrunnlagPrStatus.getSammenligningsgrunnlagType(), mapBeløp(sammenligningsgrunnlagPrStatus.getRapportertPrÅr()),
            mapPromille(sammenligningsgrunnlagPrStatus.getGjeldendeAvvik()));
    }

    private static BigDecimal mapPromille(Promille promille) {
        return promille == null ? null : promille.getVerdi();
    }


    private static List<BeregningsgrunnlagPeriodeDto> mapBeregningsgrunnlagPerioder(BeregningsgrunnlagEntitet beregningsgrunnlagEntitet) {
        return beregningsgrunnlagEntitet.getBeregningsgrunnlagPerioder()
            .stream()
            .map(MapDetaljertBeregningsgrunnlag::mapPeriode)
            .collect(Collectors.toList());
    }

    private static BeregningsgrunnlagPeriodeDto mapPeriode(BeregningsgrunnlagPeriodeEntitet beregningsgrunnlagPeriode) {
        return new BeregningsgrunnlagPeriodeDto(mapAndeler(beregningsgrunnlagPeriode.getBeregningsgrunnlagPrStatusOgAndelList()),
            new Periode(beregningsgrunnlagPeriode.getBeregningsgrunnlagPeriodeFom(), beregningsgrunnlagPeriode.getBeregningsgrunnlagPeriodeTom()),
            mapBeløp(beregningsgrunnlagPeriode.getBruttoPrÅr()), mapBeløp(beregningsgrunnlagPeriode.getAvkortetPrÅr()),
            mapBeløp(beregningsgrunnlagPeriode.getRedusertPrÅr()), beregningsgrunnlagPeriode.getDagsats(),
            beregningsgrunnlagPeriode.getPeriodeÅrsaker(), beregningsgrunnlagPeriode.getTotalUtbetalingsgradFraUttak(),
            beregningsgrunnlagPeriode.getTotalUtbetalingsgradEtterReduksjonVedTilkommetInntekt(),
            beregningsgrunnlagPeriode.getReduksjonsfaktorInaktivTypeA());
    }

    private static List<BeregningsgrunnlagPrStatusOgAndelDto> mapAndeler(List<BeregningsgrunnlagPrStatusOgAndelEntitet> beregningsgrunnlagPrStatusOgAndelList) {
        return beregningsgrunnlagPrStatusOgAndelList.stream().map(MapDetaljertBeregningsgrunnlag::mapAndel).collect(Collectors.toList());
    }

    private static BeregningsgrunnlagPrStatusOgAndelDto mapAndel(BeregningsgrunnlagPrStatusOgAndelEntitet beregningsgrunnlagPrStatusOgAndel) {
        return new BeregningsgrunnlagPrStatusOgAndelDto.Builder().medAndelsnr(beregningsgrunnlagPrStatusOgAndel.getAndelsnr())
            .medAktivitetStatus(beregningsgrunnlagPrStatusOgAndel.getAktivitetStatus())
            .medBeregningsperiode(mapBeregningsperiode(beregningsgrunnlagPrStatusOgAndel))
            .medArbeidsforholdType(beregningsgrunnlagPrStatusOgAndel.getArbeidsforholdType())
            .medBruttoPrÅr(mapBeløp(beregningsgrunnlagPrStatusOgAndel.getBruttoPrÅr()))
            .medRedusertRefusjonPrÅr(mapBeløp(beregningsgrunnlagPrStatusOgAndel.getRedusertRefusjonPrÅr()))
            .medRedusertBrukersAndelPrÅr(mapBeløp(beregningsgrunnlagPrStatusOgAndel.getRedusertBrukersAndelPrÅr()))
            .medDagsatsBruker(beregningsgrunnlagPrStatusOgAndel.getDagsatsBruker())
            .medDagsatsArbeidsgiver(beregningsgrunnlagPrStatusOgAndel.getDagsatsArbeidsgiver())
            .medInntektskategori(beregningsgrunnlagPrStatusOgAndel.getFastsattInntektskategori().getGjeldendeInntektskategori())
            .medBgAndelArbeidsforhold(mapBgAndelArbeidsforhold(beregningsgrunnlagPrStatusOgAndel))
            .medOverstyrtPrÅr(mapBeløp(beregningsgrunnlagPrStatusOgAndel.getOverstyrtPrÅr()))
            .medAvkortetPrÅr(mapBeløp(beregningsgrunnlagPrStatusOgAndel.getAvkortetPrÅr()))
            .medRedusertPrÅr(mapBeløp(beregningsgrunnlagPrStatusOgAndel.getRedusertPrÅr()))
            .medBeregnetPrÅr(mapBeløp(beregningsgrunnlagPrStatusOgAndel.getBeregnetPrÅr()))
            .medBesteberegningPrÅr(mapBeløp(beregningsgrunnlagPrStatusOgAndel.getBesteberegningPrÅr()))
            .medFordeltPrÅr(mapBeløp(beregningsgrunnlagPrStatusOgAndel.getFordeltPrÅr()))
            .medManueltFordeltPrÅr(mapBeløp(beregningsgrunnlagPrStatusOgAndel.getManueltFordeltPrÅr()))
            .medMaksimalRefusjonPrÅr(mapBeløp(beregningsgrunnlagPrStatusOgAndel.getMaksimalRefusjonPrÅr()))
            .medAvkortetRefusjonPrÅr(mapBeløp(beregningsgrunnlagPrStatusOgAndel.getAvkortetRefusjonPrÅr()))
            .medAvkortetBrukersAndelPrÅr(mapBeløp(beregningsgrunnlagPrStatusOgAndel.getAvkortetBrukersAndelPrÅr()))
            .medPgiSnitt(mapBeløp(beregningsgrunnlagPrStatusOgAndel.getPgiSnitt()))
            .medPgi1(mapBeløp(beregningsgrunnlagPrStatusOgAndel.getPgi1()))
            .medPgi2(mapBeløp(beregningsgrunnlagPrStatusOgAndel.getPgi2()))
            .medPgi3(mapBeløp(beregningsgrunnlagPrStatusOgAndel.getPgi3()))
            .medAndelKilde(beregningsgrunnlagPrStatusOgAndel.getKilde())
            .medÅrsbeløpFraTilstøtendeYtelse(mapBeløp(beregningsgrunnlagPrStatusOgAndel.getBruttoPrÅr()))
            .medFastsattAvSaksbehandler(beregningsgrunnlagPrStatusOgAndel.getFastsattAvSaksbehandler())
            .medLagtTilAvSaksbehandler(
                beregningsgrunnlagPrStatusOgAndel.getKilde().equals(AndelKilde.SAKSBEHANDLER_KOFAKBER) || beregningsgrunnlagPrStatusOgAndel.getKilde()
                    .equals(AndelKilde.SAKSBEHANDLER_FORDELING))
            .medOrginalDagsatsFraTilstøtendeYtelse(beregningsgrunnlagPrStatusOgAndel.getOrginalDagsatsFraTilstøtendeYtelse())
            .build();
    }

    private static Periode mapBeregningsperiode(BeregningsgrunnlagPrStatusOgAndelEntitet beregningsgrunnlagPrStatusOgAndel) {
        return beregningsgrunnlagPrStatusOgAndel.getBeregningsperiodeFom() == null ? null : new Periode(
            beregningsgrunnlagPrStatusOgAndel.getBeregningsperiodeFom(), beregningsgrunnlagPrStatusOgAndel.getBeregningsperiodeTom());
    }

    private static BGAndelArbeidsforhold mapBgAndelArbeidsforhold(BeregningsgrunnlagPrStatusOgAndelEntitet beregningsgrunnlagPrStatusOgAndel) {
        return beregningsgrunnlagPrStatusOgAndel.getBgAndelArbeidsforhold().map(MapDetaljertBeregningsgrunnlag::mapArbeidsforhold).orElse(null);
    }

    private static BGAndelArbeidsforhold mapArbeidsforhold(BGAndelArbeidsforholdEntitet bgAndelArbeidsforhold) {
        return new BGAndelArbeidsforhold(mapArbeidsgiver(bgAndelArbeidsforhold.getArbeidsgiver()),
            bgAndelArbeidsforhold.getArbeidsforholdRef().getUUIDReferanse(), mapBeløp(bgAndelArbeidsforhold.getGjeldendeRefusjonPrÅr()),
            bgAndelArbeidsforhold.getNaturalytelseBortfaltPrÅr().map(MapDetaljertBeregningsgrunnlag::mapBeløp).orElse(null),
            bgAndelArbeidsforhold.getNaturalytelseTilkommetPrÅr().map(MapDetaljertBeregningsgrunnlag::mapBeløp).orElse(null),
            bgAndelArbeidsforhold.getArbeidsperiodeFom(), bgAndelArbeidsforhold.getArbeidsperiodeTom().orElse(null));
    }

    private static List<AktivitetStatus> mapAktivitetstatuser(BeregningsgrunnlagEntitet beregningsgrunnlagEntitet) {
        return beregningsgrunnlagEntitet.getAktivitetStatuser()
            .stream()
            .map(BeregningsgrunnlagAktivitetStatusEntitet::getAktivitetStatus)
            .collect(Collectors.toList());
    }

    private static Arbeidsgiver mapArbeidsgiver(no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.Arbeidsgiver a) {
        return new Arbeidsgiver(a.getOrgnr(), a.getAktørId() != null ? a.getAktørId().getId() : null);
    }

    private static Beløp mapBeløp(no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.Beløp beløp) {
        return beløp == null ? null : Beløp.fra(beløp.getVerdi());
    }
}
