package no.nav.folketrygdloven.kalkulus.kodeverk;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonCreator.Mode;
import com.fasterxml.jackson.annotation.JsonValue;


@JsonAutoDetect(getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE, fieldVisibility = Visibility.ANY)
public enum PensjonTrygdType implements Kodeverdi, YtelseType {

    UDEFINERT("-"),
    KVALIFISERINGSSTØNAD("KVALIFISERINGSSTØNAD")
    ;

    private static final Map<String, PensjonTrygdType> KODER = new LinkedHashMap<>();

    public static final String KODEVERK = "PENSJON_TRYGD_BESKRIVELSE";

    static {
        for (var v : values()) {
            if (KODER.putIfAbsent(v.kode, v) != null) {
                throw new IllegalArgumentException("Duplikat : " + v.kode);
            }
        }
    }

    @JsonValue
    private final String kode;

    PensjonTrygdType(String kode) {
        this.kode = kode;
    }

    @JsonCreator(mode = Mode.DELEGATING)
    public static PensjonTrygdType fraKode(Object node) {
        if (node == null) {
            return null;
        }
        String kode = TempAvledeKode.getVerdi(PensjonTrygdType.class, node, "kode");
        var ad = KODER.get(kode);
        if (ad == null) {
            throw new IllegalArgumentException("Ukjent PensjonTrygdType: " + kode);
        }
        return ad;
    }

    public static Map<String, PensjonTrygdType> kodeMap() {
        return Collections.unmodifiableMap(KODER);
    }

    @Override
    public String getKodeverk() {
        return KODEVERK;
    }

    @Override
    public String getKode() {
        return kode;
    }

}
