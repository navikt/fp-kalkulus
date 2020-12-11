package no.nav.folketrygdloven.kalkulus.beregning;


import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.lang.reflect.Field;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulus.kodeverk.HåndteringKode;

public class MapHåndteringskodeTilTilstandTest {

    @Test
    public void alle_håndteringskoder_skal_vere_mappet_til_tilstander() {
        Field[] felter = HåndteringKode.class.getDeclaredFields();
        for (Field felt : felter) {
            if (felt.getType().getName().equals(HåndteringKode.class.getName())) {
                try {
                    HåndteringKode håndteringKode = (HåndteringKode) felt.get(null);
                    BeregningsgrunnlagTilstand tilstand = MapHåndteringskodeTilTilstand.map(håndteringKode);
                    assertThat(tilstand).isNotNull();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
