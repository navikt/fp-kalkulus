package no.nav.folketrygdloven.kalkulus.beregning;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulus.kodeverk.StegType;

public class MapStegTilTilstand {

    private static Map<StegType, BeregningsgrunnlagTilstand> mapStegTilstand = new HashMap<>();
    private static Map<StegType, BeregningsgrunnlagTilstand> mapStegUtTilstand = new HashMap<>();

    static {
        mapStegTilstand.put(StegType.KOFAKBER, BeregningsgrunnlagTilstand.OPPDATERT_MED_ANDELER);
        mapStegTilstand.put(StegType.FORS_BESTEBEREGNING, BeregningsgrunnlagTilstand.BESTEBEREGNET);
        mapStegTilstand.put(StegType.FORS_BERGRUNN, BeregningsgrunnlagTilstand.FORESLÅTT);
        mapStegTilstand.put(StegType.VURDER_REF_BERGRUNN, BeregningsgrunnlagTilstand.VURDERT_REFUSJON);
        mapStegTilstand.put(StegType.FORDEL_BERGRUNN, BeregningsgrunnlagTilstand.OPPDATERT_MED_REFUSJON_OG_GRADERING);
        mapStegTilstand.put(StegType.FAST_BERGRUNN, BeregningsgrunnlagTilstand.FASTSATT);

        mapStegUtTilstand.put(StegType.KOFAKBER, BeregningsgrunnlagTilstand.KOFAKBER_UT);
        mapStegUtTilstand.put(StegType.FORS_BERGRUNN, BeregningsgrunnlagTilstand.FORESLÅTT_UT);
        mapStegUtTilstand.put(StegType.VURDER_REF_BERGRUNN, BeregningsgrunnlagTilstand.VURDERT_REFUSJON_UT);
        mapStegUtTilstand.put(StegType.FORDEL_BERGRUNN, BeregningsgrunnlagTilstand.FASTSATT_INN);
    }

    public static BeregningsgrunnlagTilstand mapTilStegTilstand(StegType kode) {
        if (mapStegTilstand.containsKey(kode)) {
            return mapStegTilstand.get(kode);
        }
        throw new IllegalStateException("Finner ikke tilstand for steg " + kode.getKode());
    }

    public static Optional<BeregningsgrunnlagTilstand> mapTilStegUtTilstand(StegType kode) {
        return Optional.ofNullable(mapStegUtTilstand.get(kode));
    }



}
