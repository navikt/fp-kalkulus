package no.nav.folketrygdloven.kalkulus.mapFraEntitet;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetOverstyringDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetOverstyringerDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningRefusjonOverstyringDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningRefusjonOverstyringerDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;
import no.nav.folketrygdloven.kalkulator.modell.opptjening.OpptjeningAktivitetType;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningAktivitetAggregatEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningAktivitetEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningAktivitetOverstyringerEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningRefusjonOverstyringerEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagGrunnlagEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagRegelSporing;
import no.nav.folketrygdloven.kalkulus.felles.jpa.IntervallEntitet;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.BeregningAktivitetHandlingType;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.BeregningsgrunnlagRegelType;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.FaktaOmBeregningTilfelle;

public class BehandlingslagerTilKalkulusMapper {


    public static BeregningsgrunnlagGrunnlagDto mapGrunnlag(BeregningsgrunnlagGrunnlagEntitet beregningsgrunnlagFraFpsak, Collection<InntektsmeldingDto> inntektsmeldinger) {
        BeregningsgrunnlagGrunnlagDtoBuilder oppdatere = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(Optional.empty());

        beregningsgrunnlagFraFpsak.getBeregningsgrunnlag().ifPresent(beregningsgrunnlagDto -> oppdatere.medBeregningsgrunnlag(mapBeregningsgrunnlag(beregningsgrunnlagDto, inntektsmeldinger)));
        beregningsgrunnlagFraFpsak.getOverstyring().ifPresent(beregningAktivitetOverstyringerDto -> oppdatere.medOverstyring(mapAktivitetOverstyring(beregningAktivitetOverstyringerDto)));
        oppdatere.medRegisterAktiviteter(mapRegisterAktiviteter(beregningsgrunnlagFraFpsak.getRegisterAktiviteter()));
        beregningsgrunnlagFraFpsak.getSaksbehandletAktiviteter().ifPresent(beregningAktivitetAggregatDto -> oppdatere.medSaksbehandletAktiviteter(mapSaksbehandletAktivitet(beregningAktivitetAggregatDto)));
        beregningsgrunnlagFraFpsak.getRefusjonOverstyringer().ifPresent(beregningRefusjonOverstyringerDto -> oppdatere.medRefusjonOverstyring(mapRefusjonOverstyring(beregningRefusjonOverstyringerDto)));

        return oppdatere.build(beregningsgrunnlagFraFpsak.getBeregningsgrunnlagTilstand());
    }


    private static BeregningsgrunnlagDto mapBeregningsgrunnlag(BeregningsgrunnlagEntitet beregningsgrunnlagFraFpsak, Collection<InntektsmeldingDto> inntektsmeldinger) {
        BeregningsgrunnlagDto.Builder builder = BeregningsgrunnlagDto.builder();

        //med
        builder.medGrunnbeløp(beregningsgrunnlagFraFpsak.getGrunnbeløp().getVerdi());
        builder.medOverstyring(beregningsgrunnlagFraFpsak.isOverstyrt());
        leggTilSporingHvisFinnes(beregningsgrunnlagFraFpsak, builder, BeregningsgrunnlagRegelType.PERIODISERING);
        leggTilSporingHvisFinnes(beregningsgrunnlagFraFpsak, builder, BeregningsgrunnlagRegelType.PERIODISERING_NATURALYTELSE);
        leggTilSporingHvisFinnes(beregningsgrunnlagFraFpsak, builder, BeregningsgrunnlagRegelType.PERIODISERING_REFUSJON);
        leggTilSporingHvisFinnes(beregningsgrunnlagFraFpsak, builder, BeregningsgrunnlagRegelType.BRUKERS_STATUS);
        leggTilSporingHvisFinnes(beregningsgrunnlagFraFpsak, builder, BeregningsgrunnlagRegelType.SKJÆRINGSTIDSPUNKT);
        builder.medSkjæringstidspunkt(beregningsgrunnlagFraFpsak.getSkjæringstidspunkt());
        if (beregningsgrunnlagFraFpsak.getSammenligningsgrunnlag() != null) {
            builder.medSammenligningsgrunnlag(BGMapperTilKalkulus.mapSammenligningsgrunnlag(beregningsgrunnlagFraFpsak.getSammenligningsgrunnlag()));
        }

        //lister
        beregningsgrunnlagFraFpsak.getAktivitetStatuser().forEach(beregningsgrunnlagAktivitetStatus -> builder.leggTilAktivitetStatus(BGMapperTilKalkulus.mapAktivitetStatus(beregningsgrunnlagAktivitetStatus)));
        beregningsgrunnlagFraFpsak.getBeregningsgrunnlagPerioder().forEach(beregningsgrunnlagPeriode -> builder.leggTilBeregningsgrunnlagPeriode(BGMapperTilKalkulus.mapBeregningsgrunnlagPeriode(beregningsgrunnlagPeriode, inntektsmeldinger)));
        builder.leggTilFaktaOmBeregningTilfeller(beregningsgrunnlagFraFpsak.getFaktaOmBeregningTilfeller().stream().map(fakta -> FaktaOmBeregningTilfelle.fraKode(fakta.getKode())).collect(Collectors.toList()));
        beregningsgrunnlagFraFpsak.getSammenligningsgrunnlagPrStatusListe().forEach(sammenligningsgrunnlagPrStatus -> builder.leggTilSammenligningsgrunnlag(BGMapperTilKalkulus.mapSammenligningsgrunnlagMedStatus(sammenligningsgrunnlagPrStatus)));

        return builder.build();
    }

    private static void leggTilSporingHvisFinnes(BeregningsgrunnlagEntitet beregningsgrunnlagFraFpsak, BeregningsgrunnlagDto.Builder builder, BeregningsgrunnlagRegelType regelType) {
        if (beregningsgrunnlagFraFpsak.getRegelsporing(regelType) != null) {
            BeregningsgrunnlagRegelSporing regelsporing = beregningsgrunnlagFraFpsak.getRegelsporing(regelType);
            builder.medRegellogg(regelsporing.getRegelInput(), regelsporing.getRegelEvaluering(), regelType);
        }
    }

    private static BeregningRefusjonOverstyringerDto mapRefusjonOverstyring(BeregningRefusjonOverstyringerEntitet refusjonOverstyringerFraFpsak) {
        BeregningRefusjonOverstyringerDto.Builder dtoBuilder = BeregningRefusjonOverstyringerDto.builder();

        refusjonOverstyringerFraFpsak.getRefusjonOverstyringer().forEach(beregningRefusjonOverstyring -> {
            BeregningRefusjonOverstyringDto dto = new BeregningRefusjonOverstyringDto(IAYMapperTilKalkulus.mapArbeidsgiver(beregningRefusjonOverstyring.getArbeidsgiver()), beregningRefusjonOverstyring.getFørsteMuligeRefusjonFom().orElse(null));
            dtoBuilder.leggTilOverstyring(dto);
        });
        return dtoBuilder.build();
    }

    private static BeregningAktivitetAggregatDto mapSaksbehandletAktivitet(BeregningAktivitetAggregatEntitet saksbehandletAktiviteterFraFpsak) {
        BeregningAktivitetAggregatDto.Builder dtoBuilder = BeregningAktivitetAggregatDto.builder();
        dtoBuilder.medSkjæringstidspunktOpptjening(saksbehandletAktiviteterFraFpsak.getSkjæringstidspunktOpptjening());
        saksbehandletAktiviteterFraFpsak.getBeregningAktiviteter().forEach(mapAktivitet(dtoBuilder));
        return dtoBuilder.build();
    }

    private static Consumer<BeregningAktivitetEntitet> mapAktivitet(BeregningAktivitetAggregatDto.Builder dtoBuilder) {
        return beregningAktivitet -> {
            BeregningAktivitetDto.Builder builder = BeregningAktivitetDto.builder();
            builder.medArbeidsforholdRef(beregningAktivitet.getArbeidsforholdRef() == null ? null : IAYMapperTilKalkulus.mapArbeidsforholdRef(beregningAktivitet.getArbeidsforholdRef()));
            builder.medArbeidsgiver(beregningAktivitet.getArbeidsgiver() == null ? null : IAYMapperTilKalkulus.mapArbeidsgiver(beregningAktivitet.getArbeidsgiver()));
            builder.medOpptjeningAktivitetType(OpptjeningAktivitetType.fraKode(beregningAktivitet.getOpptjeningAktivitetType().getKode()));
            builder.medPeriode(mapDatoIntervall(beregningAktivitet.getPeriode()));
            dtoBuilder.leggTilAktivitet(builder.build());
        };
    }

    private static Intervall mapDatoIntervall(IntervallEntitet periode) {
        return periode.getTomDato() == null ? Intervall.fraOgMed(periode.getFomDato()) : Intervall.fraOgMedTilOgMed(periode.getFomDato(), periode.getTomDato());
    }

    private static BeregningAktivitetOverstyringerDto mapAktivitetOverstyring(BeregningAktivitetOverstyringerEntitet beregningAktivitetOverstyringerFraFpsak) {
        BeregningAktivitetOverstyringerDto.Builder dtoBuilder = BeregningAktivitetOverstyringerDto.builder();
        beregningAktivitetOverstyringerFraFpsak.getOverstyringer().forEach(overstyring -> {
            BeregningAktivitetOverstyringDto.Builder builder = BeregningAktivitetOverstyringDto.builder();
            builder.medArbeidsforholdRef(overstyring.getArbeidsforholdRef() == null ? null : IAYMapperTilKalkulus.mapArbeidsforholdRef(overstyring.getArbeidsforholdRef()));
            overstyring.getArbeidsgiver().ifPresent(arbeidsgiver -> builder.medArbeidsgiver(IAYMapperTilKalkulus.mapArbeidsgiver(arbeidsgiver)));
            builder.medHandling(overstyring.getHandling() == null ? null : BeregningAktivitetHandlingType.fraKode(overstyring.getHandling().getKode()));
            builder.medOpptjeningAktivitetType(OpptjeningAktivitetType.fraKode(overstyring.getOpptjeningAktivitetType().getKode()));
            builder.medPeriode(mapDatoIntervall(overstyring.getPeriode()));
            dtoBuilder.leggTilOverstyring(builder.build());
        });
        return dtoBuilder.build();
    }

    private static BeregningAktivitetAggregatDto mapRegisterAktiviteter(BeregningAktivitetAggregatEntitet registerAktiviteter) {
        BeregningAktivitetAggregatDto.Builder builder = BeregningAktivitetAggregatDto.builder();
        builder.medSkjæringstidspunktOpptjening(registerAktiviteter.getSkjæringstidspunktOpptjening());
        registerAktiviteter.getBeregningAktiviteter().forEach(mapAktivitet(builder));
        return builder.build();
    }
}
