package no.nav.folketrygdloven.kalkulus.felles.verktøy;

import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.feil.Feil;

public class TomtResultatException extends TekniskException {
    public TomtResultatException(Feil feil) {
        super(feil);
    }
}
