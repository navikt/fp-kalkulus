package no.nav.folketrygdloven.kalkulus.web.app.exceptions;

import java.io.Serializable;

public record FeltFeilDto(String navn, String melding) implements Serializable {
}
