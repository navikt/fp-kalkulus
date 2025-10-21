package no.nav.folketrygdloven.kalkulus.beregning;


import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningSteg;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand;

class MapStegTilTilstandTest {

    @Test
    void alle_steg_skal_være_mappet_til_en_tilstand() {
        Arrays.asList(BeregningSteg.values()).forEach(steg -> {
            BeregningsgrunnlagTilstand tilstand = MapStegTilTilstand.mapTilStegTilstand(steg);
            assertThat(tilstand).isNotNull();
        });
    }

}
