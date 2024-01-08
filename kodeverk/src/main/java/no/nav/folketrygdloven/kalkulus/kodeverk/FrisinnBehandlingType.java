package no.nav.folketrygdloven.kalkulus.kodeverk;

import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonCreator.Mode;
import com.fasterxml.jackson.annotation.JsonValue;

@JsonAutoDetect(getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE, fieldVisibility = Visibility.ANY)
public enum FrisinnBehandlingType implements Kodeverdi, KontraktKode {

    REVURDERING("REVURDERING"),
    NY_SØKNADSPERIODE("NY_SØKNADSPERIODE");

    private static final Map<String, FrisinnBehandlingType> KODER = new LinkedHashMap<>();

    static {
        for (var v : values()) {
            if (KODER.putIfAbsent(v.kode, v) != null) {
                throw new IllegalArgumentException("Duplikat : " + v.kode);
            }
        }
    }

    @JsonValue
    private final String kode;

    FrisinnBehandlingType(String kode) {
        this.kode = kode;
    }

    @Override
    public String getKode() {
        return kode;
    }


    @JsonCreator(mode = Mode.DELEGATING)
    public static FrisinnBehandlingType fraKode(Object node) {
        if (node == null) {
            return null;
        }
        String kode = TempAvledeKode.getVerdi(FrisinnBehandlingType.class, node, "kode");
        var ad = KODER.get(kode);
        if (ad == null) {
            throw new IllegalArgumentException("Ukjent FrisinnBehandlingType: " + kode);
        }
        return ad;
    }


}
