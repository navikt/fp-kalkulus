package no.nav.folketrygdloven.kalkulus.kodeverk;

import java.util.LinkedHashMap;
import java.util.Map;


public enum BeregningSatsType implements Kodeverdi, DatabaseKode {
    ENGANG,
    GRUNNBELØP,
    GSNITT,
    UDEFINERT,
    ;

    public static BeregningSatsType fraKode(String kode) {
        if (kode == null) {
            return null;
        }
        if (KodeKonstanter.UDEFINERT.equals(kode)) {
            return UDEFINERT;
        }
        return BeregningSatsType.valueOf(kode);
    }

    @Override
    public String getKode() {
        return this == UDEFINERT ? KodeKonstanter.UDEFINERT : name();
    }

    public static BeregningSatsType fraDatabaseKode(String databaseKode) {
        return fraKode(databaseKode);
    }

}
