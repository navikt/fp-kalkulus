package no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.periodisering;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.Test;

import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.RefusjonDto;
import no.nav.folketrygdloven.kalkulator.regelmodell.Periode;
import no.nav.folketrygdloven.kalkulator.regelmodell.grunnlag.inntekt.Refusjonskrav;

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
}
