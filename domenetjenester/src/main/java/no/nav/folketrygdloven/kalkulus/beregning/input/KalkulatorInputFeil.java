package no.nav.folketrygdloven.kalkulus.beregning.input;

import java.util.List;

import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;

public interface KalkulatorInputFeil extends DeklarerteFeil {

    @TekniskFeil(feilkode = "FT-KALKULUS-INPUT-1000000", feilmelding = "Kalkulus finner ikke kalkulator input for koblingId: %s", logLevel = LogLevel.ERROR)
    Feil kalkulusFinnerIkkeKalkulatorInput(Long koblingId);

    @TekniskFeil(feilkode = "FT-KALKULUS-INPUT-1000000", feilmelding = "Kalkulus finner ikke kalkulator input for koblingId: %s", logLevel = LogLevel.ERROR)
    Feil kalkulusFinnerIkkeKalkulatorInput(List<Long> koblingId);

    @TekniskFeil(feilkode = "FT-KALKULUS-INPUT-1000002", feilmelding = "Kalkulus klarte ikke lese opp input for koblingId %s med følgende feilmelding %s", logLevel = LogLevel.ERROR)
    Feil kalkulusKlarteIkkeLeseOppInput(Long koblingId, String message);

    @TekniskFeil(feilkode = "FT-KALKULUS-INPUT-1000002", feilmelding = "Kalkulus klarte ikke lagre ned input for koblingId: %s med følgende feilmelding %s", logLevel = LogLevel.ERROR)
    Feil kalkulusKlarteIkkeLagreNedInput(Long koblingId, String message);

    @TekniskFeil(feilkode = "FT-KALKULUS-INPUT-1000003", feilmelding = "Kalkulus finner ikke kobling: %s", logLevel = LogLevel.ERROR)
    Feil kalkulusFinnerIkkeKobling(Long koblingId);
}
