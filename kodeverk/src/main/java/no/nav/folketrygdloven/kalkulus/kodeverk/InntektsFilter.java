package no.nav.folketrygdloven.kalkulus.kodeverk;

import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonCreator.Mode;
import com.fasterxml.jackson.annotation.JsonValue;

@JsonAutoDetect(getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE, fieldVisibility = Visibility.ANY)
public enum InntektsFilter implements Kodeverdi {

    BEREGNINGSGRUNNLAG("BEREGNINGSGRUNNLAG"),
    OPPTJENINGSGRUNNLAG("OPPTJENINGSGRUNNLAG"),
    SAMMENLIGNINGSGRUNNLAG("SAMMENLIGNINGSGRUNNLAG"),
    UDEFINERT("-"),
    ;

    private static final Map<String, InntektsFilter> KODER = new LinkedHashMap<>();

    static {
        for (var v : values()) {
            if (KODER.putIfAbsent(v.kode, v) != null) {
                throw new IllegalArgumentException("Duplikat : " + v.kode);
            }
        }
    }

    @JsonValue
    private final String kode;


    InntektsFilter(String kode) {
        this.kode = kode;
    }

    @JsonCreator(mode = Mode.DELEGATING)
    public static InntektsFilter fraKode(Object node) {
        if (node == null) {
            return null;
        }
        String kode = TempAvledeKode.getVerdi(InntektsFilter.class, node, "kode");
        var ad = KODER.get(kode);
        if (ad == null) {
            throw new IllegalArgumentException("Ukjent InntektsFilter: " + kode);
        }
        return ad;
    }

    @Override
    public String getKode() {
        return kode;
    }

}
