package no.nav.folketrygdloven.kalkulus.mapFraEntitet;

import java.math.BigDecimal;
import java.util.List;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagAktivitetStatusDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.SammenligningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.SammenligningsgrunnlagPrStatusDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Refusjon;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BGAndelArbeidsforhold;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagAktivitetStatus;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndel;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.Sammenligningsgrunnlag;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.SammenligningsgrunnlagPrStatus;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.Beløp;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.Promille;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.AndelKilde;
import no.nav.folketrygdloven.kalkulus.kodeverk.Hjemmel;
import no.nav.folketrygdloven.kalkulus.kodeverk.Inntektskategori;
import no.nav.folketrygdloven.kalkulus.kodeverk.OpptjeningAktivitetType;
import no.nav.folketrygdloven.kalkulus.kodeverk.PeriodeÅrsak;
import no.nav.folketrygdloven.kalkulus.kodeverk.SammenligningsgrunnlagType;


public class BGMapperTilKalkulus {
    public static SammenligningsgrunnlagDto mapSammenligningsgrunnlag(Sammenligningsgrunnlag fraFagsystem) {
        SammenligningsgrunnlagDto.Builder builder = SammenligningsgrunnlagDto.builder();
        builder.medAvvikPromilleNy(mapFraPromille(fraFagsystem.getAvvikPromilleNy()));
        builder.medRapportertPrÅr(mapFraBeløp(fraFagsystem.getRapportertPrÅr()));
        builder.medSammenligningsperiode(fraFagsystem.getSammenligningsperiodeFom(), fraFagsystem.getSammenligningsperiodeTom());
        return builder.build();
    }

    public static BeregningsgrunnlagAktivitetStatusDto.Builder mapAktivitetStatus(BeregningsgrunnlagAktivitetStatus fraFagsystem) {
        BeregningsgrunnlagAktivitetStatusDto.Builder builder = new BeregningsgrunnlagAktivitetStatusDto.Builder();
        builder.medAktivitetStatus(AktivitetStatus.fraKode(fraFagsystem.getAktivitetStatus().getKode()));
        builder.medHjemmel(Hjemmel.fraKode(fraFagsystem.getHjemmel().getKode()));

        return builder;
    }

    public static BeregningsgrunnlagPeriodeDto.Builder mapBeregningsgrunnlagPeriode(BeregningsgrunnlagPeriode fraFagsystem) {
        BeregningsgrunnlagPeriodeDto.Builder builder = new BeregningsgrunnlagPeriodeDto.Builder();

        //med
        builder.medAvkortetPrÅr(mapFraBeløp(fraFagsystem.getAvkortetPrÅr()));
        builder.medBeregningsgrunnlagPeriode(fraFagsystem.getBeregningsgrunnlagPeriodeFom(), fraFagsystem.getBeregningsgrunnlagPeriodeTom());
        builder.medBruttoPrÅr(mapFraBeløp(fraFagsystem.getBruttoPrÅr()));
        builder.medRedusertPrÅr(mapFraBeløp(fraFagsystem.getRedusertPrÅr()));

        //legg til
        fraFagsystem.getPeriodeÅrsaker().forEach(periodeÅrsak -> builder.leggTilPeriodeÅrsak(PeriodeÅrsak.fraKode(periodeÅrsak.getKode())));
        fraFagsystem.getBeregningsgrunnlagPrStatusOgAndelList().forEach(statusOgAndel -> builder.leggTilBeregningsgrunnlagPrStatusOgAndel(mapStatusOgAndel(statusOgAndel)));

        return builder;
    }

    public static SammenligningsgrunnlagPrStatusDto.Builder mapSammenligningsgrunnlagMedStatus(SammenligningsgrunnlagPrStatus fraFagsystem) {
        SammenligningsgrunnlagPrStatusDto.Builder builder = new SammenligningsgrunnlagPrStatusDto.Builder();
        builder.medAvvikPromilleNy(mapFraPromille(fraFagsystem.getAvvikPromilleNy()));
        builder.medRapportertPrÅr(mapFraBeløp(fraFagsystem.getRapportertPrÅr()));
        builder.medSammenligningsgrunnlagType(SammenligningsgrunnlagType.fraKode(fraFagsystem.getSammenligningsgrunnlagType().getKode()));
        builder.medSammenligningsperiode(fraFagsystem.getSammenligningsperiodeFom(), fraFagsystem.getSammenligningsperiodeTom());

        return builder;
    }

    private static BeregningsgrunnlagPrStatusOgAndelDto.Builder mapStatusOgAndel(BeregningsgrunnlagPrStatusOgAndel fraFagsystem) {
        BeregningsgrunnlagPrStatusOgAndelDto.Builder builder = BeregningsgrunnlagPrStatusOgAndelDto.ny()
                .medAktivitetStatus(AktivitetStatus.fraKode(fraFagsystem.getAktivitetStatus().getKode()))
                .medAndelsnr(fraFagsystem.getAndelsnr())
                .medArbforholdType(fraFagsystem.getArbeidsforholdType() == null ? null : OpptjeningAktivitetType.fraKode(fraFagsystem.getArbeidsforholdType().getKode()))
                .medAvkortetBrukersAndelPrÅr(mapFraBeløp(fraFagsystem.getAvkortetBrukersAndelPrÅr()))
                .medAvkortetPrÅr(mapFraBeløp(fraFagsystem.getAvkortetPrÅr()))
                .medAvkortetRefusjonPrÅr(mapFraBeløp(fraFagsystem.getAvkortetRefusjonPrÅr()))
                .medBeregnetPrÅr(mapFraBeløp(fraFagsystem.getBeregnetPrÅr()))
                .medBesteberegningPrÅr(mapFraBeløp(fraFagsystem.getBesteberegningPrÅr()))
                .medFastsattAvSaksbehandler(fraFagsystem.getFastsattAvSaksbehandler())
                .medOverstyrtPrÅr(mapFraBeløp(fraFagsystem.getOverstyrtPrÅr()))
                .medFordeltPrÅr(mapFraBeløp(fraFagsystem.getFordeltPrÅr()))
                .medRedusertPrÅr(mapFraBeløp(fraFagsystem.getRedusertPrÅr()))
                .medRedusertBrukersAndelPrÅr(mapFraBeløp(fraFagsystem.getRedusertBrukersAndelPrÅr()))
                .medMaksimalRefusjonPrÅr(mapFraBeløp(fraFagsystem.getMaksimalRefusjonPrÅr()))
                .medRedusertRefusjonPrÅr(mapFraBeløp(fraFagsystem.getRedusertRefusjonPrÅr()))
                .medÅrsbeløpFraTilstøtendeYtelse(mapFraBeløp(fraFagsystem.getÅrsbeløpFraTilstøtendeYtelse()))
                .medInntektskategori(fraFagsystem.getInntektskategori() == null ? null : Inntektskategori.fraKode(fraFagsystem.getInntektskategori().getKode()))
                .medKilde(AndelKilde.fraKode(fraFagsystem.getKilde().getKode()))
                .medOrginalDagsatsFraTilstøtendeYtelse(fraFagsystem.getOrginalDagsatsFraTilstøtendeYtelse())
                .medAvkortetFørGraderingPrÅr(mapFraBeløp(fraFagsystem.getAvkortetFørGraderingPrÅr()));

        if (fraFagsystem.getBeregningsperiodeFom() != null) {
            builder.medBeregningsperiode(fraFagsystem.getBeregningsperiodeFom(), fraFagsystem.getBeregningsperiodeTom());
        }

        if (fraFagsystem.getPgiSnitt() != null) {
            builder.medPgi(mapFraBeløp(fraFagsystem.getPgiSnitt()), List.of(
                    mapFraBeløp(fraFagsystem.getPgi1()),
                    mapFraBeløp(fraFagsystem.getPgi2()),
                    mapFraBeløp(fraFagsystem.getPgi3())));
        }

        fraFagsystem.getBgAndelArbeidsforhold().ifPresent(bgAndelArbeidsforhold -> builder.medBGAndelArbeidsforhold(BGMapperTilKalkulus.magBGAndelArbeidsforhold(bgAndelArbeidsforhold)));
        return builder;
    }

    private static BGAndelArbeidsforholdDto.Builder magBGAndelArbeidsforhold(BGAndelArbeidsforhold fraFagsystem) {
        BGAndelArbeidsforholdDto.Builder builder = BGAndelArbeidsforholdDto.builder();
        builder.medArbeidsforholdRef(IAYMapperTilKalkulus.mapArbeidsforholdRef(fraFagsystem.getArbeidsforholdRef()));
        builder.medArbeidsgiver(IAYMapperTilKalkulus.mapArbeidsgiver(fraFagsystem.getArbeidsgiver()));
        builder.medArbeidsperiodeFom(fraFagsystem.getArbeidsperiodeFom());
        builder.medRefusjon(mapRefusjon(fraFagsystem.getRefusjon()));
        builder.medSaksbehandletRefusjonPrÅr(mapFraBeløp(fraFagsystem.getSaksbehandletRefusjonPrÅr()));
        builder.medFordeltRefusjonPrÅr(mapFraBeløp(fraFagsystem.getFordeltRefusjonPrÅr()));

        fraFagsystem.getArbeidsperiodeTom().ifPresent(builder::medArbeidsperiodeTom);
        fraFagsystem.getNaturalytelseBortfaltPrÅr().map(Beløp::getVerdi).ifPresent(builder::medNaturalytelseBortfaltPrÅr);
        fraFagsystem.getNaturalytelseTilkommetPrÅr().map(Beløp::getVerdi).ifPresent(builder::medNaturalytelseTilkommetPrÅr);
        return builder;
    }

    private static Refusjon mapRefusjon(no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.Refusjon refusjon) {
        if (refusjon == null) {
            return null;
        }
        return new Refusjon(
                mapFraBeløp(refusjon.getRefusjonskravPrÅr()),
                mapFraBeløp(refusjon.getSaksbehandletRefusjonPrÅr()),
                mapFraBeløp(refusjon.getFordeltRefusjonPrÅr()),
                refusjon.getHjemmelForRefusjonskravfrist(),
                refusjon.getRefusjonskravFristUtfall());
    }

    private static BigDecimal mapFraBeløp(Beløp beløp) {
        return beløp == null ? null : beløp.getVerdi();
    }

    private static BigDecimal mapFraPromille(Promille promille) {
        return promille == null ? null : promille.getVerdi();
    }

}
