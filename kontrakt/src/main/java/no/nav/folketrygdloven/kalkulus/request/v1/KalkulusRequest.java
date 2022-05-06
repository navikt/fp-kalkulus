package no.nav.folketrygdloven.kalkulus.request.v1;

import java.util.UUID;

public interface KalkulusRequest {

    /** Angitt saksnummer for sporing */
    String getSaksnummer();

    /** BehandingUuid */
    UUID getBehandlingUuid();

}
