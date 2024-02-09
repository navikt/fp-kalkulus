package no.nav.folketrygdloven.kalkulus.kodeverk;

import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonCreator.Mode;
import com.fasterxml.jackson.annotation.JsonValue;


@JsonAutoDetect(getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE, fieldVisibility = Visibility.ANY)
public enum VirksomhetType implements Kodeverdi, KontraktKode {

    DAGMAMMA("DAGMAMMA"),
    FISKE("FISKE"),
    FRILANSER("FRILANSER"),
    JORDBRUK_SKOGBRUK("JORDBRUK_SKOGBRUK"),
    ENKELTPERSONFORETAK("ENK"),
    ANNEN("ANNEN"),
    UDEFINERT(KodeKonstanter.UDEFINERT),
    ;
    private static final Map<String, VirksomhetType> KODER = new LinkedHashMap<>();

    static {
        for (var v : values()) {
            if (KODER.putIfAbsent(v.kode, v) != null) {
                throw new IllegalArgumentException("Duplikat : " + v.kode);
            }
        }
    }

    @JsonValue
    private final String kode;

    VirksomhetType(String kode) {
        this.kode = kode;
    }

    @JsonCreator(mode = Mode.DELEGATING)
    public static VirksomhetType fraKode(Object node) {
        if (node == null) {
            return null;
        }
        String kode = TempAvledeKode.getVerdi(VirksomhetType.class, node, "kode");
        var ad = KODER.get(kode);
        if (ad == null) {
            throw new IllegalArgumentException("Ukjent VirksomhetType: " + kode);
        }
        return ad;
    }


    @Override
    public String getKode() {
        return kode;
    }

}
