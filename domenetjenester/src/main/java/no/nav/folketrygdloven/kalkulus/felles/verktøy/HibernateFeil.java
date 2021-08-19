package no.nav.folketrygdloven.kalkulus.felles.verktøy;

import no.nav.folketrygdloven.kalkulus.felles.feil.TekniskException;

public class HibernateFeil {

    static TekniskException ikkeUniktResultat(String spørring) {
        return new TekniskException("FT-KALKULUS-DB-1000000", String.format("Spørringen %s returnerte ikke et unikt resultat", spørring));
    }

    static TekniskException merEnnEttResultat(String spørring) {
        return new TekniskException("FT-KALKULUS-DB-1000001", String.format("Spørringen %s returnerte mer enn eksakt ett resultat", spørring));
    }

    static TekniskException tomtResultat(String spørring) {
        return new TomtResultatException("FT-KALKULUS-DB-1000002", String.format("Spørringen %s returnerte tomt resultat", spørring));
    }
}
