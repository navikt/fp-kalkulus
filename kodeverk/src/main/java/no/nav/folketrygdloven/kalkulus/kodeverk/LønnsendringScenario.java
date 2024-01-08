package no.nav.folketrygdloven.kalkulus.kodeverk;

import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonCreator.Mode;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonValue;


@JsonAutoDetect(getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE, fieldVisibility = Visibility.ANY)
public enum LønnsendringScenario implements Kodeverdi, KontraktKode {

    MANUELT_BEHANDLET("MANUELT_BEHANDLET", "Inntekt er manuelt satt i fakta om beregning"),
    DELVIS_MÅNEDSINNTEKT_SISTE_MND("DELVIS_MÅNEDSINNTEKT_SISTE_MND", "Inntekt er beregnet fra siste måned som har delvis ny og gammel inntekt"),
    FULL_MÅNEDSINNTEKT_EN_MND("FULL_MÅNEDSINNTEKT_EN_MND", "Inntekt er beregnet fra siste måned som kun har ny inntekt."),
    FULL_MÅNEDSINNTEKT_TO_MND("FULL_MÅNEDSINNTEKT_TO_MND", "Inntekt er beregnet fra de siste to månedene som begge har ny inntekt"),

    ;
    private static final Map<String, LønnsendringScenario> KODER = new LinkedHashMap<>();

    static {
        for (var v : values()) {
            if (KODER.putIfAbsent(v.kode, v) != null) {
                throw new IllegalArgumentException("Duplikat : " + v.kode);
            }
        }
    }

    @JsonIgnore
    private final String navn;
    @JsonValue
    private final String kode;

    LønnsendringScenario(String kode, String navn) {
        this.kode = kode;
        this.navn = navn;
    }

    @JsonCreator(mode = Mode.DELEGATING)
    public static LønnsendringScenario fraKode(Object node) {
        if (node == null) {
            return null;
        }
        String kode = TempAvledeKode.getVerdi(LønnsendringScenario.class, node, "kode");
        var ad = KODER.get(kode);
        if (ad == null) {
            throw new IllegalArgumentException("Ukjent LønnsendringScenario: " + kode);
        }
        return ad;
    }

    @Override
    public String getKode() {
        return kode;
    }

}
