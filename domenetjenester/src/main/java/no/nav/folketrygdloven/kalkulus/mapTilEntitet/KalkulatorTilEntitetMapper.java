package no.nav.folketrygdloven.kalkulus.mapTilEntitet;

import static no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand.KOFAKBER_UT;
import static no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand.OPPDATERT_MED_ANDELER;
import static no.nav.folketrygdloven.kalkulus.mapTilEntitet.KalkulatorTilBGMapper.mapAktivitetStatus;
import static no.nav.folketrygdloven.kalkulus.mapTilEntitet.KalkulatorTilBGMapper.mapBeregningsgrunnlagPeriode;
import static no.nav.folketrygdloven.kalkulus.mapTilEntitet.KalkulatorTilBGMapper.mapSammenligningsgrunnlagMedStatus;
import static no.nav.folketrygdloven.kalkulus.mapTilEntitet.KalkulatorTilIAYMapper.mapArbeidsforholdRef;
import static no.nav.folketrygdloven.kalkulus.mapTilEntitet.KalkulatorTilIAYMapper.mapArbeidsgiver;

import java.time.LocalDate;
import java.util.function.Consumer;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetOverstyringerDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningRefusjonOverstyringerDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningRefusjonPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.FaktaAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.FaktaAktørDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.FaktaArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.steg.besteberegning.BesteberegningVurderingGrunnlag;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.AktivitetAggregatEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.AktivitetEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.RefusjonOverstyringEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.RefusjonOverstyringerEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.RefusjonPeriodeEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagGrunnlagBuilder;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.FaktaAggregatEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.FaktaAktørEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.FaktaArbeidsforholdEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.InternArbeidsforholdRef;
import no.nav.folketrygdloven.kalkulus.felles.jpa.IntervallEntitet;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulus.mappers.VerdityperMapper;


/**
 * Skal etterhvert benytte seg av kontrakten som skal lages i fp-kalkulus, benytter foreløping en, en-til-en mapping på klassenivå...
 */
public class KalkulatorTilEntitetMapper {

    public static BeregningsgrunnlagGrunnlagBuilder mapGrunnlag(BeregningsgrunnlagGrunnlagDto beregningsgrunnlagFraKalkulus) {
        var oppdatere = BeregningsgrunnlagGrunnlagBuilder.nytt();
        var stpOpptjening = beregningsgrunnlagFraKalkulus.getRegisterAktiviteter().getSkjæringstidspunktOpptjening();

        beregningsgrunnlagFraKalkulus.getBeregningsgrunnlagHvisFinnes().ifPresent(beregningsgrunnlagDto -> oppdatere.medBeregningsgrunnlag(mapBeregningsgrunnlag(beregningsgrunnlagDto)));
        beregningsgrunnlagFraKalkulus.getOverstyring().ifPresent(beregningAktivitetOverstyringerDto -> oppdatere.medOverstyring(mapAktivitetOverstyring(beregningAktivitetOverstyringerDto, stpOpptjening)));
        oppdatere.medRegisterAktiviteter(mapRegisterAktiviteter(beregningsgrunnlagFraKalkulus.getRegisterAktiviteter()));
        beregningsgrunnlagFraKalkulus.getSaksbehandletAktiviteter().ifPresent(beregningAktivitetAggregatDto -> oppdatere.medSaksbehandletAktiviteter(mapSaksbehandletAktivitet(beregningAktivitetAggregatDto)));
        beregningsgrunnlagFraKalkulus.getRefusjonOverstyringer().ifPresent(beregningRefusjonOverstyringerDto -> oppdatere.medRefusjonOverstyring(mapRefusjonOverstyring(beregningRefusjonOverstyringerDto)));

        BeregningsgrunnlagTilstand tilstand = beregningsgrunnlagFraKalkulus.getBeregningsgrunnlagTilstand();
        if (KOFAKBER_UT.equals(tilstand) || OPPDATERT_MED_ANDELER.equals(tilstand)) {
            beregningsgrunnlagFraKalkulus.getFaktaAggregat().ifPresent(fakta -> oppdatere.medFaktaAggregat(mapFakta(fakta)));
        }

        return oppdatere;
    }

    public static BeregningsgrunnlagEntitet mapBeregningsgrunnlagMedBesteberegning(BeregningsgrunnlagDto beregningsgrunnlagFraKalkulus, BesteberegningVurderingGrunnlag besteberegningVurderingGrunnlag) {
        var builder = opprettBuilderMedBeregningsgrunnlag(beregningsgrunnlagFraKalkulus);
        builder.medBesteberegninggrunnlag(BesteberegningMapper.mapBestebergninggrunnlag(besteberegningVurderingGrunnlag));
        return builder.build();
    }

    private static BeregningsgrunnlagEntitet mapBeregningsgrunnlag(BeregningsgrunnlagDto beregningsgrunnlagFraKalkulus) {
        return opprettBuilderMedBeregningsgrunnlag(beregningsgrunnlagFraKalkulus).build();
    }

    private static BeregningsgrunnlagEntitet.Builder opprettBuilderMedBeregningsgrunnlag(BeregningsgrunnlagDto beregningsgrunnlagFraKalkulus) {
        var builder = BeregningsgrunnlagEntitet.builder();

        //med
        builder.medGrunnbeløp(VerdityperMapper.beløpTilDao(beregningsgrunnlagFraKalkulus.getGrunnbeløp()));
        builder.medOverstyring(beregningsgrunnlagFraKalkulus.isOverstyrt());
        builder.medSkjæringstidspunkt(beregningsgrunnlagFraKalkulus.getSkjæringstidspunkt());

        //lister
        beregningsgrunnlagFraKalkulus.getAktivitetStatuser().forEach(beregningsgrunnlagAktivitetStatus -> builder.leggTilAktivitetstatus(
            mapAktivitetStatus(beregningsgrunnlagAktivitetStatus)));
        beregningsgrunnlagFraKalkulus.getBeregningsgrunnlagPerioder().forEach(beregningsgrunnlagPeriode -> builder.leggTilBeregningsgrunnlagPeriode(
            mapBeregningsgrunnlagPeriode(beregningsgrunnlagPeriode)));
        beregningsgrunnlagFraKalkulus.getFaktaOmBeregningTilfeller().forEach(builder::leggTilFaktaTilfelle);
        beregningsgrunnlagFraKalkulus.getSammenligningsgrunnlagPrStatusListe().forEach(sammenligningsgrunnlagPrStatus -> builder.leggTilSammenligningsgrunnlag(
            mapSammenligningsgrunnlagMedStatus(sammenligningsgrunnlagPrStatus)));

        return builder;
    }

    private static RefusjonOverstyringerEntitet mapRefusjonOverstyring(BeregningRefusjonOverstyringerDto refusjonOverstyringerFraKalkulus) {
        RefusjonOverstyringerEntitet.Builder entitetBuilder = RefusjonOverstyringerEntitet.builder();

        refusjonOverstyringerFraKalkulus.getRefusjonOverstyringer().forEach(beregningRefusjonOverstyring -> {
            RefusjonOverstyringEntitet.Builder builder = RefusjonOverstyringEntitet.builder()
                    .medArbeidsgiver(mapArbeidsgiver(beregningRefusjonOverstyring.getArbeidsgiver()))
                    .medErFristUtvidet(beregningRefusjonOverstyring.getErFristUtvidet().orElse(null))
                    .medFørsteMuligeRefusjonFom(beregningRefusjonOverstyring.getFørsteMuligeRefusjonFom().orElse(null));
            beregningRefusjonOverstyring.getRefusjonPerioder().forEach(periode -> builder.leggTilRefusjonPeriode(mapRefusjonsperiode(periode)));
            entitetBuilder.leggTilOverstyring(builder.build());
        });
        return entitetBuilder.build();
    }

    private static RefusjonPeriodeEntitet mapRefusjonsperiode(BeregningRefusjonPeriodeDto periode) {
        InternArbeidsforholdRef ref = periode.getArbeidsforholdRef() == null ? null : mapArbeidsforholdRef(periode.getArbeidsforholdRef());
        return new RefusjonPeriodeEntitet(ref, periode.getStartdatoRefusjon());
    }

    private static AktivitetAggregatEntitet mapSaksbehandletAktivitet(BeregningAktivitetAggregatDto saksbehandletAktiviteterFraKalkulus) {
        AktivitetAggregatEntitet.Builder entitetBuilder = AktivitetAggregatEntitet.builder();
        entitetBuilder.medSkjæringstidspunktOpptjening(saksbehandletAktiviteterFraKalkulus.getSkjæringstidspunktOpptjening());
        saksbehandletAktiviteterFraKalkulus.getBeregningAktiviteter().forEach(mapBeregningAktivitet(entitetBuilder));
        return entitetBuilder.build();
    }

    private static Consumer<BeregningAktivitetDto> mapBeregningAktivitet(AktivitetAggregatEntitet.Builder entitetBuilder) {
        return beregningAktivitet -> {
            AktivitetEntitet.Builder builder = AktivitetEntitet.builder();
            builder.medArbeidsforholdRef(beregningAktivitet.getArbeidsforholdRef() == null ? null : mapArbeidsforholdRef(beregningAktivitet.getArbeidsforholdRef()));
            builder.medArbeidsgiver(beregningAktivitet.getArbeidsgiver() == null ? null : mapArbeidsgiver(beregningAktivitet.getArbeidsgiver()));
            builder.medOpptjeningAktivitetType(beregningAktivitet.getOpptjeningAktivitetType());
            builder.medPeriode(mapDatoIntervall(beregningAktivitet.getPeriode()));
            entitetBuilder.leggTilAktivitet(builder.build());
        };
    }

    private static AktivitetAggregatEntitet mapAktivitetOverstyring(BeregningAktivitetOverstyringerDto beregningAktivitetOverstyringerFraKalkulus,
                                                                         LocalDate stpOpptjening) {
        AktivitetAggregatEntitet.Builder entitetBuilder = AktivitetAggregatEntitet.builder().medSkjæringstidspunktOpptjening(stpOpptjening);
        beregningAktivitetOverstyringerFraKalkulus.getOverstyringer().forEach(overstyring -> {
            AktivitetEntitet.Builder builder = AktivitetEntitet.builder();
            builder.medArbeidsforholdRef(overstyring.getArbeidsforholdRef() == null ? null : mapArbeidsforholdRef(overstyring.getArbeidsforholdRef()));
            overstyring.getArbeidsgiver().ifPresent(arbeidsgiver -> builder.medArbeidsgiver(mapArbeidsgiver(arbeidsgiver)));
            builder.medOverstyrHandlingType(overstyring.getHandling());
            builder.medOpptjeningAktivitetType(overstyring.getOpptjeningAktivitetType());
            builder.medPeriode(mapDatoIntervall(overstyring.getPeriode()));
            entitetBuilder.leggTilAktivitet(builder.build());
        });
        return entitetBuilder.build();
    }

    private static IntervallEntitet mapDatoIntervall(Intervall periode) {
        return IntervallEntitet.fraOgMedTilOgMed(periode.getFomDato(), periode.getTomDato());
    }

    private static FaktaAggregatEntitet mapFakta(FaktaAggregatDto fakta) {
        FaktaAggregatEntitet.Builder faktaAggregatBuilder = FaktaAggregatEntitet.builder();
        fakta.getFaktaAktør().map(KalkulatorTilEntitetMapper::mapFaktaAktør).ifPresent(faktaAggregatBuilder::medFaktaAktør);
        fakta.getFaktaArbeidsforhold().stream().map(KalkulatorTilEntitetMapper::mapFaktaArbeidsforhold)
                .forEach(faktaAggregatBuilder::leggTilFaktaArbeidsforholdIgnorerOmEksisterer);
        return faktaAggregatBuilder.build();
    }

    private static FaktaArbeidsforholdEntitet mapFaktaArbeidsforhold(FaktaArbeidsforholdDto faktaArbeidsforholdDto) {
        return FaktaArbeidsforholdEntitet.builder()
                .medArbeidsgiver(mapArbeidsgiver(faktaArbeidsforholdDto.getArbeidsgiver()))
                .medArbeidsforholdRef(mapArbeidsforholdRef(faktaArbeidsforholdDto.getArbeidsforholdRef()))
                .medErTidsbegrenset(faktaArbeidsforholdDto.getErTidsbegrensetVurdering())
                .medHarMottattYtelse(faktaArbeidsforholdDto.getHarMottattYtelseVurdering())
                .medHarLønnsendringIBeregningsperioden(faktaArbeidsforholdDto.getHarLønnsendringIBeregningsperiodenVurdering())
                .build();
    }

    private static FaktaAktørEntitet mapFaktaAktør(FaktaAktørDto faktaAktørDto) {
        return FaktaAktørEntitet.builder()
                .medSkalBesteberegnes(faktaAktørDto.getSkalBesteberegnesVurdering())
                .medMottarEtterlønnSluttpakke(faktaAktørDto.getMottarEtterlønnSluttpakkeVurdering())
                .medErNyIArbeidslivetSN(faktaAktørDto.getErNyIArbeidslivetSNVurdering())
                .medErNyoppstartetFL(faktaAktørDto.getErNyoppstartetFLVurdering())
                .medHarFLMottattYtelse(faktaAktørDto.getHarFLMottattYtelseVurdering())
                .medSkalBeregnesSomMilitær(faktaAktørDto.getSkalBeregnesSomMilitærVurdering())
                .build();
    }

    private static AktivitetAggregatEntitet mapRegisterAktiviteter(BeregningAktivitetAggregatDto registerAktiviteter) {
        AktivitetAggregatEntitet.Builder builder = AktivitetAggregatEntitet.builder();
        registerAktiviteter.getBeregningAktiviteter().forEach(mapBeregningAktivitet(builder));
        builder.medSkjæringstidspunktOpptjening(registerAktiviteter.getSkjæringstidspunktOpptjening());
        return builder.build();
    }

}
