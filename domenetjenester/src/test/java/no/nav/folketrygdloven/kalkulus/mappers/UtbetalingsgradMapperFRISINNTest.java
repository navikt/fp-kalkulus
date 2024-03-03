package no.nav.folketrygdloven.kalkulus.mappers;

import static no.nav.folketrygdloven.kalkulus.felles.tid.AbstractIntervall.TIDENES_ENDE;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.OppgittFrilansDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.OppgittFrilansInntektDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.OppgittOpptjeningDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.svp.PeriodeMedUtbetalingsgradDto;
import no.nav.folketrygdloven.kalkulator.modell.svp.UtbetalingsgradPrAktivitetDto;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulator.ytelse.frisinn.FrisinnPeriode;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningAktivitetAggregatEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagGrunnlagBuilder;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagGrunnlagEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndel;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.Beløp;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.Årsgrunnlag;
import no.nav.folketrygdloven.kalkulus.felles.v1.Organisasjon;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulus.kodeverk.UttakArbeidType;
import no.nav.folketrygdloven.kalkulus.typer.Utbetalingsgrad;


class UtbetalingsgradMapperFRISINNTest {

    public static final Organisasjon ARBEIDSGIVER = new Organisasjon("974652269");

    @Test
    void skal_mappe_til_1_prosent_utbetaling_ved_tilnærmet_ingen_bortfalt_inntekt() {
        // Arrange
        LocalDate skjæringstidspunkt = LocalDate.of(2020, 4, 1);
        Optional<BeregningsgrunnlagGrunnlagEntitet> bgGrunnlagEntitet = lagBeregningsgrunnlagMedSN(skjæringstidspunkt, BigDecimal.valueOf(360_000));
        Intervall april = Intervall.fraOgMedTilOgMed(LocalDate.of(2020, 4, 1), LocalDate.of(2020, 4, 30));
        BigDecimal månedsinntekt = BigDecimal.valueOf(30_000);
        List<OppgittOpptjeningDtoBuilder.EgenNæringBuilder> oppgittNæring = List.of(lagOppgittInntekt(april, månedsinntekt));
        InntektArbeidYtelseGrunnlagDto iayGrunnlag = InntektArbeidYtelseGrunnlagDtoBuilder.nytt()
                .medOppgittOpptjening(OppgittOpptjeningDtoBuilder.ny().leggTilEgneNæringer(oppgittNæring))
                .build();
        List<FrisinnPeriode> frisinnPerioder = Arrays.asList(lagFrisinnperiode(april, AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE));

        // Act
        List<UtbetalingsgradPrAktivitetDto> utbetalingsgrader = UtbetalingsgradMapperFRISINN.map(iayGrunnlag, bgGrunnlagEntitet, frisinnPerioder);

        // Assert
        assertThat(utbetalingsgrader).hasSize(2);
        UtbetalingsgradPrAktivitetDto utbetalingsgradPrAktivitetDto = utbetalingsgrader.get(0);
        assertThat(utbetalingsgradPrAktivitetDto.getUtbetalingsgradArbeidsforhold().getUttakArbeidType()).isEqualTo(UttakArbeidType.SELVSTENDIG_NÆRINGSDRIVENDE);
        assertThat(utbetalingsgradPrAktivitetDto.getPeriodeMedUtbetalingsgrad()).hasSize(1);
        PeriodeMedUtbetalingsgradDto periode1 = utbetalingsgradPrAktivitetDto.getPeriodeMedUtbetalingsgrad().get(0);
        assertPeriode(april, periode1, Utbetalingsgrad.valueOf(1));
    }

    private void assertPeriode(Intervall april, PeriodeMedUtbetalingsgradDto periode, Utbetalingsgrad utbetalingsprosent) {
        assertThat(periode.getPeriode().getFomDato()).isEqualTo(april.getFomDato());
        assertThat(periode.getPeriode().getTomDato()).isEqualTo(april.getTomDato());
        assertThat(periode.getUtbetalingsgrad().verdi().intValue()).isEqualByComparingTo(utbetalingsprosent.verdi().intValue());
    }

    @Test
    void skal_mappe_til_100_prosent_utbetaling_ved_fullt_bortfalt_inntekt() {
        // Arrange
        LocalDate skjæringstidspunkt = LocalDate.of(2020, 4, 1);
        Optional<BeregningsgrunnlagGrunnlagEntitet> bgGrunnlagEntitet = lagBeregningsgrunnlagMedSN(skjæringstidspunkt, BigDecimal.valueOf(360_000));
        Intervall april = Intervall.fraOgMedTilOgMed(LocalDate.of(2020, 4, 1), LocalDate.of(2020, 4, 30));
        BigDecimal månedsinntekt = BigDecimal.ZERO;
        List<OppgittOpptjeningDtoBuilder.EgenNæringBuilder> oppgittNæring = List.of(lagOppgittInntekt(april, månedsinntekt));
        InntektArbeidYtelseGrunnlagDto iayGrunnlag = InntektArbeidYtelseGrunnlagDtoBuilder.nytt()
                .medOppgittOpptjening(OppgittOpptjeningDtoBuilder.ny().leggTilEgneNæringer(oppgittNæring))
                .build();
        List<FrisinnPeriode> frisinnPerioder = Arrays.asList(lagFrisinnperiode(april, AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE));

        // Act
        List<UtbetalingsgradPrAktivitetDto> utbetalingsgrader = UtbetalingsgradMapperFRISINN.map(iayGrunnlag, bgGrunnlagEntitet, frisinnPerioder);

        // Assert
        assertThat(utbetalingsgrader).hasSize(2);
        UtbetalingsgradPrAktivitetDto utbetalingsgradPrAktivitetDto = utbetalingsgrader.get(0);
        assertThat(utbetalingsgradPrAktivitetDto.getUtbetalingsgradArbeidsforhold().getUttakArbeidType()).isEqualTo(UttakArbeidType.SELVSTENDIG_NÆRINGSDRIVENDE);
        assertThat(utbetalingsgradPrAktivitetDto.getPeriodeMedUtbetalingsgrad()).hasSize(1);
        PeriodeMedUtbetalingsgradDto periode1 = utbetalingsgradPrAktivitetDto.getPeriodeMedUtbetalingsgrad().get(0);
        assertPeriode(april, periode1, Utbetalingsgrad.valueOf(100));
    }

    @Test
    void skal_til_51_prosent_utbetaling_ved_delvis_bortfalt_inntekt() {
        // Arrange
        LocalDate skjæringstidspunkt = LocalDate.of(2020, 4, 1);
        Optional<BeregningsgrunnlagGrunnlagEntitet> bgGrunnlagEntitet = lagBeregningsgrunnlagMedSN(skjæringstidspunkt, BigDecimal.valueOf(360_000));
        Intervall april = Intervall.fraOgMedTilOgMed(LocalDate.of(2020, 4, 1), LocalDate.of(2020, 4, 30));
        BigDecimal månedsinntekt = BigDecimal.valueOf(15_000);
        List<OppgittOpptjeningDtoBuilder.EgenNæringBuilder> oppgittNæring = List.of(lagOppgittInntekt(april, månedsinntekt));
        InntektArbeidYtelseGrunnlagDto iayGrunnlag = InntektArbeidYtelseGrunnlagDtoBuilder.nytt()
                .medOppgittOpptjening(OppgittOpptjeningDtoBuilder.ny().leggTilEgneNæringer(oppgittNæring))
                .build();
        List<FrisinnPeriode> frisinnPerioder = Arrays.asList(lagFrisinnperiode(april, AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE));

        // Act
        List<UtbetalingsgradPrAktivitetDto> utbetalingsgrader = UtbetalingsgradMapperFRISINN.map(iayGrunnlag, bgGrunnlagEntitet, frisinnPerioder);

        // Assert
        assertThat(utbetalingsgrader).hasSize(2);
        UtbetalingsgradPrAktivitetDto utbetalingsgradPrAktivitetDto = utbetalingsgrader.get(0);
        assertThat(utbetalingsgradPrAktivitetDto.getUtbetalingsgradArbeidsforhold().getUttakArbeidType()).isEqualTo(UttakArbeidType.SELVSTENDIG_NÆRINGSDRIVENDE);
        assertThat(utbetalingsgradPrAktivitetDto.getPeriodeMedUtbetalingsgrad()).hasSize(1);
        PeriodeMedUtbetalingsgradDto periode1 = utbetalingsgradPrAktivitetDto.getPeriodeMedUtbetalingsgrad().get(0);
        assertPeriode(april, periode1, Utbetalingsgrad.valueOf(50));
    }

    @Test
    void skal_halv_måned_med_tilnærmet_full_inntekt_til_utbetalingsgrad() {
        // Arrange
        LocalDate skjæringstidspunkt = LocalDate.of(2020, 4, 1);
        Optional<BeregningsgrunnlagGrunnlagEntitet> bgGrunnlagEntitet = lagBeregningsgrunnlagMedSN(skjæringstidspunkt, BigDecimal.valueOf(360_000));
        Intervall april = Intervall.fraOgMedTilOgMed(LocalDate.of(2020, 4, 1), LocalDate.of(2020, 4, 30));
        BigDecimal periodeInntekt = BigDecimal.valueOf(15_000);
        Intervall halveApril = Intervall.fraOgMedTilOgMed(april.getFomDato(), LocalDate.of(2020, 4, 15));
        List<OppgittOpptjeningDtoBuilder.EgenNæringBuilder> oppgittNæring = List.of(lagOppgittInntekt(
                halveApril, periodeInntekt));
        InntektArbeidYtelseGrunnlagDto iayGrunnlag = InntektArbeidYtelseGrunnlagDtoBuilder.nytt()
                .medOppgittOpptjening(OppgittOpptjeningDtoBuilder.ny().leggTilEgneNæringer(oppgittNæring))
                .build();
        List<FrisinnPeriode> frisinnPerioder = Arrays.asList(lagFrisinnperiode(halveApril, AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE));

        // Act
        List<UtbetalingsgradPrAktivitetDto> utbetalingsgrader = UtbetalingsgradMapperFRISINN.map(iayGrunnlag, bgGrunnlagEntitet, frisinnPerioder);

        // Assert
        assertThat(utbetalingsgrader).hasSize(2);
        UtbetalingsgradPrAktivitetDto utbetalingsgradPrAktivitetDto = utbetalingsgrader.get(0);
        assertThat(utbetalingsgradPrAktivitetDto.getUtbetalingsgradArbeidsforhold().getUttakArbeidType()).isEqualTo(UttakArbeidType.SELVSTENDIG_NÆRINGSDRIVENDE);
        assertThat(utbetalingsgradPrAktivitetDto.getPeriodeMedUtbetalingsgrad()).hasSize(1);
        PeriodeMedUtbetalingsgradDto periode1 = utbetalingsgradPrAktivitetDto.getPeriodeMedUtbetalingsgrad().get(0);
        assertPeriode(halveApril, periode1, Utbetalingsgrad.valueOf(1));
    }

    @Test
    void skal_mappe_periode_over_2_måneder_med_inntekt_til_utbetaling() {
        // Arrange
        LocalDate skjæringstidspunkt = LocalDate.of(2020, 4, 1);
        Optional<BeregningsgrunnlagGrunnlagEntitet> bgGrunnlagEntitet = lagBeregningsgrunnlagMedSN(skjæringstidspunkt, BigDecimal.valueOf(360_000));
        Intervall april = Intervall.fraOgMedTilOgMed(LocalDate.of(2020, 4, 1), LocalDate.of(2020, 4, 30));
        BigDecimal periodeInntekt = BigDecimal.valueOf(45_000);
        Intervall periode = Intervall.fraOgMedTilOgMed(LocalDate.of(2020, 3, 18), april.getTomDato());
        List<OppgittOpptjeningDtoBuilder.EgenNæringBuilder> oppgittNæring = List.of(lagOppgittInntekt(
                periode, periodeInntekt));
        InntektArbeidYtelseGrunnlagDto iayGrunnlag = InntektArbeidYtelseGrunnlagDtoBuilder.nytt()
                .medOppgittOpptjening(OppgittOpptjeningDtoBuilder.ny().leggTilEgneNæringer(oppgittNæring))
                .build();

        List<FrisinnPeriode> frisinnPerioder = Arrays.asList(lagFrisinnperiode(periode, AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE));

        // Act
        List<UtbetalingsgradPrAktivitetDto> utbetalingsgrader = UtbetalingsgradMapperFRISINN.map(iayGrunnlag, bgGrunnlagEntitet, frisinnPerioder);

        // Assert
        assertThat(utbetalingsgrader).hasSize(2);
        UtbetalingsgradPrAktivitetDto utbetalingsgradPrAktivitetDto = utbetalingsgrader.get(0);
        assertThat(utbetalingsgradPrAktivitetDto.getUtbetalingsgradArbeidsforhold().getUttakArbeidType()).isEqualTo(UttakArbeidType.SELVSTENDIG_NÆRINGSDRIVENDE);
        assertThat(utbetalingsgradPrAktivitetDto.getPeriodeMedUtbetalingsgrad()).hasSize(1);
        PeriodeMedUtbetalingsgradDto periode1 = utbetalingsgradPrAktivitetDto.getPeriodeMedUtbetalingsgrad().get(0);
        assertPeriode(periode, periode1, Utbetalingsgrad.ZERO);
    }

    @Test
    void skal_til_51_prosent_utbetaling_ved_delvis_bortfalt_inntekt_frilans() {
        // Arrange
        LocalDate skjæringstidspunkt = LocalDate.of(2020, 4, 1);
        Optional<BeregningsgrunnlagGrunnlagEntitet> bgGrunnlagEntitet = lagBeregningsgrunnlagMedFL(skjæringstidspunkt, BigDecimal.valueOf(360_000));
        Intervall april = Intervall.fraOgMedTilOgMed(LocalDate.of(2020, 4, 1), LocalDate.of(2020, 4, 30));
        BigDecimal månedsinntekt = BigDecimal.valueOf(15_000);
        List<OppgittFrilansInntektDto> oppgittFrilansInntekt = List.of(lagOppgittFrilansInntekt(april, månedsinntekt));
        InntektArbeidYtelseGrunnlagDto iayGrunnlag = InntektArbeidYtelseGrunnlagDtoBuilder.nytt()
                .medOppgittOpptjening(OppgittOpptjeningDtoBuilder.ny()
                        .leggTilFrilansOpplysninger(new OppgittFrilansDto(false, oppgittFrilansInntekt)))
                .build();
        List<FrisinnPeriode> frisinnPerioder = Arrays.asList(lagFrisinnperiode(april, AktivitetStatus.FRILANSER));

        // Act
        List<UtbetalingsgradPrAktivitetDto> utbetalingsgrader = UtbetalingsgradMapperFRISINN.map(iayGrunnlag, bgGrunnlagEntitet, frisinnPerioder);

        // Assert
        assertThat(utbetalingsgrader).hasSize(2);
        UtbetalingsgradPrAktivitetDto utbetalingsgradPrAktivitetDto = utbetalingsgrader.get(1);
        assertThat(utbetalingsgradPrAktivitetDto.getUtbetalingsgradArbeidsforhold().getUttakArbeidType()).isEqualTo(UttakArbeidType.FRILANS);
        assertThat(utbetalingsgradPrAktivitetDto.getPeriodeMedUtbetalingsgrad()).hasSize(1);
        PeriodeMedUtbetalingsgradDto periode1 = utbetalingsgradPrAktivitetDto.getPeriodeMedUtbetalingsgrad().get(0);
        assertPeriode(april, periode1, Utbetalingsgrad.valueOf(50));
    }

    @Test
    void skal_ikke_lage_perioder_for_andeler_det_ikke_er_søkt_om() {
        // Arrange
        LocalDate skjæringstidspunkt = LocalDate.of(2020, 4, 1);
        Optional<BeregningsgrunnlagGrunnlagEntitet> bgGrunnlagEntitet = lagBeregningsgrunnlagMedFL(skjæringstidspunkt, BigDecimal.valueOf(360_000));
        Intervall april = Intervall.fraOgMedTilOgMed(LocalDate.of(2020, 4, 1), LocalDate.of(2020, 4, 30));
        BigDecimal månedsinntekt = BigDecimal.valueOf(15_000);
        List<OppgittFrilansInntektDto> oppgittFrilansInntekt = List.of(lagOppgittFrilansInntekt(april, månedsinntekt));
        List<FrisinnPeriode> frisinnPerioder = Arrays.asList(lagFrisinnperiode(april, AktivitetStatus.FRILANSER));
        List<OppgittOpptjeningDtoBuilder.EgenNæringBuilder> oppgittNæring = List.of(lagOppgittInntekt(
                april, månedsinntekt));

        InntektArbeidYtelseGrunnlagDto iayGrunnlag = InntektArbeidYtelseGrunnlagDtoBuilder.nytt()
                .medOppgittOpptjening(OppgittOpptjeningDtoBuilder.ny()
                        .leggTilFrilansOpplysninger(new OppgittFrilansDto(false, oppgittFrilansInntekt))
                        .leggTilEgneNæringer(oppgittNæring))
                .build();

        // Act
        List<UtbetalingsgradPrAktivitetDto> utbetalingsgrader = UtbetalingsgradMapperFRISINN.map(iayGrunnlag, bgGrunnlagEntitet, frisinnPerioder);

        // Assert
        assertThat(utbetalingsgrader).hasSize(2);
        UtbetalingsgradPrAktivitetDto utbetalingsgradPrAktivitetDto = utbetalingsgrader.get(1);
        assertThat(utbetalingsgradPrAktivitetDto.getUtbetalingsgradArbeidsforhold().getUttakArbeidType()).isEqualTo(UttakArbeidType.FRILANS);
        assertThat(utbetalingsgradPrAktivitetDto.getPeriodeMedUtbetalingsgrad()).hasSize(1);
        PeriodeMedUtbetalingsgradDto periode1 = utbetalingsgradPrAktivitetDto.getPeriodeMedUtbetalingsgrad().get(0);

        UtbetalingsgradPrAktivitetDto utbetalingsgradPrAktivitetDto2 = utbetalingsgrader.get(0);
        assertThat(utbetalingsgradPrAktivitetDto2.getUtbetalingsgradArbeidsforhold().getUttakArbeidType()).isEqualTo(UttakArbeidType.SELVSTENDIG_NÆRINGSDRIVENDE);
        assertThat(utbetalingsgradPrAktivitetDto2.getPeriodeMedUtbetalingsgrad()).isEmpty();

        assertPeriode(april, periode1, Utbetalingsgrad.valueOf(50));
    }

    @Test
    void skal_ikke_lage_perioder_for_to_andeler_når_det_er_søkt_om_begge() {
        // Arrange
        LocalDate skjæringstidspunkt = LocalDate.of(2020, 4, 1);
        Optional<BeregningsgrunnlagGrunnlagEntitet> bgGrunnlagEntitet = lagBeregningsgrunnlagMedFL(skjæringstidspunkt, BigDecimal.valueOf(360_000));
        Intervall april = Intervall.fraOgMedTilOgMed(LocalDate.of(2020, 4, 1), LocalDate.of(2020, 4, 30));
        Intervall halveApril = Intervall.fraOgMedTilOgMed(LocalDate.of(2020, 4, 15), LocalDate.of(2020, 4, 30));
        BigDecimal månedsinntekt = BigDecimal.valueOf(15_000);
        List<OppgittFrilansInntektDto> oppgittFrilansInntekt = List.of(lagOppgittFrilansInntekt(april, månedsinntekt));
        List<FrisinnPeriode> frisinnPerioder = Arrays.asList(lagFrisinnperiode(april, AktivitetStatus.FRILANSER), lagFrisinnperiode(halveApril, AktivitetStatus.FRILANSER, AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE));
        List<OppgittOpptjeningDtoBuilder.EgenNæringBuilder> oppgittNæring = List.of(lagOppgittInntekt(
                halveApril, månedsinntekt));

        InntektArbeidYtelseGrunnlagDto iayGrunnlag = InntektArbeidYtelseGrunnlagDtoBuilder.nytt()
                .medOppgittOpptjening(OppgittOpptjeningDtoBuilder.ny()
                        .leggTilFrilansOpplysninger(new OppgittFrilansDto(false, oppgittFrilansInntekt))
                        .leggTilEgneNæringer(oppgittNæring))
                .build();

        // Act
        List<UtbetalingsgradPrAktivitetDto> utbetalingsgrader = UtbetalingsgradMapperFRISINN.map(iayGrunnlag, bgGrunnlagEntitet, frisinnPerioder);

        // Assert
        assertThat(utbetalingsgrader).hasSize(2);
        UtbetalingsgradPrAktivitetDto utbetalingsgradPrAktivitetDto = utbetalingsgrader.get(1);
        assertThat(utbetalingsgradPrAktivitetDto.getUtbetalingsgradArbeidsforhold().getUttakArbeidType()).isEqualTo(UttakArbeidType.FRILANS);
        assertThat(utbetalingsgradPrAktivitetDto.getPeriodeMedUtbetalingsgrad()).hasSize(1);
        PeriodeMedUtbetalingsgradDto periode1 = utbetalingsgradPrAktivitetDto.getPeriodeMedUtbetalingsgrad().get(0);
        assertPeriode(april, periode1, Utbetalingsgrad.valueOf(50));

        UtbetalingsgradPrAktivitetDto utbetalingsgradPrAktivitetDto2 = utbetalingsgrader.get(0);
        assertThat(utbetalingsgradPrAktivitetDto2.getUtbetalingsgradArbeidsforhold().getUttakArbeidType()).isEqualTo(UttakArbeidType.SELVSTENDIG_NÆRINGSDRIVENDE);
        assertThat(utbetalingsgradPrAktivitetDto2.getPeriodeMedUtbetalingsgrad()).hasSize(1);
        PeriodeMedUtbetalingsgradDto periode2 = utbetalingsgradPrAktivitetDto2.getPeriodeMedUtbetalingsgrad().get(0);

        assertPeriode(halveApril, periode2, Utbetalingsgrad.ZERO);
    }


    private OppgittOpptjeningDtoBuilder.EgenNæringBuilder lagOppgittInntekt(Intervall april, BigDecimal periodeInntekt) {
        return OppgittOpptjeningDtoBuilder.EgenNæringBuilder.ny()
                .medPeriode(april)
                .medVirksomhet(ARBEIDSGIVER.getIdent())
                .medBruttoInntekt(no.nav.folketrygdloven.kalkulator.modell.typer.Beløp.fra(periodeInntekt));
    }

    private OppgittFrilansInntektDto lagOppgittFrilansInntekt(Intervall april, BigDecimal periodeInntekt) {
        return new OppgittFrilansInntektDto(april, no.nav.folketrygdloven.kalkulator.modell.typer.Beløp.fra(periodeInntekt));
    }

    private Optional<BeregningsgrunnlagGrunnlagEntitet> lagBeregningsgrunnlagMedSN(LocalDate skjæringstidspunkt, BigDecimal beregnetPrÅr) {
        BeregningsgrunnlagEntitet bg = BeregningsgrunnlagEntitet.builder()
                .medSkjæringstidspunkt(skjæringstidspunkt)
                .build();
        BeregningsgrunnlagPeriode.builder()
                .medBeregningsgrunnlagPeriode(skjæringstidspunkt, TIDENES_ENDE)
                .leggTilBeregningsgrunnlagPrStatusOgAndel(BeregningsgrunnlagPrStatusOgAndel.builder()
                        .medAktivitetStatus(AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE)
                        .medAndelsnr(1L)
                        .medGrunnlagPrÅr(lagÅrsgrunnlag(beregnetPrÅr)))
                .build(bg);
        return Optional.of(BeregningsgrunnlagGrunnlagBuilder.kopiere(Optional.empty())
                .medRegisterAktiviteter(BeregningAktivitetAggregatEntitet.builder()
                        .medSkjæringstidspunktOpptjening(skjæringstidspunkt)
                        .build())
                .medBeregningsgrunnlag(bg)
                .build(1L, BeregningsgrunnlagTilstand.FORESLÅTT));
    }

    private Optional<BeregningsgrunnlagGrunnlagEntitet> lagBeregningsgrunnlagMedFL(LocalDate skjæringstidspunkt, BigDecimal beregnetPrÅr) {
        BeregningsgrunnlagEntitet bg = BeregningsgrunnlagEntitet.builder()
                .medSkjæringstidspunkt(skjæringstidspunkt)
                .build();
        BeregningsgrunnlagPeriode.builder()
                .medBeregningsgrunnlagPeriode(skjæringstidspunkt, TIDENES_ENDE)
                .leggTilBeregningsgrunnlagPrStatusOgAndel(BeregningsgrunnlagPrStatusOgAndel.builder()
                        .medAktivitetStatus(AktivitetStatus.FRILANSER)
                        .medAndelsnr(1L)
                        .medGrunnlagPrÅr(lagÅrsgrunnlag(beregnetPrÅr)))
                .build(bg);
        return Optional.of(BeregningsgrunnlagGrunnlagBuilder.kopiere(Optional.empty())
                .medRegisterAktiviteter(BeregningAktivitetAggregatEntitet.builder()
                        .medSkjæringstidspunktOpptjening(skjæringstidspunkt)
                        .build())
                .medBeregningsgrunnlag(bg)
                .build(1L, BeregningsgrunnlagTilstand.FORESLÅTT));
    }

    private Årsgrunnlag lagÅrsgrunnlag(BigDecimal beregnetPrÅr) {
        return new Årsgrunnlag(new Beløp(beregnetPrÅr), null, null, null, null, new Beløp(beregnetPrÅr));
    }

    private FrisinnPeriode lagFrisinnperiode(Intervall periode, AktivitetStatus... statuser) {
        List<AktivitetStatus> aktivitetStatuses = Arrays.asList(statuser);
        boolean søkerSN = aktivitetStatuses.contains(AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE);
        boolean søkerFL = aktivitetStatuses.contains(AktivitetStatus.FRILANSER);
        return new FrisinnPeriode(periode, søkerFL, søkerSN);
    }


}
