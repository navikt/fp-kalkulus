package no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.periodisering;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Refusjonskrav;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto.Builder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingAggregatDto.InntektsmeldingAggregatDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.RefusjonDto;

public class MapRefusjonskravFraVLTilRegelTest {

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
    public void skal_finne_høyeste_refusjonskrav_i_bgPeriode_og_gi_årssummert_størst_i_endring() {
        // Arrange
        LocalDate idag = LocalDate.now();
        LocalDate om2Måneder = LocalDate.now().plusMonths(2);

        Builder periodeBuilder = BeregningsgrunnlagPeriodeDto.builder();
        periodeBuilder.medBeregningsgrunnlagPeriode(idag, om2Måneder);
        BeregningsgrunnlagPeriodeDto periodeDto = periodeBuilder.build();

        InntektsmeldingAggregatDtoBuilder inntektsmeldingAggregatDtoBuilder = InntektsmeldingAggregatDtoBuilder.ny();
        InntektsmeldingDtoBuilder inntektsmeldingDtoBuilder = InntektsmeldingDtoBuilder.builder();
        inntektsmeldingDtoBuilder.medStartDatoPermisjon(idag)
                .medRefusjon(BigDecimal.valueOf(2000))
                .leggTil(new RefusjonDto(BigDecimal.valueOf(10000), idag.plusMonths(1)));

        inntektsmeldingAggregatDtoBuilder.leggTil(inntektsmeldingDtoBuilder.build());
        InntektsmeldingAggregatDto inntektsmeldingAggregatDto = inntektsmeldingAggregatDtoBuilder.build();

        // Act
        BigDecimal høyestRefusjonskravForBGPerioden = MapRefusjonskravFraVLTilRegel.finnHøyestRefusjonskravForBGPerioden(periodeDto, Optional.of(inntektsmeldingAggregatDto), idag);

        // Assert
        assertThat(høyestRefusjonskravForBGPerioden).isEqualByComparingTo(BigDecimal.valueOf(120000));
    }

    @Test
    public void skal_finne_høyeste_refusjonskrav_i_bgPeriode_og_gi_årssummert_størst_opprinnelig() {
        // Arrange
        LocalDate idag = LocalDate.now();
        LocalDate om2Måneder = LocalDate.now().plusMonths(2);

        Builder periodeBuilder = BeregningsgrunnlagPeriodeDto.builder();
        periodeBuilder.medBeregningsgrunnlagPeriode(idag, om2Måneder);
        BeregningsgrunnlagPeriodeDto periodeDto = periodeBuilder.build();

        InntektsmeldingAggregatDtoBuilder inntektsmeldingAggregatDtoBuilder = InntektsmeldingAggregatDtoBuilder.ny();
        InntektsmeldingDtoBuilder inntektsmeldingDtoBuilder = InntektsmeldingDtoBuilder.builder();
        inntektsmeldingDtoBuilder.medStartDatoPermisjon(idag)
                .medRefusjon(BigDecimal.valueOf(12000))
                .leggTil(new RefusjonDto(BigDecimal.valueOf(10000), idag.plusMonths(1)));

        inntektsmeldingAggregatDtoBuilder.leggTil(inntektsmeldingDtoBuilder.build());
        InntektsmeldingAggregatDto inntektsmeldingAggregatDto = inntektsmeldingAggregatDtoBuilder.build();

        // Act
        BigDecimal høyestRefusjonskravForBGPerioden = MapRefusjonskravFraVLTilRegel.finnHøyestRefusjonskravForBGPerioden(periodeDto, Optional.of(inntektsmeldingAggregatDto), idag);

        // Assert
        assertThat(høyestRefusjonskravForBGPerioden).isEqualByComparingTo(BigDecimal.valueOf(144000));
    }
}
