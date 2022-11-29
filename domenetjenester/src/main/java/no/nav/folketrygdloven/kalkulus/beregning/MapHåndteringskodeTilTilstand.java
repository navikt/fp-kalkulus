package no.nav.folketrygdloven.kalkulus.beregning;

import java.util.HashMap;
import java.util.Map;

import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulus.kodeverk.HåndteringKode;

public class MapHåndteringskodeTilTilstand {

    private static Map<HåndteringKode, BeregningsgrunnlagTilstand> map = new HashMap<>();

    static {
        map.put(HåndteringKode.AVKLAR_AKTIVITETER, BeregningsgrunnlagTilstand.FASTSATT_BEREGNINGSAKTIVITETER);
        map.put(HåndteringKode.OVERSTYRING_AV_BEREGNINGSAKTIVITETER_KODE, BeregningsgrunnlagTilstand.FASTSATT_BEREGNINGSAKTIVITETER);
        map.put(HåndteringKode.FAKTA_OM_BEREGNING, BeregningsgrunnlagTilstand.KOFAKBER_UT);
        map.put(HåndteringKode.OVERSTYRING_AV_BEREGNINGSGRUNNLAG_KODE, BeregningsgrunnlagTilstand.KOFAKBER_UT);
        map.put(HåndteringKode.FASTSETT_BEREGNINGSGRUNNLAG_ARBEIDSTAKER_FRILANS_KODE, BeregningsgrunnlagTilstand.FORESLÅTT_UT);
        map.put(HåndteringKode.FASTSETT_BEREGNINGSGRUNNLAG_SELVSTENDIG_NÆRINGSDRIVENDE_KODE, BeregningsgrunnlagTilstand.FORESLÅTT_2_UT);
        map.put(HåndteringKode.FASTSETT_BEREGNINGSGRUNNLAG_FOR_SN_NY_I_ARBEIDSLIVET_KODE, BeregningsgrunnlagTilstand.FORESLÅTT_2_UT);
        map.put(HåndteringKode.FASTSETT_BEREGNINGSGRUNNLAG_TIDSBEGRENSET_ARBEIDSFORHOLD_KODE, BeregningsgrunnlagTilstand.FORESLÅTT_UT);
        map.put(HåndteringKode.VURDER_VARIG_ENDRET_ELLER_NYOPPSTARTET_NÆRING_SELVSTENDIG_NÆRINGSDRIVENDE_KODE, BeregningsgrunnlagTilstand.FORESLÅTT_2_UT);
        map.put(HåndteringKode.VURDER_VARIG_ENDRET_ARBEIDSSITUASJON_KODE, BeregningsgrunnlagTilstand.FORESLÅTT_2_UT);
        map.put(HåndteringKode.VURDER_REFUSJON_BEREGNINGSGRUNNLAG, BeregningsgrunnlagTilstand.VURDERT_REFUSJON_UT);
        map.put(HåndteringKode.FAKTA_OM_FORDELING, BeregningsgrunnlagTilstand.FASTSATT_INN);
        map.put(HåndteringKode.VURDER_NYTT_INNTEKTSFORHOLD, BeregningsgrunnlagTilstand.FASTSATT_INN);

    }

    public static BeregningsgrunnlagTilstand map(HåndteringKode kode) {
        if (map.containsKey(kode)) {
            return map.get(kode);
        }
        throw new IllegalStateException("Finner ikke tilstand for kode " + kode.getKode());
    }

}
