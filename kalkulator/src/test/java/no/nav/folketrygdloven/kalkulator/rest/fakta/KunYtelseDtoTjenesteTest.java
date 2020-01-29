package no.nav.folketrygdloven.kalkulator.rest.fakta;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulator.BehandlingReferanseMock;
import no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.ForeldrepengerGrunnlag;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagRestInput;
import no.nav.folketrygdloven.kalkulator.modell.behandling.BehandlingReferanse;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetAggregatRestDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetRestDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagAktivitetStatusRestDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagRestDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagRestDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeRestDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelRestDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagRestDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.kodeverk.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.kodeverk.Inntektskategori;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.opptjening.OpptjeningAktivitetType;
import no.nav.folketrygdloven.kalkulator.rest.dto.AndelMedBeløpDto;
import no.nav.folketrygdloven.kalkulator.rest.dto.KunYtelseDto;
import no.nav.folketrygdloven.kalkulator.testutilities.behandling.beregningsgrunnlag.BeregningAktivitetTestUtil;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;

public class KunYtelseDtoTjenesteTest {

    private static final LocalDate SKJÆRINGSTIDSPUNKT_OPPTJENING = LocalDate.of(2018, Month.MAY, 10);
    private static final int BRUTTO_PR_ÅR = 10000;
    private BehandlingReferanse behandlingReferanse = new BehandlingReferanseMock(SKJÆRINGSTIDSPUNKT_OPPTJENING);

    private KunYtelseDtoTjeneste kunYtelseDtoTjeneste;

    @BeforeEach
    public void setUp() {
        this.kunYtelseDtoTjeneste = new KunYtelseDtoTjeneste();
    }

    @Test
    public void fødende_kvinne_uten_dagpenger() {
        // Arrange
        Intervall periode = Intervall.fraOgMedTilOgMed(
            SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(8),
            SKJÆRINGSTIDSPUNKT_OPPTJENING.minusDays(1));
        BeregningAktivitetAggregatRestDto beregningAktivitetAggregat = BeregningAktivitetTestUtil.opprettBeregningAktiviteter(SKJÆRINGSTIDSPUNKT_OPPTJENING, periode, OpptjeningAktivitetType.SYKEPENGER);
        var beregningsgrunnlagGrunnlag = lagBeregningsgrunnlag(beregningAktivitetAggregat);
        ForeldrepengerGrunnlag medBesteberegning = new ForeldrepengerGrunnlag(100, false);

        // Act
        var input = new BeregningsgrunnlagRestInput(behandlingReferanse, InntektArbeidYtelseGrunnlagDtoBuilder.nytt().build(), null, List.of(), medBesteberegning)
                .medBeregningsgrunnlagGrunnlag(beregningsgrunnlagGrunnlag);

        KunYtelseDto kunytelse = kunYtelseDtoTjeneste.lagKunYtelseDto(input);

        // Assert
        assertAndel(kunytelse);
        assertThat(kunytelse.isFodendeKvinneMedDP()).isFalse();
        assertThat(kunytelse.getErBesteberegning()).isNull();
    }

    @Test
    public void fødende_kvinne_med_dagpenger() {
        // Arrange
        BeregningAktivitetAggregatRestDto beregningAktivitetAggregat = beregningAktivitetSykepengerOgDagpenger();
        var beregningsgrunnlagGrunnlag = lagBeregningsgrunnlag(beregningAktivitetAggregat);
        ForeldrepengerGrunnlag medBesteberegning = new ForeldrepengerGrunnlag(100, true);

        // Act
        var input = new BeregningsgrunnlagRestInput(behandlingReferanse, InntektArbeidYtelseGrunnlagDtoBuilder.nytt().build(), null, List.of(), medBesteberegning)
                .medBeregningsgrunnlagGrunnlag(beregningsgrunnlagGrunnlag);

        KunYtelseDto kunytelse = kunYtelseDtoTjeneste.lagKunYtelseDto(input);

        // Assert
        assertAndel(kunytelse);
        assertThat(kunytelse.isFodendeKvinneMedDP()).isTrue();
        assertThat(kunytelse.getErBesteberegning()).isNull();
    }

    @Test
    public void adopsjon_kvinne_med_dagpenger() {
        // Arrange
        BeregningAktivitetAggregatRestDto beregningAktivitetAggregat = beregningAktivitetSykepengerOgDagpenger();

        var beregningsgrunnlagGrunnlag = lagBeregningsgrunnlag(beregningAktivitetAggregat);
        ForeldrepengerGrunnlag utenBesteberegning = new ForeldrepengerGrunnlag(100, false);

        // Act
        var input = new BeregningsgrunnlagRestInput(behandlingReferanse, InntektArbeidYtelseGrunnlagDtoBuilder.nytt().build(), null, List.of(), utenBesteberegning)
                .medBeregningsgrunnlagGrunnlag(beregningsgrunnlagGrunnlag);

        KunYtelseDto kunytelse = kunYtelseDtoTjeneste.lagKunYtelseDto(input);

        // Assert
        assertAndel(kunytelse);
        assertThat(kunytelse.isFodendeKvinneMedDP()).isFalse();
        assertThat(kunytelse.getErBesteberegning()).isNull();
    }

    private BeregningAktivitetAggregatRestDto beregningAktivitetSykepengerOgDagpenger() {
        BeregningAktivitetAggregatRestDto.Builder builder = BeregningAktivitetAggregatRestDto.builder()
            .medSkjæringstidspunktOpptjening(SKJÆRINGSTIDSPUNKT_OPPTJENING);
        builder.leggTilAktivitet(BeregningAktivitetRestDto.builder()
            .medPeriode(Intervall.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(8), SKJÆRINGSTIDSPUNKT_OPPTJENING.minusDays(1)))
            .medOpptjeningAktivitetType(OpptjeningAktivitetType.SYKEPENGER)
            .build());
        builder.leggTilAktivitet(BeregningAktivitetRestDto.builder()
            .medPeriode(Intervall.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(12), SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(8).minusDays(1)))
            .medOpptjeningAktivitetType(OpptjeningAktivitetType.DAGPENGER)
            .build());
        return builder.build();
    }

    @Test
    public void mann_med_dagpenger() {
        // Arrange
        BeregningAktivitetAggregatRestDto beregningAktivitetAggregat = beregningAktivitetSykepengerOgDagpenger();
        var beregningsgrunnlagGrunnlag = lagBeregningsgrunnlag(beregningAktivitetAggregat);
        ForeldrepengerGrunnlag utenBesteberegning = new ForeldrepengerGrunnlag(100, false);

        // Act
        var input = new BeregningsgrunnlagRestInput(behandlingReferanse, InntektArbeidYtelseGrunnlagDtoBuilder.nytt().build(), null, List.of(), utenBesteberegning)
                .medBeregningsgrunnlagGrunnlag(beregningsgrunnlagGrunnlag);

        KunYtelseDto kunytelse = kunYtelseDtoTjeneste.lagKunYtelseDto(input);

        // Assert
        assertAndel(kunytelse);
        assertThat(kunytelse.isFodendeKvinneMedDP()).isFalse();
        assertThat(kunytelse.getErBesteberegning()).isNull();
    }

    @Test
    public void skal_sette_verdier_om_forrige_grunnlag_var_kun_ytelse() {
        // Arrange
        BeregningAktivitetAggregatRestDto beregningAktivitetAggregat = beregningAktivitetSykepengerOgDagpenger();
        var beregningsgrunnlagGrunnlag = lagForrigeBeregningsgrunnlagMedLagtTilAndel(beregningAktivitetAggregat);
        ForeldrepengerGrunnlag utenBesteberegning = new ForeldrepengerGrunnlag(100, false);
        // Act
        var input = new BeregningsgrunnlagRestInput(behandlingReferanse, InntektArbeidYtelseGrunnlagDtoBuilder.nytt().build(), null, List.of(), utenBesteberegning)
                .medBeregningsgrunnlagGrunnlag(beregningsgrunnlagGrunnlag);

        KunYtelseDto kunytelse = kunYtelseDtoTjeneste.lagKunYtelseDto(input);

        // Assert
        List<AndelMedBeløpDto> andeler = kunytelse.getAndeler();
        assertThat(andeler).hasSize(2);
        assertThat(andeler.get(0).getAktivitetStatus()).isEqualTo(AktivitetStatus.BRUKERS_ANDEL);
        assertThat(andeler.get(0).getAndelsnr()).isEqualTo(1L);
        assertThat(andeler.get(0).getInntektskategori()).isEqualTo(Inntektskategori.ARBEIDSTAKER);
        assertThat(andeler.get(1).getAktivitetStatus()).isEqualTo(AktivitetStatus.BRUKERS_ANDEL);
        assertThat(andeler.get(1).getAndelsnr()).isEqualTo(2L);
        assertThat(andeler.get(1).getInntektskategori()).isEqualTo(Inntektskategori.FRILANSER);
    }



    @Test
    public void skal_sette_verdier_fra_forrige_med_besteberegning() {
        // Arrange
        Intervall periode = Intervall.fraOgMedTilOgMed(
            SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(8),
            SKJÆRINGSTIDSPUNKT_OPPTJENING.minusDays(1));
        BeregningAktivitetAggregatRestDto beregningAktivitetAggregat = BeregningAktivitetTestUtil.opprettBeregningAktiviteter(SKJÆRINGSTIDSPUNKT_OPPTJENING, periode, OpptjeningAktivitetType.SYKEPENGER);
        var beregningsgrunnlagGrunnlag  = lagForrigeBeregningsgrunnlag(true, beregningAktivitetAggregat);
        ForeldrepengerGrunnlag medBesteberegning = new ForeldrepengerGrunnlag(100, true);

        // Act
        var input = new BeregningsgrunnlagRestInput(behandlingReferanse, InntektArbeidYtelseGrunnlagDtoBuilder.nytt().build(), null, List.of(), medBesteberegning)
                .medBeregningsgrunnlagGrunnlag(beregningsgrunnlagGrunnlag);

        KunYtelseDto kunytelse = kunYtelseDtoTjeneste.lagKunYtelseDto(input);


        // Assert
        List<AndelMedBeløpDto> andeler = kunytelse.getAndeler();
        assertThat(andeler).hasSize(1);
        assertThat(andeler.get(0).getFastsattBelopPrMnd()).isNotEqualByComparingTo(BigDecimal.valueOf(BRUTTO_PR_ÅR));
        assertThat(andeler.get(0).getAktivitetStatus()).isEqualTo(AktivitetStatus.BRUKERS_ANDEL);
        assertThat(andeler.get(0).getAndelsnr()).isEqualTo(1L);
        assertThat(andeler.get(0).getInntektskategori()).isEqualTo(Inntektskategori.UDEFINERT);
        assertThat(kunytelse.getErBesteberegning()).isTrue();
        assertThat(kunytelse.isFodendeKvinneMedDP()).isTrue();
    }

    @Test
    public void skal_sette_verdier_fra_forrige_uten_besteberegning() {
        // Arrange
        Intervall periode = Intervall.fraOgMedTilOgMed(
            SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(8),
            SKJÆRINGSTIDSPUNKT_OPPTJENING.minusDays(1));
        BeregningAktivitetAggregatRestDto beregningAktivitetAggregat = BeregningAktivitetTestUtil.opprettBeregningAktiviteter(SKJÆRINGSTIDSPUNKT_OPPTJENING, periode, OpptjeningAktivitetType.SYKEPENGER);
        var beregningsgrunnlagGrunnlag  = lagForrigeBeregningsgrunnlag(false, beregningAktivitetAggregat);
        ForeldrepengerGrunnlag utenBesteberegning = new ForeldrepengerGrunnlag(100, false);

        // Act
        var input = new BeregningsgrunnlagRestInput(behandlingReferanse, InntektArbeidYtelseGrunnlagDtoBuilder.nytt().build(), null, List.of(), utenBesteberegning)
                .medBeregningsgrunnlagGrunnlag(beregningsgrunnlagGrunnlag);

        KunYtelseDto kunytelse = kunYtelseDtoTjeneste.lagKunYtelseDto(input);


        // Assert
        List<AndelMedBeløpDto> andeler = kunytelse.getAndeler();
        assertThat(andeler).hasSize(1);
        assertThat(andeler.get(0).getFastsattBelopPrMnd()).isNotEqualByComparingTo(BigDecimal.valueOf(BRUTTO_PR_ÅR));
        assertThat(andeler.get(0).getAktivitetStatus()).isEqualTo(AktivitetStatus.BRUKERS_ANDEL);
        assertThat(andeler.get(0).getAndelsnr()).isEqualTo(1L);
        assertThat(andeler.get(0).getInntektskategori()).isEqualTo(Inntektskategori.UDEFINERT);
        assertThat(kunytelse.getErBesteberegning()).isFalse();
        assertThat(kunytelse.isFodendeKvinneMedDP()).isFalse();
    }


    private void assertAndel(KunYtelseDto kunytelse) {
        List<AndelMedBeløpDto> andeler = kunytelse.getAndeler();
        assertThat(andeler).hasSize(1);
        assertThat(andeler.get(0).getFastsattBelopPrMnd()).isNull();
        assertThat(andeler.get(0).getAktivitetStatus()).isEqualTo(AktivitetStatus.BRUKERS_ANDEL);
        assertThat(andeler.get(0).getAndelsnr()).isEqualTo(1L);
        assertThat(andeler.get(0).getInntektskategori()).isEqualTo(Inntektskategori.UDEFINERT);
    }

    private BeregningsgrunnlagGrunnlagRestDto lagForrigeBeregningsgrunnlag(boolean medBesteberegning, BeregningAktivitetAggregatRestDto beregningAktivitetAggregat) {
        BeregningsgrunnlagRestDto bg = BeregningsgrunnlagRestDto.builder()
            .medSkjæringstidspunkt(LocalDate.now().minusDays(5))
            .medGrunnbeløp(BigDecimal.valueOf(90000))
            .leggTilAktivitetStatus(BeregningsgrunnlagAktivitetStatusRestDto.builder().medAktivitetStatus(AktivitetStatus.KUN_YTELSE))
            .build();
        BeregningsgrunnlagPeriodeRestDto periode1 = BeregningsgrunnlagPeriodeRestDto.builder()
            .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT_OPPTJENING, null)
            .build(bg);
        BeregningsgrunnlagPrStatusOgAndelRestDto.kopier()
            .medLagtTilAvSaksbehandler(false)
            .medBesteberegningPrÅr(medBesteberegning ? BigDecimal.valueOf(BRUTTO_PR_ÅR) : null)
            .medBeregnetPrÅr(BigDecimal.valueOf(BRUTTO_PR_ÅR))
            .medAktivitetStatus(AktivitetStatus.BRUKERS_ANDEL)
            .build(periode1);

        var builder = BeregningsgrunnlagGrunnlagRestDtoBuilder.oppdatere(Optional.empty())
                .medBeregningsgrunnlag(bg)
                .medRegisterAktiviteter(beregningAktivitetAggregat);

        return builder.build(BeregningsgrunnlagTilstand.KOFAKBER_UT);
    }

    private BeregningsgrunnlagGrunnlagRestDto lagBeregningsgrunnlag(BeregningAktivitetAggregatRestDto beregningAktivitetAggregat) {
        BeregningsgrunnlagRestDto bg = BeregningsgrunnlagRestDto.builder()
            .medSkjæringstidspunkt(LocalDate.now().minusDays(5))
            .medGrunnbeløp(BigDecimal.valueOf(90000))
            .leggTilAktivitetStatus(BeregningsgrunnlagAktivitetStatusRestDto.builder().medAktivitetStatus(AktivitetStatus.KUN_YTELSE))
            .build();
        BeregningsgrunnlagPeriodeRestDto periode1 = BeregningsgrunnlagPeriodeRestDto.builder()
            .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT_OPPTJENING, null)
            .build(bg);
        BeregningsgrunnlagPrStatusOgAndelRestDto.kopier()
            .medLagtTilAvSaksbehandler(false)
            .medAktivitetStatus(AktivitetStatus.BRUKERS_ANDEL)
            .build(periode1);

        var builder = BeregningsgrunnlagGrunnlagRestDtoBuilder.oppdatere(Optional.empty())
                .medBeregningsgrunnlag(bg)
                .medRegisterAktiviteter(beregningAktivitetAggregat);

        return builder.build(BeregningsgrunnlagTilstand.OPPRETTET);
    }

    private BeregningsgrunnlagGrunnlagRestDto lagForrigeBeregningsgrunnlagMedLagtTilAndel(BeregningAktivitetAggregatRestDto beregningAktivitetAggregat) {
        BeregningsgrunnlagRestDto bg = BeregningsgrunnlagRestDto.builder()
            .medSkjæringstidspunkt(LocalDate.now().minusDays(5))
            .medGrunnbeløp(BigDecimal.valueOf(90000))
            .leggTilAktivitetStatus(BeregningsgrunnlagAktivitetStatusRestDto.builder().medAktivitetStatus(AktivitetStatus.KUN_YTELSE))
            .build();
        BeregningsgrunnlagPeriodeRestDto periode1 = BeregningsgrunnlagPeriodeRestDto.builder()
            .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT_OPPTJENING, null)
            .build(bg);
        BeregningsgrunnlagPrStatusOgAndelRestDto.kopier()
            .medLagtTilAvSaksbehandler(false)
            .medAktivitetStatus(AktivitetStatus.BRUKERS_ANDEL)
            .medInntektskategori(Inntektskategori.ARBEIDSTAKER)
            .build(periode1);
        BeregningsgrunnlagPrStatusOgAndelRestDto.kopier()
            .medLagtTilAvSaksbehandler(true)
            .medAktivitetStatus(AktivitetStatus.BRUKERS_ANDEL)
            .medInntektskategori(Inntektskategori.FRILANSER)
            .build(periode1);

        return BeregningsgrunnlagGrunnlagRestDtoBuilder.oppdatere(Optional.empty())
                .medBeregningsgrunnlag(bg)
                .medRegisterAktiviteter(beregningAktivitetAggregat)
                .build(BeregningsgrunnlagTilstand.OPPRETTET);
    }

}
