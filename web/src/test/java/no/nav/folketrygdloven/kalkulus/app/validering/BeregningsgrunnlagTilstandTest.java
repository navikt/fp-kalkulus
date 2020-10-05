package no.nav.folketrygdloven.kalkulus.app.validering;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.BeregningsgrunnlagTilstand;

import org.junit.jupiter.api.Test;

public class BeregningsgrunnlagTilstandTest {

    @Test
    public void skal_sjekke_at_alle_tilstander_har_definert_rekkefølge() {
        BeregningsgrunnlagTilstand[] alleTilstander = BeregningsgrunnlagTilstand.values();
        List<BeregningsgrunnlagTilstand> tilstandRekkefølge = BeregningsgrunnlagTilstand.getTilstandRekkefølge();
        for (BeregningsgrunnlagTilstand tilstand : alleTilstander) {
            if (!tilstand.equals(BeregningsgrunnlagTilstand.UDEFINERT)) {
                assertTrue(tilstandRekkefølge.contains(tilstand), "Tilstand " + tilstand + " er ikke definert i tilstandrekkefølge");
            }
        }
    }

}
