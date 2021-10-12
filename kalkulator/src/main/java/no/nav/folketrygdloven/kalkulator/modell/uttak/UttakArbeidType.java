package no.nav.folketrygdloven.kalkulator.modell.uttak;

import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
@JsonAutoDetect(getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE, fieldVisibility = Visibility.ANY)
public enum UttakArbeidType {

    ORDINÆRT_ARBEID("ORDINÆRT_ARBEID", "Ordinært arbeid", "AT"),
    SELVSTENDIG_NÆRINGSDRIVENDE("SELVSTENDIG_NÆRINGSDRIVENDE", "Selvstendig næringsdrivende", "SN"),
    FRILANS("FRILANS", "Frilans", "FL"),
    MIDL_INAKTIV("MIDL_INAKTIV", "Inaktiv", "MIDL_INAKTIV"),
    DAGPENGER("DAGPENGER", "Dagpenger", "DP"),
    SYKEPENGER_AV_DAGPENGER("SP_AV_DP", "Sykepenger av dagpenger", "SP_AV_DP"),
    BRUKERS_ANDEL("BA", "Brukers andel", "BA"), // Brukes når søker kun søker uttak for ytelse (PSB)
    IKKE_YRKESAKTIV("IKKE_YRKESAKTIV", "Ikke yrkesaktiv", "IKKE_YRKESAKTIV"),
    ANNET("ANNET", "Annet"),
    ;
    public static final String KODEVERK = "UTTAK_ARBEID_TYPE";
    private static final Map<String, UttakArbeidType> KODER = new LinkedHashMap<>();
    private static final Map<String, UttakArbeidType> KODER_K9 = new LinkedHashMap<>();

    static {
        for (var v : values()) {
            if (KODER.putIfAbsent(v.kode, v) != null) {
                throw new IllegalArgumentException("Duplikat : " + v.kode);
            }
            if (KODER_K9.putIfAbsent(v.k9Kode, v) != null) {
                throw new IllegalArgumentException("Duplikat : " + v.k9Kode);
            }
        }
    }

    @JsonIgnore
    private String navn;
    private String k9Kode;
    private String kode;

    UttakArbeidType(String kode, String navn) {
        this.kode = kode;
        this.navn = navn;
    }

    UttakArbeidType(String kode, String navn, String k9Kode) {
        this.kode = kode;
        this.navn = navn;
        this.k9Kode = k9Kode;
    }

    @JsonCreator
    public static UttakArbeidType fraKode(@JsonProperty("kode") String kode) {
        if (kode == null) {
            return null;
        }
        var ad = KODER.get(kode);
        if (ad == null) {
            ad = KODER_K9.get(kode);
            if (ad == null) {
                throw new IllegalArgumentException("Ukjent UttakArbeidType: " + kode);
            }
        }
        return ad;
    }

    @JsonProperty
    public String getKode() {
        return kode;
    }

    @JsonProperty
    public String getKodeverk() {
        return KODEVERK;
    }

    public String getOffisiellKode() {
        return this.getKode();
    }

    public String getNavn() {
        return navn;
    }
}
