package no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.kodeverk;


import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.kodeverk.PeriodeÅrsak;


public class MapPeriodeÅrsakFraVlTilRegel {

    private static final Map<PeriodeÅrsak, no.nav.folketrygdloven.kalkulator.regelmodell.PeriodeÅrsak> PERIODE_ÅRSAK_MAP;

    static {
        Map<PeriodeÅrsak, no.nav.folketrygdloven.kalkulator.regelmodell.PeriodeÅrsak> mapPeriodeÅrsak = new LinkedHashMap<>();

        mapPeriodeÅrsak.put(PeriodeÅrsak.NATURALYTELSE_BORTFALT, no.nav.folketrygdloven.kalkulator.regelmodell.PeriodeÅrsak.NATURALYTELSE_BORTFALT);
        mapPeriodeÅrsak.put(PeriodeÅrsak.ARBEIDSFORHOLD_AVSLUTTET, no.nav.folketrygdloven.kalkulator.regelmodell.PeriodeÅrsak.ARBEIDSFORHOLD_AVSLUTTET);
        mapPeriodeÅrsak.put(PeriodeÅrsak.NATURALYTELSE_TILKOMMER, no.nav.folketrygdloven.kalkulator.regelmodell.PeriodeÅrsak.NATURALYTELSE_TILKOMMER);
        mapPeriodeÅrsak.put(PeriodeÅrsak.ENDRING_I_REFUSJONSKRAV, no.nav.folketrygdloven.kalkulator.regelmodell.PeriodeÅrsak.ENDRING_I_REFUSJONSKRAV);
        mapPeriodeÅrsak.put(PeriodeÅrsak.REFUSJON_OPPHØRER, no.nav.folketrygdloven.kalkulator.regelmodell.PeriodeÅrsak.REFUSJON_OPPHØRER);
        mapPeriodeÅrsak.put(PeriodeÅrsak.GRADERING, no.nav.folketrygdloven.kalkulator.regelmodell.PeriodeÅrsak.GRADERING);
        mapPeriodeÅrsak.put(PeriodeÅrsak.GRADERING_OPPHØRER, no.nav.folketrygdloven.kalkulator.regelmodell.PeriodeÅrsak.GRADERING_OPPHØRER);
        mapPeriodeÅrsak.put(PeriodeÅrsak.ENDRING_I_AKTIVITETER_SØKT_FOR, no.nav.folketrygdloven.kalkulator.regelmodell.PeriodeÅrsak.ENDRING_I_AKTIVITETER_SØKT_FOR);

        PERIODE_ÅRSAK_MAP = Collections.unmodifiableMap(mapPeriodeÅrsak);

    }

    private MapPeriodeÅrsakFraVlTilRegel() {
        // skjul public constructor
    }

    public static no.nav.folketrygdloven.kalkulator.regelmodell.PeriodeÅrsak map(PeriodeÅrsak periodeÅrsak) {
        return PERIODE_ÅRSAK_MAP.get(periodeÅrsak);
    }

}
