package no.nav.folketrygdloven.kalkulus.mapFraEntitet;

import static no.nav.folketrygdloven.kalkulus.mapFraEntitet.IAYMapperTilKalkulus.mapArbeidsforholdRef;
import static no.nav.folketrygdloven.kalkulus.mapFraEntitet.IAYMapperTilKalkulus.mapArbeidsgiver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import no.nav.folketrygdloven.kalkulator.modell.avklaringsbehov.AvklaringsbehovDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetOverstyringDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetOverstyringerDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningRefusjonOverstyringDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningRefusjonOverstyringerDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningRefusjonPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.FaktaAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.FaktaAktørDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.FaktaArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.avklaringsbehov.AvklaringsbehovEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.AktivitetAggregatEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.AktivitetEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.RefusjonOverstyringerEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.RefusjonPeriodeEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagGrunnlagEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.FaktaAggregatEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.FaktaAktørEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.FaktaArbeidsforholdEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.FaktaVurdering;
import no.nav.folketrygdloven.kalkulus.felles.jpa.IntervallEntitet;
import no.nav.folketrygdloven.kalkulus.mappers.VerdityperMapper;

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
                .medErTidsbegrenset(mapFaktaVurdering(faktaArbeidsforholdEntitet.getErTidsbegrenset()))
                .medHarMottattYtelse(mapFaktaVurdering(faktaArbeidsforholdEntitet.getHarMottattYtelse()))
                .medHarLønnsendringIBeregningsperioden(mapFaktaVurdering(faktaArbeidsforholdEntitet.getHarLønnsendringIBeregningsperioden()))
                .build();
    }

    private static FaktaAktørDto mapFaktaAktør(FaktaAktørEntitet faktaAktørEntitet) {
        return FaktaAktørDto.builder()
                .medErNyoppstartetFL(mapFaktaVurdering(faktaAktørEntitet.getErNyoppstartetFL()))
                .medErNyIArbeidslivetSN(mapFaktaVurdering(faktaAktørEntitet.getErNyIArbeidslivetSN()))
                .medMottarEtterlønnSluttpakke(mapFaktaVurdering(faktaAktørEntitet.getMottarEtterlønnSluttpakke()))
                .medHarFLMottattYtelse(mapFaktaVurdering(faktaAktørEntitet.getHarFLMottattYtelse()))
                .medSkalBesteberegnes(mapFaktaVurdering(faktaAktørEntitet.getSkalBesteberegnes()))
                .medErMilitærSiviltjeneste(mapFaktaVurdering(faktaAktørEntitet.getSkalBeregnesSomMilitær()))
                .build();
    }

    private static no.nav.folketrygdloven.kalkulator.modell.typer.FaktaVurdering mapFaktaVurdering(FaktaVurdering vurdering) {
        return vurdering == null ? null : new no.nav.folketrygdloven.kalkulator.modell.typer.FaktaVurdering(vurdering.getVurdering(), vurdering.getKilde());
    }


    public static BeregningsgrunnlagDto mapBeregningsgrunnlag(BeregningsgrunnlagEntitet beregningsgrunnlagFraFagsystem) {
        BeregningsgrunnlagDto.Builder builder = BeregningsgrunnlagDto.builder();

        builder.medGrunnbeløp(VerdityperMapper.beløpFraDao(beregningsgrunnlagFraFagsystem.getGrunnbeløp()));
        builder.medOverstyring(beregningsgrunnlagFraFagsystem.isOverstyrt());

        builder.medSkjæringstidspunkt(beregningsgrunnlagFraFagsystem.getSkjæringstidspunkt());

        //lister
        beregningsgrunnlagFraFagsystem.getAktivitetStatuser().forEach(beregningsgrunnlagAktivitetStatus -> builder.leggTilAktivitetStatus(BGMapperTilKalkulus.mapAktivitetStatus(beregningsgrunnlagAktivitetStatus)));
        beregningsgrunnlagFraFagsystem.getBeregningsgrunnlagPerioder().forEach(beregningsgrunnlagPeriode -> builder.leggTilBeregningsgrunnlagPeriode(BGMapperTilKalkulus.mapBeregningsgrunnlagPeriode(beregningsgrunnlagPeriode)));
        builder.leggTilFaktaOmBeregningTilfeller(new ArrayList<>(beregningsgrunnlagFraFagsystem.getFaktaOmBeregningTilfeller()));
        beregningsgrunnlagFraFagsystem.getSammenligningsgrunnlagPrStatusListe().forEach(sammenligningsgrunnlagPrStatus -> builder.leggTilSammenligningsgrunnlag(BGMapperTilKalkulus.mapSammenligningsgrunnlagMedStatus(sammenligningsgrunnlagPrStatus)));

        return builder.build();
    }

    private static BeregningRefusjonOverstyringerDto mapRefusjonOverstyring(RefusjonOverstyringerEntitet refusjonOverstyringerFraFpsak) {
        BeregningRefusjonOverstyringerDto.Builder dtoBuilder = BeregningRefusjonOverstyringerDto.builder();

        refusjonOverstyringerFraFpsak.getRefusjonOverstyringer().forEach(beregningRefusjonOverstyring -> {
            List<BeregningRefusjonPeriodeDto> refusjonPerioder = beregningRefusjonOverstyring.getRefusjonPerioder() == null
                    ? Collections.emptyList()
                    : beregningRefusjonOverstyring.getRefusjonPerioder().stream()
                    .map(BehandlingslagerTilKalkulusMapper::mapRefusjonPeriode)
                    .toList();
            BeregningRefusjonOverstyringDto dto = new BeregningRefusjonOverstyringDto(mapArbeidsgiver(beregningRefusjonOverstyring.getArbeidsgiver()),
                    beregningRefusjonOverstyring.getFørsteMuligeRefusjonFom().orElse(null), refusjonPerioder, beregningRefusjonOverstyring.getErFristUtvidet());
            dtoBuilder.leggTilOverstyring(dto);
        });
        return dtoBuilder.build();
    }

    private static BeregningRefusjonPeriodeDto mapRefusjonPeriode(RefusjonPeriodeEntitet ro) {
        return new BeregningRefusjonPeriodeDto(mapArbeidsforholdRef(ro.getArbeidsforholdRef()), ro.getStartdatoRefusjon());
    }

    private static Consumer<AktivitetEntitet> mapAktivitet(BeregningAktivitetAggregatDto.Builder dtoBuilder) {
        return beregningAktivitet -> {
            BeregningAktivitetDto.Builder builder = BeregningAktivitetDto.builder();
            builder.medArbeidsforholdRef(beregningAktivitet.getArbeidsforholdRef() == null ? null : IAYMapperTilKalkulus.mapArbeidsforholdRef(beregningAktivitet.getArbeidsforholdRef()));
            builder.medArbeidsgiver(beregningAktivitet.getArbeidsgiver() == null ? null : mapArbeidsgiver(beregningAktivitet.getArbeidsgiver()));
            builder.medOpptjeningAktivitetType(beregningAktivitet.getOpptjeningAktivitetType());
            builder.medPeriode(mapDatoIntervall(beregningAktivitet.getPeriode()));
            dtoBuilder.leggTilAktivitet(builder.build());
        };
    }

    private static Intervall mapDatoIntervall(IntervallEntitet periode) {
        return periode.getTomDato() == null ? Intervall.fraOgMed(periode.getFomDato()) : Intervall.fraOgMedTilOgMed(periode.getFomDato(), periode.getTomDato());
    }

    private static BeregningAktivitetOverstyringerDto mapAktivitetOverstyring(AktivitetAggregatEntitet aktivitetAggregatOverstyringer) {
        BeregningAktivitetOverstyringerDto.Builder dtoBuilder = BeregningAktivitetOverstyringerDto.builder();
        aktivitetAggregatOverstyringer.getAktiviteter().forEach(overstyring -> {
            BeregningAktivitetOverstyringDto.Builder builder = BeregningAktivitetOverstyringDto.builder();
            builder.medArbeidsforholdRef(overstyring.getArbeidsforholdRef() == null ? null : IAYMapperTilKalkulus.mapArbeidsforholdRef(overstyring.getArbeidsforholdRef()));
            builder.medArbeidsgiver(overstyring.getArbeidsgiver() == null ? null : mapArbeidsgiver(overstyring.getArbeidsgiver()));
            builder.medHandling(overstyring.getOverstyrHandlingType().orElseThrow());
            builder.medOpptjeningAktivitetType(overstyring.getOpptjeningAktivitetType());
            builder.medPeriode(mapDatoIntervall(overstyring.getPeriode()));
            dtoBuilder.leggTilOverstyring(builder.build());
        });
        return dtoBuilder.build();
    }

    public static BeregningAktivitetAggregatDto mapAktiviteter(AktivitetAggregatEntitet registerAktiviteter) {
        BeregningAktivitetAggregatDto.Builder builder = BeregningAktivitetAggregatDto.builder();
        builder.medSkjæringstidspunktOpptjening(registerAktiviteter.getSkjæringstidspunktOpptjening());
        registerAktiviteter.getAktiviteter().forEach(mapAktivitet(builder));
        return builder.build();
    }

    public static List<AvklaringsbehovDto> mapAvklaringsbehov(List<AvklaringsbehovEntitet> avklaringsbehov) {
        return avklaringsbehov.stream().map(BehandlingslagerTilKalkulusMapper::mapAvklaringsbehov).toList();
    }

    private static AvklaringsbehovDto mapAvklaringsbehov(AvklaringsbehovEntitet avklaringsbehovEntitet) {
        return new AvklaringsbehovDto(avklaringsbehovEntitet.getDefinisjon(), avklaringsbehovEntitet.getStatus(), avklaringsbehovEntitet.getBegrunnelse(), avklaringsbehovEntitet.getErTrukket(), avklaringsbehovEntitet.getVurdertAv(), avklaringsbehovEntitet.getVurdertTidspunkt());
    }
}
