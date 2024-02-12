package no.nav.folketrygdloven.kalkulus.kodeverk;

public enum Utfall implements Kodeverdi, DatabaseKode {

    GODKJENT,
    UNDERKJENT,
    UDEFINERT,
    ;

    public static Utfall fraKode(String kode) {
        if (kode == null) {
            return null;
        }
        if (KodeKonstanter.UDEFINERT.equals(kode)) {
            return UDEFINERT;
        }
        return Utfall.valueOf(kode);
    }

    @Override
    public String getKode() {
        return this == UDEFINERT ? KodeKonstanter.UDEFINERT : name();
    }

    @Override
    public String getDatabaseKode() {
        return this == UDEFINERT ? KodeKonstanter.UDEFINERT : name();
    }


    public static Utfall fraDatabaseKode(String databaseKode) {
        return fraKode(databaseKode);
    }

}
