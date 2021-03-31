package no.nav.folketrygdloven.kalkulator.ytelse.svp;

import javax.enterprise.context.ApplicationScoped;

import no.nav.folketrygdloven.kalkulator.FagsakYtelseTypeRef;
import no.nav.folketrygdloven.kalkulator.ytelse.utbgradytelse.MapRefusjonPerioderFraVLTilRegelUtbgrad;

@FagsakYtelseTypeRef("SVP")
@ApplicationScoped
public class MapRefusjonPerioderFraVLTilRegelSVP extends MapRefusjonPerioderFraVLTilRegelUtbgrad {

    // Fordi annotasjoner propageres til subklasser
    // Burde vurdere å gjere om til interface

}
