package no.nav.folketrygdloven.kalkulus.rest.forvaltning.dump;

import java.util.List;

import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.Saksnummer;

public interface DebugDumpSak {

    List<DumpOutput> dump(Saksnummer saksnummer);
}
