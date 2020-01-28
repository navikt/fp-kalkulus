package no.nav.folketrygdloven.kalkulator.modell.kodeverk;

/** Kodeverk som er portet til java. */
public interface Kodeverdi extends BasisKodeverdi {

    @Override
    String getKode();

    @Override
    String getKodeverk();

}
