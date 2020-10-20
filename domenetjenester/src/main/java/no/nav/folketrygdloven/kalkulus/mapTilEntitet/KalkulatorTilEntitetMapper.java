package no.nav.folketrygdloven.kalkulus.mapTilEntitet;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetOverstyringerDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningRefusjonOverstyringerDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningRefusjonPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagArbeidstakerAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningAktivitetAggregatEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningAktivitetEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningAktivitetOverstyringEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningAktivitetOverstyringerEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningRefusjonOverstyringEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningRefusjonOverstyringerEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningRefusjonPeriodeEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagGrunnlagBuilder;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.FaktaAggregatEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.FaktaAktørEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.FaktaArbeidsforholdEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.Beløp;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.InternArbeidsforholdRef;
import no.nav.folketrygdloven.kalkulus.felles.jpa.IntervallEntitet;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.BeregningAktivitetHandlingType;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.FaktaOmBeregningTilfelle;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.OpptjeningAktivitetType;


/**
 * Skal etterhvert benytte seg av kontrakten som skal lages i ft-Kalkulus, benytter foreløping en, en-til-en mapping på klassenivå...
 *
 */
public class KalkulatorTilEntitetMapper {

    public static BeregningsgrunnlagEntitet mapBeregningsgrunnlag(BeregningsgrunnlagDto beregningsgrunnlagFraKalkulus) {
        BeregningsgrunnlagEntitet.Builder builder = BeregningsgrunnlagEntitet.builder();

        //med
        builder.medGrunnbeløp(new Beløp(beregningsgrunnlagFraKalkulus.getGrunnbeløp().getVerdi()));
        builder.medOverstyring(beregningsgrunnlagFraKalkulus.isOverstyrt());
        builder.medSkjæringstidspunkt(beregningsgrunnlagFraKalkulus.getSkjæringstidspunkt());
        if (beregningsgrunnlagFraKalkulus.getSammenligningsgrunnlag() != null) {
            builder.medSammenligningsgrunnlagOld(KalkulatorTilBGMapper.mapSammenligningsgrunnlag(beregningsgrunnlagFraKalkulus.getSammenligningsgrunnlag()));
        }

        //lister
        beregningsgrunnlagFraKalkulus.getAktivitetStatuser().forEach(beregningsgrunnlagAktivitetStatus -> builder.leggTilAktivitetStatus(KalkulatorTilBGMapper.mapAktivitetStatus(beregningsgrunnlagAktivitetStatus)));
        beregningsgrunnlagFraKalkulus.getBeregningsgrunnlagPerioder().forEach(beregningsgrunnlagPeriode -> builder.leggTilBeregningsgrunnlagPeriode(KalkulatorTilBGMapper.mapBeregningsgrunnlagPeriode(beregningsgrunnlagPeriode)));
        builder.leggTilFaktaOmBeregningTilfeller(beregningsgrunnlagFraKalkulus.getFaktaOmBeregningTilfeller().stream().map(fakta -> FaktaOmBeregningTilfelle.fraKode(fakta.getKode())).collect(Collectors.toList()));
        beregningsgrunnlagFraKalkulus.getSammenligningsgrunnlagPrStatusListe().forEach(sammenligningsgrunnlagPrStatus -> builder.leggTilSammenligningsgrunnlag(KalkulatorTilBGMapper.mapSammenligningsgrunnlagMedStatus(sammenligningsgrunnlagPrStatus)));

        return builder.build();
    }

    public static BeregningRefusjonOverstyringerEntitet mapRefusjonOverstyring(BeregningRefusjonOverstyringerDto refusjonOverstyringerFraKalkulus) {
        BeregningRefusjonOverstyringerEntitet.Builder entitetBuilder = BeregningRefusjonOverstyringerEntitet.builder();

        refusjonOverstyringerFraKalkulus.getRefusjonOverstyringer().forEach(beregningRefusjonOverstyring -> {
            BeregningRefusjonOverstyringEntitet.Builder builder = BeregningRefusjonOverstyringEntitet.builder()
                    .medArbeidsgiver(KalkulatorTilIAYMapper.mapArbeidsgiver(beregningRefusjonOverstyring.getArbeidsgiver()))
                    .medFørsteMuligeRefusjonFom(beregningRefusjonOverstyring.getFørsteMuligeRefusjonFom().orElse(null));
            beregningRefusjonOverstyring.getRefusjonPerioder().forEach(periode -> builder.leggTilRefusjonPeriode(mapRefusjonsperiode(periode)));
            entitetBuilder.leggTilOverstyring(builder.build());
        });
        return entitetBuilder.build();
    }

    private static BeregningRefusjonPeriodeEntitet mapRefusjonsperiode(BeregningRefusjonPeriodeDto periode) {
        InternArbeidsforholdRef ref = periode.getArbeidsforholdRef() == null ? null : KalkulatorTilIAYMapper.mapArbeidsforholdRef(periode.getArbeidsforholdRef());
        return new BeregningRefusjonPeriodeEntitet(ref, periode.getStartdatoRefusjon());
    }

    public static BeregningAktivitetAggregatEntitet mapSaksbehandletAktivitet(BeregningAktivitetAggregatDto saksbehandletAktiviteterFraKalkulus) {
        BeregningAktivitetAggregatEntitet.Builder entitetBuilder = BeregningAktivitetAggregatEntitet.builder();
        entitetBuilder.medSkjæringstidspunktOpptjening(saksbehandletAktiviteterFraKalkulus.getSkjæringstidspunktOpptjening());
        saksbehandletAktiviteterFraKalkulus.getBeregningAktiviteter().forEach(mapBeregningAktivitet(entitetBuilder));
        return entitetBuilder.build();
    }

    private static Consumer<BeregningAktivitetDto> mapBeregningAktivitet(BeregningAktivitetAggregatEntitet.Builder entitetBuilder) {
        return beregningAktivitet -> {
            BeregningAktivitetEntitet.Builder builder = BeregningAktivitetEntitet.builder();
            builder.medArbeidsforholdRef(beregningAktivitet.getArbeidsforholdRef() == null ? null : KalkulatorTilIAYMapper.mapArbeidsforholdRef(beregningAktivitet.getArbeidsforholdRef()));
            builder.medArbeidsgiver(beregningAktivitet.getArbeidsgiver() == null ? null : KalkulatorTilIAYMapper.mapArbeidsgiver(beregningAktivitet.getArbeidsgiver()));
            builder.medOpptjeningAktivitetType(OpptjeningAktivitetType.fraKode(beregningAktivitet.getOpptjeningAktivitetType().getKode()));
            builder.medPeriode(mapDatoIntervall(beregningAktivitet.getPeriode()));
            entitetBuilder.leggTilAktivitet(builder.build());
        };
    }

    public static BeregningAktivitetOverstyringerEntitet mapAktivitetOverstyring(BeregningAktivitetOverstyringerDto beregningAktivitetOverstyringerFraKalkulus) {
        BeregningAktivitetOverstyringerEntitet.Builder entitetBuilder = BeregningAktivitetOverstyringerEntitet.builder();
        beregningAktivitetOverstyringerFraKalkulus.getOverstyringer().forEach(overstyring -> {
            BeregningAktivitetOverstyringEntitet.Builder builder = BeregningAktivitetOverstyringEntitet.builder();
            builder.medArbeidsforholdRef(overstyring.getArbeidsforholdRef() == null ? null : KalkulatorTilIAYMapper.mapArbeidsforholdRef(overstyring.getArbeidsforholdRef()));
            overstyring.getArbeidsgiver().ifPresent(arbeidsgiver -> builder.medArbeidsgiver(KalkulatorTilIAYMapper.mapArbeidsgiver(arbeidsgiver)));
            builder.medHandling(overstyring.getHandling() == null ? null : BeregningAktivitetHandlingType.fraKode(overstyring.getHandling().getKode()));
            builder.medOpptjeningAktivitetType(OpptjeningAktivitetType.fraKode(overstyring.getOpptjeningAktivitetType().getKode()));
            builder.medPeriode(mapDatoIntervall(overstyring.getPeriode()));
            entitetBuilder.leggTilOverstyring(builder.build());
        });
        return entitetBuilder.build();
    }

    private static IntervallEntitet mapDatoIntervall(Intervall periode) {
        return IntervallEntitet.fraOgMedTilOgMed(periode.getFomDato(), periode.getTomDato());
    }

    public static BeregningsgrunnlagGrunnlagBuilder mapGrunnlag(BeregningsgrunnlagGrunnlagDto beregningsgrunnlagFraKalkulus) {
        BeregningsgrunnlagGrunnlagBuilder oppdatere = BeregningsgrunnlagGrunnlagBuilder.oppdatere(Optional.empty());

        beregningsgrunnlagFraKalkulus.getBeregningsgrunnlag().ifPresent(beregningsgrunnlagDto -> oppdatere.medBeregningsgrunnlag(mapBeregningsgrunnlag(beregningsgrunnlagDto)));
        beregningsgrunnlagFraKalkulus.getOverstyring().ifPresent(beregningAktivitetOverstyringerDto -> oppdatere.medOverstyring(mapAktivitetOverstyring(beregningAktivitetOverstyringerDto)));
        oppdatere.medRegisterAktiviteter(mapRegisterAktiviteter(beregningsgrunnlagFraKalkulus.getRegisterAktiviteter()));
        beregningsgrunnlagFraKalkulus.getSaksbehandletAktiviteter().ifPresent(beregningAktivitetAggregatDto -> oppdatere.medSaksbehandletAktiviteter(mapSaksbehandletAktivitet(beregningAktivitetAggregatDto)));
        beregningsgrunnlagFraKalkulus.getRefusjonOverstyringer().ifPresent(beregningRefusjonOverstyringerDto -> oppdatere.medRefusjonOverstyring(mapRefusjonOverstyring(beregningRefusjonOverstyringerDto)));

        return oppdatere;
    }

    private static BeregningAktivitetAggregatEntitet mapRegisterAktiviteter(BeregningAktivitetAggregatDto registerAktiviteter) {
        BeregningAktivitetAggregatEntitet.Builder builder = BeregningAktivitetAggregatEntitet.builder();
        registerAktiviteter.getBeregningAktiviteter().forEach(mapBeregningAktivitet(builder));
        builder.medSkjæringstidspunktOpptjening(registerAktiviteter.getSkjæringstidspunktOpptjening());
        return builder.build();
    }


    public static Optional<FaktaAggregatEntitet> mapFaktaAggregat(BeregningsgrunnlagDto beregningsgrunnlagDto) {
        // I fakta om beregning settes alle faktaavklaringer på første periode og vi kan derfor bruke denne til å hente ut avklart fakta
        BeregningsgrunnlagPeriodeDto førstePeriode = beregningsgrunnlagDto.getBeregningsgrunnlagPerioder().get(0);
        List<BeregningsgrunnlagPrStatusOgAndelDto> andeler = førstePeriode.getBeregningsgrunnlagPrStatusOgAndelList();
        FaktaAggregatEntitet.Builder faktaAggregatBuilder = FaktaAggregatEntitet.builder();
        mapFaktaArbeidsforhold(andeler).forEach(faktaAggregatBuilder::leggTilFaktaArbeidsforhold);
        mapFaktaAktør(andeler, beregningsgrunnlagDto.getFaktaOmBeregningTilfeller())
        .ifPresent(faktaAggregatBuilder::medFaktaAktør);
        return faktaAggregatBuilder.manglerFakta() ? Optional.empty() : Optional.of(faktaAggregatBuilder.build());
    }

    private static List<FaktaArbeidsforholdEntitet> mapFaktaArbeidsforhold(List<BeregningsgrunnlagPrStatusOgAndelDto> andeler) {
        return andeler.stream()
                .filter(a -> a.getAktivitetStatus().erArbeidstaker())
                .filter(a -> a.getArbeidsgiver().isPresent())
                .map(a -> {
                    FaktaArbeidsforholdEntitet.Builder builder = FaktaArbeidsforholdEntitet.builder();
                    builder.medArbeidsgiver(KalkulatorTilIAYMapper.mapArbeidsgiver(a.getArbeidsgiver().get()));
                    builder.medArbeidsforholdRef(a.getArbeidsforholdRef().map(KalkulatorTilIAYMapper::mapArbeidsforholdRef).orElse(null));
                    a.getBgAndelArbeidsforhold().map(BGAndelArbeidsforholdDto::getErTidsbegrensetArbeidsforhold).ifPresent(builder::medErTidsbegrenset);
                    a.getBeregningsgrunnlagArbeidstakerAndel().map(BeregningsgrunnlagArbeidstakerAndelDto::getMottarYtelse).ifPresent(builder::medHarMottattYtelse);
                    return builder.erUgyldig() ? null : builder.build();
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private static Optional<FaktaAktørEntitet> mapFaktaAktør(List<BeregningsgrunnlagPrStatusOgAndelDto> andeler, List<FaktaOmBeregningTilfelle> faktaOmBeregningTilfeller) {
        FaktaAktørEntitet.Builder faktaAktørBuilder = FaktaAktørEntitet.builder();
        mapEtterlønnSluttpakke(faktaOmBeregningTilfeller, faktaAktørBuilder);
        mapMottarFLYtelse(andeler, faktaOmBeregningTilfeller, faktaAktørBuilder);
        mapErNyIArbeidslivetSN(andeler, faktaOmBeregningTilfeller, faktaAktørBuilder);
        mapSkalBesteberegnes(faktaOmBeregningTilfeller, faktaAktørBuilder);
        mapErNyoppstartetFL(andeler, faktaOmBeregningTilfeller, faktaAktørBuilder);
        return faktaAktørBuilder.erUgyldig() ? Optional.empty() : Optional.of(faktaAktørBuilder.build());
    }

    private static void mapErNyoppstartetFL(List<BeregningsgrunnlagPrStatusOgAndelDto> andeler, List<FaktaOmBeregningTilfelle> faktaOmBeregningTilfeller, FaktaAktørEntitet.Builder faktaAktørBuilder) {
        boolean harVurdertNyoppstartetFL = faktaOmBeregningTilfeller.stream().anyMatch(tilfelle -> tilfelle.equals(FaktaOmBeregningTilfelle.VURDER_NYOPPSTARTET_FL));
        if (harVurdertNyoppstartetFL) {
            andeler.stream().filter(a -> a.getAktivitetStatus().erFrilanser() && a.erNyoppstartet().isPresent())
                    .findFirst()
                    .map(BeregningsgrunnlagPrStatusOgAndelDto::erNyoppstartet)
                    .map(Optional::get)
                    .ifPresent(faktaAktørBuilder::medErNyoppstartetFL);
        }
    }

    private static void mapSkalBesteberegnes(List<FaktaOmBeregningTilfelle> faktaOmBeregningTilfeller, FaktaAktørEntitet.Builder faktaAktørBuilder) {
        boolean harVurdertBesteberegning = faktaOmBeregningTilfeller.stream().anyMatch(tilfelle -> tilfelle.equals(FaktaOmBeregningTilfelle.VURDER_BESTEBEREGNING));
        if (harVurdertBesteberegning) {
            boolean harFastsattBesteberegning = faktaOmBeregningTilfeller.stream().anyMatch(tilfelle -> tilfelle.equals(FaktaOmBeregningTilfelle.FASTSETT_BESTEBEREGNING_FØDENDE_KVINNE));
            faktaAktørBuilder.medSkalBesteberegnes(harFastsattBesteberegning);
        }
    }

    private static void mapErNyIArbeidslivetSN(List<BeregningsgrunnlagPrStatusOgAndelDto> andeler, List<FaktaOmBeregningTilfelle> faktaOmBeregningTilfeller, FaktaAktørEntitet.Builder faktaAktørBuilder) {
        boolean harVurdertNyIArbeidslivetSN = faktaOmBeregningTilfeller.stream().anyMatch(tilfelle -> tilfelle.equals(FaktaOmBeregningTilfelle.VURDER_SN_NY_I_ARBEIDSLIVET));
        if (harVurdertNyIArbeidslivetSN) {
            andeler.stream().filter(a -> a.getAktivitetStatus().erSelvstendigNæringsdrivende())
                    .findFirst()
                    .map(BeregningsgrunnlagPrStatusOgAndelDto::getNyIArbeidslivet)
                    .ifPresent(faktaAktørBuilder::medErNyIArbeidslivetSN);
        }
    }

    private static void mapMottarFLYtelse(List<BeregningsgrunnlagPrStatusOgAndelDto> andeler, List<FaktaOmBeregningTilfelle> faktaOmBeregningTilfeller, FaktaAktørEntitet.Builder faktaAktørBuilder) {
        boolean harVurdertMottarYtelse = faktaOmBeregningTilfeller.stream().anyMatch(tilfelle -> tilfelle.equals(FaktaOmBeregningTilfelle.VURDER_MOTTAR_YTELSE));
        if (harVurdertMottarYtelse) {
            andeler.stream().filter(a -> a.getAktivitetStatus().erFrilanser() && a.mottarYtelse().isPresent())
                    .findFirst()
                    .map(BeregningsgrunnlagPrStatusOgAndelDto::mottarYtelse)
                    .map(Optional::get)
                    .ifPresent(faktaAktørBuilder::medHarFLMottattYtelse);
        }
    }

    private static void mapEtterlønnSluttpakke(List<FaktaOmBeregningTilfelle> faktaOmBeregningTilfeller, FaktaAktørEntitet.Builder faktaAktørBuilder) {
        boolean harVurdertEtterlønnSluttpakke = faktaOmBeregningTilfeller.stream().anyMatch(tilfelle -> tilfelle.equals(FaktaOmBeregningTilfelle.VURDER_ETTERLØNN_SLUTTPAKKE));
        boolean harEtterlønnSlutpakke = faktaOmBeregningTilfeller.stream().anyMatch(tilfelle -> tilfelle.equals(FaktaOmBeregningTilfelle.FASTSETT_ETTERLØNN_SLUTTPAKKE));
        if (harVurdertEtterlønnSluttpakke) {
            faktaAktørBuilder.medMottarEtterlønnSluttpakke(harEtterlønnSlutpakke);
        }
    }


}
