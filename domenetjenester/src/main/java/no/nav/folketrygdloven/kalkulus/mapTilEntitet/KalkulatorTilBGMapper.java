package no.nav.folketrygdloven.kalkulus.mapTilEntitet;

import java.util.List;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagAktivitetStatusDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.SammenligningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.SammenligningsgrunnlagPrStatusDto;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BGAndelArbeidsforhold;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagAktivitetStatus;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndel;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.Sammenligningsgrunnlag;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.SammenligningsgrunnlagPrStatus;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.AndelKilde;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.Hjemmel;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.Inntektskategori;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.OpptjeningAktivitetType;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.PeriodeÅrsak;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.SammenligningsgrunnlagType;


public class KalkulatorTilBGMapper {
    public static Sammenligningsgrunnlag mapSammenligningsgrunnlag(SammenligningsgrunnlagDto fraKalkulus) {
        Sammenligningsgrunnlag.Builder builder = Sammenligningsgrunnlag.builder();
        builder.medAvvikPromilleNy(fraKalkulus.getAvvikPromilleNy());
        builder.medRapportertPrÅr(fraKalkulus.getRapportertPrÅr());
        builder.medSammenligningsperiode(fraKalkulus.getSammenligningsperiodeFom(), fraKalkulus.getSammenligningsperiodeTom());
        return builder.build();
    }

    public static BeregningsgrunnlagAktivitetStatus.Builder mapAktivitetStatus(BeregningsgrunnlagAktivitetStatusDto fraKalkulus) {
        BeregningsgrunnlagAktivitetStatus.Builder builder = new BeregningsgrunnlagAktivitetStatus.Builder();
        builder.medAktivitetStatus(AktivitetStatus.fraKode(fraKalkulus.getAktivitetStatus().getKode()));
        builder.medHjemmel(Hjemmel.fraKode(fraKalkulus.getHjemmel().getKode()));

        return builder;
    }

    public static BeregningsgrunnlagPeriode.Builder mapBeregningsgrunnlagPeriode(BeregningsgrunnlagPeriodeDto fraKalkulus) {
        BeregningsgrunnlagPeriode.Builder builder = new BeregningsgrunnlagPeriode.Builder();

        //med
        builder.medAvkortetPrÅr(fraKalkulus.getAvkortetPrÅr());
        builder.medBeregningsgrunnlagPeriode(fraKalkulus.getBeregningsgrunnlagPeriodeFom(), fraKalkulus.getBeregningsgrunnlagPeriodeTom());
        builder.medBruttoPrÅr(fraKalkulus.getBruttoPrÅr());
        builder.medRedusertPrÅr(fraKalkulus.getRedusertPrÅr());

        //legg til
        fraKalkulus.getPeriodeÅrsaker().forEach(periodeÅrsak -> builder.leggTilPeriodeÅrsak(PeriodeÅrsak.fraKode(periodeÅrsak.getKode())));
        fraKalkulus.getBeregningsgrunnlagPrStatusOgAndelList().forEach(statusOgAndel -> builder.leggTilBeregningsgrunnlagPrStatusOgAndel(mapStatusOgAndel(statusOgAndel)));

        return builder;
    }

    public static SammenligningsgrunnlagPrStatus.Builder mapSammenligningsgrunnlagMedStatus(SammenligningsgrunnlagPrStatusDto fraKalkulus) {
        SammenligningsgrunnlagPrStatus.Builder builder = new SammenligningsgrunnlagPrStatus.Builder();
        builder.medAvvikPromilleNy(fraKalkulus.getAvvikPromilleNy());
        builder.medRapportertPrÅr(fraKalkulus.getRapportertPrÅr());
        builder.medSammenligningsgrunnlagType(SammenligningsgrunnlagType.fraKode(fraKalkulus.getSammenligningsgrunnlagType().getKode()));
        builder.medSammenligningsperiode(fraKalkulus.getSammenligningsperiodeFom(), fraKalkulus.getSammenligningsperiodeTom());

        return builder;
    }

    private static BeregningsgrunnlagPrStatusOgAndel.Builder mapStatusOgAndel(BeregningsgrunnlagPrStatusOgAndelDto fraKalkulus) {
        BeregningsgrunnlagPrStatusOgAndel.Builder builder = BeregningsgrunnlagPrStatusOgAndel.builder()
                .medAktivitetStatus(AktivitetStatus.fraKode(fraKalkulus.getAktivitetStatus().getKode()))
                .medAndelsnr(fraKalkulus.getAndelsnr())
                .medArbforholdType(fraKalkulus.getArbeidsforholdType() == null ? null : OpptjeningAktivitetType.fraKode(fraKalkulus.getArbeidsforholdType().getKode()))
                .medAvkortetBrukersAndelPrÅr(fraKalkulus.getAvkortetBrukersAndelPrÅr())
                .medAvkortetPrÅr(fraKalkulus.getAvkortetPrÅr())
                .medAvkortetRefusjonPrÅr(fraKalkulus.getAvkortetRefusjonPrÅr())
                .medBeregnetPrÅr(fraKalkulus.getBeregnetPrÅr())
                .medBesteberegningPrÅr(fraKalkulus.getBesteberegningPrÅr())
                .medFastsattAvSaksbehandler(fraKalkulus.getFastsattAvSaksbehandler())
                .medKilde(AndelKilde.fraKode(fraKalkulus.getKilde().getKode()))
                .medOverstyrtPrÅr(fraKalkulus.getOverstyrtPrÅr())
                .medFordeltPrÅr(fraKalkulus.getFordeltPrÅr())
                .medRedusertPrÅr(fraKalkulus.getRedusertPrÅr())
                .medRedusertBrukersAndelPrÅr(fraKalkulus.getRedusertBrukersAndelPrÅr())
                .medMaksimalRefusjonPrÅr(fraKalkulus.getMaksimalRefusjonPrÅr())
                .medRedusertRefusjonPrÅr(fraKalkulus.getRedusertRefusjonPrÅr())
                .medÅrsbeløpFraTilstøtendeYtelse(fraKalkulus.getÅrsbeløpFraTilstøtendeYtelse() == null ? null : fraKalkulus.getÅrsbeløpFraTilstøtendeYtelse().getVerdi())
                .medInntektskategori(fraKalkulus.getInntektskategori() == null ? null : Inntektskategori.fraKode(fraKalkulus.getInntektskategori().getKode()))
                .medOrginalDagsatsFraTilstøtendeYtelse(fraKalkulus.getOrginalDagsatsFraTilstøtendeYtelse());

        if (fraKalkulus.getBeregningsperiodeFom() != null) {
            builder.medBeregningsperiode(fraKalkulus.getBeregningsperiodeFom(), fraKalkulus.getBeregningsperiodeTom());
        }

        if (fraKalkulus.getPgiSnitt() != null) {
            builder.medPgi(fraKalkulus.getPgiSnitt(), List.of(fraKalkulus.getPgi1(), fraKalkulus.getPgi2(), fraKalkulus.getPgi3()));
        }

        fraKalkulus.getBgAndelArbeidsforhold().ifPresent(bgAndelArbeidsforhold -> builder.medBGAndelArbeidsforhold(KalkulatorTilBGMapper.mapBGAndelArbeidsforhold(bgAndelArbeidsforhold)));
        return builder;
    }

    private static BGAndelArbeidsforhold.Builder mapBGAndelArbeidsforhold(BGAndelArbeidsforholdDto fraKalkulus) {
        BGAndelArbeidsforhold.Builder builder = BGAndelArbeidsforhold.builder();
        builder.medArbeidsforholdRef(KalkulatorTilIAYMapper.mapArbeidsforholdRef(fraKalkulus.getArbeidsforholdRef()));
        builder.medArbeidsgiver(KalkulatorTilIAYMapper.mapArbeidsgiver(fraKalkulus.getArbeidsgiver()));
        builder.medArbeidsperiodeFom(fraKalkulus.getArbeidsperiodeFom());
        builder.medRefusjonskravPrÅr(fraKalkulus.getRefusjonskravPrÅr());
        builder.medSaksbehandletRefusjonPrÅr(fraKalkulus.getSaksbehandletRefusjonPrÅr());
        builder.medFordeltRefusjonPrÅr(fraKalkulus.getFordeltRefusjonPrÅr());
        builder.medHjemmel(fraKalkulus.getHjemmelForRefusjonskravfrist() == null
                || fraKalkulus.getHjemmelForRefusjonskravfrist().equals(Hjemmel.UDEFINERT) ?
                null : fraKalkulus.getHjemmelForRefusjonskravfrist());

        fraKalkulus.getArbeidsperiodeTom().ifPresent(builder::medArbeidsperiodeTom);
        fraKalkulus.getNaturalytelseBortfaltPrÅr().ifPresent(builder::medNaturalytelseBortfaltPrÅr);
        fraKalkulus.getNaturalytelseTilkommetPrÅr().ifPresent(builder::medNaturalytelseTilkommetPrÅr);
        return builder;
    }
}
