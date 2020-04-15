package no.nav.folketrygdloven.kalkulus.mappers;

import static no.nav.folketrygdloven.kalkulus.felles.tid.AbstractIntervall.TIDENES_ENDE;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
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
import no.nav.folketrygdloven.kalkulator.modell.uttak.UttakArbeidType;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagGrunnlagBuilder;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagGrunnlagEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndel;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulus.felles.v1.Organisasjon;


class UtbetalingsgradMapperFRISINNTest {

    public static final Organisasjon ARBEIDSGIVER = new Organisasjon("923609016");

    @Test
    void skal_mappe_en_hel_måned_med_inntekt_til_100_prosent_utbetaling_ved_fullt_bortfalt_inntekt() {
        // Arrange
        LocalDate skjæringstidspunkt = LocalDate.of(2020, 4, 1);
        Optional<BeregningsgrunnlagGrunnlagEntitet> bgGrunnlagEntitet = lagBeregningsgrunnlagMedSN(skjæringstidspunkt, BigDecimal.valueOf(360_000));
        Intervall april = Intervall.fraOgMedTilOgMed(LocalDate.of(2020, 4, 1), LocalDate.of(2020, 4, 30));
        BigDecimal månedsinntekt = BigDecimal.valueOf(30_000);
        List<OppgittOpptjeningDtoBuilder.EgenNæringBuilder> oppgittNæring = List.of(lagOppgittInntekt(april, månedsinntekt));
        InntektArbeidYtelseGrunnlagDto iayGrunnlag = InntektArbeidYtelseGrunnlagDtoBuilder.nytt()
                .medOppgittOpptjening(OppgittOpptjeningDtoBuilder.ny().leggTilEgneNæringer(oppgittNæring))
                .build();

        // Act
        List<UtbetalingsgradPrAktivitetDto> utbetalingsgrader = UtbetalingsgradMapperFRISINN.map(iayGrunnlag, bgGrunnlagEntitet, LocalDate.of(2020, 5, 6));

        // Assert
        assertThat(utbetalingsgrader.size()).isEqualTo(1);
        UtbetalingsgradPrAktivitetDto utbetalingsgradPrAktivitetDto = utbetalingsgrader.get(0);
        assertThat(utbetalingsgradPrAktivitetDto.getUtbetalingsgradArbeidsforhold().getUttakArbeidType()).isEqualTo(UttakArbeidType.SELVSTENDIG_NÆRINGSDRIVENDE);
        assertThat(utbetalingsgradPrAktivitetDto.getPeriodeMedUtbetalingsgrad().size()).isEqualTo(2);
        PeriodeMedUtbetalingsgradDto periode1 = utbetalingsgradPrAktivitetDto.getPeriodeMedUtbetalingsgrad().get(0);
        assertPeriode(april, periode1, 0);
    }

    private void assertPeriode(Intervall april, PeriodeMedUtbetalingsgradDto periode, int utbetalingsprosent) {
        assertThat(periode.getPeriode().getFomDato()).isEqualTo(april.getFomDato());
        assertThat(periode.getPeriode().getTomDato()).isEqualTo(april.getTomDato());
        assertThat(periode.getUtbetalingsgrad().intValue()).isEqualTo(utbetalingsprosent);
    }

    @Test
    void skal_mappe_en_hel_måned_med_inntekt_til_100_prosent_utbetaling_ved_ingen_innsendt_inntekt_fra_næring() {
        // Arrange
        LocalDate skjæringstidspunkt = LocalDate.of(2020, 4, 1);
        Optional<BeregningsgrunnlagGrunnlagEntitet> bgGrunnlagEntitet = lagBeregningsgrunnlagMedSN(skjæringstidspunkt, BigDecimal.valueOf(360_000));
        Intervall april = Intervall.fraOgMedTilOgMed(LocalDate.of(2020, 4, 1), LocalDate.of(2020, 4, 30));
        BigDecimal månedsinntekt = BigDecimal.valueOf(30_000);
        List<OppgittOpptjeningDtoBuilder.EgenNæringBuilder> oppgittNæring = List.of(lagOppgittInntekt(april, månedsinntekt));
        InntektArbeidYtelseGrunnlagDto iayGrunnlag = InntektArbeidYtelseGrunnlagDtoBuilder.nytt()
                .medOppgittOpptjening(OppgittOpptjeningDtoBuilder.ny().leggTilEgneNæringer(oppgittNæring))
                .build();

        // Act
        List<UtbetalingsgradPrAktivitetDto> utbetalingsgrader = UtbetalingsgradMapperFRISINN.map(iayGrunnlag, bgGrunnlagEntitet, LocalDate.of(2020, 5, 6));

        // Assert
        assertThat(utbetalingsgrader.size()).isEqualTo(1);
        UtbetalingsgradPrAktivitetDto utbetalingsgradPrAktivitetDto = utbetalingsgrader.get(0);
        assertThat(utbetalingsgradPrAktivitetDto.getUtbetalingsgradArbeidsforhold().getUttakArbeidType()).isEqualTo(UttakArbeidType.SELVSTENDIG_NÆRINGSDRIVENDE);
        assertThat(utbetalingsgradPrAktivitetDto.getPeriodeMedUtbetalingsgrad().size()).isEqualTo(2);
        PeriodeMedUtbetalingsgradDto periode1 = utbetalingsgradPrAktivitetDto.getPeriodeMedUtbetalingsgrad().get(0);
        assertPeriode(april, periode1, 0);
    }

    @Test
    void skal_mappe_en_hel_måned_med_inntekt_til_0_prosent_utbetaling_ved_ingen_bortfalt_inntekt() {
        // Arrange
        LocalDate skjæringstidspunkt = LocalDate.of(2020, 4, 1);
        Optional<BeregningsgrunnlagGrunnlagEntitet> bgGrunnlagEntitet = lagBeregningsgrunnlagMedSN(skjæringstidspunkt, BigDecimal.valueOf(360_000));
        Intervall april = Intervall.fraOgMedTilOgMed(LocalDate.of(2020, 4, 1), LocalDate.of(2020, 4, 30));
        BigDecimal månedsinntekt = BigDecimal.ZERO;
        List<OppgittOpptjeningDtoBuilder.EgenNæringBuilder> oppgittNæring = List.of(lagOppgittInntekt(april, månedsinntekt));
        InntektArbeidYtelseGrunnlagDto iayGrunnlag = InntektArbeidYtelseGrunnlagDtoBuilder.nytt()
                .medOppgittOpptjening(OppgittOpptjeningDtoBuilder.ny().leggTilEgneNæringer(oppgittNæring))
                .build();

        // Act
        List<UtbetalingsgradPrAktivitetDto> utbetalingsgrader = UtbetalingsgradMapperFRISINN.map(iayGrunnlag, bgGrunnlagEntitet, LocalDate.of(2020, 5, 6));

        // Assert
        assertThat(utbetalingsgrader.size()).isEqualTo(1);
        UtbetalingsgradPrAktivitetDto utbetalingsgradPrAktivitetDto = utbetalingsgrader.get(0);
        assertThat(utbetalingsgradPrAktivitetDto.getUtbetalingsgradArbeidsforhold().getUttakArbeidType()).isEqualTo(UttakArbeidType.SELVSTENDIG_NÆRINGSDRIVENDE);
        assertThat(utbetalingsgradPrAktivitetDto.getPeriodeMedUtbetalingsgrad().size()).isEqualTo(2);
        PeriodeMedUtbetalingsgradDto periode1 = utbetalingsgradPrAktivitetDto.getPeriodeMedUtbetalingsgrad().get(0);
        assertPeriode(april, periode1, 100);
    }

    @Test
    void skal_mappe_en_hel_måned_med_inntekt_til_50_prosent_utbetaling_ved_delvis_bortfalt_inntekt() {
        // Arrange
        LocalDate skjæringstidspunkt = LocalDate.of(2020, 4, 1);
        Optional<BeregningsgrunnlagGrunnlagEntitet> bgGrunnlagEntitet = lagBeregningsgrunnlagMedSN(skjæringstidspunkt, BigDecimal.valueOf(360_000));
        Intervall april = Intervall.fraOgMedTilOgMed(LocalDate.of(2020, 4, 1), LocalDate.of(2020, 4, 30));
        BigDecimal månedsinntekt = BigDecimal.valueOf(15_000);
        List<OppgittOpptjeningDtoBuilder.EgenNæringBuilder> oppgittNæring = List.of(lagOppgittInntekt(april, månedsinntekt));
        InntektArbeidYtelseGrunnlagDto iayGrunnlag = InntektArbeidYtelseGrunnlagDtoBuilder.nytt()
                .medOppgittOpptjening(OppgittOpptjeningDtoBuilder.ny().leggTilEgneNæringer(oppgittNæring))
                .build();

        // Act
        List<UtbetalingsgradPrAktivitetDto> utbetalingsgrader = UtbetalingsgradMapperFRISINN.map(iayGrunnlag, bgGrunnlagEntitet, LocalDate.of(2020, 5, 6));

        // Assert
        assertThat(utbetalingsgrader.size()).isEqualTo(1);
        UtbetalingsgradPrAktivitetDto utbetalingsgradPrAktivitetDto = utbetalingsgrader.get(0);
        assertThat(utbetalingsgradPrAktivitetDto.getUtbetalingsgradArbeidsforhold().getUttakArbeidType()).isEqualTo(UttakArbeidType.SELVSTENDIG_NÆRINGSDRIVENDE);
        assertThat(utbetalingsgradPrAktivitetDto.getPeriodeMedUtbetalingsgrad().size()).isEqualTo(2);
        PeriodeMedUtbetalingsgradDto periode1 = utbetalingsgradPrAktivitetDto.getPeriodeMedUtbetalingsgrad().get(0);
        assertPeriode(april, periode1, 50);
    }

    @Test
    void skal_mappe_deler_av_måned_med_inntekt_til_utbetaling() {
        // Arrange
        LocalDate skjæringstidspunkt = LocalDate.of(2020, 4, 1);
        Optional<BeregningsgrunnlagGrunnlagEntitet> bgGrunnlagEntitet = lagBeregningsgrunnlagMedSN(skjæringstidspunkt, BigDecimal.valueOf(360_000));
        Intervall april = Intervall.fraOgMedTilOgMed(LocalDate.of(2020, 4, 1), LocalDate.of(2020, 4, 30));
        BigDecimal periodeInntekt = BigDecimal.valueOf(15_000);
        List<OppgittOpptjeningDtoBuilder.EgenNæringBuilder> oppgittNæring = List.of(lagOppgittInntekt(
                Intervall.fraOgMedTilOgMed(april.getFomDato(), LocalDate.of(2020, 4, 15)), periodeInntekt));
        InntektArbeidYtelseGrunnlagDto iayGrunnlag = InntektArbeidYtelseGrunnlagDtoBuilder.nytt()
                .medOppgittOpptjening(OppgittOpptjeningDtoBuilder.ny().leggTilEgneNæringer(oppgittNæring))
                .build();

        // Act
        List<UtbetalingsgradPrAktivitetDto> utbetalingsgrader = UtbetalingsgradMapperFRISINN.map(iayGrunnlag, bgGrunnlagEntitet, LocalDate.of(2020, 5, 6));

        // Assert
        assertThat(utbetalingsgrader.size()).isEqualTo(1);
        UtbetalingsgradPrAktivitetDto utbetalingsgradPrAktivitetDto = utbetalingsgrader.get(0);
        assertThat(utbetalingsgradPrAktivitetDto.getUtbetalingsgradArbeidsforhold().getUttakArbeidType()).isEqualTo(UttakArbeidType.SELVSTENDIG_NÆRINGSDRIVENDE);
        assertThat(utbetalingsgradPrAktivitetDto.getPeriodeMedUtbetalingsgrad().size()).isEqualTo(2);
        PeriodeMedUtbetalingsgradDto periode1 = utbetalingsgradPrAktivitetDto.getPeriodeMedUtbetalingsgrad().get(0);
        assertPeriode(april, periode1, 50);
    }

    @Test
    void skal_mappe_periode_over_2_måneder_med_inntekt_til_utbetaling() {
        // Arrange
        LocalDate skjæringstidspunkt = LocalDate.of(2020, 4, 1);
        Optional<BeregningsgrunnlagGrunnlagEntitet> bgGrunnlagEntitet = lagBeregningsgrunnlagMedSN(skjæringstidspunkt, BigDecimal.valueOf(360_000));
        Intervall april = Intervall.fraOgMedTilOgMed(LocalDate.of(2020, 4, 1), LocalDate.of(2020, 4, 30));
        BigDecimal periodeInntekt = BigDecimal.valueOf(45_000);
        List<OppgittOpptjeningDtoBuilder.EgenNæringBuilder> oppgittNæring = List.of(lagOppgittInntekt(
                Intervall.fraOgMedTilOgMed(LocalDate.of(2020, 3, 17), april.getTomDato()), periodeInntekt));
        InntektArbeidYtelseGrunnlagDto iayGrunnlag = InntektArbeidYtelseGrunnlagDtoBuilder.nytt()
                .medOppgittOpptjening(OppgittOpptjeningDtoBuilder.ny().leggTilEgneNæringer(oppgittNæring))
                .build();

        // Act
        List<UtbetalingsgradPrAktivitetDto> utbetalingsgrader = UtbetalingsgradMapperFRISINN.map(iayGrunnlag, bgGrunnlagEntitet, LocalDate.of(2020, 5, 6));

        // Assert
        assertThat(utbetalingsgrader.size()).isEqualTo(1);
        UtbetalingsgradPrAktivitetDto utbetalingsgradPrAktivitetDto = utbetalingsgrader.get(0);
        assertThat(utbetalingsgradPrAktivitetDto.getUtbetalingsgradArbeidsforhold().getUttakArbeidType()).isEqualTo(UttakArbeidType.SELVSTENDIG_NÆRINGSDRIVENDE);
        assertThat(utbetalingsgradPrAktivitetDto.getPeriodeMedUtbetalingsgrad().size()).isEqualTo(2);
        PeriodeMedUtbetalingsgradDto periode1 = utbetalingsgradPrAktivitetDto.getPeriodeMedUtbetalingsgrad().get(0);
        assertPeriode(april, periode1, 0);
    }

    @Test
    void skal_mappe_periode_over_2_måneder_med_inntekt_til_utbetaling_med_skjæringstidspunkt_midt_i_måned() {
        // Arrange
        LocalDate skjæringstidspunkt = LocalDate.of(2020, 3, 15);
        Optional<BeregningsgrunnlagGrunnlagEntitet> bgGrunnlagEntitet = lagBeregningsgrunnlagMedSN(skjæringstidspunkt, BigDecimal.valueOf(360_000));
        Intervall april = Intervall.fraOgMedTilOgMed(LocalDate.of(2020, 4, 1), LocalDate.of(2020, 4, 30));
        Intervall mars = Intervall.fraOgMedTilOgMed(LocalDate.of(2020, 3, 1), LocalDate.of(2020, 3, 31));
        BigDecimal periodeInntekt = BigDecimal.valueOf(61_0000);
        List<OppgittOpptjeningDtoBuilder.EgenNæringBuilder> oppgittNæring = List.of(lagOppgittInntekt(
                Intervall.fraOgMedTilOgMed(LocalDate.of(2020, 3, 15), april.getTomDato()), periodeInntekt));
        InntektArbeidYtelseGrunnlagDto iayGrunnlag = InntektArbeidYtelseGrunnlagDtoBuilder.nytt()
                .medOppgittOpptjening(OppgittOpptjeningDtoBuilder.ny().leggTilEgneNæringer(oppgittNæring))
                .build();

        // Act
        List<UtbetalingsgradPrAktivitetDto> utbetalingsgrader = UtbetalingsgradMapperFRISINN.map(iayGrunnlag, bgGrunnlagEntitet, LocalDate.of(2020, 5, 6));

        // Assert
        assertThat(utbetalingsgrader.size()).isEqualTo(1);
        UtbetalingsgradPrAktivitetDto utbetalingsgradPrAktivitetDto = utbetalingsgrader.get(0);
        assertThat(utbetalingsgradPrAktivitetDto.getUtbetalingsgradArbeidsforhold().getUttakArbeidType()).isEqualTo(UttakArbeidType.SELVSTENDIG_NÆRINGSDRIVENDE);
        assertThat(utbetalingsgradPrAktivitetDto.getPeriodeMedUtbetalingsgrad().size()).isEqualTo(3);
        PeriodeMedUtbetalingsgradDto periode1 = utbetalingsgradPrAktivitetDto.getPeriodeMedUtbetalingsgrad().get(0);
        assertPeriode(mars, periode1, 0);
        PeriodeMedUtbetalingsgradDto periode2 = utbetalingsgradPrAktivitetDto.getPeriodeMedUtbetalingsgrad().get(1);
        assertPeriode(april, periode2, 0);

    }

    @Test
    void skal_mappe_en_hel_måned_med_inntekt_til_50_prosent_utbetaling_ved_delvis_bortfalt_inntekt_frilans() {
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

        // Act
        List<UtbetalingsgradPrAktivitetDto> utbetalingsgrader = UtbetalingsgradMapperFRISINN.map(iayGrunnlag, bgGrunnlagEntitet, LocalDate.of(2020, 5, 6));

        // Assert
        assertThat(utbetalingsgrader.size()).isEqualTo(1);
        UtbetalingsgradPrAktivitetDto utbetalingsgradPrAktivitetDto = utbetalingsgrader.get(0);
        assertThat(utbetalingsgradPrAktivitetDto.getUtbetalingsgradArbeidsforhold().getUttakArbeidType()).isEqualTo(UttakArbeidType.FRILANS);
        assertThat(utbetalingsgradPrAktivitetDto.getPeriodeMedUtbetalingsgrad().size()).isEqualTo(2);
        PeriodeMedUtbetalingsgradDto periode1 = utbetalingsgradPrAktivitetDto.getPeriodeMedUtbetalingsgrad().get(0);
        assertPeriode(april, periode1, 50);
    }

    private OppgittOpptjeningDtoBuilder.EgenNæringBuilder lagOppgittInntekt(Intervall april, BigDecimal periodeInntekt) {
        return OppgittOpptjeningDtoBuilder.EgenNæringBuilder.ny()
                .medPeriode(april)
                .medVirksomhet(ARBEIDSGIVER.getIdent())
                .medBruttoInntekt(periodeInntekt);
    }

    private OppgittFrilansInntektDto lagOppgittFrilansInntekt(Intervall april, BigDecimal periodeInntekt) {
        return new OppgittFrilansInntektDto(april, periodeInntekt);
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
                        .medBeregnetPrÅr(beregnetPrÅr))
                .build(bg);
        return Optional.of(BeregningsgrunnlagGrunnlagBuilder.oppdatere(Optional.empty())
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
                        .medBeregnetPrÅr(beregnetPrÅr))
                .build(bg);
        return Optional.of(BeregningsgrunnlagGrunnlagBuilder.oppdatere(Optional.empty())
                .medBeregningsgrunnlag(bg)
                .build(1L, BeregningsgrunnlagTilstand.FORESLÅTT));
    }
}
