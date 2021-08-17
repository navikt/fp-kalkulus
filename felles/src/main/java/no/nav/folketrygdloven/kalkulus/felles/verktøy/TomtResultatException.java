package no.nav.folketrygdloven.kalkulus.felles.verktøy;

import no.nav.folketrygdloven.kalkulus.felles.feil.TekniskException;

public class TomtResultatException extends TekniskException {
    public TomtResultatException(String kode, String msg) {
        super(kode, msg);
    }
}
