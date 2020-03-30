package no.nav.folketrygdloven.kalkulus.mapTilKontrakt;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.SammenligningsgrunnlagPrStatus;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.FaktaOmBeregningTilfelle;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.PeriodeÅrsak;
import no.nav.folketrygdloven.kalkulus.felles.v1.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulus.felles.v1.Periode;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningAktivitetHandlingType;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagPeriodeRegelType;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagRegelType;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulus.kodeverk.Inntektskategori;
import no.nav.folketrygdloven.kalkulus.kodeverk.OpptjeningAktivitetType;
import no.nav.folketrygdloven.kalkulus.kodeverk.SammenligningsgrunnlagType;
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
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.detaljert.BeregningsgrunnlagPeriodeRegelSporing;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.detaljert.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.detaljert.BeregningsgrunnlagRegelSporing;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.detaljert.Sammenligningsgrunnlag;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.detaljert.SammenligningsgrunnlagPrStatusDto;

public class MapDetaljertBeregningsgrunnlag {

    public static BeregningsgrunnlagGrunnlagDto mapGrunnlag(BeregningsgrunnlagGrunnlagEntitet beregningsgrunnlagGrunnlagEntitet) {
        return new BeregningsgrunnlagGrunnlagDto(
                beregningsgrunnlagGrunnlagEntitet.getBeregningsgrunnlag().map(MapDetaljertBeregningsgrunnlag::map).orElse(null),
                mapBeregningAktivitetAggregat(beregningsgrunnlagGrunnlagEntitet.getRegisterAktiviteter()),
                beregningsgrunnlagGrunnlagEntitet.getSaksbehandletAktiviteter().map(MapDetaljertBeregningsgrunnlag::mapBeregningAktivitetAggregat).orElse(null),
                beregningsgrunnlagGrunnlagEntitet.getOverstyring().map(MapDetaljertBeregningsgrunnlag::mapOverstyrteAktiviteterAggregat).orElse(null),
                beregningsgrunnlagGrunnlagEntitet.getRefusjonOverstyringer().map(MapDetaljertBeregningsgrunnlag::mapRefusjonOverstyringAggregat).orElse(null),
                beregningsgrunnlagGrunnlagEntitet.erAktivt(),
                new BeregningsgrunnlagTilstand(beregningsgrunnlagGrunnlagEntitet.getBeregningsgrunnlagTilstand().getKode())
        );
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
                beregningRefusjonOverstyringEntitet.getFørsteMuligeRefusjonFom()
        );
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
                beregningAktivitetOverstyringEntitet.getArbeidsforholdRef() == null || beregningAktivitetOverstyringEntitet.getArbeidsforholdRef().getReferanse() == null ? null : new InternArbeidsforholdRefDto(beregningAktivitetOverstyringEntitet.getArbeidsforholdRef().getReferanse()),
                new OpptjeningAktivitetType(beregningAktivitetOverstyringEntitet.getOpptjeningAktivitetType().getKode()),
                new BeregningAktivitetHandlingType(beregningAktivitetOverstyringEntitet.getHandling().getKode())
        );
    }

    private static BeregningAktivitetAggregatDto mapBeregningAktivitetAggregat(BeregningAktivitetAggregatEntitet aktivitetAggregatEntitet) {
        return new BeregningAktivitetAggregatDto(
                mapBeregningAktiviteter(aktivitetAggregatEntitet.getBeregningAktiviteter()),
                aktivitetAggregatEntitet.getSkjæringstidspunktOpptjening()
        );
    }

    private static List<BeregningAktivitetDto> mapBeregningAktiviteter(List<BeregningAktivitetEntitet> beregningAktiviteter) {
        return beregningAktiviteter.stream().map(MapDetaljertBeregningsgrunnlag::mapBeregningAktivitet).collect(Collectors.toList());
    }

    private static BeregningAktivitetDto mapBeregningAktivitet(BeregningAktivitetEntitet beregningAktivitetEntitet) {
        return new BeregningAktivitetDto(
                new Periode(beregningAktivitetEntitet.getPeriode().getFomDato(), beregningAktivitetEntitet.getPeriode().getTomDato()),
                beregningAktivitetEntitet.getArbeidsgiver() == null ? null : mapArbeidsgiver(beregningAktivitetEntitet.getArbeidsgiver()),
                beregningAktivitetEntitet.getArbeidsforholdRef() == null || beregningAktivitetEntitet.getArbeidsforholdRef().getReferanse() == null ? null : new InternArbeidsforholdRefDto(beregningAktivitetEntitet.getArbeidsforholdRef().getReferanse()),
                new OpptjeningAktivitetType(beregningAktivitetEntitet.getOpptjeningAktivitetType().getKode())
        );
    }

    public static BeregningsgrunnlagDto map(BeregningsgrunnlagEntitet beregningsgrunnlagEntitet) {
        return new BeregningsgrunnlagDto(
                beregningsgrunnlagEntitet.getSkjæringstidspunkt(),
                mapAktivitetstatuser(beregningsgrunnlagEntitet),
                mapBeregningsgrunnlagPerioder(beregningsgrunnlagEntitet),
                mapSammenligningsgrunnlag(beregningsgrunnlagEntitet),
                mapSammenligningsgrunnlagPrStatusListe(beregningsgrunnlagEntitet),
                beregningsgrunnlagEntitet.getFaktaOmBeregningTilfeller().stream().map(MapDetaljertBeregningsgrunnlag::mapTilfelle).collect(Collectors.toList()),
                beregningsgrunnlagEntitet.isOverstyrt(),
                mapRegelsporing(beregningsgrunnlagEntitet.getRegelSporingMap()),
                beregningsgrunnlagEntitet.getGrunnbeløp() == null ? null : beregningsgrunnlagEntitet.getGrunnbeløp().getVerdi()
        );
    }

    private static Map<BeregningsgrunnlagRegelType, BeregningsgrunnlagRegelSporing> mapRegelsporing(Map<no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.BeregningsgrunnlagRegelType, no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagRegelSporing> regelsporingMap) {
        Map<BeregningsgrunnlagRegelType, BeregningsgrunnlagRegelSporing> regelmap = new HashMap<>();
        regelsporingMap.forEach((type, sporing) -> regelmap.put(new BeregningsgrunnlagRegelType(type.getKode()), mapSporing(sporing)));
        return regelmap;
    }

    private static BeregningsgrunnlagRegelSporing mapSporing(no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagRegelSporing sporing) {
        return new BeregningsgrunnlagRegelSporing(
                sporing.getRegelEvaluering(),
                sporing.getRegelInput(),
                new BeregningsgrunnlagRegelType(sporing.getRegelType().getKode())
        );
    }

    private static no.nav.folketrygdloven.kalkulus.kodeverk.FaktaOmBeregningTilfelle mapTilfelle(FaktaOmBeregningTilfelle faktaOmBeregningTilfelle) {
        return new no.nav.folketrygdloven.kalkulus.kodeverk.FaktaOmBeregningTilfelle(faktaOmBeregningTilfelle.getKode());
    }

    private static List<SammenligningsgrunnlagPrStatusDto> mapSammenligningsgrunnlagPrStatusListe(BeregningsgrunnlagEntitet beregningsgrunnlagEntitet) {
        if (beregningsgrunnlagEntitet.getSammenligningsgrunnlagPrStatusListe() == null) {
            return null;
        }
        return beregningsgrunnlagEntitet.getSammenligningsgrunnlagPrStatusListe().stream().map(MapDetaljertBeregningsgrunnlag::mapSammeligningsgrunnlagPrStatus).collect(Collectors.toList());
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
        return beregningsgrunnlagEntitet.getBeregningsgrunnlagPerioder().stream().map(MapDetaljertBeregningsgrunnlag::mapPeriode).collect(Collectors.toList());
    }

    private static BeregningsgrunnlagPeriodeDto mapPeriode(BeregningsgrunnlagPeriode beregningsgrunnlagPeriode) {
        return new BeregningsgrunnlagPeriodeDto(
                mapAndeler(beregningsgrunnlagPeriode.getBeregningsgrunnlagPrStatusOgAndelList()),
                new Periode(beregningsgrunnlagPeriode.getBeregningsgrunnlagPeriodeFom(), beregningsgrunnlagPeriode.getBeregningsgrunnlagPeriodeTom()),
                beregningsgrunnlagPeriode.getBruttoPrÅr(),
                beregningsgrunnlagPeriode.getAvkortetPrÅr(),
                beregningsgrunnlagPeriode.getRedusertPrÅr(),
                beregningsgrunnlagPeriode.getDagsats(),
                beregningsgrunnlagPeriode.getPeriodeÅrsaker().stream().map(MapDetaljertBeregningsgrunnlag::mapPeriodeÅrsak).collect(Collectors.toList()),
                mapPeriodeRegelSporingMap(beregningsgrunnlagPeriode.getRegelSporingMap()));
    }

    private static Map<BeregningsgrunnlagPeriodeRegelType, BeregningsgrunnlagPeriodeRegelSporing> mapPeriodeRegelSporingMap(Map<no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.BeregningsgrunnlagPeriodeRegelType, no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagPeriodeRegelSporing> regelSporingMap) {
        Map<BeregningsgrunnlagPeriodeRegelType, BeregningsgrunnlagPeriodeRegelSporing> regelmap = new HashMap<>();
        regelSporingMap.forEach((type, sporing) -> regelmap.put(new BeregningsgrunnlagPeriodeRegelType(type.getKode()), mapSporingPeriode(sporing)));
        return regelmap;
    }

    private static BeregningsgrunnlagPeriodeRegelSporing mapSporingPeriode(no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagPeriodeRegelSporing sporing) {
        return new BeregningsgrunnlagPeriodeRegelSporing(
                sporing.getRegelEvaluering(),
                sporing.getRegelInput(),
                new BeregningsgrunnlagPeriodeRegelType(sporing.getRegelType().getKode())
        );
    }

    private static no.nav.folketrygdloven.kalkulus.kodeverk.PeriodeÅrsak mapPeriodeÅrsak(PeriodeÅrsak periodeÅrsak) {
        return new no.nav.folketrygdloven.kalkulus.kodeverk.PeriodeÅrsak(periodeÅrsak.getKode());
    }

    private static List<BeregningsgrunnlagPrStatusOgAndelDto> mapAndeler(List<BeregningsgrunnlagPrStatusOgAndel> beregningsgrunnlagPrStatusOgAndelList) {
        return beregningsgrunnlagPrStatusOgAndelList.stream().map(MapDetaljertBeregningsgrunnlag::mapAndel).collect(Collectors.toList());
    }

    private static BeregningsgrunnlagPrStatusOgAndelDto mapAndel(BeregningsgrunnlagPrStatusOgAndel beregningsgrunnlagPrStatusOgAndel) {
        return new BeregningsgrunnlagPrStatusOgAndelDto(
                beregningsgrunnlagPrStatusOgAndel.getAndelsnr(),
                new AktivitetStatus(beregningsgrunnlagPrStatusOgAndel.getAktivitetStatus().getKode()),
                new Periode(beregningsgrunnlagPrStatusOgAndel.getBeregningsperiodeFom(), beregningsgrunnlagPrStatusOgAndel.getBeregningsperiodeTom()),
                new OpptjeningAktivitetType(beregningsgrunnlagPrStatusOgAndel.getArbeidsforholdType().getKode()),
                beregningsgrunnlagPrStatusOgAndel.getBruttoPrÅr(),
                beregningsgrunnlagPrStatusOgAndel.getRedusertRefusjonPrÅr(),
                beregningsgrunnlagPrStatusOgAndel.getRedusertBrukersAndelPrÅr(),
                beregningsgrunnlagPrStatusOgAndel.getDagsatsBruker(),
                beregningsgrunnlagPrStatusOgAndel.getDagsatsArbeidsgiver(),
                new Inntektskategori(beregningsgrunnlagPrStatusOgAndel.getInntektskategori().getKode()),
                mapBgAndelArbeidsforhold(beregningsgrunnlagPrStatusOgAndel),
                beregningsgrunnlagPrStatusOgAndel.getOverstyrtPrÅr(),
                beregningsgrunnlagPrStatusOgAndel.getAvkortetPrÅr(),
                beregningsgrunnlagPrStatusOgAndel.getRedusertPrÅr(),
                beregningsgrunnlagPrStatusOgAndel.getBeregnetPrÅr(),
                beregningsgrunnlagPrStatusOgAndel.getFordeltPrÅr(),
                beregningsgrunnlagPrStatusOgAndel.getMaksimalRefusjonPrÅr(),
                beregningsgrunnlagPrStatusOgAndel.getAvkortetRefusjonPrÅr(),
                beregningsgrunnlagPrStatusOgAndel.getAvkortetBrukersAndelPrÅr(),
                beregningsgrunnlagPrStatusOgAndel.getPgiSnitt(),
                beregningsgrunnlagPrStatusOgAndel.getPgi1(),
                beregningsgrunnlagPrStatusOgAndel.getPgi2(),
                beregningsgrunnlagPrStatusOgAndel.getPgi3(),
                beregningsgrunnlagPrStatusOgAndel.getBruttoPrÅr(),
                beregningsgrunnlagPrStatusOgAndel.getNyIArbeidslivet(),
                beregningsgrunnlagPrStatusOgAndel.getFastsattAvSaksbehandler(),
                beregningsgrunnlagPrStatusOgAndel.getBesteberegningPrÅr(),
                beregningsgrunnlagPrStatusOgAndel.getLagtTilAvSaksbehandler(),
                beregningsgrunnlagPrStatusOgAndel.getOrginalDagsatsFraTilstøtendeYtelse(),
                beregningsgrunnlagPrStatusOgAndel.mottarYtelse().orElse(null),
                beregningsgrunnlagPrStatusOgAndel.erNyoppstartet().orElse(null)
        );
    }

    private static BGAndelArbeidsforhold mapBgAndelArbeidsforhold(BeregningsgrunnlagPrStatusOgAndel beregningsgrunnlagPrStatusOgAndel) {
        return beregningsgrunnlagPrStatusOgAndel.getBgAndelArbeidsforhold().map(MapDetaljertBeregningsgrunnlag::mapArbeidsforhold).orElse(null);
    }

    private static BGAndelArbeidsforhold mapArbeidsforhold(no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BGAndelArbeidsforhold bgAndelArbeidsforhold) {
        return new BGAndelArbeidsforhold(
                mapArbeidsgiver(bgAndelArbeidsforhold.getArbeidsgiver()),
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

    private static Arbeidsgiver mapArbeidsgiver(no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.Arbeidsgiver a) {
        return new Arbeidsgiver(a.getOrgnr(), a.getAktørId() != null ? a.getAktørId().getId() : null);
    }

}
