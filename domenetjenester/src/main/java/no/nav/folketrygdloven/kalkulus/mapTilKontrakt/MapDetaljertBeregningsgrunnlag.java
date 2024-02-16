package no.nav.folketrygdloven.kalkulus.mapTilKontrakt;

import java.util.List;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagGUIInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagAktivitetStatusDto;
import no.nav.folketrygdloven.kalkulus.felles.v1.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulus.felles.v1.Periode;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.AndelKilde;
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
import no.nav.folketrygdloven.kalkulus.typer.Beløp;

public class MapDetaljertBeregningsgrunnlag {

    public static BeregningsgrunnlagGrunnlagDto mapMedBrevfelt(no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto grunnlag, BeregningsgrunnlagGUIInput input) {
        BeregningsgrunnlagGrunnlagDto dto = mapGrunnlag(grunnlag);
        BeregningsgrunnlagGrunnlagDto beregningsgrunnlagGrunnlagDto = MapFormidlingsdataBeregningsgrunnlag.mapMedBrevfelt(dto, input);
        return beregningsgrunnlagGrunnlagDto;
    }

    public static BeregningsgrunnlagGrunnlagDto mapGrunnlag(no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto beregningsgrunnlagGrunnlagEntitet) {
        BeregningsgrunnlagDto beregningsgrunnlag = beregningsgrunnlagGrunnlagEntitet.getBeregningsgrunnlag()
                .map(MapDetaljertBeregningsgrunnlag::map).orElse(null);

        return new BeregningsgrunnlagGrunnlagDto(
                beregningsgrunnlag,
                beregningsgrunnlagGrunnlagEntitet.getFaktaAggregat().map(MapDetaljertBeregningsgrunnlag::mapFaktaAggregat).orElse(null),
                mapBeregningAktivitetAggregat(beregningsgrunnlagGrunnlagEntitet.getRegisterAktiviteter()),
                beregningsgrunnlagGrunnlagEntitet.getSaksbehandletAktiviteter().map(MapDetaljertBeregningsgrunnlag::mapBeregningAktivitetAggregat).orElse(null),
                beregningsgrunnlagGrunnlagEntitet.getOverstyring().map(MapDetaljertBeregningsgrunnlag::mapOverstyrteAktiviteterAggregat).orElse(null),
                beregningsgrunnlagGrunnlagEntitet.getRefusjonOverstyringer().map(MapDetaljertBeregningsgrunnlag::mapRefusjonOverstyringAggregat).orElse(null),
                beregningsgrunnlagGrunnlagEntitet.getBeregningsgrunnlagTilstand());
    }

    private static FaktaAggregatDto mapFaktaAggregat(no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.FaktaAggregatDto faktaAggregatEntitet) {
        return new FaktaAggregatDto(mapFaktaArbeidsforholdListe(faktaAggregatEntitet.getFaktaArbeidsforhold()),
                faktaAggregatEntitet.getFaktaAktør().map(MapDetaljertBeregningsgrunnlag::mapFaktaAktør).orElse(null));
    }

    private static List<FaktaArbeidsforholdDto> mapFaktaArbeidsforholdListe(List<no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.FaktaArbeidsforholdDto> faktaArbeidsforhold) {
        return faktaArbeidsforhold.stream().map(MapDetaljertBeregningsgrunnlag::mapFaktaArbeidsforhold).collect(Collectors.toList());
    }

    private static FaktaArbeidsforholdDto mapFaktaArbeidsforhold(no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.FaktaArbeidsforholdDto faktaArbeidsforholdEntitet) {
        return new FaktaArbeidsforholdDto(
                mapArbeidsgiver(faktaArbeidsforholdEntitet.getArbeidsgiver()),
                faktaArbeidsforholdEntitet.getArbeidsforholdRef() == null || faktaArbeidsforholdEntitet.getArbeidsforholdRef().getReferanse() == null ? null
                        : new InternArbeidsforholdRefDto(faktaArbeidsforholdEntitet.getArbeidsforholdRef().getReferanse()),
                faktaArbeidsforholdEntitet.getErTidsbegrensetVurdering(),
                faktaArbeidsforholdEntitet.getHarMottattYtelseVurdering(),
                faktaArbeidsforholdEntitet.getHarLønnsendringIBeregningsperiodenVurdering()
        );
    }

    private static FaktaAktørDto mapFaktaAktør(no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.FaktaAktørDto faktaAktørEntitet) {
        return new FaktaAktørDto(faktaAktørEntitet.getErNyIArbeidslivetSNVurdering(),
                faktaAktørEntitet.getErNyoppstartetFLVurdering(),
                faktaAktørEntitet.getHarFLMottattYtelseVurdering(),
                faktaAktørEntitet.getSkalBeregnesSomMilitærVurdering(),
                faktaAktørEntitet.getSkalBesteberegnesVurdering(),
                faktaAktørEntitet.getMottarEtterlønnSluttpakkeVurdering());
    }

    private static BeregningRefusjonOverstyringerDto mapRefusjonOverstyringAggregat(no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningRefusjonOverstyringerDto beregningRefusjonOverstyringerEntitet) {
        return new BeregningRefusjonOverstyringerDto(mapRefusjonOverstyringer(beregningRefusjonOverstyringerEntitet.getRefusjonOverstyringer()));
    }

    private static List<BeregningRefusjonOverstyringDto> mapRefusjonOverstyringer(List<no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningRefusjonOverstyringDto> refusjonOverstyringer) {
        return refusjonOverstyringer.stream().map(MapDetaljertBeregningsgrunnlag::mapRefusjonOverstyring).collect(Collectors.toList());
    }

    private static BeregningRefusjonOverstyringDto mapRefusjonOverstyring(no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningRefusjonOverstyringDto beregningRefusjonOverstyringEntitet) {
        return new BeregningRefusjonOverstyringDto(
                mapArbeidsgiver(beregningRefusjonOverstyringEntitet.getArbeidsgiver()),
                beregningRefusjonOverstyringEntitet.getFørsteMuligeRefusjonFom().orElse(null),
                beregningRefusjonOverstyringEntitet.getErFristUtvidet().orElse(null));
    }

    private static BeregningAktivitetOverstyringerDto mapOverstyrteAktiviteterAggregat(no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetOverstyringerDto beregningAktivitetOverstyringerEntitet) {
        return new BeregningAktivitetOverstyringerDto(mapOverstyringer(beregningAktivitetOverstyringerEntitet.getOverstyringer()));
    }

    private static List<BeregningAktivitetOverstyringDto> mapOverstyringer(List<no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetOverstyringDto> overstyringer) {
        return overstyringer.stream().map(MapDetaljertBeregningsgrunnlag::mapOverstyrtAktivitet).collect(Collectors.toList());
    }

    private static BeregningAktivitetOverstyringDto mapOverstyrtAktivitet(no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetOverstyringDto beregningAktivitetOverstyringEntitet) {
        return new BeregningAktivitetOverstyringDto(
                new Periode(beregningAktivitetOverstyringEntitet.getPeriode().getFomDato(), beregningAktivitetOverstyringEntitet.getPeriode().getTomDato()),
                beregningAktivitetOverstyringEntitet.getArbeidsgiver().map(MapDetaljertBeregningsgrunnlag::mapArbeidsgiver).orElse(null),
                beregningAktivitetOverstyringEntitet.getArbeidsforholdRef() == null
                        || beregningAktivitetOverstyringEntitet.getArbeidsforholdRef().getReferanse() == null ? null
                        : new InternArbeidsforholdRefDto(beregningAktivitetOverstyringEntitet.getArbeidsforholdRef().getReferanse()),
                beregningAktivitetOverstyringEntitet.getOpptjeningAktivitetType(),
                beregningAktivitetOverstyringEntitet.getHandling());
    }

    private static BeregningAktivitetAggregatDto mapBeregningAktivitetAggregat(no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetAggregatDto aktivitetAggregatEntitet) {
        return new BeregningAktivitetAggregatDto(
                mapBeregningAktiviteter(aktivitetAggregatEntitet.getBeregningAktiviteter()),
                aktivitetAggregatEntitet.getSkjæringstidspunktOpptjening());
    }

    private static List<BeregningAktivitetDto> mapBeregningAktiviteter(List<no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetDto> beregningAktiviteter) {
        return beregningAktiviteter.stream().map(MapDetaljertBeregningsgrunnlag::mapBeregningAktivitet).collect(Collectors.toList());
    }

    private static BeregningAktivitetDto mapBeregningAktivitet(no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetDto beregningAktivitetEntitet) {
        return new BeregningAktivitetDto(
                new Periode(beregningAktivitetEntitet.getPeriode().getFomDato(), beregningAktivitetEntitet.getPeriode().getTomDato()),
                beregningAktivitetEntitet.getArbeidsgiver() == null ? null : mapArbeidsgiver(beregningAktivitetEntitet.getArbeidsgiver()),
                beregningAktivitetEntitet.getArbeidsforholdRef() == null || beregningAktivitetEntitet.getArbeidsforholdRef().getReferanse() == null ? null
                        : new InternArbeidsforholdRefDto(beregningAktivitetEntitet.getArbeidsforholdRef().getReferanse()),
                beregningAktivitetEntitet.getOpptjeningAktivitetType());
    }

    public static BeregningsgrunnlagDto map(no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto beregningsgrunnlagEntitet) {
        return new BeregningsgrunnlagDto(
                beregningsgrunnlagEntitet.getSkjæringstidspunkt(),
                mapAktivitetstatuser(beregningsgrunnlagEntitet),
                mapBeregningsgrunnlagPerioder(beregningsgrunnlagEntitet),
                mapSammenligningsgrunnlagPrStatusListe(beregningsgrunnlagEntitet),
                beregningsgrunnlagEntitet.getFaktaOmBeregningTilfeller(),
                beregningsgrunnlagEntitet.isOverstyrt(),
                beregningsgrunnlagEntitet.getGrunnbeløp() == null ? null : beregningsgrunnlagEntitet.getGrunnbeløp());
    }

    private static List<SammenligningsgrunnlagPrStatusDto> mapSammenligningsgrunnlagPrStatusListe(no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto beregningsgrunnlagEntitet) {
        return beregningsgrunnlagEntitet.getSammenligningsgrunnlagPrStatusListe().stream().map(MapDetaljertBeregningsgrunnlag::mapSammeligningsgrunnlagPrStatus)
                .collect(Collectors.toList());
    }

    private static SammenligningsgrunnlagPrStatusDto mapSammeligningsgrunnlagPrStatus(no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.SammenligningsgrunnlagPrStatusDto sammenligningsgrunnlagPrStatus) {
        return new SammenligningsgrunnlagPrStatusDto(
                new Periode(sammenligningsgrunnlagPrStatus.getSammenligningsperiodeFom(), sammenligningsgrunnlagPrStatus.getSammenligningsperiodeTom()),
                sammenligningsgrunnlagPrStatus.getSammenligningsgrunnlagType(),
                Beløp.fra(sammenligningsgrunnlagPrStatus.getRapportertPrÅr()),
                sammenligningsgrunnlagPrStatus.getAvvikPromilleNy());
    }


    private static List<BeregningsgrunnlagPeriodeDto> mapBeregningsgrunnlagPerioder(no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto beregningsgrunnlagEntitet) {
        return beregningsgrunnlagEntitet.getBeregningsgrunnlagPerioder().stream().map(MapDetaljertBeregningsgrunnlag::mapPeriode).collect(Collectors.toList());
    }

    private static BeregningsgrunnlagPeriodeDto mapPeriode(no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto beregningsgrunnlagPeriode) {
        return new BeregningsgrunnlagPeriodeDto(
                mapAndeler(beregningsgrunnlagPeriode.getBeregningsgrunnlagPrStatusOgAndelList()),
                new Periode(beregningsgrunnlagPeriode.getBeregningsgrunnlagPeriodeFom(), beregningsgrunnlagPeriode.getBeregningsgrunnlagPeriodeTom()),
                Beløp.fra(beregningsgrunnlagPeriode.getBruttoPrÅr()),
                Beløp.fra(beregningsgrunnlagPeriode.getAvkortetPrÅr()),
                Beløp.fra(beregningsgrunnlagPeriode.getRedusertPrÅr()),
                beregningsgrunnlagPeriode.getDagsats(),
                beregningsgrunnlagPeriode.getPeriodeÅrsaker(),
                beregningsgrunnlagPeriode.getTotalUtbetalingsgradFraUttak(),
                beregningsgrunnlagPeriode.getTotalUtbetalingsgradEtterReduksjonVedTilkommetInntekt(),
                beregningsgrunnlagPeriode.getReduksjonsfaktorInaktivTypeA());
    }

    private static List<BeregningsgrunnlagPrStatusOgAndelDto> mapAndeler(List<no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto> beregningsgrunnlagPrStatusOgAndelList) {
        return beregningsgrunnlagPrStatusOgAndelList.stream().map(MapDetaljertBeregningsgrunnlag::mapAndel).collect(Collectors.toList());
    }

    private static BeregningsgrunnlagPrStatusOgAndelDto mapAndel(no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto beregningsgrunnlagPrStatusOgAndel) {
        return new BeregningsgrunnlagPrStatusOgAndelDto.Builder()
                .medAndelsnr(beregningsgrunnlagPrStatusOgAndel.getAndelsnr())
                .medAktivitetStatus(beregningsgrunnlagPrStatusOgAndel.getAktivitetStatus())
                .medBeregningsperiode(mapBeregningsperiode(beregningsgrunnlagPrStatusOgAndel))
                .medArbeidsforholdType(beregningsgrunnlagPrStatusOgAndel.getArbeidsforholdType())
                .medBruttoPrÅr(Beløp.fra(beregningsgrunnlagPrStatusOgAndel.getBruttoPrÅr()))
                .medRedusertRefusjonPrÅr(Beløp.fra(beregningsgrunnlagPrStatusOgAndel.getRedusertRefusjonPrÅr()))
                .medRedusertBrukersAndelPrÅr(Beløp.fra(beregningsgrunnlagPrStatusOgAndel.getRedusertBrukersAndelPrÅr()))
                .medDagsatsBruker(beregningsgrunnlagPrStatusOgAndel.getDagsatsBruker())
                .medDagsatsArbeidsgiver(beregningsgrunnlagPrStatusOgAndel.getDagsatsArbeidsgiver())
                .medInntektskategori(beregningsgrunnlagPrStatusOgAndel.getGjeldendeInntektskategori())
                .medBgAndelArbeidsforhold(mapBgAndelArbeidsforhold(beregningsgrunnlagPrStatusOgAndel))
                .medOverstyrtPrÅr(Beløp.fra(beregningsgrunnlagPrStatusOgAndel.getOverstyrtPrÅr()))
                .medAvkortetPrÅr(Beløp.fra(beregningsgrunnlagPrStatusOgAndel.getAvkortetPrÅr()))
                .medRedusertPrÅr(Beløp.fra(beregningsgrunnlagPrStatusOgAndel.getRedusertPrÅr()))
                .medBeregnetPrÅr(Beløp.fra(beregningsgrunnlagPrStatusOgAndel.getBeregnetPrÅr()))
                .medBesteberegningPrÅr(Beløp.fra(beregningsgrunnlagPrStatusOgAndel.getBesteberegningPrÅr()))
                .medFordeltPrÅr(Beløp.fra(beregningsgrunnlagPrStatusOgAndel.getFordeltPrÅr()))
                .medManueltFordeltPrÅr(Beløp.fra(beregningsgrunnlagPrStatusOgAndel.getManueltFordeltPrÅr()))
                .medMaksimalRefusjonPrÅr(Beløp.fra(beregningsgrunnlagPrStatusOgAndel.getMaksimalRefusjonPrÅr()))
                .medAvkortetRefusjonPrÅr(Beløp.fra(beregningsgrunnlagPrStatusOgAndel.getAvkortetRefusjonPrÅr()))
                .medAvkortetBrukersAndelPrÅr(Beløp.fra(beregningsgrunnlagPrStatusOgAndel.getAvkortetBrukersAndelPrÅr()))
                .medPgiSnitt(Beløp.fra(beregningsgrunnlagPrStatusOgAndel.getPgiSnitt()))
                .medPgi1(Beløp.fra(beregningsgrunnlagPrStatusOgAndel.getPgi1()))
                .medPgi2(Beløp.fra(beregningsgrunnlagPrStatusOgAndel.getPgi2()))
                .medPgi3(Beløp.fra(beregningsgrunnlagPrStatusOgAndel.getPgi3()))
                .medÅrsbeløpFraTilstøtendeYtelse(Beløp.fra(beregningsgrunnlagPrStatusOgAndel.getBruttoPrÅr()))
                .medFastsattAvSaksbehandler(beregningsgrunnlagPrStatusOgAndel.getFastsattAvSaksbehandler())
                .medLagtTilAvSaksbehandler(beregningsgrunnlagPrStatusOgAndel.getKilde().equals(AndelKilde.SAKSBEHANDLER_KOFAKBER) || beregningsgrunnlagPrStatusOgAndel.getKilde().equals(AndelKilde.SAKSBEHANDLER_FORDELING))
                .medOrginalDagsatsFraTilstøtendeYtelse(beregningsgrunnlagPrStatusOgAndel.getOrginalDagsatsFraTilstøtendeYtelse())
                .build();
    }

    private static Periode mapBeregningsperiode(no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto beregningsgrunnlagPrStatusOgAndel) {
        return beregningsgrunnlagPrStatusOgAndel.getBeregningsperiodeFom() == null ? null
                : new Periode(beregningsgrunnlagPrStatusOgAndel.getBeregningsperiodeFom(), beregningsgrunnlagPrStatusOgAndel.getBeregningsperiodeTom());
    }

    private static BGAndelArbeidsforhold mapBgAndelArbeidsforhold(no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto beregningsgrunnlagPrStatusOgAndel) {
        return beregningsgrunnlagPrStatusOgAndel.getBgAndelArbeidsforhold().map(MapDetaljertBeregningsgrunnlag::mapArbeidsforhold).orElse(null);
    }

    private static BGAndelArbeidsforhold mapArbeidsforhold(BGAndelArbeidsforholdDto bgAndelArbeidsforhold) {
        return new BGAndelArbeidsforhold(
                mapArbeidsgiver(bgAndelArbeidsforhold.getArbeidsgiver()),
                bgAndelArbeidsforhold.getArbeidsforholdRef().getReferanse(),
                Beløp.fra(bgAndelArbeidsforhold.getGjeldendeRefusjonPrÅr()),
                bgAndelArbeidsforhold.getNaturalytelseBortfaltPrÅr().map(Beløp::fra).orElse(null),
                bgAndelArbeidsforhold.getNaturalytelseTilkommetPrÅr().map(Beløp::fra).orElse(null),
                bgAndelArbeidsforhold.getArbeidsperiodeFom(),
                bgAndelArbeidsforhold.getArbeidsperiodeTom().orElse(null));
    }

    private static List<AktivitetStatus> mapAktivitetstatuser(no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto beregningsgrunnlagEntitet) {
        return beregningsgrunnlagEntitet.getAktivitetStatuser().stream()
                .map(BeregningsgrunnlagAktivitetStatusDto::getAktivitetStatus)
                .collect(Collectors.toList());
    }

    private static Arbeidsgiver mapArbeidsgiver(no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver a) {
        return new Arbeidsgiver(a.getOrgnr(), a.getAktørId() != null ? a.getAktørId().getId() : null);
    }


}
