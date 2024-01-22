package no.nav.folketrygdloven.kalkulus.kodeverk;

import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

@JsonAutoDetect(getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE, fieldVisibility = Visibility.ANY)
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
    private static final Map<String, UttakArbeidType> KODER = new LinkedHashMap<>();

    static {
        for (var v : values()) {
            if (KODER.putIfAbsent(v.kode, v) != null) {
                throw new IllegalArgumentException("Duplikat : " + v.kode);
            }
        }
    }

    @JsonValue
    private String kode;

    UttakArbeidType(String kode) {
        this.kode = kode;
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static UttakArbeidType fraKode(Object node) {
        if (node == null) {
            return null;
        }
        String kode = TempAvledeKode.getVerdi(UttakArbeidType.class, node, "kode");
        var ad = KODER.get(kode);
        if (ad == null) {
            throw new IllegalArgumentException("Ukjent UttakArbeidType: " + kode);
        }
        return ad;
    }

    @Override
    public String getKode() {
        return kode;
    }
}
