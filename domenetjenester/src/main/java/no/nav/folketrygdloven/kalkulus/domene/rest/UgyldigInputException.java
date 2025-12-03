package no.nav.folketrygdloven.kalkulus.domene.rest;

import no.nav.folketrygdloven.kalkulator.KalkulatorException;

public class UgyldigInputException extends KalkulatorException {

    public UgyldigInputException(String kode, String melding) {
        super(kode, melding);
    }

}
