package no.nav.folketrygdloven.kalkulus.kodeverk;

import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonCreator.Mode;
import com.fasterxml.jackson.annotation.JsonValue;

@JsonAutoDetect(getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE, fieldVisibility = Visibility.ANY)
public enum Fagsystem implements Kodeverdi {

    FPSAK("FPSAK"),
    TPS("TPS"),
    JOARK("JOARK"),
    INFOTRYGD("INFOTRYGD"),
    ARENA("ARENA"),
    INNTEKT("INNTEKT"),
    MEDL("MEDL"),
    GOSYS("GOSYS"),
    ENHETSREGISTERET("ENHETSREGISTERET"),
    AAREGISTERET("AAREGISTERET"),

    /**
     * Alle kodeverk må ha en verdi, det kan ikke være null i databasen. Denne koden gjør samme nytten.
     */
    UDEFINERT("-"),
    ;

    private static final Map<String, Fagsystem> KODER = new LinkedHashMap<>();

    @JsonValue
    private final String kode;

    Fagsystem(String kode) {
        this.kode = kode;
    }

    @JsonCreator(mode = Mode.DELEGATING)
    public static Fagsystem fraKode(Object node) {
        if (node == null) {
            return null;
        }
        String kode = TempAvledeKode.getVerdi(Fagsystem.class, node, "kode");
        var ad = KODER.get(kode);
        if (ad == null) {
            throw new IllegalArgumentException("Ukjent Fagsystem: " + kode);
        }
        return ad;
    }

    @Override
    public String getKode() {
        return kode;
    }


    static {
        for (var v : values()) {
            if (KODER.putIfAbsent(v.kode, v) != null) {
                throw new IllegalArgumentException("Duplikat : " + v.kode);
            }
        }
    }

}
