package no.nav.folketrygdloven.kalkulus.kodeverk;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonCreator.Mode;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;


@JsonFormat(shape = Shape.OBJECT)
@JsonAutoDetect(getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE, fieldVisibility = Visibility.ANY)
public enum InntektskildeType implements Kodeverdi {

    UDEFINERT("-", "Ikke definert", null),
    INNTEKT_OPPTJENING("INNTEKT_OPPTJENING", "INNTEKT_OPPTJENING", "INNTEKT"),
    INNTEKT_BEREGNING("INNTEKT_BEREGNING", "INNTEKT_BEREGNING", null),
    INNTEKT_SAMMENLIGNING("INNTEKT_SAMMENLIGNING", "INNTEKT_SAMMENLIGNING", null),
    SIGRUN("SIGRUN", "Sigrun", "SIGRUN"),
    VANLIG("VANLIG", "Vanlig", "VANLIG"),
    ;

    private static final Map<String, InntektskildeType> KODER = new LinkedHashMap<>();

    public static final String KODEVERK = "INNTEKTS_KILDE";

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
    
    @JsonIgnore
    private String offisiellKode;

    private InntektskildeType(String kode) {
        this.kode = kode;
    }

    private InntektskildeType(String kode, String navn, String offisiellKode) {
        this.kode = kode;
        this.navn = navn;
        this.offisiellKode = offisiellKode;
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

    public static Map<String, InntektskildeType> kodeMap() {
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
