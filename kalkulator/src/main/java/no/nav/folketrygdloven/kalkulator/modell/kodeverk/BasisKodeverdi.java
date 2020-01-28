package no.nav.folketrygdloven.kalkulator.modell.kodeverk;

import no.nav.folketrygdloven.kalkulator.modell.diff.IndexKey;

public interface BasisKodeverdi extends IndexKey {
    String getKode();

    String getOffisiellKode();

    String getKodeverk();

    String getNavn();

    @Override
    default String getIndexKey() {
        return getKode();
    }
}
