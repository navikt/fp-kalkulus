package no.nav.folketrygdloven.kalkulus.beregning;

import java.util.HashMap;
import java.util.Map;

import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulus.kodeverk.StegType;

class MapStegTilTilstand {

    private static Map<StegType, BeregningsgrunnlagTilstand> map = new HashMap<>();

    static {
        map.put(StegType.KOFAKBER, BeregningsgrunnlagTilstand.OPPDATERT_MED_ANDELER);
        map.put(StegType.FORS_BERGRUNN, BeregningsgrunnlagTilstand.FORESLÅTT);
        map.put(StegType.VURDER_REF_BERGRUNN, BeregningsgrunnlagTilstand.VURDERT_REFUSJON);
        map.put(StegType.FORDEL_BERGRUNN, BeregningsgrunnlagTilstand.OPPDATERT_MED_REFUSJON_OG_GRADERING);
        map.put(StegType.FAST_BERGRUNN, BeregningsgrunnlagTilstand.FASTSATT);
    }

    public static BeregningsgrunnlagTilstand map(StegType kode) {
        if (map.containsKey(kode)) {
            return map.get(kode);
        }
        throw new IllegalStateException("Finner ikke tilstand for steg " + kode.getKode());
    }

}
