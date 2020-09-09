package no.nav.folketrygdloven.kalkulator.skjæringstidspunkt;

import static no.nav.folketrygdloven.kalkulator.modell.opptjening.OpptjeningAktivitetType.ARBEID;
import static no.nav.folketrygdloven.kalkulator.modell.opptjening.OpptjeningAktivitetType.VENTELØNN_VARTPENGER;
import static no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.FagsakYtelseType.ARBEIDSAVKLARINGSPENGER;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.kalkulator.AvklarAktiviteterTjeneste;
import no.nav.folketrygdloven.kalkulator.KoblingReferanseMock;
import no.nav.folketrygdloven.kalkulator.modell.behandling.KoblingReferanse;
import no.nav.folketrygdloven.kalkulator.modell.behandling.Skjæringstidspunkt;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagAktivitetStatusDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.AktørYtelseDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseAggregatBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.VersjonTypeDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YtelseAnvistDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YtelseDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.opptjening.OpptjeningAktivitetType;
import no.nav.folketrygdloven.kalkulator.modell.typer.AktørId;
import no.nav.folketrygdloven.kalkulator.modell.virksomhet.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.FagsakYtelseType;
import no.nav.vedtak.util.Tuple;


public class AvklarAktiviteterTjenesteImplTest {
    private static final LocalDate SKJÆRINGSTIDSPUNKT_BEREGNING = LocalDate.of(2018, 9, 30);
    private static final Arbeidsgiver ARBEIDSGIVER = Arbeidsgiver.virksomhet("900050001");
    private static final AktørId AKTØR_ID = AktørId.dummy();
    private static final long BEHANDLING_ID = 4234034L;

    private KoblingReferanse koblingReferanse = new KoblingReferanseMock();

    @BeforeEach
    public void setUp() {
        koblingReferanse = nyBehandling();
    }

    @Test
    public void skal_returnere_false_om_ingen_aktiviteter() {
        // Arrange
        BeregningAktivitetAggregatDto beregningAktivitetAggregat = BeregningAktivitetAggregatDto.builder()
            .medSkjæringstidspunktOpptjening(SKJÆRINGSTIDSPUNKT_BEREGNING)
            .build();

        // Act
        boolean harVentelønnEllerVartpenger = AvklarAktiviteterTjeneste.harVentelønnEllerVartpengerSomSisteAktivitetIOpptjeningsperioden(beregningAktivitetAggregat);

        // Assert
        assertThat(harVentelønnEllerVartpenger).isFalse();
    }

    @Test
    public void skal_returnere_false_om_aktiviteter_som_ikke_er_ventelønn_vartpenger() {
        // Arrange
        BeregningAktivitetDto arbeidAktivitet = lagBeregningAktivitetAggregat(SKJÆRINGSTIDSPUNKT_BEREGNING.minusMonths(10),
            SKJÆRINGSTIDSPUNKT_BEREGNING, ARBEID);
        BeregningAktivitetAggregatDto beregningAktivitetAggregat = BeregningAktivitetAggregatDto.builder()
            .medSkjæringstidspunktOpptjening(SKJÆRINGSTIDSPUNKT_BEREGNING)
            .leggTilAktivitet(
                arbeidAktivitet
            ).build();

        // Act
        boolean harVentelønnEllerVartpenger = AvklarAktiviteterTjeneste.harVentelønnEllerVartpengerSomSisteAktivitetIOpptjeningsperioden(beregningAktivitetAggregat);

        // Assert
        assertThat(harVentelønnEllerVartpenger).isFalse();
    }

    @Test
    public void skal_returnere_false_om_ventelønn_vertpenger_ikke_er_siste_aktivitet() {
        // Arrange
        BeregningAktivitetDto arbeidsperiode = lagBeregningAktivitetAggregat(SKJÆRINGSTIDSPUNKT_BEREGNING.minusMonths(10),
            SKJÆRINGSTIDSPUNKT_BEREGNING, ARBEID);
        BeregningAktivitetDto ventelønnVartpenger = lagBeregningAktivitetAggregat(SKJÆRINGSTIDSPUNKT_BEREGNING.minusMonths(10),
            SKJÆRINGSTIDSPUNKT_BEREGNING.minusDays(1), VENTELØNN_VARTPENGER);
        BeregningAktivitetAggregatDto beregningAktivitetAggregat = BeregningAktivitetAggregatDto.builder()
            .medSkjæringstidspunktOpptjening(SKJÆRINGSTIDSPUNKT_BEREGNING)
            .leggTilAktivitet(arbeidsperiode)
            .leggTilAktivitet(ventelønnVartpenger)
            .build();

        // Act
        boolean harVentelønnEllerVartpenger = AvklarAktiviteterTjeneste.harVentelønnEllerVartpengerSomSisteAktivitetIOpptjeningsperioden(beregningAktivitetAggregat);

        // Assert
        assertThat(harVentelønnEllerVartpenger).isFalse();
    }

    @Test
    public void skal_returnere_true_om_ventelønn_vertpenger_avslutter_samtidig_med_siste_aktivitet() {
        // Arrange
        BeregningAktivitetDto arbeidsperiode = lagBeregningAktivitetAggregat(SKJÆRINGSTIDSPUNKT_BEREGNING.minusMonths(10),
            SKJÆRINGSTIDSPUNKT_BEREGNING, ARBEID);
        BeregningAktivitetDto ventelønnVartpenger = lagBeregningAktivitetAggregat(SKJÆRINGSTIDSPUNKT_BEREGNING.minusMonths(10),
            SKJÆRINGSTIDSPUNKT_BEREGNING, VENTELØNN_VARTPENGER);
        BeregningAktivitetAggregatDto beregningAktivitetAggregat = BeregningAktivitetAggregatDto.builder()
            .medSkjæringstidspunktOpptjening(SKJÆRINGSTIDSPUNKT_BEREGNING)
            .leggTilAktivitet(arbeidsperiode)
            .leggTilAktivitet(ventelønnVartpenger)
            .build();

        // Act
        boolean harVentelønnEllerVartpenger = AvklarAktiviteterTjeneste.harVentelønnEllerVartpengerSomSisteAktivitetIOpptjeningsperioden(beregningAktivitetAggregat);

        // Assert
        assertThat(harVentelønnEllerVartpenger).isTrue();
    }

    @Test
    public void skal_returnere_true_om_ventelønn_vartpenger_avslutter_etter_arbeidsaktivitet_som_slutter_dagen_før_skjæringstidspunkt() {
        // Arrange
        BeregningAktivitetDto arbeidsperiode = lagBeregningAktivitetAggregat(SKJÆRINGSTIDSPUNKT_BEREGNING.minusMonths(10),
            SKJÆRINGSTIDSPUNKT_BEREGNING.minusDays(1), ARBEID);
        BeregningAktivitetDto ventelønnVartpenger = lagBeregningAktivitetAggregat(SKJÆRINGSTIDSPUNKT_BEREGNING.minusMonths(10),
            SKJÆRINGSTIDSPUNKT_BEREGNING.plusDays(1), VENTELØNN_VARTPENGER);
        BeregningAktivitetAggregatDto beregningAktivitetAggregat = BeregningAktivitetAggregatDto.builder()
            .medSkjæringstidspunktOpptjening(SKJÆRINGSTIDSPUNKT_BEREGNING)
            .leggTilAktivitet(arbeidsperiode)
            .leggTilAktivitet(ventelønnVartpenger)
            .build();

        // Act
        boolean harVentelønnEllerVartpenger = AvklarAktiviteterTjeneste.harVentelønnEllerVartpengerSomSisteAktivitetIOpptjeningsperioden(beregningAktivitetAggregat);

        // Assert
        assertThat(harVentelønnEllerVartpenger).isTrue();
    }

    @Test
    public void skal_returnere_true_om_ventelønn_vartpenger_sammen_med_arbeid_som_starter_på_skjæringstidspunkt() {
        // Arrange
        BeregningAktivitetDto arbeidsperiode = lagBeregningAktivitetAggregat(SKJÆRINGSTIDSPUNKT_BEREGNING, SKJÆRINGSTIDSPUNKT_BEREGNING.plusDays(10), ARBEID);
        BeregningAktivitetDto ventelønnVartpenger = lagBeregningAktivitetAggregat(SKJÆRINGSTIDSPUNKT_BEREGNING.minusMonths(10),
            SKJÆRINGSTIDSPUNKT_BEREGNING.plusDays(1), VENTELØNN_VARTPENGER);
        BeregningAktivitetAggregatDto beregningAktivitetAggregat = BeregningAktivitetAggregatDto.builder()
            .medSkjæringstidspunktOpptjening(SKJÆRINGSTIDSPUNKT_BEREGNING)
            .leggTilAktivitet(arbeidsperiode)
            .leggTilAktivitet(ventelønnVartpenger)
            .build();

        // Act
        boolean harVentelønnEllerVartpenger = AvklarAktiviteterTjeneste.harVentelønnEllerVartpengerSomSisteAktivitetIOpptjeningsperioden(beregningAktivitetAggregat);

        // Assert
        assertThat(harVentelønnEllerVartpenger).isTrue();
    }

    @Test
    public void skal_returnere_false_om_ventelønn_vartpenger_starter_på_skjæringstidspunkt() {
        // Arrange
        BeregningAktivitetDto arbeidsperiode = lagBeregningAktivitetAggregat(SKJÆRINGSTIDSPUNKT_BEREGNING.minusMonths(10),
            SKJÆRINGSTIDSPUNKT_BEREGNING.plusDays(10), ARBEID);
        BeregningAktivitetDto ventelønnVartpenger = lagBeregningAktivitetAggregat(SKJÆRINGSTIDSPUNKT_BEREGNING,
            SKJÆRINGSTIDSPUNKT_BEREGNING.plusDays(1), VENTELØNN_VARTPENGER);
        BeregningAktivitetAggregatDto beregningAktivitetAggregat = BeregningAktivitetAggregatDto.builder()
            .medSkjæringstidspunktOpptjening(SKJÆRINGSTIDSPUNKT_BEREGNING)
            .leggTilAktivitet(arbeidsperiode)
            .leggTilAktivitet(ventelønnVartpenger)
            .build();

        // Act
        boolean harVentelønnEllerVartpenger = AvklarAktiviteterTjeneste.harVentelønnEllerVartpengerSomSisteAktivitetIOpptjeningsperioden(beregningAktivitetAggregat);

        // Assert
        assertThat(harVentelønnEllerVartpenger).isFalse();
    }

    @Test
    public void skal_returnere_false_når_ikke_AAP() {
        //Arrange
        BeregningsgrunnlagDto beregningsgrunnlag = beregningsgrunnlagMedStatus(AktivitetStatus.ARBEIDSTAKER);

        //Act
        boolean harFullAAPMedAndreAktiviteter = AvklarAktiviteterTjeneste.harFullAAPPåStpMedAndreAktiviteter(beregningsgrunnlag, Optional.empty(), FagsakYtelseType.FORELDREPENGER);

        //Assert
        assertThat(harFullAAPMedAndreAktiviteter).isFalse();
    }

    @Test
    public void skal_returnere_false_når_bare_AAP_uten_andre_aktiviteter_på_stp() {
        //Arrange
        BeregningsgrunnlagDto beregningsgrunnlag = beregningsgrunnlagMedStatus(AktivitetStatus.ARBEIDSAVKLARINGSPENGER);

        //Act
        boolean harFullAAPMedAndreAktiviteter = AvklarAktiviteterTjeneste.harFullAAPPåStpMedAndreAktiviteter(beregningsgrunnlag, Optional.empty(), FagsakYtelseType.FORELDREPENGER);

        //Assert
        assertThat(harFullAAPMedAndreAktiviteter).isFalse();
    }

    @Test
    public void skal_returnere_false_når_AAP_med_andre_aktiviteter_på_stp_med_siste_meldekort_uten_full_utbetaling() {
        //Arrange
        BeregningsgrunnlagDto beregningsgrunnlag = beregningsgrunnlagMedStatus(AktivitetStatus.ARBEIDSAVKLARINGSPENGER, AktivitetStatus.ARBEIDSTAKER);

        Tuple<Periode, Integer> meldekort1 = lagMeldekort(SKJÆRINGSTIDSPUNKT_BEREGNING.minusDays(15), 200);
        Tuple<Periode, Integer> meldekort2 = lagMeldekort(SKJÆRINGSTIDSPUNKT_BEREGNING.minusDays(1), 180);
        InntektArbeidYtelseGrunnlagDto iayGrunnlag = lagAktørYtelse(meldekort1, meldekort2);

        //Act
        boolean harFullAAPMedAndreAktiviteter = AvklarAktiviteterTjeneste.harFullAAPPåStpMedAndreAktiviteter(beregningsgrunnlag, getAktørYtelseFraRegister(koblingReferanse, iayGrunnlag), FagsakYtelseType.FORELDREPENGER);

        //Assert
        assertThat(harFullAAPMedAndreAktiviteter).isFalse();
    }

    @Test
    public void skal_returnere_true_når_AAP_med_andre_aktiviteter_på_stp_med_siste_meldekort_med_full_utbetaling() {
        //Arrange
        BeregningsgrunnlagDto beregningsgrunnlag = beregningsgrunnlagMedStatus(AktivitetStatus.ARBEIDSAVKLARINGSPENGER, AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE);

        Tuple<Periode, Integer> meldekort1 = lagMeldekort(SKJÆRINGSTIDSPUNKT_BEREGNING.minusDays(15), 99);
        Tuple<Periode, Integer> meldekort2 = lagMeldekort(SKJÆRINGSTIDSPUNKT_BEREGNING.minusDays(1), 200);
        Tuple<Periode, Integer> meldekort3 = lagMeldekort(SKJÆRINGSTIDSPUNKT_BEREGNING.plusDays(1), 179); //Skal ikke tas med siden avsluttes etter stp
        InntektArbeidYtelseGrunnlagDto iayGrunnlag = lagAktørYtelse(meldekort1, meldekort2, meldekort3);

        //Act
        boolean harFullAAPMedAndreAktiviteter = AvklarAktiviteterTjeneste.harFullAAPPåStpMedAndreAktiviteter(beregningsgrunnlag, getAktørYtelseFraRegister(koblingReferanse, iayGrunnlag), FagsakYtelseType.FORELDREPENGER);

        //Assert
        assertThat(harFullAAPMedAndreAktiviteter).isTrue();
    }


    private BeregningsgrunnlagDto beregningsgrunnlagMedStatus(AktivitetStatus... aktivitetStatus) {
        BeregningsgrunnlagDto.Builder builder = BeregningsgrunnlagDto.builder();
        builder.medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT_BEREGNING);
        Stream.of(aktivitetStatus).forEach(status -> builder.leggTilAktivitetStatus(BeregningsgrunnlagAktivitetStatusDto.builder().medAktivitetStatus(status)));
        return builder.build();
    }


    private BeregningAktivitetDto lagBeregningAktivitetAggregat(LocalDate fom, LocalDate tom, OpptjeningAktivitetType type) {
        return BeregningAktivitetDto.builder()
            .medArbeidsgiver(ARBEIDSGIVER)
            .medOpptjeningAktivitetType(type)
            .medPeriode(Intervall.fraOgMedTilOgMed(fom, tom))
            .build();
    }

    @SafeVarargs
    private InntektArbeidYtelseGrunnlagDto lagAktørYtelse(Tuple<Periode, Integer>... meldekortPerioder) {
        InntektArbeidYtelseAggregatBuilder inntektArbeidYtelseAggregatBuilder = InntektArbeidYtelseAggregatBuilder.oppdatere(Optional.empty(), VersjonTypeDto.REGISTER);
        InntektArbeidYtelseAggregatBuilder.AktørYtelseBuilder aktørYtelseBuilder = inntektArbeidYtelseAggregatBuilder.getAktørYtelseBuilder(AKTØR_ID);
        YtelseDtoBuilder ytelseBuilder = aktørYtelseBuilder.getYtelselseBuilderForType(ARBEIDSAVKLARINGSPENGER, Intervall.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT_BEREGNING.minusMonths(6), SKJÆRINGSTIDSPUNKT_BEREGNING.minusDays(1)));
        if (meldekortPerioder != null && meldekortPerioder.length > 0) {
            Stream.of(meldekortPerioder).forEach(meldekort -> ytelseBuilder.leggTilYtelseAnvist(lagYtelseAnvist(ytelseBuilder, meldekort.getElement1(), meldekort.getElement2())));
        }
        aktørYtelseBuilder.leggTilYtelse(ytelseBuilder);
        inntektArbeidYtelseAggregatBuilder.leggTilAktørYtelse(aktørYtelseBuilder);
        return InntektArbeidYtelseGrunnlagDtoBuilder.nytt().medData(inntektArbeidYtelseAggregatBuilder).build();
    }

    private YtelseAnvistDto lagYtelseAnvist(YtelseDtoBuilder ytelseBuilder, Periode periode, int utbetalingsgrad) {
        return ytelseBuilder.getAnvistBuilder()
            .medAnvistPeriode(Intervall.fraOgMedTilOgMed(periode.getFom(), periode.getTom()))
            .medUtbetalingsgradProsent(BigDecimal.valueOf(utbetalingsgrad))
            .medDagsats(BigDecimal.valueOf(1000))
            .medBeløp(BigDecimal.valueOf(10000))
            .build();
    }

    private Tuple<Periode, Integer> lagMeldekort(LocalDate tom, int utbetalingsgrad) {
        return new Tuple<>(Periode.of(tom.minusDays(13), tom), utbetalingsgrad);
    }

    private KoblingReferanse nyBehandling() {
        return KoblingReferanse.fra(
            FagsakYtelseType.FORELDREPENGER,
                AKTØR_ID,
            BEHANDLING_ID,
            UUID.randomUUID(),
            Optional.empty(),
            Skjæringstidspunkt.builder().medFørsteUttaksdato(SKJÆRINGSTIDSPUNKT_BEREGNING).build()
        );
    }

    private Optional<AktørYtelseDto> getAktørYtelseFraRegister(KoblingReferanse ref, InntektArbeidYtelseGrunnlagDto iayGrunnlag) {
        return iayGrunnlag.getAktørYtelseFraRegister(ref.getAktørId());
    }
}
