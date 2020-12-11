package no.nav.folketrygdloven.kalkulus.kodeverk;

/** Kodeverk som er portet til java. */
public interface Kodeverdi extends BasisKodeverdi {

    @Override
    String getKode();
    
    /** skal fjernes straks klienter oppdatert til nytt kodeverk modul.*/
    @Deprecated(forRemoval = true, since = "2020-12-11")
    String getKodeverk();

}
