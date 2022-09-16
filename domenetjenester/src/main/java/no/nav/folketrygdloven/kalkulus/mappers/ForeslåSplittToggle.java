package no.nav.folketrygdloven.kalkulus.mappers;

import no.nav.folketrygdloven.kalkulator.KonfigurasjonVerdi;

public class ForeslåSplittToggle {

    public static boolean erTogglePå() {
        return KonfigurasjonVerdi.get("SPLITT_FORESLÅ_TOGGLE", false);
    }
}
