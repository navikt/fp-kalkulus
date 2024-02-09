package no.nav.folketrygdloven.kalkulus.kodeverk;

import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonCreator.Mode;
import com.fasterxml.jackson.annotation.JsonValue;


@JsonAutoDetect(getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE, fieldVisibility = Visibility.ANY)
public enum InntektskildeType implements Kodeverdi, KontraktKode {

    UDEFINERT(KodeKonstanter.UDEFINERT),
    INNTEKT_OPPTJENING("INNTEKT_OPPTJENING"),
    INNTEKT_BEREGNING("INNTEKT_BEREGNING"),
    INNTEKT_SAMMENLIGNING("INNTEKT_SAMMENLIGNING"),
    SIGRUN("SIGRUN"),
    VANLIG("VANLIG"),
    ;

    private static final Map<String, InntektskildeType> KODER = new LinkedHashMap<>();

    static {
        for (var v : values()) {
            if (KODER.putIfAbsent(v.kode, v) != null) {
                throw new IllegalArgumentException("Duplikat : " + v.kode);
            }
        }
    }

    @JsonValue
    private final String kode;

    InntektskildeType(String kode) {
        this.kode = kode;
    }

    @JsonCreator(mode = Mode.DELEGATING)
    public static InntektskildeType fraKode(Object node) {
        if (node == null) {
            return null;
        }
        String kode = TempAvledeKode.getVerdi(InntektskildeType.class, node, "kode");
        var ad = KODER.get(kode);
        if (ad == null) {
            throw new IllegalArgumentException("Ukjent InntektsKilde: " + kode);
        }
        return ad;
    }

    @Override
    public String getKode() {
        return kode;
    }

}
