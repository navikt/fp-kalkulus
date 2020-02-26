package no.nav.folketrygdloven.kalkulus.felles.verktøy;

import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;

public interface HibernateFeil extends DeklarerteFeil {
    @TekniskFeil(feilkode = "FT-KALKULUS-DB-1000000", feilmelding = "Spørringen %s returnerte ikke et unikt resultat", logLevel = LogLevel.WARN)
    Feil ikkeUniktResultat(String spørring);

    @TekniskFeil(feilkode = "FT-KALKULUS-DB-1000001", feilmelding = "Spørringen %s returnerte mer enn eksakt ett resultat", logLevel = LogLevel.WARN)
    Feil merEnnEttResultat(String spørring);

    @TekniskFeil(feilkode = "FT-KALKULUS-DB-1000002", feilmelding = "Spørringen %s returnerte tomt resultat", logLevel = LogLevel.WARN, exceptionClass = TomtResultatException.class)
    Feil tomtResultat(String spørring);
}
