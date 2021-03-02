package no.nav.folketrygdloven.kalkulator.ytelse.fp;

import javax.enterprise.context.ApplicationScoped;

import no.nav.folketrygdloven.kalkulator.FagsakYtelseTypeRef;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.periodisering.refusjon.MapRefusjonPerioderFraVLTilRegel;

@FagsakYtelseTypeRef("FP")
@ApplicationScoped
public class MapRefusjonPerioderFraVLTilRegelFP extends MapRefusjonPerioderFraVLTilRegel {

    // For CDI (for håndtere at annotation propageres til subklasser)

}
