package no.nav.folketrygdloven.kalkulus.beregning.input;

import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;

public interface KalkulatorInputFeil extends DeklarerteFeil {

    @TekniskFeil(feilkode = "FT-KALKULUS-INPUT-1000000", feilmelding = "Kalkulus finner ikke kalkulator input for koblingId: %s", logLevel = LogLevel.ERROR)
    Feil kalkulusFinnerIkkeKalkulatorInput(Long koblingId);

    @TekniskFeil(feilkode = "FT-KALKULUS-INPUT-1000001", feilmelding = "Kalkulus har ikke beregningsgrunnlag for koblingId: %s", logLevel = LogLevel.ERROR)
    Feil kalkulusHarIkkeBeregningsgrunnlag(Long koblingId);

    @TekniskFeil(feilkode = "FT-KALKULUS-INPUT-1000002", feilmelding = "Kalkulus klarte ikke lagre ned input for koblingId: %s", logLevel = LogLevel.ERROR)
    Feil kalkulusKlarteIkkeLagreNedInput(Long koblingId);

    @TekniskFeil(feilkode = "FT-KALKULUS-INPUT-1000003", feilmelding = "Kalkulus finner ikke kobling: %s", logLevel = LogLevel.ERROR)
    Feil kalkulusFinnerIkkeKobling(Long koblingId);
}
