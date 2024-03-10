package no.nav.folketrygdloven.kalkulus.kodeverk;

import java.util.Arrays;

import com.fasterxml.jackson.annotation.JsonValue;

public enum UttakArbeidType implements Kodeverdi, KontraktKode {

    ORDINÆRT_ARBEID("AT"),
    SELVSTENDIG_NÆRINGSDRIVENDE("SN"),
    FRILANS("FL"),
    MIDL_INAKTIV("MIDL_INAKTIV"),
    DAGPENGER("DP"),
    SYKEPENGER_AV_DAGPENGER("SP_AV_DP"),
    PLEIEPENGER_AV_DAGPENGER("PSB_AV_DP"),
    BRUKERS_ANDEL("BA"), // Brukes når søker kun søker uttak for ytelse (PSB)
    IKKE_YRKESAKTIV("IKKE_YRKESAKTIV"),
    ANNET("ANNET"),
    ;

    @JsonValue
    private String kode;

    UttakArbeidType(String kode) {
        this.kode = kode;
    }

    @Override
    public String getKode() {
        return kode;
    }


    // Service til k9-sak som mapper fra en Kotlin-string ...
    public static UttakArbeidType fraKode(String kode) {
        if (kode == null) {
            return null;
        }
        var ad = Arrays.stream(values()).filter(v -> v.kode.equals(kode)).findFirst().orElse(null);
        if (ad == null) {
            throw new IllegalArgumentException("Ukjent UttakArbeidType: " + kode);
        }
        return ad;
    }

}
