package no.nav.folketrygdloven.kalkulus.mappers;

import no.nav.folketrygdloven.kalkulator.KonfigurasjonVerdi;

public class ForeslåSplittToggle {

    public static boolean erTogglePå() {
        return KonfigurasjonVerdi.get("SPLITT_FORESLA_TOGGLE", false);
    }
}
