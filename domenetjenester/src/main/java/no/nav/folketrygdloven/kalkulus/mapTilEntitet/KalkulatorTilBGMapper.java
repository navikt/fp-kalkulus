package no.nav.folketrygdloven.kalkulus.mapTilEntitet;

import java.math.BigDecimal;
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
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.Beløp;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.Promille;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.Refusjon;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.AndelKilde;
import no.nav.folketrygdloven.kalkulus.kodeverk.Hjemmel;
import no.nav.folketrygdloven.kalkulus.kodeverk.Inntektskategori;
import no.nav.folketrygdloven.kalkulus.kodeverk.OpptjeningAktivitetType;
import no.nav.folketrygdloven.kalkulus.kodeverk.PeriodeÅrsak;
import no.nav.folketrygdloven.kalkulus.kodeverk.SammenligningsgrunnlagType;


public class KalkulatorTilBGMapper {
    public static Sammenligningsgrunnlag mapSammenligningsgrunnlag(SammenligningsgrunnlagDto fraKalkulus) {
        Sammenligningsgrunnlag.Builder builder = Sammenligningsgrunnlag.builder();
        builder.medAvvikPromilleNy(mapTilPromille(fraKalkulus.getAvvikPromilleNy()));
        builder.medRapportertPrÅr(mapTilBeløp(fraKalkulus.getRapportertPrÅr()));
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
        builder.medAvkortetPrÅr(mapTilBeløp(fraKalkulus.getAvkortetPrÅr()));
        builder.medBeregningsgrunnlagPeriode(fraKalkulus.getBeregningsgrunnlagPeriodeFom(), fraKalkulus.getBeregningsgrunnlagPeriodeTom());
        builder.medBruttoPrÅr(mapTilBeløp(fraKalkulus.getBruttoPrÅr()));
        builder.medRedusertPrÅr(mapTilBeløp(fraKalkulus.getRedusertPrÅr()));

        //legg til
        fraKalkulus.getPeriodeÅrsaker().forEach(periodeÅrsak -> builder.leggTilPeriodeÅrsak(PeriodeÅrsak.fraKode(periodeÅrsak.getKode())));
        fraKalkulus.getBeregningsgrunnlagPrStatusOgAndelList().forEach(statusOgAndel -> builder.leggTilBeregningsgrunnlagPrStatusOgAndel(mapStatusOgAndel(statusOgAndel)));

        return builder;
    }

    public static SammenligningsgrunnlagPrStatus.Builder mapSammenligningsgrunnlagMedStatus(SammenligningsgrunnlagPrStatusDto fraKalkulus) {
        SammenligningsgrunnlagPrStatus.Builder builder = new SammenligningsgrunnlagPrStatus.Builder();
        builder.medAvvikPromilleNy(mapTilPromille(fraKalkulus.getAvvikPromilleNy()));
        builder.medRapportertPrÅr(mapTilBeløp(fraKalkulus.getRapportertPrÅr()));
        builder.medSammenligningsgrunnlagType(SammenligningsgrunnlagType.fraKode(fraKalkulus.getSammenligningsgrunnlagType().getKode()));
        builder.medSammenligningsperiode(fraKalkulus.getSammenligningsperiodeFom(), fraKalkulus.getSammenligningsperiodeTom());

        return builder;
    }

    private static BeregningsgrunnlagPrStatusOgAndel.Builder mapStatusOgAndel(BeregningsgrunnlagPrStatusOgAndelDto fraKalkulus) {
        BeregningsgrunnlagPrStatusOgAndel.Builder builder = BeregningsgrunnlagPrStatusOgAndel.builder()
                .medAktivitetStatus(AktivitetStatus.fraKode(fraKalkulus.getAktivitetStatus().getKode()))
                .medAndelsnr(fraKalkulus.getAndelsnr())
                .medArbforholdType(fraKalkulus.getArbeidsforholdType() == null ? null : OpptjeningAktivitetType.fraKode(fraKalkulus.getArbeidsforholdType().getKode()))
                .medAvkortetBrukersAndelPrÅr(mapTilBeløp(fraKalkulus.getAvkortetBrukersAndelPrÅr()))
                .medAvkortetPrÅr(mapTilBeløp(fraKalkulus.getAvkortetPrÅr()))
                .medAvkortetRefusjonPrÅr(mapTilBeløp(fraKalkulus.getAvkortetRefusjonPrÅr()))
                .medBeregnetPrÅr(mapTilBeløp(fraKalkulus.getBeregnetPrÅr()))
                .medBesteberegningPrÅr(mapTilBeløp(fraKalkulus.getBesteberegningPrÅr()))
                .medFastsattAvSaksbehandler(fraKalkulus.getFastsattAvSaksbehandler())
                .medKilde(AndelKilde.fraKode(fraKalkulus.getKilde().getKode()))
                .medOverstyrtPrÅr(mapTilBeløp(fraKalkulus.getOverstyrtPrÅr()))
                .medFordeltPrÅr(mapTilBeløp(fraKalkulus.getFordeltPrÅr()))
                .medRedusertPrÅr(mapTilBeløp(fraKalkulus.getRedusertPrÅr()))
                .medRedusertBrukersAndelPrÅr(mapTilBeløp(fraKalkulus.getRedusertBrukersAndelPrÅr()))
                .medMaksimalRefusjonPrÅr(mapTilBeløp(fraKalkulus.getMaksimalRefusjonPrÅr()))
                .medRedusertRefusjonPrÅr(mapTilBeløp(fraKalkulus.getRedusertRefusjonPrÅr()))
                .medÅrsbeløpFraTilstøtendeYtelse(mapTilBeløp(fraKalkulus.getÅrsbeløpFraTilstøtendeYtelse() == null ? null : fraKalkulus.getÅrsbeløpFraTilstøtendeYtelse().getVerdi()))
                .medInntektskategori(fraKalkulus.getInntektskategori() == null ? null : Inntektskategori.fraKode(fraKalkulus.getInntektskategori().getKode()))
                .medOrginalDagsatsFraTilstøtendeYtelse(fraKalkulus.getOrginalDagsatsFraTilstøtendeYtelse())
                .medAvkortetFørGraderingPrÅr(mapTilBeløp(fraKalkulus.getAvkortetFørGraderingPrÅr()));

        if (fraKalkulus.getBeregningsperiodeFom() != null) {
            builder.medBeregningsperiode(fraKalkulus.getBeregningsperiodeFom(), fraKalkulus.getBeregningsperiodeTom());
        }

        if (fraKalkulus.getPgiSnitt() != null) {
            builder.medPgi(mapTilBeløp(fraKalkulus.getPgiSnitt()), List.of(mapTilBeløp(fraKalkulus.getPgi1()), mapTilBeløp(fraKalkulus.getPgi2()), mapTilBeløp(fraKalkulus.getPgi3())));
        }

        fraKalkulus.getBgAndelArbeidsforhold().ifPresent(bgAndelArbeidsforhold -> builder.medBGAndelArbeidsforhold(KalkulatorTilBGMapper.mapBGAndelArbeidsforhold(bgAndelArbeidsforhold)));
        return builder;
    }

    private static Beløp mapTilBeløp(BigDecimal beløp) {
        return beløp == null ? null : new Beløp(beløp);
    }

    private static Promille mapTilPromille(BigDecimal verdi) {
        return verdi == null ? null : new Promille(verdi);
    }

    private static BGAndelArbeidsforhold.Builder mapBGAndelArbeidsforhold(BGAndelArbeidsforholdDto fraKalkulus) {
        BGAndelArbeidsforhold.Builder builder = BGAndelArbeidsforhold.builder();
        builder.medArbeidsforholdRef(KalkulatorTilIAYMapper.mapArbeidsforholdRef(fraKalkulus.getArbeidsforholdRef()));
        builder.medArbeidsgiver(KalkulatorTilIAYMapper.mapArbeidsgiver(fraKalkulus.getArbeidsgiver()));
        builder.medArbeidsperiodeFom(fraKalkulus.getArbeidsperiodeFom());
        builder.medRefusjon(fraKalkulus.getRefusjon().map(KalkulatorTilBGMapper::mapRefusjon).orElse(null));
        fraKalkulus.getArbeidsperiodeTom().ifPresent(builder::medArbeidsperiodeTom);
        fraKalkulus.getNaturalytelseBortfaltPrÅr().map(KalkulatorTilBGMapper::mapTilBeløp).ifPresent(builder::medNaturalytelseBortfaltPrÅr);
        fraKalkulus.getNaturalytelseTilkommetPrÅr().map(KalkulatorTilBGMapper::mapTilBeløp).ifPresent(builder::medNaturalytelseTilkommetPrÅr);
        return builder;
    }

    private static Refusjon mapRefusjon(no.nav.folketrygdloven.kalkulator.modell.typer.Refusjon refusjon) {
        if (!harRefusjon(refusjon)) {
            return null;
        }
        return new Refusjon(
                mapTilBeløp(refusjon.getRefusjonskravPrÅr()),
                mapTilBeløp(refusjon.getSaksbehandletRefusjonPrÅr()),
                mapTilBeløp(refusjon.getFordeltRefusjonPrÅr()),
                refusjon.getHjemmelForRefusjonskravfrist(),
                refusjon.getRefusjonskravFristUtfall()
                );
    }

    private static boolean harRefusjon(no.nav.folketrygdloven.kalkulator.modell.typer.Refusjon refusjon) {
        if (refusjon == null) {
            return false;
        }
        return refusjon.getGjeldendeRefusjonPrÅr() != null;
    }


}
