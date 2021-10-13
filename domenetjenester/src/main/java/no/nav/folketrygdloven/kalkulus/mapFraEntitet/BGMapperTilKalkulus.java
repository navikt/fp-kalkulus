package no.nav.folketrygdloven.kalkulus.mapFraEntitet;

import java.util.List;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagAktivitetStatusDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.SammenligningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.SammenligningsgrunnlagPrStatusDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.OpptjeningAktivitetType;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BGAndelArbeidsforhold;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagAktivitetStatus;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndel;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.Sammenligningsgrunnlag;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.SammenligningsgrunnlagPrStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.AndelKilde;
import no.nav.folketrygdloven.kalkulus.kodeverk.Hjemmel;
import no.nav.folketrygdloven.kalkulus.kodeverk.Inntektskategori;
import no.nav.folketrygdloven.kalkulus.kodeverk.PeriodeÅrsak;
import no.nav.folketrygdloven.kalkulus.kodeverk.SammenligningsgrunnlagType;


public class BGMapperTilKalkulus {
    public static SammenligningsgrunnlagDto mapSammenligningsgrunnlag(Sammenligningsgrunnlag fraFagsystem) {
        SammenligningsgrunnlagDto.Builder builder = SammenligningsgrunnlagDto.builder();
        builder.medAvvikPromilleNy(fraFagsystem.getAvvikPromilleNy());
        builder.medRapportertPrÅr(fraFagsystem.getRapportertPrÅr());
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
        builder.medAvkortetPrÅr(fraFagsystem.getAvkortetPrÅr());
        builder.medBeregningsgrunnlagPeriode(fraFagsystem.getBeregningsgrunnlagPeriodeFom(), fraFagsystem.getBeregningsgrunnlagPeriodeTom());
        builder.medBruttoPrÅr(fraFagsystem.getBruttoPrÅr());
        builder.medRedusertPrÅr(fraFagsystem.getRedusertPrÅr());

        //legg til
        fraFagsystem.getPeriodeÅrsaker().forEach(periodeÅrsak -> builder.leggTilPeriodeÅrsak(PeriodeÅrsak.fraKode(periodeÅrsak.getKode())));
        fraFagsystem.getBeregningsgrunnlagPrStatusOgAndelList().forEach(statusOgAndel -> builder.leggTilBeregningsgrunnlagPrStatusOgAndel(mapStatusOgAndel(statusOgAndel)));

        return builder;
    }

    public static SammenligningsgrunnlagPrStatusDto.Builder mapSammenligningsgrunnlagMedStatus(SammenligningsgrunnlagPrStatus fraFagsystem) {
        SammenligningsgrunnlagPrStatusDto.Builder builder = new SammenligningsgrunnlagPrStatusDto.Builder();
        builder.medAvvikPromilleNy(fraFagsystem.getAvvikPromilleNy());
        builder.medRapportertPrÅr(fraFagsystem.getRapportertPrÅr());
        builder.medSammenligningsgrunnlagType(SammenligningsgrunnlagType.fraKode(fraFagsystem.getSammenligningsgrunnlagType().getKode()));
        builder.medSammenligningsperiode(fraFagsystem.getSammenligningsperiodeFom(), fraFagsystem.getSammenligningsperiodeTom());

        return builder;
    }

    private static BeregningsgrunnlagPrStatusOgAndelDto.Builder mapStatusOgAndel(BeregningsgrunnlagPrStatusOgAndel fraFagsystem) {
        BeregningsgrunnlagPrStatusOgAndelDto.Builder builder = BeregningsgrunnlagPrStatusOgAndelDto.ny()
                .medAktivitetStatus(AktivitetStatus.fraKode(fraFagsystem.getAktivitetStatus().getKode()))
                .medAndelsnr(fraFagsystem.getAndelsnr())
                .medArbforholdType(fraFagsystem.getArbeidsforholdType() == null ? null : OpptjeningAktivitetType.fraKode(fraFagsystem.getArbeidsforholdType().getKode()))
                .medAvkortetBrukersAndelPrÅr(fraFagsystem.getAvkortetBrukersAndelPrÅr())
                .medAvkortetPrÅr(fraFagsystem.getAvkortetPrÅr())
                .medAvkortetRefusjonPrÅr(fraFagsystem.getAvkortetRefusjonPrÅr())
                .medBeregnetPrÅr(fraFagsystem.getBeregnetPrÅr())
                .medBesteberegningPrÅr(fraFagsystem.getBesteberegningPrÅr())
                .medFastsattAvSaksbehandler(fraFagsystem.getFastsattAvSaksbehandler())
                .medOverstyrtPrÅr(fraFagsystem.getOverstyrtPrÅr())
                .medFordeltPrÅr(fraFagsystem.getFordeltPrÅr())
                .medRedusertPrÅr(fraFagsystem.getRedusertPrÅr())
                .medRedusertBrukersAndelPrÅr(fraFagsystem.getRedusertBrukersAndelPrÅr())
                .medMaksimalRefusjonPrÅr(fraFagsystem.getMaksimalRefusjonPrÅr())
                .medRedusertRefusjonPrÅr(fraFagsystem.getRedusertRefusjonPrÅr())
                .medÅrsbeløpFraTilstøtendeYtelse(fraFagsystem.getÅrsbeløpFraTilstøtendeYtelse() == null ? null : fraFagsystem.getÅrsbeløpFraTilstøtendeYtelse().getVerdi())
                .medInntektskategori(fraFagsystem.getInntektskategori() == null ? null : Inntektskategori.fraKode(fraFagsystem.getInntektskategori().getKode()))
                .medKilde(AndelKilde.fraKode(fraFagsystem.getKilde().getKode()))
                .medOrginalDagsatsFraTilstøtendeYtelse(fraFagsystem.getOrginalDagsatsFraTilstøtendeYtelse())
                .medAvkortetFørGraderingPrÅr(fraFagsystem.getAvkortetFørGraderingPrÅr());

        if (fraFagsystem.getBeregningsperiodeFom() != null) {
            builder.medBeregningsperiode(fraFagsystem.getBeregningsperiodeFom(), fraFagsystem.getBeregningsperiodeTom());
        }

        if (fraFagsystem.getPgiSnitt() != null) {
            builder.medPgi(fraFagsystem.getPgiSnitt(), List.of(fraFagsystem.getPgi1(), fraFagsystem.getPgi2(), fraFagsystem.getPgi3()));
        }

        fraFagsystem.getBgAndelArbeidsforhold().ifPresent(bgAndelArbeidsforhold -> builder.medBGAndelArbeidsforhold(BGMapperTilKalkulus.magBGAndelArbeidsforhold(bgAndelArbeidsforhold)));
        return builder;
    }

    private static BGAndelArbeidsforholdDto.Builder magBGAndelArbeidsforhold(BGAndelArbeidsforhold fraFagsystem) {
        BGAndelArbeidsforholdDto.Builder builder = BGAndelArbeidsforholdDto.builder();
        builder.medArbeidsforholdRef(IAYMapperTilKalkulus.mapArbeidsforholdRef(fraFagsystem.getArbeidsforholdRef()));
        builder.medArbeidsgiver(IAYMapperTilKalkulus.mapArbeidsgiver(fraFagsystem.getArbeidsgiver()));
        builder.medArbeidsperiodeFom(fraFagsystem.getArbeidsperiodeFom());
        builder.medRefusjonskravPrÅr(fraFagsystem.getRefusjonskravPrÅr());
        builder.medSaksbehandletRefusjonPrÅr(fraFagsystem.getSaksbehandletRefusjonPrÅr());
        builder.medFordeltRefusjonPrÅr(fraFagsystem.getFordeltRefusjonPrÅr());
        builder.medHjemmel(fraFagsystem.getHjemmelForRefusjonskravfrist() == null ? Hjemmel.UDEFINERT : Hjemmel.fraKode(fraFagsystem.getHjemmelForRefusjonskravfrist().getKode()));

        fraFagsystem.getArbeidsperiodeTom().ifPresent(builder::medArbeidsperiodeTom);
        fraFagsystem.getNaturalytelseBortfaltPrÅr().ifPresent(builder::medNaturalytelseBortfaltPrÅr);
        fraFagsystem.getNaturalytelseTilkommetPrÅr().ifPresent(builder::medNaturalytelseTilkommetPrÅr);
        return builder;
    }
}
