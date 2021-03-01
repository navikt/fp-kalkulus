package no.nav.folketrygdloven.kalkulus.kodeverk;

import java.util.Collections;
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
public enum InntektAktivitetType implements Kodeverdi {

    ARBEIDSTAKERINNTEKT("ARBEIDSTAKERINNTEKT", "Inntekt tjent som arbeidstaker"),
    FRILANSINNTEKT("FRILANSINNTEKT", "Inntekt tjent som frilans"),
    YTELSEINNTEKT("YTELSEINNTEKT", "Inntekt fra ytelser"),
    UDEFINERT("-", "Udefinert");

    private static final Map<String, InntektAktivitetType> KODER = new LinkedHashMap<>();

    public static final String KODEVERK = "INNTEKT_AKTIVITET_TYPE";

    static {
        for (var v : values()) {
            if (KODER.putIfAbsent(v.kode, v) != null) {
                throw new IllegalArgumentException("Duplikat : " + v.kode);
            }
        }
    }

    @JsonIgnore
    private String navn;

    private String kode;

    InntektAktivitetType(String kode, String navn) {
        this.kode = kode;
        this.navn = navn;
    }

    @JsonCreator
    public static InntektAktivitetType fraKode(@JsonProperty("kode") String kode) {
        if (kode == null) {
            return null;
        }
        var ad = KODER.get(kode);
        if (ad == null) {
            throw new IllegalArgumentException("Ukjent InntektAktivitetType: " + kode);
        }
        return ad;
    }

    public static Map<String, InntektAktivitetType> kodeMap() {
        return Collections.unmodifiableMap(KODER);
    }

    @JsonProperty
    @Override
    public String getKode() {
        return kode;
    }

    @JsonProperty
    @Override
    public String getKodeverk() {
        return KODEVERK;
    }
}
