package no.nav.folketrygdloven.kalkulus.beregning;


import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Field;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulus.kodeverk.StegType;

public class MapStegTilTilstandTest {

    @Test
    public void alle_steg_skal_være_mappet_til_en_tilstand() {
        Field[] felter = StegType.class.getDeclaredFields();
        for (Field felt : felter) {
            if (felt.getType().getName().equals(StegType.class.getName())) {
                try {
                    StegType stegType = (StegType) felt.get(null);
                    BeregningsgrunnlagTilstand tilstand = MapStegTilTilstand.mapTilStegTilstand(stegType);
                    assertThat(tilstand).isNotNull();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
