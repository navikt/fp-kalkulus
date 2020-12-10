package no.nav.folketrygdloven.kalkulus.felles.kodeverk;


import no.nav.folketrygdloven.kalkulus.felles.diff.IndexKey;

public interface BasisKodeverdi extends IndexKey {
    String getKode();

    @Override
    default String getIndexKey() {
        return getKode();
    }
}
