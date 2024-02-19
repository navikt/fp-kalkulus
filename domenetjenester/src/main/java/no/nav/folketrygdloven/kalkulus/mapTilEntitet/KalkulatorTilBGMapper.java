package no.nav.folketrygdloven.kalkulus.mapTilEntitet;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagAktivitetStatusDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.SammenligningsgrunnlagPrStatusDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.TilkommetInntektDto;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BGAndelArbeidsforhold;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagAktivitetStatus;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndel;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.SammenligningsgrunnlagPrStatus;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.TilkommetInntekt;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.Beløp;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.FastsattInntektskategori;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.Promille;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.Prosent;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.Refusjon;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.Årsgrunnlag;


public class KalkulatorTilBGMapper {
    public static BeregningsgrunnlagAktivitetStatus.Builder mapAktivitetStatus(BeregningsgrunnlagAktivitetStatusDto fraKalkulus) {
        BeregningsgrunnlagAktivitetStatus.Builder builder = new BeregningsgrunnlagAktivitetStatus.Builder();
        builder.medAktivitetStatus(fraKalkulus.getAktivitetStatus());
        builder.medHjemmel(fraKalkulus.getHjemmel());

        return builder;
    }

    public static BeregningsgrunnlagPeriode.Builder mapBeregningsgrunnlagPeriode(BeregningsgrunnlagPeriodeDto fraKalkulus) {
        BeregningsgrunnlagPeriode.Builder builder = new BeregningsgrunnlagPeriode.Builder();

        //med
        builder.medAvkortetPrÅr(Beløp.fraKalkulatorBeløp(fraKalkulus.getAvkortetPrÅr()));
        builder.medBeregningsgrunnlagPeriode(fraKalkulus.getBeregningsgrunnlagPeriodeFom(), fraKalkulus.getBeregningsgrunnlagPeriodeTom());
        builder.medBruttoPrÅr(Beløp.fraKalkulatorBeløp(fraKalkulus.getBruttoPrÅr()));
        builder.medRedusertPrÅr(Beløp.fraKalkulatorBeløp(fraKalkulus.getRedusertPrÅr()));
        builder.medInntektGraderingsprosentBrutto(fraKalkulus.getInntektgraderingsprosentBrutto() != null ? new Prosent(fraKalkulus.getInntektgraderingsprosentBrutto()) : null);
        builder.medTotalUtbetalingsgradFraUttak(fraKalkulus.getTotalUtbetalingsgradFraUttak());
        builder.medTotalUtbetalingsgradEtterReduksjonVedTilkommetInntekt(fraKalkulus.getTotalUtbetalingsgradEtterReduksjonVedTilkommetInntekt());
        builder.medReduksjonsfaktorInaktivTypeA(fraKalkulus.getReduksjonsfaktorInaktivTypeA());

        //legg til
        fraKalkulus.getPeriodeÅrsaker().forEach(builder::leggTilPeriodeÅrsak);
        fraKalkulus.getBeregningsgrunnlagPrStatusOgAndelList().forEach(statusOgAndel -> builder.leggTilBeregningsgrunnlagPrStatusOgAndel(mapStatusOgAndel(statusOgAndel)));
        fraKalkulus.getTilkomneInntekter().stream()
                .map(KalkulatorTilBGMapper::mapTilkommetInntekt)
                .forEach(builder::leggTilTilkommetInntekt);

        return builder;
    }

    public static SammenligningsgrunnlagPrStatus mapSammenligningsgrunnlagMedStatus(SammenligningsgrunnlagPrStatusDto fraKalkulus) {
        SammenligningsgrunnlagPrStatus.Builder builder = new SammenligningsgrunnlagPrStatus.Builder();
        builder.medAvvikPromilleNy(mapTilPromille(fraKalkulus.getAvvikPromilleNy()));
        builder.medAvvikPromille(mapTilPromille(fraKalkulus.getAvvikPromilleNy()));
        builder.medRapportertPrÅr(Beløp.fraKalkulatorBeløp(fraKalkulus.getRapportertPrÅr()));
        builder.medSammenligningsgrunnlagType(fraKalkulus.getSammenligningsgrunnlagType());
        builder.medSammenligningsperiode(fraKalkulus.getSammenligningsperiodeFom(), fraKalkulus.getSammenligningsperiodeTom());

        return builder.build();
    }

    private static BeregningsgrunnlagPrStatusOgAndel.Builder mapStatusOgAndel(BeregningsgrunnlagPrStatusOgAndelDto fraKalkulus) {
        BeregningsgrunnlagPrStatusOgAndel.Builder builder = BeregningsgrunnlagPrStatusOgAndel.builder()
                .medAktivitetStatus(fraKalkulus.getAktivitetStatus())
                .medAndelsnr(fraKalkulus.getAndelsnr())
                .medArbforholdType(fraKalkulus.getArbeidsforholdType())
                .medAvkortetBrukersAndelPrÅr(Beløp.fraKalkulatorBeløp(fraKalkulus.getAvkortetBrukersAndelPrÅr()))
                .medAvkortetPrÅr(Beløp.fraKalkulatorBeløp(fraKalkulus.getAvkortetPrÅr()))
                .medAvkortetRefusjonPrÅr(Beløp.fraKalkulatorBeløp(fraKalkulus.getAvkortetRefusjonPrÅr()))
                .medFastsattAvSaksbehandler(fraKalkulus.getFastsattAvSaksbehandler())
                .medKilde(fraKalkulus.getKilde())
                .medGrunnlagPrÅr(map(fraKalkulus.getGrunnlagPrÅr()))
                .medRedusertPrÅr(Beløp.fraKalkulatorBeløp(fraKalkulus.getRedusertPrÅr()))
                .medRedusertBrukersAndelPrÅr(Beløp.fraKalkulatorBeløp(fraKalkulus.getRedusertBrukersAndelPrÅr()))
                .medMaksimalRefusjonPrÅr(Beløp.fraKalkulatorBeløp(fraKalkulus.getMaksimalRefusjonPrÅr()))
                .medRedusertRefusjonPrÅr(Beløp.fraKalkulatorBeløp(fraKalkulus.getRedusertRefusjonPrÅr()))
                .medÅrsbeløpFraTilstøtendeYtelse(Beløp.fraKalkulatorBeløp(fraKalkulus.getÅrsbeløpFraTilstøtendeYtelse()))
                .medFastsattInntektskategori(mapTilInntektskategori(fraKalkulus))
                .medOrginalDagsatsFraTilstøtendeYtelse(fraKalkulus.getOrginalDagsatsFraTilstøtendeYtelse())
                .medAvkortetFørGraderingPrÅr(Beløp.fraKalkulatorBeløp(fraKalkulus.getAvkortetFørGraderingPrÅr()));

        if (fraKalkulus.getBeregningsperiodeFom() != null) {
            builder.medBeregningsperiode(fraKalkulus.getBeregningsperiodeFom(), fraKalkulus.getBeregningsperiodeTom());
        }

        if (fraKalkulus.getPgiSnitt() != null) {
            builder.medPgi(Beløp.fraKalkulatorBeløp(fraKalkulus.getPgiSnitt()), Stream.of(fraKalkulus.getPgi1(), fraKalkulus.getPgi2(), fraKalkulus.getPgi3()).filter(Objects::nonNull).map(Beløp::fraKalkulatorBeløp).collect(Collectors.toList()));
        }

        fraKalkulus.getBgAndelArbeidsforhold().ifPresent(bgAndelArbeidsforhold -> builder.medBGAndelArbeidsforhold(KalkulatorTilBGMapper.mapBGAndelArbeidsforhold(bgAndelArbeidsforhold)));
        return builder;
    }

    private static FastsattInntektskategori mapTilInntektskategori(BeregningsgrunnlagPrStatusOgAndelDto fraKalkulus) {
        no.nav.folketrygdloven.kalkulator.modell.typer.FastsattInntektskategori inntektskategori = fraKalkulus.getFastsattInntektskategori();
        if (inntektskategori == null) {
            return null;
        }
        return new FastsattInntektskategori(
                inntektskategori.getInntektskategori(),
                inntektskategori.getInntektskategoriAutomatiskFordeling(),
                inntektskategori.getInntektskategoriManuellFordeling());
    }

    private static Årsgrunnlag map(no.nav.folketrygdloven.kalkulator.modell.typer.Årsgrunnlag grunnlagPrÅr) {
        return grunnlagPrÅr == null || !grunnlagPrÅr.erSatt() ?
                new Årsgrunnlag() : new Årsgrunnlag(
                Beløp.fraKalkulatorBeløp(grunnlagPrÅr.getBeregnetPrÅr()),
                Beløp.fraKalkulatorBeløp(grunnlagPrÅr.getFordeltPrÅr()),
                Beløp.fraKalkulatorBeløp(grunnlagPrÅr.getManueltFordeltPrÅr()),
                Beløp.fraKalkulatorBeløp(grunnlagPrÅr.getOverstyrtPrÅr()),
                Beløp.fraKalkulatorBeløp(grunnlagPrÅr.getBesteberegningPrÅr()),
                Beløp.fraKalkulatorBeløp(grunnlagPrÅr.getBruttoPrÅr())
        );
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
        fraKalkulus.getNaturalytelseBortfaltPrÅr().map(Beløp::fraKalkulatorBeløp).ifPresent(builder::medNaturalytelseBortfaltPrÅr);
        fraKalkulus.getNaturalytelseTilkommetPrÅr().map(Beløp::fraKalkulatorBeløp).ifPresent(builder::medNaturalytelseTilkommetPrÅr);
        return builder;
    }

    private static Refusjon mapRefusjon(no.nav.folketrygdloven.kalkulator.modell.typer.Refusjon refusjon) {
        if (!harRefusjon(refusjon)) {
            return null;
        }
        return new Refusjon(
                Beløp.fraKalkulatorBeløp(refusjon.getRefusjonskravPrÅr()),
                Beløp.fraKalkulatorBeløp(refusjon.getSaksbehandletRefusjonPrÅr()),
                Beløp.fraKalkulatorBeløp(refusjon.getFordeltRefusjonPrÅr()),
                Beløp.fraKalkulatorBeløp(refusjon.getManueltFordeltRefusjonPrÅr()),
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

    private static TilkommetInntekt mapTilkommetInntekt(TilkommetInntektDto it) {
        return new TilkommetInntekt(
                it.getAktivitetStatus(),
                it.getArbeidsgiver().map(KalkulatorTilIAYMapper::mapArbeidsgiver).orElse(null),
                KalkulatorTilIAYMapper.mapArbeidsforholdRef(it.getArbeidsforholdRef()),
                Beløp.fraKalkulatorBeløp(it.getBruttoInntektPrÅr()),
                Beløp.fraKalkulatorBeløp(it.getTilkommetInntektPrÅr()),
                it.skalRedusereUtbetaling());
    }


}
