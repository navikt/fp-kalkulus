package no.nav.folketrygdloven.kalkulus.kodeverk;

public enum FaktaVurderingKilde implements Kodeverdi, DatabaseKode {

    SAKSBEHANDLER,
    KALKULATOR,
    UDEFINERT,
    ;

    public static FaktaVurderingKilde fraKode(String kode) {
        if (kode == null) {
            return null;
        }
        if (KodeKonstanter.UDEFINERT.equals(kode)) {
            return UDEFINERT;
        }
        return FaktaVurderingKilde.valueOf(kode);
    }

    @Override
    public String getKode() {
        return this == UDEFINERT ? KodeKonstanter.UDEFINERT : name();
    }

    public static FaktaVurderingKilde fraDatabaseKode(String databaseKode) {
        return fraKode(databaseKode);
    }

}
