package no.nav.folketrygdloven.kalkulus.domene.mapTilEntitet;

import static no.nav.folketrygdloven.kalkulus.domene.mappers.VerdityperMapper.beløpTilDao;

import no.nav.folketrygdloven.kalkulator.steg.besteberegning.BesteberegningMånedGrunnlag;
import no.nav.folketrygdloven.kalkulator.steg.besteberegning.BesteberegningVurderingGrunnlag;
import no.nav.folketrygdloven.kalkulator.steg.besteberegning.Inntekt;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BesteberegningInntektEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BesteberegningMånedsgrunnlagEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BesteberegninggrunnlagEntitet;
import no.nav.folketrygdloven.kalkulus.kodeverk.OpptjeningAktivitetType;

public final class BesteberegningMapper {

    private BesteberegningMapper() {
    }

    public static BesteberegninggrunnlagEntitet mapBestebergninggrunnlag(BesteberegningVurderingGrunnlag besteberegningVurderingGrunnlag) {
        var builder = BesteberegninggrunnlagEntitet.builder();
        besteberegningVurderingGrunnlag.getSeksBesteMåneder()
            .stream()
            .map(BesteberegningMapper::mapBesteberegningMåned)
            .forEach(builder::leggTilMånedsgrunnlag);
        builder.medAvvik(beløpTilDao(besteberegningVurderingGrunnlag.getAvvikFraFørsteLedd()));
        return builder.build();
    }

    private static BesteberegningMånedsgrunnlagEntitet mapBesteberegningMåned(BesteberegningMånedGrunnlag besteberegningMånedGrunnlag) {
        var måned = besteberegningMånedGrunnlag.getMåned();
        var månedBuilder = BesteberegningMånedsgrunnlagEntitet.builder()
            .medPeriode(måned.atDay(1), måned.atEndOfMonth());
        besteberegningMånedGrunnlag.getInntekter()
            .stream()
            .map(BesteberegningMapper::mapBesteberegningInntekt)
            .forEach(månedBuilder::leggTilInntekt);
        return månedBuilder.build();
    }

    private static BesteberegningInntektEntitet mapBesteberegningInntekt(Inntekt inntekt) {
        if (inntekt.getArbeidsgiver() != null) {
            return BesteberegningInntektEntitet.builder()
                .medArbeidsgiver(KalkulatorTilIAYMapper.mapArbeidsgiver(inntekt.getArbeidsgiver()))
                .medOpptjeningAktivitetType(safeMapOpptjeningAktivitet(inntekt.getOpptjeningAktivitetType(), OpptjeningAktivitetType.ARBEID))
                .medArbeidsforholdRef(KalkulatorTilIAYMapper.mapArbeidsforholdRef(inntekt.getArbeidsforholdRef()))
                .medInntekt(beløpTilDao(inntekt.getInntekt()))
                .build();
        }
        return BesteberegningInntektEntitet.builder()
            .medOpptjeningAktivitetType(safeMapOpptjeningAktivitet(inntekt.getOpptjeningAktivitetType(), OpptjeningAktivitetType.DAGPENGER))
            .medInntekt(beløpTilDao(inntekt.getInntekt()))
            .build();
    }

    private static OpptjeningAktivitetType safeMapOpptjeningAktivitet(OpptjeningAktivitetType inntektOpptjeningAktivitet, OpptjeningAktivitetType defaultAktivitet) {
        if (inntektOpptjeningAktivitet == null) {
            return defaultAktivitet;
        }
        return inntektOpptjeningAktivitet;
    }
}
