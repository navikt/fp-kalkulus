package no.nav.foreldrepenger.kalkulus.v1;

import java.util.UUID;

public interface KalkulusRequest {

    /** Angitt saksnummer for sporing */
    Saksnummer getSaksnummer();

    /** BehandingUuid */
    UUID getBehandlingUuid();

}
