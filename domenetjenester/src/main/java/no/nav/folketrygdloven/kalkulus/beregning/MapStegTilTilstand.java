package no.nav.folketrygdloven.kalkulus.beregning;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningSteg;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand;

public class MapStegTilTilstand {

    private static Map<BeregningSteg, BeregningsgrunnlagTilstand> mapStegTilstand = new HashMap<>();
    private static Map<BeregningSteg, BeregningsgrunnlagTilstand> mapStegUtTilstand = new HashMap<>();

    static {
        mapStegTilstand.put(BeregningSteg.FASTSETT_STP_BER, BeregningsgrunnlagTilstand.OPPRETTET);
        mapStegTilstand.put(BeregningSteg.KOFAKBER, BeregningsgrunnlagTilstand.OPPDATERT_MED_ANDELER);
        mapStegTilstand.put(BeregningSteg.FORS_BESTEBEREGNING, BeregningsgrunnlagTilstand.BESTEBEREGNET);
        mapStegTilstand.put(BeregningSteg.FORS_BERGRUNN, BeregningsgrunnlagTilstand.FORESLÅTT);
        mapStegTilstand.put(BeregningSteg.VURDER_REF_BERGRUNN, BeregningsgrunnlagTilstand.VURDERT_REFUSJON);
        mapStegTilstand.put(BeregningSteg.FORDEL_BERGRUNN, BeregningsgrunnlagTilstand.OPPDATERT_MED_REFUSJON_OG_GRADERING);
        mapStegTilstand.put(BeregningSteg.FAST_BERGRUNN, BeregningsgrunnlagTilstand.FASTSATT);

        mapStegUtTilstand.put(BeregningSteg.FASTSETT_STP_BER, BeregningsgrunnlagTilstand.FASTSATT_BEREGNINGSAKTIVITETER);
        mapStegUtTilstand.put(BeregningSteg.KOFAKBER, BeregningsgrunnlagTilstand.KOFAKBER_UT);
        mapStegUtTilstand.put(BeregningSteg.FORS_BERGRUNN, BeregningsgrunnlagTilstand.FORESLÅTT_UT);
        mapStegUtTilstand.put(BeregningSteg.VURDER_REF_BERGRUNN, BeregningsgrunnlagTilstand.VURDERT_REFUSJON_UT);
        mapStegUtTilstand.put(BeregningSteg.FORDEL_BERGRUNN, BeregningsgrunnlagTilstand.FASTSATT_INN);
    }

    public static BeregningsgrunnlagTilstand mapTilStegTilstand(BeregningSteg kode) {
        if (mapStegTilstand.containsKey(kode)) {
            return mapStegTilstand.get(kode);
        }
        throw new IllegalStateException("Finner ikke tilstand for steg " + kode.getKode());
    }

    public static Optional<BeregningsgrunnlagTilstand> mapTilStegUtTilstand(BeregningSteg kode) {
        return Optional.ofNullable(mapStegUtTilstand.get(kode));
    }



}
