package no.nav.folketrygdloven.kalkulus.domene.mapTilKontrakt;

import java.math.BigDecimal;
import java.util.List;

import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.AndelArbeidsforholdEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.AktivitetAggregatEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.AktivitetEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.RefusjonOverstyringEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.RefusjonOverstyringerEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagGrunnlagEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagPeriodeEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagAndelEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BesteberegningInntektEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BesteberegningMånedsgrunnlagEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BesteberegninggrunnlagEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.FaktaAggregatEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.FaktaAktørEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.FaktaArbeidsforholdEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.SammenligningsgrunnlagPrStatusEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.Promille;
import no.nav.foreldrepenger.kalkulus.kontrakt.response.beregningsgrunnlag.besteberegning.BesteberegningGrunnlagDto;
import no.nav.foreldrepenger.kalkulus.kontrakt.response.beregningsgrunnlag.detaljert.BGAndelArbeidsforhold;
import no.nav.foreldrepenger.kalkulus.kontrakt.typer.Beløp;
import no.nav.foreldrepenger.kalkulus.kontrakt.typer.InternArbeidsforholdRefDto;
import no.nav.foreldrepenger.kalkulus.kontrakt.typer.Periode;
import no.nav.folketrygdloven.kalkulus.kodeverk.AndelKilde;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand;
import no.nav.foreldrepenger.kalkulus.kontrakt.typer.Arbeidsgiver;
import no.nav.foreldrepenger.kalkulus.kontrakt.response.beregningsgrunnlag.detaljert.BeregningAktivitetAggregatDto;
import no.nav.foreldrepenger.kalkulus.kontrakt.response.beregningsgrunnlag.detaljert.BeregningAktivitetDto;
import no.nav.foreldrepenger.kalkulus.kontrakt.response.beregningsgrunnlag.detaljert.BeregningAktivitetOverstyringDto;
import no.nav.foreldrepenger.kalkulus.kontrakt.response.beregningsgrunnlag.detaljert.BeregningAktivitetOverstyringerDto;
import no.nav.foreldrepenger.kalkulus.kontrakt.response.beregningsgrunnlag.detaljert.BeregningRefusjonOverstyringDto;
import no.nav.foreldrepenger.kalkulus.kontrakt.response.beregningsgrunnlag.detaljert.BeregningRefusjonOverstyringerDto;
import no.nav.foreldrepenger.kalkulus.kontrakt.response.beregningsgrunnlag.detaljert.BeregningRefusjonPeriodeDto;
import no.nav.foreldrepenger.kalkulus.kontrakt.response.beregningsgrunnlag.detaljert.BeregningsgrunnlagAktivitetStatusDto;
import no.nav.foreldrepenger.kalkulus.kontrakt.response.beregningsgrunnlag.detaljert.BeregningsgrunnlagDto;
import no.nav.foreldrepenger.kalkulus.kontrakt.response.beregningsgrunnlag.detaljert.BeregningsgrunnlagGrunnlagDto;
import no.nav.foreldrepenger.kalkulus.kontrakt.response.beregningsgrunnlag.detaljert.BeregningsgrunnlagPeriodeDto;
import no.nav.foreldrepenger.kalkulus.kontrakt.response.beregningsgrunnlag.detaljert.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.foreldrepenger.kalkulus.kontrakt.response.beregningsgrunnlag.detaljert.FaktaAggregatDto;
import no.nav.foreldrepenger.kalkulus.kontrakt.response.beregningsgrunnlag.detaljert.FaktaAktørDto;
import no.nav.foreldrepenger.kalkulus.kontrakt.response.beregningsgrunnlag.detaljert.FaktaArbeidsforholdDto;
import no.nav.foreldrepenger.kalkulus.kontrakt.response.beregningsgrunnlag.detaljert.SammenligningsgrunnlagPrStatusDto;

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

    public static BesteberegningGrunnlagDto mapBesteberegningsgrunlag(BesteberegninggrunnlagEntitet bbg) {
        var måneder = bbg.getSeksBesteMåneder().stream().map(MapDetaljertBeregningsgrunnlag::mapBBMåned).toList();
        return new BesteberegningGrunnlagDto(måneder, mapBeløp(bbg.getAvvik().orElse(null)));
    }

    private static BesteberegningGrunnlagDto.BesteberegningMånedDto mapBBMåned(BesteberegningMånedsgrunnlagEntitet måned) {
        var inntekter = måned.getInntekter().stream().map(MapDetaljertBeregningsgrunnlag::mapBBInntekt).toList();
        var periode = new Periode(måned.getPeriode().getFomDato(), måned.getPeriode().getTomDato());
        return new BesteberegningGrunnlagDto.BesteberegningMånedDto(periode, inntekter);
    }

    private static BesteberegningGrunnlagDto.BesteberegningInntektDto mapBBInntekt(BesteberegningInntektEntitet i) {
        return new BesteberegningGrunnlagDto.BesteberegningInntektDto(i.getOpptjeningAktivitetType(), mapBeløp(i.getInntekt()),
            i.getArbeidsgiver() == null ? null : mapArbeidsgiver(i.getArbeidsgiver()), i.getArbeidsforholdRef() == null ? null : new InternArbeidsforholdRefDto(i.getArbeidsforholdRef().getReferanse()));
    }

    private static FaktaAggregatDto mapFaktaAggregat(FaktaAggregatEntitet faktaAggregatEntitet) {
        return new FaktaAggregatDto(mapFaktaArbeidsforholdListe(faktaAggregatEntitet.getFaktaArbeidsforhold()),
            faktaAggregatEntitet.getFaktaAktør().map(MapDetaljertBeregningsgrunnlag::mapFaktaAktør).orElse(null));
    }

    private static List<FaktaArbeidsforholdDto> mapFaktaArbeidsforholdListe(List<FaktaArbeidsforholdEntitet> faktaArbeidsforhold) {
        return faktaArbeidsforhold.stream().map(MapDetaljertBeregningsgrunnlag::mapFaktaArbeidsforhold).toList();
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

    private static BeregningRefusjonOverstyringerDto mapRefusjonOverstyringAggregat(RefusjonOverstyringerEntitet refusjonOverstyringerEntitet) {
        return new BeregningRefusjonOverstyringerDto(mapRefusjonOverstyringer(refusjonOverstyringerEntitet.getRefusjonOverstyringer()));
    }

    private static List<BeregningRefusjonOverstyringDto> mapRefusjonOverstyringer(List<RefusjonOverstyringEntitet> refusjonOverstyringer) {
        return refusjonOverstyringer.stream().map(MapDetaljertBeregningsgrunnlag::mapRefusjonOverstyring).toList();
    }

    private static BeregningRefusjonOverstyringDto mapRefusjonOverstyring(RefusjonOverstyringEntitet refusjonOverstyringEntitet) {
        var perioder = refusjonOverstyringEntitet.getRefusjonPerioder()
            .stream()
            .map(rp -> new BeregningRefusjonPeriodeDto(rp.getArbeidsforholdRef().getReferanse() == null ? null : new InternArbeidsforholdRefDto(rp.getArbeidsforholdRef().getReferanse()),
                rp.getStartdatoRefusjon()))
            .toList();
        return new BeregningRefusjonOverstyringDto(mapArbeidsgiver(refusjonOverstyringEntitet.getArbeidsgiver()),
            refusjonOverstyringEntitet.getFørsteMuligeRefusjonFom().orElse(null), refusjonOverstyringEntitet.getErFristUtvidet(), perioder);
    }

    private static BeregningAktivitetOverstyringerDto mapOverstyrteAktiviteterAggregat(AktivitetAggregatEntitet aktivitetOverstyringerEntitet) {
        return new BeregningAktivitetOverstyringerDto(mapOverstyringer(aktivitetOverstyringerEntitet.getAktiviteter()));
    }

    private static List<BeregningAktivitetOverstyringDto> mapOverstyringer(List<AktivitetEntitet> overstyringer) {
        return overstyringer.stream().map(MapDetaljertBeregningsgrunnlag::mapOverstyrtAktivitet).toList();
    }

    private static BeregningAktivitetOverstyringDto mapOverstyrtAktivitet(AktivitetEntitet aktivitetEntitet) {
        return new BeregningAktivitetOverstyringDto(new Periode(aktivitetEntitet.getPeriode().getFomDato(),
            aktivitetEntitet.getPeriode().getTomDato()),
            aktivitetEntitet.getArbeidsgiver() == null ? null : mapArbeidsgiver(aktivitetEntitet.getArbeidsgiver()),
            aktivitetEntitet.getArbeidsforholdRef() == null
                || aktivitetEntitet.getArbeidsforholdRef().getReferanse() == null ? null : new InternArbeidsforholdRefDto(
                aktivitetEntitet.getArbeidsforholdRef().getReferanse()),
            aktivitetEntitet.getOpptjeningAktivitetType(),
            aktivitetEntitet.getOverstyrHandlingType().orElse(null));
    }

    private static BeregningAktivitetAggregatDto mapBeregningAktivitetAggregat(AktivitetAggregatEntitet aktivitetAggregatEntitet) {
        return new BeregningAktivitetAggregatDto(mapBeregningAktiviteter(aktivitetAggregatEntitet.getAktiviteter()),
            aktivitetAggregatEntitet.getSkjæringstidspunktOpptjening());
    }

    private static List<BeregningAktivitetDto> mapBeregningAktiviteter(List<AktivitetEntitet> beregningAktiviteter) {
        return beregningAktiviteter.stream().map(MapDetaljertBeregningsgrunnlag::mapBeregningAktivitet).toList();
    }

    private static BeregningAktivitetDto mapBeregningAktivitet(AktivitetEntitet aktivitetEntitet) {
        return new BeregningAktivitetDto(
            new Periode(aktivitetEntitet.getPeriode().getFomDato(), aktivitetEntitet.getPeriode().getTomDato()),
            aktivitetEntitet.getArbeidsgiver() == null ? null : mapArbeidsgiver(aktivitetEntitet.getArbeidsgiver()),
            aktivitetEntitet.getArbeidsforholdRef() == null
                || aktivitetEntitet.getArbeidsforholdRef().getReferanse() == null ? null : new InternArbeidsforholdRefDto(
                aktivitetEntitet.getArbeidsforholdRef().getReferanse()), aktivitetEntitet.getOpptjeningAktivitetType());
    }

    private static BeregningsgrunnlagDto mapGrunnlag(BeregningsgrunnlagEntitet beregningsgrunnlagEntitet) {
        return new BeregningsgrunnlagDto(beregningsgrunnlagEntitet.getSkjæringstidspunkt(), null,
            mapBeregningsgrunnlagPerioder(beregningsgrunnlagEntitet), mapSammenligningsgrunnlagPrStatusListe(beregningsgrunnlagEntitet),
            beregningsgrunnlagEntitet.getFaktaOmBeregningTilfeller(), beregningsgrunnlagEntitet.isOverstyrt(),
            beregningsgrunnlagEntitet.getGrunnbeløp() == null ? null : mapBeløp(beregningsgrunnlagEntitet.getGrunnbeløp()), mapAktivitetstatuserMedHjemler(beregningsgrunnlagEntitet));
    }

    private static List<SammenligningsgrunnlagPrStatusDto> mapSammenligningsgrunnlagPrStatusListe(BeregningsgrunnlagEntitet beregningsgrunnlagEntitet) {
        return beregningsgrunnlagEntitet.getSammenligningsgrunnlagPrStatusListe()
            .stream()
            .map(MapDetaljertBeregningsgrunnlag::mapSammeligningsgrunnlagPrStatus)
            .toList();
    }

    private static SammenligningsgrunnlagPrStatusDto mapSammeligningsgrunnlagPrStatus(SammenligningsgrunnlagPrStatusEntitet sammenligningsgrunnlagPrStatus) {
        return new SammenligningsgrunnlagPrStatusDto(
            new Periode(sammenligningsgrunnlagPrStatus.getSammenligningsperiodeFom(), sammenligningsgrunnlagPrStatus.getSammenligningsperiodeTom()),
            sammenligningsgrunnlagPrStatus.getSammenligningsgrunnlagType(), mapBeløp(sammenligningsgrunnlagPrStatus.getRapportertPrÅr()),
            mapPromille(sammenligningsgrunnlagPrStatus.getAvvikPromille()));
    }

    private static BigDecimal mapPromille(Promille promille) {
        return promille == null ? null : promille.getVerdi();
    }


    private static List<BeregningsgrunnlagPeriodeDto> mapBeregningsgrunnlagPerioder(BeregningsgrunnlagEntitet beregningsgrunnlagEntitet) {
        return beregningsgrunnlagEntitet.getBeregningsgrunnlagPerioder()
            .stream()
            .map(MapDetaljertBeregningsgrunnlag::mapPeriode)
            .toList();
    }

    private static BeregningsgrunnlagPeriodeDto mapPeriode(BeregningsgrunnlagPeriodeEntitet beregningsgrunnlagPeriode) {
        return new BeregningsgrunnlagPeriodeDto.Builder()
            .medPeriode(new Periode(beregningsgrunnlagPeriode.getBeregningsgrunnlagPeriodeFom(), beregningsgrunnlagPeriode.getBeregningsgrunnlagPeriodeTom()))
            .medBeregningsgrunnlagPrStatusOgAndelList(mapAndeler(beregningsgrunnlagPeriode.getBeregningsgrunnlagAndelList()))
            .medBruttoPrÅr(mapBeløp(beregningsgrunnlagPeriode.getBruttoPrÅr()))
            .medAvkortetPrÅr(mapBeløp(beregningsgrunnlagPeriode.getAvkortetPrÅr()))
            .medRedusertPrÅr(mapBeløp(beregningsgrunnlagPeriode.getRedusertPrÅr()))
            .medDagsats(beregningsgrunnlagPeriode.getDagsats())
            .medPeriodeÅrsaker(beregningsgrunnlagPeriode.getPeriodeÅrsaker())
            .medTotalUtbetalingsgradFraUttak(beregningsgrunnlagPeriode.getTotalUtbetalingsgradFraUttak())
            .build();
    }

    private static List<BeregningsgrunnlagPrStatusOgAndelDto> mapAndeler(List<BeregningsgrunnlagAndelEntitet> beregningsgrunnlagAndelList) {
        return beregningsgrunnlagAndelList.stream().map(MapDetaljertBeregningsgrunnlag::mapAndel).toList();
    }

    private static BeregningsgrunnlagPrStatusOgAndelDto mapAndel(BeregningsgrunnlagAndelEntitet beregningsgrunnlagPrStatusOgAndel) {
        return new BeregningsgrunnlagPrStatusOgAndelDto.Builder().medAndelsnr(beregningsgrunnlagPrStatusOgAndel.getAndelsnr())
            .medAktivitetStatus(beregningsgrunnlagPrStatusOgAndel.getAktivitetStatus())
            .medBeregningsperiode(mapBeregningsperiode(beregningsgrunnlagPrStatusOgAndel))
            .medArbeidsforholdType(beregningsgrunnlagPrStatusOgAndel.getArbeidsforholdType())
            .medBruttoPrÅr(mapBeløp(beregningsgrunnlagPrStatusOgAndel.getBruttoPrÅr()))
            .medRedusertRefusjonPrÅr(mapBeløp(beregningsgrunnlagPrStatusOgAndel.getRedusertRefusjonPrÅr()))
            .medRedusertBrukersAndelPrÅr(mapBeløp(beregningsgrunnlagPrStatusOgAndel.getRedusertBrukersAndelPrÅr()))
            .medDagsatsBruker(beregningsgrunnlagPrStatusOgAndel.getDagsatsBruker())
            .medDagsatsArbeidsgiver(beregningsgrunnlagPrStatusOgAndel.getDagsatsArbeidsgiver())
            .medInntektskategori(beregningsgrunnlagPrStatusOgAndel.getInntektskategori())
            .medBgAndelArbeidsforhold(mapAndelArbeidsforhold(beregningsgrunnlagPrStatusOgAndel))
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
            .medÅrsbeløpFraTilstøtendeYtelse(mapBeløp(beregningsgrunnlagPrStatusOgAndel.getÅrsbeløpFraTilstøtendeYtelse()))
            .medFastsattAvSaksbehandler(beregningsgrunnlagPrStatusOgAndel.getFastsattAvSaksbehandler())
            .medLagtTilAvSaksbehandler(
                beregningsgrunnlagPrStatusOgAndel.getKilde().equals(AndelKilde.SAKSBEHANDLER_KOFAKBER) || beregningsgrunnlagPrStatusOgAndel.getKilde()
                    .equals(AndelKilde.SAKSBEHANDLER_FORDELING))
            .medOrginalDagsatsFraTilstøtendeYtelse(beregningsgrunnlagPrStatusOgAndel.getOrginalDagsatsFraTilstøtendeYtelse())
            .build();
    }

    private static Periode mapBeregningsperiode(BeregningsgrunnlagAndelEntitet beregningsgrunnlagPrStatusOgAndel) {
        return beregningsgrunnlagPrStatusOgAndel.getBeregningsperiodeFom() == null ? null : new Periode(
            beregningsgrunnlagPrStatusOgAndel.getBeregningsperiodeFom(), beregningsgrunnlagPrStatusOgAndel.getBeregningsperiodeTom());
    }

    private static BGAndelArbeidsforhold mapAndelArbeidsforhold(BeregningsgrunnlagAndelEntitet beregningsgrunnlagPrStatusOgAndel) {
        return beregningsgrunnlagPrStatusOgAndel.getAndelArbeidsforhold().map(MapDetaljertBeregningsgrunnlag::mapArbeidsforhold).orElse(null);
    }

    private static BGAndelArbeidsforhold mapArbeidsforhold(AndelArbeidsforholdEntitet andelArbeidsforhold) {
        return new BGAndelArbeidsforhold(mapArbeidsgiver(andelArbeidsforhold.getArbeidsgiver()),
            andelArbeidsforhold.getArbeidsforholdRef().getUUIDReferanse(), mapBeløp(andelArbeidsforhold.getGjeldendeRefusjonPrÅr()),
            andelArbeidsforhold.getNaturalytelseBortfaltPrÅr().map(MapDetaljertBeregningsgrunnlag::mapBeløp).orElse(null),
            andelArbeidsforhold.getNaturalytelseTilkommetPrÅr().map(MapDetaljertBeregningsgrunnlag::mapBeløp).orElse(null),
            andelArbeidsforhold.getArbeidsperiodeFom(), andelArbeidsforhold.getArbeidsperiodeTom().orElse(null));
    }

    private static List<BeregningsgrunnlagAktivitetStatusDto> mapAktivitetstatuserMedHjemler(BeregningsgrunnlagEntitet beregningsgrunnlagEntitet) {
        return beregningsgrunnlagEntitet.getAktivitetStatuser()
            .stream()
            .map(as -> new BeregningsgrunnlagAktivitetStatusDto(as.getAktivitetStatus(), as.getHjemmel()))
            .toList();
    }

    private static Arbeidsgiver mapArbeidsgiver(no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.Arbeidsgiver a) {
        return new Arbeidsgiver(a.getOrgnr(), a.getAktørId() != null ? a.getAktørId().getId() : null);
    }

    private static Beløp mapBeløp(no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.Beløp beløp) {
        return beløp == null ? null : Beløp.fra(beløp.getVerdi());
    }
}
