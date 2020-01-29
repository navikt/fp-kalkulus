package no.nav.folketrygdloven.kalkulator.rest.dto;


import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.math.BigDecimal;
import java.util.Collections;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.Inntektskategori;


public class FordelBeregningsgrunnlagAndelDtoTest {

    @Test
    public void skal_avrunde_refusjonskrav() {

        FaktaOmBeregningAndelDto superDto = new FaktaOmBeregningAndelDto(1L, null, Inntektskategori.ARBEIDSTAKER, AktivitetStatus.ARBEIDSTAKER, false, false, Collections.emptyList());
        FordelBeregningsgrunnlagAndelDto andel = new FordelBeregningsgrunnlagAndelDto(superDto);
        andel.setRefusjonskravPrAar(BigDecimal.valueOf(122_000.61));

        assertThat(andel.getRefusjonskravPrAar()).isEqualByComparingTo(BigDecimal.valueOf(122_001));
    }
}
