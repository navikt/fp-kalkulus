package no.nav.folketrygdloven.kalkulus.domene.entiteter.mapping;

import java.util.function.Function;

import no.nav.folketrygdloven.kalkulus.kodeverk.DatabaseKode;

public class KodeKonstanter {

    private KodeKonstanter() {
    }

    // Standard verdi når man har kolonner som ikke er nullable eller vil skulle null / undefined
    public static final String UDEFINERT = "-";

    public static <T extends DatabaseKode> String tilDatabasekode(T enumInst, T udefinert) {
        if (enumInst == null) {
            return null;
        }
        if (udefinert.equals(enumInst)) {
            return UDEFINERT;
        }
        return enumInst.getDatabaseKode();
    }

    // De som har UDEFINERT (med konvensjon "-")
    public static <T extends DatabaseKode> T fraDatabasekode(String databasekode, T udefinert, Function<String, T> factory) {
        if (databasekode == null) {
            return null;
        }
        if (UDEFINERT.equals(databasekode)) {
            return udefinert;
        }
        return factory.apply(databasekode);
    }

    // De som ikke har UDEFINERT
    public static <T extends DatabaseKode> T fraDatabasekode(String databasekode, Function<String, T> factory) {
        if (databasekode == null) {
            return null;
        }
        return factory.apply(databasekode);
    }

}
