package no.nav.folketrygdloven.kalkulus.domene.mapTilEntitet;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.stream.Stream;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagAktivitetStatusDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.SammenligningsgrunnlagPrStatusDto;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.AndelArbeidsforholdEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagAktivitetStatusEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagPeriodeEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagAndelEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.SammenligningsgrunnlagPrStatusEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.FastsattInntektskategori;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.Promille;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.Prosent;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.Refusjon;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.Årsgrunnlag;
import no.nav.folketrygdloven.kalkulus.domene.mappers.VerdityperMapper;


public class KalkulatorTilBGMapper {
    static BeregningsgrunnlagAktivitetStatusEntitet mapAktivitetStatus(BeregningsgrunnlagAktivitetStatusDto fraKalkulus) {
        BeregningsgrunnlagAktivitetStatusEntitet.Builder builder = new BeregningsgrunnlagAktivitetStatusEntitet.Builder();
        builder.medAktivitetStatus(fraKalkulus.getAktivitetStatus());
        builder.medHjemmel(fraKalkulus.getHjemmel());

        return builder.build();
    }

    static BeregningsgrunnlagPeriodeEntitet mapBeregningsgrunnlagPeriode(BeregningsgrunnlagPeriodeDto fraKalkulus) {
        BeregningsgrunnlagPeriodeEntitet.Builder builder = new BeregningsgrunnlagPeriodeEntitet.Builder();

        //med
        builder.medAvkortetPrÅr(VerdityperMapper.beløpTilDao(fraKalkulus.getAvkortetPrÅr()));
        builder.medBeregningsgrunnlagPeriode(fraKalkulus.getBeregningsgrunnlagPeriodeFom(), fraKalkulus.getBeregningsgrunnlagPeriodeTom());
        builder.medBruttoPrÅr(VerdityperMapper.beløpTilDao(fraKalkulus.getBruttoPrÅr()));
        builder.medRedusertPrÅr(VerdityperMapper.beløpTilDao(fraKalkulus.getRedusertPrÅr()));
        builder.medInntektGraderingsprosentBrutto(fraKalkulus.getInntektgraderingsprosentBrutto() != null ? new Prosent(fraKalkulus.getInntektgraderingsprosentBrutto()) : null);
        builder.medTotalUtbetalingsgradFraUttak(fraKalkulus.getTotalUtbetalingsgradFraUttak());

        //legg til
        fraKalkulus.getPeriodeÅrsaker().forEach(builder::leggTilPeriodeÅrsak);
        fraKalkulus.getBeregningsgrunnlagPrStatusOgAndelList().forEach(statusOgAndel -> builder.leggTilBeregningsgrunnlagAndel(mapStatusOgAndel(statusOgAndel)));

        return builder.build();
    }

    static SammenligningsgrunnlagPrStatusEntitet mapSammenligningsgrunnlagMedStatus(SammenligningsgrunnlagPrStatusDto fraKalkulus) {
        SammenligningsgrunnlagPrStatusEntitet.Builder builder = new SammenligningsgrunnlagPrStatusEntitet.Builder();
        builder.medAvvikPromille(mapTilPromille(fraKalkulus.getAvvikPromilleNy()));
        builder.medRapportertPrÅr(VerdityperMapper.beløpTilDao(fraKalkulus.getRapportertPrÅr()));
        builder.medSammenligningsgrunnlagType(fraKalkulus.getSammenligningsgrunnlagType());
        builder.medSammenligningsperiode(fraKalkulus.getSammenligningsperiodeFom(), fraKalkulus.getSammenligningsperiodeTom());

        return builder.build();
    }

    private static BeregningsgrunnlagAndelEntitet.Builder mapStatusOgAndel(BeregningsgrunnlagPrStatusOgAndelDto fraKalkulus) {
        BeregningsgrunnlagAndelEntitet.Builder builder = BeregningsgrunnlagAndelEntitet.builder()
                .medAktivitetStatus(fraKalkulus.getAktivitetStatus())
                .medAndelsnr(fraKalkulus.getAndelsnr())
                .medArbforholdType(fraKalkulus.getArbeidsforholdType())
                .medAvkortetBrukersAndelPrÅr(VerdityperMapper.beløpTilDao(fraKalkulus.getAvkortetBrukersAndelPrÅr()))
                .medAvkortetPrÅr(VerdityperMapper.beløpTilDao(fraKalkulus.getAvkortetPrÅr()))
                .medAvkortetRefusjonPrÅr(VerdityperMapper.beløpTilDao(fraKalkulus.getAvkortetRefusjonPrÅr()))
                .medFastsattAvSaksbehandler(fraKalkulus.getFastsattAvSaksbehandler())
                .medKilde(fraKalkulus.getKilde())
                .medGrunnlagPrÅr(map(fraKalkulus.getGrunnlagPrÅr()))
                .medRedusertPrÅr(VerdityperMapper.beløpTilDao(fraKalkulus.getRedusertPrÅr()))
                .medRedusertBrukersAndelPrÅr(VerdityperMapper.beløpTilDao(fraKalkulus.getRedusertBrukersAndelPrÅr()))
                .medMaksimalRefusjonPrÅr(VerdityperMapper.beløpTilDao(fraKalkulus.getMaksimalRefusjonPrÅr()))
                .medRedusertRefusjonPrÅr(VerdityperMapper.beløpTilDao(fraKalkulus.getRedusertRefusjonPrÅr()))
                .medÅrsbeløpFraTilstøtendeYtelse(VerdityperMapper.beløpTilDao(fraKalkulus.getÅrsbeløpFraTilstøtendeYtelse()))
                .medFastsattInntektskategori(mapTilInntektskategori(fraKalkulus))
                .medOrginalDagsatsFraTilstøtendeYtelse(fraKalkulus.getOrginalDagsatsFraTilstøtendeYtelse())
                .medAvkortetFørGraderingPrÅr(VerdityperMapper.beløpTilDao(fraKalkulus.getAvkortetFørGraderingPrÅr()));

        if (fraKalkulus.getBeregningsperiodeFom() != null) {
            builder.medBeregningsperiode(fraKalkulus.getBeregningsperiodeFom(), fraKalkulus.getBeregningsperiodeTom());
        }

        if (fraKalkulus.getPgiSnitt() != null) {
            builder.medPgi(VerdityperMapper.beløpTilDao(fraKalkulus.getPgiSnitt()), Stream.of(fraKalkulus.getPgi1(), fraKalkulus.getPgi2(), fraKalkulus.getPgi3()).filter(Objects::nonNull).map(VerdityperMapper::beløpTilDao).toList());
        }

        fraKalkulus.getBgAndelArbeidsforhold().ifPresent(bgAndelArbeidsforhold -> builder.medAndelArbeidsforhold(KalkulatorTilBGMapper.mapBGAndelArbeidsforhold(bgAndelArbeidsforhold)));
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
                VerdityperMapper.beløpTilDao(grunnlagPrÅr.getBeregnetPrÅr()),
                VerdityperMapper.beløpTilDao(grunnlagPrÅr.getFordeltPrÅr()),
                VerdityperMapper.beløpTilDao(grunnlagPrÅr.getManueltFordeltPrÅr()),
                VerdityperMapper.beløpTilDao(grunnlagPrÅr.getOverstyrtPrÅr()),
                VerdityperMapper.beløpTilDao(grunnlagPrÅr.getBesteberegningPrÅr()),
                VerdityperMapper.beløpTilDao(grunnlagPrÅr.getBruttoPrÅr())
        );
    }

    private static Promille mapTilPromille(BigDecimal verdi) {
        return verdi == null ? null : new Promille(verdi);
    }

    private static AndelArbeidsforholdEntitet mapBGAndelArbeidsforhold(BGAndelArbeidsforholdDto fraKalkulus) {
        AndelArbeidsforholdEntitet.Builder builder = AndelArbeidsforholdEntitet.builder();
        builder.medArbeidsforholdRef(KalkulatorTilIAYMapper.mapArbeidsforholdRef(fraKalkulus.getArbeidsforholdRef()));
        builder.medArbeidsgiver(KalkulatorTilIAYMapper.mapArbeidsgiver(fraKalkulus.getArbeidsgiver()));
        builder.medArbeidsperiodeFom(fraKalkulus.getArbeidsperiodeFom());
        builder.medRefusjon(fraKalkulus.getRefusjon().map(KalkulatorTilBGMapper::mapRefusjon).orElse(null));
        fraKalkulus.getArbeidsperiodeTom().ifPresent(builder::medArbeidsperiodeTom);
        fraKalkulus.getNaturalytelseBortfaltPrÅr().map(VerdityperMapper::beløpTilDao).ifPresent(builder::medNaturalytelseBortfaltPrÅr);
        fraKalkulus.getNaturalytelseTilkommetPrÅr().map(VerdityperMapper::beløpTilDao).ifPresent(builder::medNaturalytelseTilkommetPrÅr);
        return builder.build();
    }

    private static Refusjon mapRefusjon(no.nav.folketrygdloven.kalkulator.modell.typer.Refusjon refusjon) {
        if (!harRefusjon(refusjon)) {
            return null;
        }
        return new Refusjon(
                VerdityperMapper.beløpTilDao(refusjon.getRefusjonskravPrÅr()),
                VerdityperMapper.beløpTilDao(refusjon.getSaksbehandletRefusjonPrÅr()),
                VerdityperMapper.beløpTilDao(refusjon.getFordeltRefusjonPrÅr()),
                VerdityperMapper.beløpTilDao(refusjon.getManueltFordeltRefusjonPrÅr()),
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
