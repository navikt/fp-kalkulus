package no.nav.folketrygdloven.kalkulus.kodeverk;

import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonCreator.Mode;
import com.fasterxml.jackson.annotation.JsonValue;


@JsonAutoDetect(getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE, fieldVisibility = Visibility.ANY)
public enum InntektAktivitetType implements Kodeverdi, KontraktKode {

    ARBEIDSTAKERINNTEKT("ARBEIDSTAKERINNTEKT"),
    FRILANSINNTEKT("FRILANSINNTEKT"),
    YTELSEINNTEKT("YTELSEINNTEKT"),
    UDEFINERT(KodeKonstanter.UDEFINERT);

    private static final Map<String, InntektAktivitetType> KODER = new LinkedHashMap<>();

    static {
        for (var v : values()) {
            if (KODER.putIfAbsent(v.kode, v) != null) {
                throw new IllegalArgumentException("Duplikat : " + v.kode);
            }
        }
    }

    @JsonValue
    private final String kode;

    InntektAktivitetType(String kode) {
        this.kode = kode;
    }

    @JsonCreator(mode = Mode.DELEGATING)
    public static InntektAktivitetType fraKode(Object node) {
        if (node == null) {
            return null;
        }
        String kode = TempAvledeKode.getVerdi(InntektAktivitetType.class, node, "kode");
        var ad = KODER.get(kode);
        if (ad == null) {
            throw new IllegalArgumentException("Ukjent InntektAktivitetType: " + kode);
        }
        return ad;
    }

    @Override
    public String getKode() {
        return kode;
    }

}
