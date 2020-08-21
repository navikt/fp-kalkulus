package no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.periodisering;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningRefusjonOverstyringDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningRefusjonOverstyringerDto;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningRefusjonPeriodeDto;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Refusjonskrav;
import no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.OmsorgspengerGrunnlag;
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
    public BeregningRefusjonOverstyringerDto.Builder refusjonOverstyringer = BeregningRefusjonOverstyringerDto.builder();

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
        List<Refusjonskrav> resultat = MapRefusjonskravFraVLTilRegel.periodiserRefusjonsbeløp(inntektsmeldingEntitet, skjæringstidspunkt, Optional.empty());

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
    public void skal_finne_refusjonskrav_på_stp_med_uten_refusjon_fra_start() {
        // Arrange
        LocalDate idag = LocalDate.now();
        InntektsmeldingAggregatDtoBuilder inntektsmeldingAggregatDtoBuilder = InntektsmeldingAggregatDtoBuilder.ny();
        InntektsmeldingDtoBuilder inntektsmeldingDtoBuilder = InntektsmeldingDtoBuilder.builder();
        inntektsmeldingDtoBuilder.medStartDatoPermisjon(idag)
                .medArbeidsgiver(ARBEIDSGIVER1)
                .medRefusjon(BigDecimal.valueOf(0))
                .leggTil(new RefusjonDto(BigDecimal.valueOf(10000), idag.plusMonths(1)));
        InntektsmeldingDtoBuilder inntektsmeldingDtoBuilder2 = InntektsmeldingDtoBuilder.builder();
        inntektsmeldingDtoBuilder2.medStartDatoPermisjon(idag)
                .medArbeidsgiver(ARBEIDSGIVER2)
                .medRefusjon(BigDecimal.valueOf(0))
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
        BigDecimal refusjonPåStp = MapRefusjonskravFraVLTilRegel.finnGradertRefusjonskravPåSkjæringstidspunktet(inntektsmeldingAggregatDto.getInntektsmeldingerSomSkalBrukes(), idag, omsorgspengerGrunnlag, Optional.empty());

        // Assert
        assertThat(refusjonPåStp).isEqualByComparingTo(BigDecimal.valueOf(0));
    }


    @Test
    public void skal_finne_refusjonskrav_på_stp_med_endring_i_refusjonskrav() {
        // Arrange
        LocalDate idag = LocalDate.now();
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
        BigDecimal refusjonPåStp = MapRefusjonskravFraVLTilRegel.finnGradertRefusjonskravPåSkjæringstidspunktet(inntektsmeldingAggregatDto.getInntektsmeldingerSomSkalBrukes(), idag, omsorgspengerGrunnlag, Optional.empty());

        // Assert
        assertThat(refusjonPåStp).isEqualByComparingTo(BigDecimal.valueOf(216_000));
    }

    @Test
    public void skal_bruke_overstyrt_dato_om_denne_finnes_og_matcher_arbeidsforholdet() {
        // Arrange
        LocalDate skjæringstidspunkt = LocalDate.now();
        InternArbeidsforholdRefDto ref = InternArbeidsforholdRefDto.nyRef();
        LocalDate overstyrtDato = skjæringstidspunkt.plusDays(15);
        InntektsmeldingDto inntektsmeldingEntitet = InntektsmeldingDtoBuilder.builder()
                .medRefusjon(BigDecimal.TEN)
                .medArbeidsgiver(ARBEIDSGIVER1)
                .medArbeidsforholdId(ref)
                .build();
        lagRefusjonoverstyring(ARBEIDSGIVER1, ref, overstyrtDato);

        // Act
        List<Refusjonskrav> resultat = MapRefusjonskravFraVLTilRegel.periodiserRefusjonsbeløp(inntektsmeldingEntitet, skjæringstidspunkt,Optional.of(refusjonOverstyringer.build()));

        // Assert
        assertThat(resultat).hasSize(1);
        assertThat(resultat).anySatisfy(start -> {
            assertThat(start.getPeriode()).isEqualTo(Periode.of(overstyrtDato, Intervall.TIDENES_ENDE));
            assertThat(start.getMånedsbeløp()).isEqualByComparingTo(BigDecimal.TEN);
        });
    }

    @Test
    public void skal_ikke_bruke_overstyrt_dato_om_denne_finnes_og_ikke_matcher_arbeidsforholdet() {
        // Arrange
        LocalDate skjæringstidspunkt = LocalDate.now();
        InternArbeidsforholdRefDto refIM = InternArbeidsforholdRefDto.nyRef();
        InternArbeidsforholdRefDto refOverstyring = InternArbeidsforholdRefDto.nyRef();
        LocalDate overstyrtDato = skjæringstidspunkt.plusDays(15);
        InntektsmeldingDto inntektsmeldingEntitet = InntektsmeldingDtoBuilder.builder()
                .medRefusjon(BigDecimal.TEN)
                .medArbeidsgiver(ARBEIDSGIVER1)
                .medArbeidsforholdId(refIM)
                .build();
        lagRefusjonoverstyring(ARBEIDSGIVER1, refOverstyring, overstyrtDato);

        // Act
        List<Refusjonskrav> resultat = MapRefusjonskravFraVLTilRegel.periodiserRefusjonsbeløp(inntektsmeldingEntitet, skjæringstidspunkt,Optional.of(refusjonOverstyringer.build()));

        // Assert
        assertThat(resultat).hasSize(1);
        assertThat(resultat).anySatisfy(start -> {
            assertThat(start.getPeriode()).isEqualTo(Periode.of(skjæringstidspunkt, Intervall.TIDENES_ENDE));
            assertThat(start.getMånedsbeløp()).isEqualByComparingTo(BigDecimal.TEN);
        });
    }


    private void lagRefusjonoverstyring(Arbeidsgiver ag, InternArbeidsforholdRefDto ref, LocalDate dato) {
        BeregningRefusjonPeriodeDto periodeOverstyring = new BeregningRefusjonPeriodeDto(ref, dato);
        BeregningRefusjonOverstyringDto overstyring = new BeregningRefusjonOverstyringDto(ag, null, Collections.singletonList(periodeOverstyring));
        refusjonOverstyringer.leggTilOverstyring(overstyring);
    }

}
