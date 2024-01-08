package no.nav.folketrygdloven.kalkulus.kodeverk;


import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

@JsonAutoDetect(getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, fieldVisibility = JsonAutoDetect.Visibility.ANY)
public enum MidlertidigInaktivType implements Kodeverdi, KontraktKode {

    A("8-47 A"), B("8-47 B");

    private static final Map<String, MidlertidigInaktivType> KODER = new LinkedHashMap<>();
    @JsonValue // TODO: Plain enum. Fjerne kode - krever at k9sak oppdaterer kontrakt ca samtidig.
    private String kode;

    static {
        for (var v : values()) {
            if (KODER.putIfAbsent(v.kode, v) != null) {
                throw new IllegalArgumentException("Duplikat : " + v.kode);
            }
            if (KODER.putIfAbsent(v.name(), v) != null) {
                throw new IllegalArgumentException("Duplikat : " + v.name());
            }
        }
    }

    MidlertidigInaktivType(String s) {
        kode = s;
    }

    @Override
    public String getKode() {
        return kode;
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static MidlertidigInaktivType fraKode(Object node) {
        if (node == null) {
            return null;
        }
        String kode = TempAvledeKode.getVerdi(MidlertidigInaktivType.class, node, "kode");
        var ad = KODER.get(kode);
        if (ad == null) {
            throw new IllegalArgumentException("Ukjent MidlertidigInaktivType: " + kode);
        }
        return ad;
    }
}
