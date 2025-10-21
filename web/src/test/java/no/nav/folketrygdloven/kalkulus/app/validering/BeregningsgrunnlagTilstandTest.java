package no.nav.folketrygdloven.kalkulus.app.validering;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand;

class BeregningsgrunnlagTilstandTest {

    @Test
    void skal_sjekke_at_alle_tilstander_har_definert_rekkefølge() {
        BeregningsgrunnlagTilstand[] alleTilstander = BeregningsgrunnlagTilstand.values();
        List<BeregningsgrunnlagTilstand> tilstandRekkefølge = BeregningsgrunnlagTilstand.getTilstandRekkefølge();
        for (BeregningsgrunnlagTilstand tilstand : alleTilstander) {
            if (!tilstand.equals(BeregningsgrunnlagTilstand.UDEFINERT)) {
                assertTrue(tilstandRekkefølge.contains(tilstand), "Tilstand " + tilstand + " er ikke definert i tilstandrekkefølge");
            }
        }
    }

}
