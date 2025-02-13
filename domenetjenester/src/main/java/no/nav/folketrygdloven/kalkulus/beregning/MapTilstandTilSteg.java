package no.nav.folketrygdloven.kalkulus.beregning;

import static no.nav.folketrygdloven.kalkulus.kodeverk.BeregningSteg.FASTSETT_STP_BER;
import static no.nav.folketrygdloven.kalkulus.kodeverk.BeregningSteg.FAST_BERGRUNN;
import static no.nav.folketrygdloven.kalkulus.kodeverk.BeregningSteg.FORDEL_BERGRUNN;
import static no.nav.folketrygdloven.kalkulus.kodeverk.BeregningSteg.FORS_BERGRUNN;
import static no.nav.folketrygdloven.kalkulus.kodeverk.BeregningSteg.FORS_BERGRUNN_2;
import static no.nav.folketrygdloven.kalkulus.kodeverk.BeregningSteg.FORS_BESTEBEREGNING;
import static no.nav.folketrygdloven.kalkulus.kodeverk.BeregningSteg.KOFAKBER;
import static no.nav.folketrygdloven.kalkulus.kodeverk.BeregningSteg.VURDER_REF_BERGRUNN;
import static no.nav.folketrygdloven.kalkulus.kodeverk.BeregningSteg.VURDER_TILKOMMET_INNTEKT;
import static no.nav.folketrygdloven.kalkulus.kodeverk.BeregningSteg.VURDER_VILKAR_BERGRUNN;
import static no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand.BESTEBEREGNET;
import static no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand.FASTSATT;
import static no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand.FASTSATT_BEREGNINGSAKTIVITETER;
import static no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand.FASTSATT_INN;
import static no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand.FORESLÅTT;
import static no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand.FORESLÅTT_DEL_2;
import static no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand.FORESLÅTT_DEL_2_UT;
import static no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand.FORESLÅTT_UT;
import static no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand.KOFAKBER_UT;
import static no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand.OPPDATERT_MED_ANDELER;
import static no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand.OPPDATERT_MED_REFUSJON_OG_GRADERING;
import static no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand.OPPRETTET;
import static no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand.VURDERT_REFUSJON;
import static no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand.VURDERT_REFUSJON_UT;
import static no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand.VURDERT_TILKOMMET_INNTEKT;
import static no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand.VURDERT_TILKOMMET_INNTEKT_UT;
import static no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand.VURDERT_VILKÅR;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningSteg;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand;

public class MapTilstandTilSteg {

    private static final Map<BeregningsgrunnlagTilstand, BeregningSteg> MAP_TILSTAND_STEG = new EnumMap<>(BeregningsgrunnlagTilstand.class);

    static {
        MAP_TILSTAND_STEG.put(OPPRETTET, FASTSETT_STP_BER);
        MAP_TILSTAND_STEG.put(FASTSATT_BEREGNINGSAKTIVITETER, FASTSETT_STP_BER);
        MAP_TILSTAND_STEG.put(OPPDATERT_MED_ANDELER, KOFAKBER);
        MAP_TILSTAND_STEG.put(KOFAKBER_UT, KOFAKBER);
        MAP_TILSTAND_STEG.put(BESTEBEREGNET, FORS_BESTEBEREGNING);
        MAP_TILSTAND_STEG.put(FORESLÅTT, FORS_BERGRUNN);
        MAP_TILSTAND_STEG.put(FORESLÅTT_UT, FORS_BERGRUNN);
        MAP_TILSTAND_STEG.put(FORESLÅTT_DEL_2, FORS_BERGRUNN_2);
        MAP_TILSTAND_STEG.put(FORESLÅTT_DEL_2_UT, FORS_BERGRUNN_2);
        MAP_TILSTAND_STEG.put(VURDERT_VILKÅR, VURDER_VILKAR_BERGRUNN);
        MAP_TILSTAND_STEG.put(VURDERT_TILKOMMET_INNTEKT, VURDER_TILKOMMET_INNTEKT);
        MAP_TILSTAND_STEG.put(VURDERT_TILKOMMET_INNTEKT_UT, VURDER_TILKOMMET_INNTEKT);
        MAP_TILSTAND_STEG.put(VURDERT_REFUSJON, VURDER_REF_BERGRUNN);
        MAP_TILSTAND_STEG.put(VURDERT_REFUSJON_UT, VURDER_REF_BERGRUNN);
        MAP_TILSTAND_STEG.put(OPPDATERT_MED_REFUSJON_OG_GRADERING, FORDEL_BERGRUNN);
        MAP_TILSTAND_STEG.put(FASTSATT_INN, FORDEL_BERGRUNN);
        MAP_TILSTAND_STEG.put(FASTSATT, FAST_BERGRUNN);
    }

    private MapTilstandTilSteg() {
        // Skjuler default konstruktør
    }

    public static BeregningSteg mapTilSteg(BeregningsgrunnlagTilstand kode) {
        if (MAP_TILSTAND_STEG.containsKey(kode)) {
            return MAP_TILSTAND_STEG.get(kode);
        }
        throw new IllegalStateException("Finner ikke steg for tilstand " + kode.getKode());
    }

}
