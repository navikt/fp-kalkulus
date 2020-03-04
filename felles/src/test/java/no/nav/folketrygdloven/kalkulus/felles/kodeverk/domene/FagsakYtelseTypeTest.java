package no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class FagsakYtelseTypeTest {

    @Test
    void skal_gi_kode_når_fpsakkode_blir_brukt() {
        String fpsakKode = "DAGPENGER";
        String nyKode = "DAG";

        FagsakYtelseType fagsakYtelseType = FagsakYtelseType.fraKode(fpsakKode);
        FagsakYtelseType fagsakYtelseType2 = FagsakYtelseType.fraKode(nyKode);

        Assertions.assertSame(fagsakYtelseType, FagsakYtelseType.DAGPENGER);
        Assertions.assertSame(fagsakYtelseType2, FagsakYtelseType.DAGPENGER);
    }
}
