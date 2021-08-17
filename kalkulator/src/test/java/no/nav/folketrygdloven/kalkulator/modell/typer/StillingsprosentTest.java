package no.nav.folketrygdloven.kalkulator.modell.typer;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

class StillingsprosentTest {

    @Test
    void name() {
        var stillingsprosent = new Stillingsprosent(BigDecimal.ZERO);

        assertThat(stillingsprosent).isEqualTo(Stillingsprosent.ZERO);
    }

    @Test
    void ikke_mer_enn_500() {
        var stillingsprosent = new Stillingsprosent(new BigDecimal(600));

        assertThat(stillingsprosent).isEqualTo(new Stillingsprosent(500));
    }
}
