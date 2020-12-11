package no.nav.folketrygdloven.kalkulus.mapFraEntitet;

import static no.nav.folketrygdloven.kalkulus.mapFraEntitet.IAYMapperTilKalkulus.mapArbeidsforholdRef;
import static no.nav.folketrygdloven.kalkulus.mapFraEntitet.IAYMapperTilKalkulus.mapArbeidsgiver;

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
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.FaktaAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.FaktaAktørDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.FaktaArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.OpptjeningAktivitetType;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningAktivitetAggregatEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningAktivitetEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningAktivitetOverstyringerEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningRefusjonOverstyringerEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagGrunnlagEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.FaktaAggregatEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.FaktaAktørEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.FaktaArbeidsforholdEntitet;
import no.nav.folketrygdloven.kalkulus.felles.jpa.IntervallEntitet;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningAktivitetHandlingType;
import no.nav.folketrygdloven.kalkulus.kodeverk.FaktaOmBeregningTilfelle;

public class BehandlingslagerTilKalkulusMapper {


    public static BeregningsgrunnlagGrunnlagDto mapGrunnlag(BeregningsgrunnlagGrunnlagEntitet beregningsgrunnlagFraFagsystem) {
        BeregningsgrunnlagGrunnlagDtoBuilder oppdatere = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(Optional.empty());

        beregningsgrunnlagFraFagsystem.getBeregningsgrunnlag().ifPresent(beregningsgrunnlagDto -> oppdatere.medBeregningsgrunnlag(mapBeregningsgrunnlag(beregningsgrunnlagDto)));
        beregningsgrunnlagFraFagsystem.getOverstyring().ifPresent(beregningAktivitetOverstyringerDto -> oppdatere.medOverstyring(mapAktivitetOverstyring(beregningAktivitetOverstyringerDto)));
        oppdatere.medRegisterAktiviteter(mapAktiviteter(beregningsgrunnlagFraFagsystem.getRegisterAktiviteter()));
        beregningsgrunnlagFraFagsystem.getSaksbehandletAktiviteter().ifPresent(beregningAktivitetAggregatDto -> oppdatere.medSaksbehandletAktiviteter(mapAktiviteter(beregningAktivitetAggregatDto)));
        beregningsgrunnlagFraFagsystem.getRefusjonOverstyringer().ifPresent(beregningRefusjonOverstyringerDto -> oppdatere.medRefusjonOverstyring(mapRefusjonOverstyring(beregningRefusjonOverstyringerDto)));
        beregningsgrunnlagFraFagsystem.getFaktaAggregat().ifPresent(fakta -> oppdatere.medFaktaAggregat(mapFakta(fakta)));
        return oppdatere.build(beregningsgrunnlagFraFagsystem.getBeregningsgrunnlagTilstand());
    }

    private static FaktaAggregatDto mapFakta(FaktaAggregatEntitet fakta) {
        FaktaAggregatDto.Builder faktaAggregatBuilder = FaktaAggregatDto.builder();
        fakta.getFaktaAktør().map(BehandlingslagerTilKalkulusMapper::mapFaktaAktør).ifPresent(faktaAggregatBuilder::medFaktaAktør);
        fakta.getFaktaArbeidsforhold().stream().map(BehandlingslagerTilKalkulusMapper::mapFaktaArbeidsforhold)
                .forEach(faktaAggregatBuilder::erstattEksisterendeEllerLeggTil);
        return faktaAggregatBuilder.build();
    }

    private static FaktaArbeidsforholdDto mapFaktaArbeidsforhold(FaktaArbeidsforholdEntitet faktaArbeidsforholdEntitet) {
        return new FaktaArbeidsforholdDto.Builder(mapArbeidsgiver(faktaArbeidsforholdEntitet.getArbeidsgiver()), mapArbeidsforholdRef(faktaArbeidsforholdEntitet.getArbeidsforholdRef()))
                .medErTidsbegrenset(faktaArbeidsforholdEntitet.getErTidsbegrenset())
                .medHarMottattYtelse(faktaArbeidsforholdEntitet.getHarMottattYtelse())
                .medHarLønnsendringIBeregningsperioden(faktaArbeidsforholdEntitet.getHarLønnsendringIBeregningsperioden())
                .build();
    }

    private static FaktaAktørDto mapFaktaAktør(FaktaAktørEntitet faktaAktørEntitet) {
        return FaktaAktørDto.builder()
                .medErNyoppstartetFL(faktaAktørEntitet.getErNyoppstartetFL())
                .medErNyIArbeidslivetSN(faktaAktørEntitet.getErNyIArbeidslivetSN())
                .medMottarEtterlønnSluttpakke(faktaAktørEntitet.getMottarEtterlønnSluttpakke())
                .medHarFLMottattYtelse(faktaAktørEntitet.getHarFLMottattYtelse())
                .medSkalBesteberegnes(faktaAktørEntitet.getSkalBesteberegnes())
                .medErMilitærSiviltjeneste(faktaAktørEntitet.getSkalBeregnesSomMilitær())
                .build();
    }


    public static BeregningsgrunnlagDto mapBeregningsgrunnlag(BeregningsgrunnlagEntitet beregningsgrunnlagFraFagsystem) {
        BeregningsgrunnlagDto.Builder builder = BeregningsgrunnlagDto.builder();

        builder.medGrunnbeløp(beregningsgrunnlagFraFagsystem.getGrunnbeløp().getVerdi());
        builder.medOverstyring(beregningsgrunnlagFraFagsystem.isOverstyrt());

        builder.medSkjæringstidspunkt(beregningsgrunnlagFraFagsystem.getSkjæringstidspunkt());

        beregningsgrunnlagFraFagsystem.getSammenligningsgrunnlag()
                .map(BGMapperTilKalkulus::mapSammenligningsgrunnlag)
                .ifPresent(builder::medSammenligningsgrunnlag);

        //lister
        beregningsgrunnlagFraFagsystem.getAktivitetStatuser().forEach(beregningsgrunnlagAktivitetStatus -> builder.leggTilAktivitetStatus(BGMapperTilKalkulus.mapAktivitetStatus(beregningsgrunnlagAktivitetStatus)));
        beregningsgrunnlagFraFagsystem.getBeregningsgrunnlagPerioder().forEach(beregningsgrunnlagPeriode -> builder.leggTilBeregningsgrunnlagPeriode(BGMapperTilKalkulus.mapBeregningsgrunnlagPeriode(beregningsgrunnlagPeriode)));
        builder.leggTilFaktaOmBeregningTilfeller(beregningsgrunnlagFraFagsystem.getFaktaOmBeregningTilfeller().stream().map(fakta -> FaktaOmBeregningTilfelle.fraKode(fakta.getKode())).collect(Collectors.toList()));
        beregningsgrunnlagFraFagsystem.getSammenligningsgrunnlagPrStatusListe().forEach(sammenligningsgrunnlagPrStatus -> builder.leggTilSammenligningsgrunnlag(BGMapperTilKalkulus.mapSammenligningsgrunnlagMedStatus(sammenligningsgrunnlagPrStatus)));

        return builder.build();
    }

    private static BeregningRefusjonOverstyringerDto mapRefusjonOverstyring(BeregningRefusjonOverstyringerEntitet refusjonOverstyringerFraFpsak) {
        BeregningRefusjonOverstyringerDto.Builder dtoBuilder = BeregningRefusjonOverstyringerDto.builder();

        refusjonOverstyringerFraFpsak.getRefusjonOverstyringer().forEach(beregningRefusjonOverstyring -> {
            BeregningRefusjonOverstyringDto dto = new BeregningRefusjonOverstyringDto(mapArbeidsgiver(beregningRefusjonOverstyring.getArbeidsgiver()), beregningRefusjonOverstyring.getFørsteMuligeRefusjonFom().orElse(null));
            dtoBuilder.leggTilOverstyring(dto);
        });
        return dtoBuilder.build();
    }

    private static Consumer<BeregningAktivitetEntitet> mapAktivitet(BeregningAktivitetAggregatDto.Builder dtoBuilder) {
        return beregningAktivitet -> {
            BeregningAktivitetDto.Builder builder = BeregningAktivitetDto.builder();
            builder.medArbeidsforholdRef(beregningAktivitet.getArbeidsforholdRef() == null ? null : IAYMapperTilKalkulus.mapArbeidsforholdRef(beregningAktivitet.getArbeidsforholdRef()));
            builder.medArbeidsgiver(beregningAktivitet.getArbeidsgiver() == null ? null : mapArbeidsgiver(beregningAktivitet.getArbeidsgiver()));
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
            overstyring.getArbeidsgiver().ifPresent(arbeidsgiver -> builder.medArbeidsgiver(mapArbeidsgiver(arbeidsgiver)));
            builder.medHandling(overstyring.getHandling() == null ? null : BeregningAktivitetHandlingType.fraKode(overstyring.getHandling().getKode()));
            builder.medOpptjeningAktivitetType(OpptjeningAktivitetType.fraKode(overstyring.getOpptjeningAktivitetType().getKode()));
            builder.medPeriode(mapDatoIntervall(overstyring.getPeriode()));
            dtoBuilder.leggTilOverstyring(builder.build());
        });
        return dtoBuilder.build();
    }

    public static BeregningAktivitetAggregatDto mapAktiviteter(BeregningAktivitetAggregatEntitet registerAktiviteter) {
        BeregningAktivitetAggregatDto.Builder builder = BeregningAktivitetAggregatDto.builder();
        builder.medSkjæringstidspunktOpptjening(registerAktiviteter.getSkjæringstidspunktOpptjening());
        registerAktiviteter.getBeregningAktiviteter().forEach(mapAktivitet(builder));
        return builder.build();
    }
}
