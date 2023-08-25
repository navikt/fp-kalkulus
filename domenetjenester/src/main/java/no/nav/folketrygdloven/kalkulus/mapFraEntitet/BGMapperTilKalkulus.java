package no.nav.folketrygdloven.kalkulus.mapFraEntitet;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagAktivitetStatusDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.SammenligningsgrunnlagPrStatusDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.TilkommetInntektDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Refusjon;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BGAndelArbeidsforhold;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagAktivitetStatus;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndel;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.SammenligningsgrunnlagPrStatus;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.TilkommetInntekt;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.Beløp;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.Promille;


public class BGMapperTilKalkulus {
    public static BeregningsgrunnlagAktivitetStatusDto.Builder mapAktivitetStatus(BeregningsgrunnlagAktivitetStatus fraFagsystem) {
        BeregningsgrunnlagAktivitetStatusDto.Builder builder = new BeregningsgrunnlagAktivitetStatusDto.Builder();
        builder.medAktivitetStatus(fraFagsystem.getAktivitetStatus());
        builder.medHjemmel(fraFagsystem.getHjemmel());

        return builder;
    }

    public static BeregningsgrunnlagPeriodeDto.Builder mapBeregningsgrunnlagPeriode(BeregningsgrunnlagPeriode fraFagsystem) {
        BeregningsgrunnlagPeriodeDto.Builder builder = new BeregningsgrunnlagPeriodeDto.Builder();

        //med
        builder.medAvkortetPrÅr(mapFraBeløp(fraFagsystem.getAvkortetPrÅr()));
        builder.medBeregningsgrunnlagPeriode(fraFagsystem.getBeregningsgrunnlagPeriodeFom(), fraFagsystem.getBeregningsgrunnlagPeriodeTom());
        builder.medBruttoPrÅr(mapFraBeløp(fraFagsystem.getBruttoPrÅr()));
        builder.medRedusertPrÅr(mapFraBeløp(fraFagsystem.getRedusertPrÅr()));
        builder.medInntektsgraderingsprosentBrutto(fraFagsystem.getInntektgraderingsprosentBrutto() != null ? fraFagsystem.getInntektgraderingsprosentBrutto().getVerdi() : null);
        builder.medTotalUtbetalingsgradFraUttak(fraFagsystem.getTotalUtbetalingsgradFraUttak());
        builder.medTotalUtbetalingsgradEtterReduksjonVedTilkommetInntekt(fraFagsystem.getTotalUtbetalingsgradEtterReduksjonVedTilkommetInntekt());

        //legg til
        fraFagsystem.getPeriodeÅrsaker().forEach(builder::leggTilPeriodeÅrsak);
        fraFagsystem.getBeregningsgrunnlagPrStatusOgAndelList().forEach(statusOgAndel -> builder.leggTilBeregningsgrunnlagPrStatusOgAndel(mapStatusOgAndel(statusOgAndel)));
        fraFagsystem.getTilkomneInntekter().forEach(it -> builder.leggTilTilkommetInntekt(mapTilkommetInntekt(it)));
        return builder;
    }

    public static SammenligningsgrunnlagPrStatusDto mapSammenligningsgrunnlagMedStatus(SammenligningsgrunnlagPrStatus fraFagsystem) {
        SammenligningsgrunnlagPrStatusDto.Builder builder = new SammenligningsgrunnlagPrStatusDto.Builder();
        builder.medAvvikPromilleNy(mapFraPromille(fraFagsystem.getGjeldendeAvvik()));
        builder.medRapportertPrÅr(mapFraBeløp(fraFagsystem.getRapportertPrÅr()));
        builder.medSammenligningsgrunnlagType(fraFagsystem.getSammenligningsgrunnlagType());
        builder.medSammenligningsperiode(fraFagsystem.getSammenligningsperiodeFom(), fraFagsystem.getSammenligningsperiodeTom());
        return builder.build();
    }

    public static BeregningsgrunnlagPrStatusOgAndelDto.Builder mapStatusOgAndel(BeregningsgrunnlagPrStatusOgAndel fraFagsystem) {
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

    public static List<SammenligningsgrunnlagPrStatusDto> mapGammeltTilNyttSammenligningsgrunnlag(BeregningsgrunnlagEntitet beregningsgrunnlagEntitet) {
        if (beregningsgrunnlagEntitet.getSammenligningsgrunnlag().isEmpty()) {
            return Collections.emptyList();
        }
        var gammeltSG = beregningsgrunnlagEntitet.getSammenligningsgrunnlag().get();
        var sammenligningsgrunnlagType = SammenligningTypeMapper.finnSammenligningtypeFraAktivitetstatus(beregningsgrunnlagEntitet);
        return Collections.singletonList(SammenligningsgrunnlagPrStatusDto.builder()
                .medSammenligningsgrunnlagType(sammenligningsgrunnlagType)
                .medSammenligningsperiode(gammeltSG.getSammenligningsperiodeFom(), gammeltSG.getSammenligningsperiodeTom())
                .medAvvikPromilleNy(mapFraPromille(gammeltSG.getAvvikPromilleNy()))
                .medRapportertPrÅr(mapFraBeløp(gammeltSG.getRapportertPrÅr()))
                .build());

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

    private static TilkommetInntektDto mapTilkommetInntekt(TilkommetInntekt it) {
        return new TilkommetInntektDto(
                it.getAktivitetStatus(),
                it.getArbeidsgiver().map(IAYMapperTilKalkulus::mapArbeidsgiver).orElse(null),
                IAYMapperTilKalkulus.mapArbeidsforholdRef(it.getArbeidsforholdRef()),
                mapFraBeløp(it.getBruttoInntektPrÅr()),
                mapFraBeløp(it.getTilkommetInntektPrÅr()),
                it.skalRedusereUtbetaling());
    }

    private static BigDecimal mapFraBeløp(Beløp beløp) {
        return beløp == null ? null : beløp.getVerdi();
    }

    private static BigDecimal mapFraPromille(Promille promille) {
        return promille == null ? null : promille.getVerdi();
    }

}
