package no.nav.folketrygdloven.kalkulator;


import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagAktivitetStatusDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;

public class BeregningInntektsmeldingTjenesteTest {

    public static final LocalDate STP = LocalDate.now();
    public static final Arbeidsgiver ARBEIDSGIVER = Arbeidsgiver.virksomhet("345678909");

    @Test
    public void skalFinneRefusjonskravForOpphørsdato() {
        // Arrange
        BeregningsgrunnlagDto beregningsgrunnlag = BeregningsgrunnlagDto.builder()
                .leggTilAktivitetStatus(BeregningsgrunnlagAktivitetStatusDto.builder().medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER))
                .medSkjæringstidspunkt(STP)
                .build();
        BeregningsgrunnlagPeriodeDto periode = BeregningsgrunnlagPeriodeDto.builder()
                .medBeregningsgrunnlagPeriode(STP, STP)
                .build(beregningsgrunnlag);
        BeregningsgrunnlagPrStatusOgAndelDto andel = BeregningsgrunnlagPrStatusOgAndelDto.Builder.ny()
                .medAndelsnr(1L)
                .medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto.builder().medArbeidsgiver(ARBEIDSGIVER))
                .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
                .build(periode);
        BigDecimal refusjonFraIM = BigDecimal.valueOf(1337);
        InntektsmeldingDto im = InntektsmeldingDtoBuilder.builder()
                .medArbeidsgiver(ARBEIDSGIVER)
                .medRefusjon(refusjonFraIM, STP)
                .build();

        // Act
        Optional<BigDecimal> refusjon = BeregningInntektsmeldingTjeneste.finnRefusjonskravPrÅrIPeriodeForAndel(andel, periode.getPeriode(), List.of(im));

        // Assert
        assertThat(refusjon).isPresent();
        assertThat(refusjon.get()).isEqualByComparingTo(refusjonFraIM.multiply(BigDecimal.valueOf(12)));

    }
}
