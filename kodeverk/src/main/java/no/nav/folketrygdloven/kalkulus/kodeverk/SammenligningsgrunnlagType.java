package no.nav.folketrygdloven.kalkulus.kodeverk;

public enum SammenligningsgrunnlagType implements Kodeverdi, DatabaseKode, KontraktKode {

    SAMMENLIGNING_AT,
    SAMMENLIGNING_FL,
    SAMMENLIGNING_AT_FL,
    SAMMENLIGNING_SN,
    SAMMENLIGNING_ATFL_SN,
    SAMMENLIGNING_MIDL_INAKTIV,
    ;

    public static SammenligningsgrunnlagType fraKode(String kode) {
        return kode == null ? null : SammenligningsgrunnlagType.valueOf(kode);
    }

    @Override
    public String getKode() {
        return name();
    }

    public static SammenligningsgrunnlagType fraDatabaseKode(String databaseKode) {
        return fraKode(databaseKode);
    }

}
