package no.nav.folketrygdloven.kalkulus.beregning;

import static no.nav.folketrygdloven.kalkulus.kodeverk.BeregningSteg.FASTSETT_STP_BER;
import static no.nav.folketrygdloven.kalkulus.kodeverk.BeregningSteg.FAST_BERGRUNN;
import static no.nav.folketrygdloven.kalkulus.kodeverk.BeregningSteg.FORDEL_BERGRUNN;
import static no.nav.folketrygdloven.kalkulus.kodeverk.BeregningSteg.FORS_BERGRUNN;
import static no.nav.folketrygdloven.kalkulus.kodeverk.BeregningSteg.FORS_BESTEBEREGNING;
import static no.nav.folketrygdloven.kalkulus.kodeverk.BeregningSteg.KOFAKBER;
import static no.nav.folketrygdloven.kalkulus.kodeverk.BeregningSteg.VURDER_REF_BERGRUNN;
import static no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand.BESTEBEREGNET;
import static no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand.FASTSATT;
import static no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand.FASTSATT_BEREGNINGSAKTIVITETER;
import static no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand.FASTSATT_INN;
import static no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand.FORESLÅTT;
import static no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand.FORESLÅTT_UT;
import static no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand.KOFAKBER_UT;
import static no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand.OPPDATERT_MED_ANDELER;
import static no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand.OPPDATERT_MED_REFUSJON_OG_GRADERING;
import static no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand.OPPRETTET;
import static no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand.VURDERT_REFUSJON;
import static no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand.VURDERT_REFUSJON_UT;

import java.util.Map;
import java.util.Optional;

import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningSteg;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand;

public class MapStegTilTilstand {

    private static final Map<BeregningSteg, BeregningsgrunnlagTilstand> MAP_STEG_TILSTAND = Map.of(
            FASTSETT_STP_BER, OPPRETTET,
            KOFAKBER, OPPDATERT_MED_ANDELER,
            FORS_BESTEBEREGNING, BESTEBEREGNET,
            FORS_BERGRUNN, FORESLÅTT,
            VURDER_REF_BERGRUNN, VURDERT_REFUSJON,
            FORDEL_BERGRUNN, OPPDATERT_MED_REFUSJON_OG_GRADERING,
            FAST_BERGRUNN, FASTSATT);

    private static final Map<BeregningSteg, BeregningsgrunnlagTilstand> MAP_STEG_UT_TILSTAND = Map.of(
            FASTSETT_STP_BER, FASTSATT_BEREGNINGSAKTIVITETER,
            KOFAKBER, KOFAKBER_UT,
            FORS_BERGRUNN, FORESLÅTT_UT,
            VURDER_REF_BERGRUNN, VURDERT_REFUSJON_UT,
            FORDEL_BERGRUNN, FASTSATT_INN);


    public static BeregningsgrunnlagTilstand mapTilStegTilstand(BeregningSteg kode) {
        if (MAP_STEG_TILSTAND.containsKey(kode)) {
            return MAP_STEG_TILSTAND.get(kode);
        }
        throw new IllegalStateException("Finner ikke tilstand for steg " + kode.getKode());
    }

    public static Optional<BeregningsgrunnlagTilstand> mapTilStegUtTilstand(BeregningSteg kode) {
        return Optional.ofNullable(MAP_STEG_UT_TILSTAND.get(kode));
    }



}
