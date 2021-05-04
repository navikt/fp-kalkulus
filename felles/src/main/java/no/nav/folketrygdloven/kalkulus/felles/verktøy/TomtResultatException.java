package no.nav.folketrygdloven.kalkulus.felles.verktøy;

import no.nav.vedtak.exception.TekniskException;

public class TomtResultatException extends TekniskException {
    public TomtResultatException(String kode, String msg) {
        super(kode, msg);
    }
}
