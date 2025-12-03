package no.nav.folketrygdloven.kalkulus.domene.mapFraEntitet;

import java.math.BigDecimal;
import java.util.List;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagAktivitetStatusDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.SammenligningsgrunnlagPrStatusDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Refusjon;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.AndelArbeidsforholdEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagAktivitetStatusEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagPeriodeEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagAndelEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.SammenligningsgrunnlagPrStatusEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.Beløp;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.Promille;
import no.nav.folketrygdloven.kalkulus.domene.mappers.VerdityperMapper;


public class BGMapperTilKalkulus {
    public static BeregningsgrunnlagAktivitetStatusDto.Builder mapAktivitetStatus(BeregningsgrunnlagAktivitetStatusEntitet fraFagsystem) {
        BeregningsgrunnlagAktivitetStatusDto.Builder builder = new BeregningsgrunnlagAktivitetStatusDto.Builder();
        builder.medAktivitetStatus(fraFagsystem.getAktivitetStatus());
        builder.medHjemmel(fraFagsystem.getHjemmel());

        return builder;
    }

    public static BeregningsgrunnlagPeriodeDto.Builder mapBeregningsgrunnlagPeriode(BeregningsgrunnlagPeriodeEntitet fraFagsystem) {
        BeregningsgrunnlagPeriodeDto.Builder builder = new BeregningsgrunnlagPeriodeDto.Builder();

        //med
        builder.medAvkortetPrÅr(mapFraBeløp(fraFagsystem.getAvkortetPrÅr()));
        builder.medBeregningsgrunnlagPeriode(fraFagsystem.getBeregningsgrunnlagPeriodeFom(), fraFagsystem.getBeregningsgrunnlagPeriodeTom());
        builder.medBruttoPrÅr(mapFraBeløp(fraFagsystem.getBruttoPrÅr()));
        builder.medRedusertPrÅr(mapFraBeløp(fraFagsystem.getRedusertPrÅr()));
        builder.medInntektsgraderingsprosentBrutto(fraFagsystem.getInntektgraderingsprosentBrutto() != null ? fraFagsystem.getInntektgraderingsprosentBrutto().getVerdi() : null);
        builder.medTotalUtbetalingsgradFraUttak(fraFagsystem.getTotalUtbetalingsgradFraUttak());

        //legg til
        fraFagsystem.getPeriodeÅrsaker().forEach(builder::leggTilPeriodeÅrsak);
        fraFagsystem.getBeregningsgrunnlagAndelList().forEach(statusOgAndel -> builder.leggTilBeregningsgrunnlagPrStatusOgAndel(mapStatusOgAndel(statusOgAndel)));
        return builder;
    }

    public static SammenligningsgrunnlagPrStatusDto mapSammenligningsgrunnlagMedStatus(SammenligningsgrunnlagPrStatusEntitet fraFagsystem) {
        SammenligningsgrunnlagPrStatusDto.Builder builder = new SammenligningsgrunnlagPrStatusDto.Builder();
        builder.medAvvikPromilleNy(mapFraPromille(fraFagsystem.getAvvikPromille()));
        builder.medRapportertPrÅr(mapFraBeløp(fraFagsystem.getRapportertPrÅr()));
        builder.medSammenligningsgrunnlagType(fraFagsystem.getSammenligningsgrunnlagType());
        builder.medSammenligningsperiode(fraFagsystem.getSammenligningsperiodeFom(), fraFagsystem.getSammenligningsperiodeTom());
        return builder.build();
    }

    public static BeregningsgrunnlagPrStatusOgAndelDto.Builder mapStatusOgAndel(BeregningsgrunnlagAndelEntitet fraFagsystem) {
        BeregningsgrunnlagPrStatusOgAndelDto.Builder builder = BeregningsgrunnlagPrStatusOgAndelDto.ny()
                .medAktivitetStatus(fraFagsystem.getAktivitetStatus())
                .medAndelsnr(fraFagsystem.getAndelsnr())
                .medArbforholdType(fraFagsystem.getArbeidsforholdType())
                .medAvkortetBrukersAndelPrÅr(mapFraBeløp(fraFagsystem.getAvkortetBrukersAndelPrÅr()))
                .medAvkortetPrÅr(mapFraBeløp(fraFagsystem.getAvkortetPrÅr()))
                .medAvkortetRefusjonPrÅr(mapFraBeløp(fraFagsystem.getAvkortetRefusjonPrÅr()))
                .medBeregnetPrÅr(mapFraBeløp(fraFagsystem.getBeregnetPrÅr()))
                .medBesteberegningPrÅr(mapFraBeløp(fraFagsystem.getBesteberegningPrÅr()))
                .medFastsattAvSaksbehandler(fraFagsystem.getFastsattAvSaksbehandler())
                .medOverstyrtPrÅr(mapFraBeløp(fraFagsystem.getOverstyrtPrÅr()))
                .medFordeltPrÅr(mapFraBeløp(fraFagsystem.getFordeltPrÅr()))
                .medManueltFordeltPrÅr(mapFraBeløp(fraFagsystem.getManueltFordeltPrÅr()))
                .medRedusertPrÅr(mapFraBeløp(fraFagsystem.getRedusertPrÅr()))
                .medRedusertBrukersAndelPrÅr(mapFraBeløp(fraFagsystem.getRedusertBrukersAndelPrÅr()))
                .medMaksimalRefusjonPrÅr(mapFraBeløp(fraFagsystem.getMaksimalRefusjonPrÅr()))
                .medRedusertRefusjonPrÅr(mapFraBeløp(fraFagsystem.getRedusertRefusjonPrÅr()))
                .medÅrsbeløpFraTilstøtendeYtelse(mapFraBeløp(fraFagsystem.getÅrsbeløpFraTilstøtendeYtelse()))
                .medInntektskategori(fraFagsystem.getInntektskategori())
                .medKilde(fraFagsystem.getKilde())
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

        fraFagsystem.getAndelArbeidsforhold().ifPresent(bgAndelArbeidsforhold -> builder.medBGAndelArbeidsforhold(BGMapperTilKalkulus.magBGAndelArbeidsforhold(bgAndelArbeidsforhold)));
        return builder;
    }

    private static BGAndelArbeidsforholdDto.Builder magBGAndelArbeidsforhold(AndelArbeidsforholdEntitet fraFagsystem) {
        BGAndelArbeidsforholdDto.Builder builder = BGAndelArbeidsforholdDto.builder();
        builder.medArbeidsforholdRef(IAYMapperTilKalkulus.mapArbeidsforholdRef(fraFagsystem.getArbeidsforholdRef()));
        builder.medArbeidsgiver(IAYMapperTilKalkulus.mapArbeidsgiver(fraFagsystem.getArbeidsgiver()));
        builder.medArbeidsperiodeFom(fraFagsystem.getArbeidsperiodeFom());
        builder.medRefusjon(mapRefusjon(fraFagsystem.getRefusjon()));
        builder.medSaksbehandletRefusjonPrÅr(mapFraBeløp(fraFagsystem.getSaksbehandletRefusjonPrÅr()));
        builder.medFordeltRefusjonPrÅr(mapFraBeløp(fraFagsystem.getFordeltRefusjonPrÅr()));

        fraFagsystem.getArbeidsperiodeTom().ifPresent(builder::medArbeidsperiodeTom);
        fraFagsystem.getNaturalytelseBortfaltPrÅr().map(VerdityperMapper::beløpFraDao).ifPresent(builder::medNaturalytelseBortfaltPrÅr);
        fraFagsystem.getNaturalytelseTilkommetPrÅr().map(VerdityperMapper::beløpFraDao).ifPresent(builder::medNaturalytelseTilkommetPrÅr);
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
                mapFraBeløp(refusjon.getManueltFordeltRefusjonPrÅr()),
                refusjon.getHjemmelForRefusjonskravfrist(),
                refusjon.getRefusjonskravFristUtfall());
    }

    private static no.nav.folketrygdloven.kalkulator.modell.typer.Beløp mapFraBeløp(Beløp beløp) {
        return VerdityperMapper.beløpFraDao(beløp);
    }

    private static BigDecimal mapFraPromille(Promille promille) {
        return promille == null ? null : promille.getVerdi();
    }
}
