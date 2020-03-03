package no.nav.folketrygdloven.kalkulus.rest.dto;


import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.math.BigDecimal;
import java.util.Collections;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.Inntektskategori;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.FaktaOmBeregningAndelDto;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.FordelBeregningsgrunnlagAndelDto;


public class FordelBeregningsgrunnlagAndelDtoTest {

    @Test
    public void skal_avrunde_refusjonskrav() {

        FaktaOmBeregningAndelDto superDto = new FaktaOmBeregningAndelDto(1L, null,
                Inntektskategori.ARBEIDSTAKER, AktivitetStatus.ARBEIDSTAKER,
                false, false, Collections.emptyList());
        FordelBeregningsgrunnlagAndelDto andel = new FordelBeregningsgrunnlagAndelDto(superDto);
        andel.setRefusjonskravPrAar(BigDecimal.valueOf(122_000.61));

        assertThat(andel.getRefusjonskravPrAar()).isEqualByComparingTo(BigDecimal.valueOf(122_001));
    }
}
