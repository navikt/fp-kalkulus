package no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.periodisering;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Refusjonskrav;
import no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.OmsorgspengerGrunnlag;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto.Builder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingAggregatDto.InntektsmeldingAggregatDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.RefusjonDto;
import no.nav.folketrygdloven.kalkulator.modell.svp.PeriodeMedUtbetalingsgradDto;
import no.nav.folketrygdloven.kalkulator.modell.svp.UtbetalingsgradArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.svp.UtbetalingsgradPrAktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.modell.uttak.UttakArbeidType;
import no.nav.folketrygdloven.kalkulator.modell.virksomhet.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;

public class MapRefusjonskravFraVLTilRegelTest {

    public static final Arbeidsgiver ARBEIDSGIVER1 = Arbeidsgiver.virksomhet("1234786124");
    public static final Arbeidsgiver ARBEIDSGIVER2 = Arbeidsgiver.virksomhet("09872335");

    @Test
    public void refusjonFraSenereDato() {
        // Arrange
        LocalDate skjæringstidspunkt = LocalDate.now();
        LocalDate endringFom = skjæringstidspunkt.plusMonths(1);
        InntektsmeldingDto inntektsmeldingEntitet = InntektsmeldingDtoBuilder.builder()
            .medRefusjon(BigDecimal.ZERO)
            .leggTil(new RefusjonDto(BigDecimal.TEN, endringFom))
            .build();

        // Act
        List<Refusjonskrav> resultat = MapRefusjonskravFraVLTilRegel.periodiserRefusjonsbeløp(inntektsmeldingEntitet, skjæringstidspunkt);

        // Assert
        assertThat(resultat).hasSize(2);
        assertThat(resultat).anySatisfy(start -> {
            assertThat(start.getPeriode()).isEqualTo(Periode.of(skjæringstidspunkt, endringFom.minusDays(1)));
            assertThat(start.getMånedsbeløp()).isEqualByComparingTo(BigDecimal.ZERO);
        });
        assertThat(resultat).anySatisfy(endring -> {
            assertThat(endring.getPeriode()).isEqualTo(Periode.of(endringFom, null));
            assertThat(endring.getMånedsbeløp()).isEqualByComparingTo(BigDecimal.TEN);
        });
    }

    @Test
    public void skal_summere_refusjonskrav_i_bgPeriode_og_gi_årssummert_i_endring() {
        // Arrange
        LocalDate idag = LocalDate.now();
        LocalDate om2Måneder = LocalDate.now().plusMonths(2);

        Builder periodeBuilder = BeregningsgrunnlagPeriodeDto.builder();
        periodeBuilder.medBeregningsgrunnlagPeriode(idag, om2Måneder);
        BeregningsgrunnlagPeriodeDto periodeDto = periodeBuilder.build();

        InntektsmeldingAggregatDtoBuilder inntektsmeldingAggregatDtoBuilder = InntektsmeldingAggregatDtoBuilder.ny();
        InntektsmeldingDtoBuilder inntektsmeldingDtoBuilder = InntektsmeldingDtoBuilder.builder();
        inntektsmeldingDtoBuilder.medStartDatoPermisjon(idag)
                .medArbeidsgiver(ARBEIDSGIVER1)
                .medRefusjon(BigDecimal.valueOf(2000))
                .leggTil(new RefusjonDto(BigDecimal.valueOf(10000), idag.plusMonths(1)));
        InntektsmeldingDtoBuilder inntektsmeldingDtoBuilder2 = InntektsmeldingDtoBuilder.builder();
        inntektsmeldingDtoBuilder2.medStartDatoPermisjon(idag)
                .medArbeidsgiver(ARBEIDSGIVER2)
                .medRefusjon(BigDecimal.valueOf(2000))
                .leggTil(new RefusjonDto(BigDecimal.valueOf(10000), idag.plusMonths(1)));

        inntektsmeldingAggregatDtoBuilder.leggTil(inntektsmeldingDtoBuilder.build());
        inntektsmeldingAggregatDtoBuilder.leggTil(inntektsmeldingDtoBuilder2.build());
        InntektsmeldingAggregatDto inntektsmeldingAggregatDto = inntektsmeldingAggregatDtoBuilder.build();

        OmsorgspengerGrunnlag omsorgspengerGrunnlag = lagOmsorgpengerGrunnlag(idag);

        // Act
        BigDecimal høyestRefusjonskravForBGPerioden = MapRefusjonskravFraVLTilRegel.finnLavesteTotalRefusjonForBGPerioden(periodeDto, inntektsmeldingAggregatDto.getInntektsmeldingerSomSkalBrukes(), idag, omsorgspengerGrunnlag);

        // Assert
        assertThat(høyestRefusjonskravForBGPerioden).isEqualByComparingTo(BigDecimal.valueOf(48_000));
    }

    @Test
    public void skal_finne_høyeste_refusjonskrav_i_bgPeriode_og_gi_årssummert_lavest_opprinnelig() {
        // Arrange
        LocalDate idag = LocalDate.now();
        LocalDate om10Dager = LocalDate.now().plusDays(10);

        Builder periodeBuilder = BeregningsgrunnlagPeriodeDto.builder();
        periodeBuilder.medBeregningsgrunnlagPeriode(idag, om10Dager);
        BeregningsgrunnlagPeriodeDto periodeDto = periodeBuilder.build();

        InntektsmeldingAggregatDtoBuilder inntektsmeldingAggregatDtoBuilder = InntektsmeldingAggregatDtoBuilder.ny();
        InntektsmeldingDtoBuilder inntektsmeldingDtoBuilder = InntektsmeldingDtoBuilder.builder();
        inntektsmeldingDtoBuilder.medStartDatoPermisjon(idag)
                .medArbeidsgiver(ARBEIDSGIVER1)
                .medRefusjon(BigDecimal.valueOf(12000))
                .leggTil(new RefusjonDto(BigDecimal.valueOf(10000), idag.plusMonths(1)));
        InntektsmeldingDtoBuilder inntektsmeldingDtoBuilder2 = InntektsmeldingDtoBuilder.builder();
        inntektsmeldingDtoBuilder2.medStartDatoPermisjon(idag)
                .medArbeidsgiver(ARBEIDSGIVER2)
                .medRefusjon(BigDecimal.valueOf(12000))
                .leggTil(new RefusjonDto(BigDecimal.valueOf(10000), idag.plusMonths(1)));

        inntektsmeldingAggregatDtoBuilder.leggTil(inntektsmeldingDtoBuilder.build());
        inntektsmeldingAggregatDtoBuilder.leggTil(inntektsmeldingDtoBuilder2.build());
        InntektsmeldingAggregatDto inntektsmeldingAggregatDto = inntektsmeldingAggregatDtoBuilder.build();

        OmsorgspengerGrunnlag omsorgspengerGrunnlag = lagOmsorgpengerGrunnlag(idag);


        // Act
        BigDecimal høyestRefusjonskravForBGPerioden = MapRefusjonskravFraVLTilRegel.finnLavesteTotalRefusjonForBGPerioden(periodeDto, inntektsmeldingAggregatDto.getInntektsmeldingerSomSkalBrukes(), idag, omsorgspengerGrunnlag);

        // Assert
        assertThat(høyestRefusjonskravForBGPerioden).isEqualByComparingTo(BigDecimal.valueOf(288_000));
    }

    @Test
    public void skal_finne_høyeste_refusjonskrav_i_bgPeriode_og_gi_årssummert_lavest_opprinnelig_med_utbetalingsgrad() {
        // Arrange
        LocalDate idag = LocalDate.now();
        LocalDate om2Måneder = idag.plusMonths(2);

        Builder periodeBuilder = BeregningsgrunnlagPeriodeDto.builder();
        periodeBuilder.medBeregningsgrunnlagPeriode(idag, om2Måneder);
        BeregningsgrunnlagPeriodeDto periodeDto = periodeBuilder.build();

        InntektsmeldingAggregatDtoBuilder inntektsmeldingAggregatDtoBuilder = InntektsmeldingAggregatDtoBuilder.ny();
        InntektsmeldingDtoBuilder inntektsmeldingDtoBuilder = InntektsmeldingDtoBuilder.builder();
        inntektsmeldingDtoBuilder.medStartDatoPermisjon(idag)
                .medArbeidsgiver(ARBEIDSGIVER1)
                .medRefusjon(BigDecimal.valueOf(12000))
                .leggTil(new RefusjonDto(BigDecimal.valueOf(10000), idag.plusMonths(1)));
        InntektsmeldingDtoBuilder inntektsmeldingDtoBuilder2 = InntektsmeldingDtoBuilder.builder();
        inntektsmeldingDtoBuilder2.medStartDatoPermisjon(idag)
                .medArbeidsgiver(ARBEIDSGIVER2)
                .medRefusjon(BigDecimal.valueOf(12000))
                .leggTil(new RefusjonDto(BigDecimal.valueOf(10000), idag.plusMonths(1)));

        inntektsmeldingAggregatDtoBuilder.leggTil(inntektsmeldingDtoBuilder.build());
        inntektsmeldingAggregatDtoBuilder.leggTil(inntektsmeldingDtoBuilder2.build());
        InntektsmeldingAggregatDto inntektsmeldingAggregatDto = inntektsmeldingAggregatDtoBuilder.build();

        OmsorgspengerGrunnlag omsorgspengerGrunnlag = new OmsorgspengerGrunnlag(List.of(
                new UtbetalingsgradPrAktivitetDto(
                        new UtbetalingsgradArbeidsforholdDto(ARBEIDSGIVER1, InternArbeidsforholdRefDto.nullRef(), UttakArbeidType.ORDINÆRT_ARBEID),
                        List.of(new PeriodeMedUtbetalingsgradDto(Intervall.fraOgMed(idag), BigDecimal.valueOf(100)))),
                new UtbetalingsgradPrAktivitetDto(
                        new UtbetalingsgradArbeidsforholdDto(ARBEIDSGIVER2, InternArbeidsforholdRefDto.nullRef(), UttakArbeidType.ORDINÆRT_ARBEID),
                        List.of(new PeriodeMedUtbetalingsgradDto(Intervall.fraOgMed(idag), BigDecimal.valueOf(50))))));

        // Act
        BigDecimal høyestRefusjonskravForBGPerioden = MapRefusjonskravFraVLTilRegel.finnLavesteTotalRefusjonForBGPerioden(periodeDto, inntektsmeldingAggregatDto.getInntektsmeldingerSomSkalBrukes(), idag, omsorgspengerGrunnlag);

        // Assert
        assertThat(høyestRefusjonskravForBGPerioden).isEqualByComparingTo(BigDecimal.valueOf(180_000));
    }

    private OmsorgspengerGrunnlag lagOmsorgpengerGrunnlag(LocalDate idag) {
        return new OmsorgspengerGrunnlag(List.of(
                new UtbetalingsgradPrAktivitetDto(
                        new UtbetalingsgradArbeidsforholdDto(ARBEIDSGIVER1, InternArbeidsforholdRefDto.nullRef(), UttakArbeidType.ORDINÆRT_ARBEID),
                        List.of(new PeriodeMedUtbetalingsgradDto(Intervall.fraOgMed(idag), BigDecimal.valueOf(100)))),
                new UtbetalingsgradPrAktivitetDto(
                        new UtbetalingsgradArbeidsforholdDto(ARBEIDSGIVER2, InternArbeidsforholdRefDto.nullRef(), UttakArbeidType.ORDINÆRT_ARBEID),
                        List.of(new PeriodeMedUtbetalingsgradDto(Intervall.fraOgMed(idag), BigDecimal.valueOf(100))))));
    }
}
