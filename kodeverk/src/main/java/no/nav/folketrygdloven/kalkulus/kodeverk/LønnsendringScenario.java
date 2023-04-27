package no.nav.folketrygdloven.kalkulus.kodeverk;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonCreator.Mode;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;


@JsonFormat(shape = JsonFormat.Shape.OBJECT)
@JsonAutoDetect(getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE, fieldVisibility = Visibility.ANY)
public enum LønnsendringScenario implements Kodeverdi {

    MANUELT_BEHANDLET("MANUELT_BEHANDLET", "Inntekt er manuelt satt i fakta om beregning"),
    DELVIS_MÅNEDSINNTEKT_SISTE_MND("DELVIS_MÅNEDSINNTEKT_SISTE_MND", "Inntekt er beregnet fra siste måned som har delvis ny og gammel inntekt"),
    FULL_MÅNEDSINNTEKT_EN_MND("FULL_MÅNEDSINNTEKT_EN_MND", "Inntekt er beregnet fra siste måned som kun har ny inntekt."),
    FULL_MÅNEDSINNTEKT_TO_MND("FULL_MÅNEDSINNTEKT_TO_MND", "Inntekt er beregnet fra de siste to månedene som begge har ny inntekt"),

    ;
    private static final Map<String, LønnsendringScenario> KODER = new LinkedHashMap<>();

    public static final String KODEVERK = "LONNSENDRING_SCENARIO";

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

    public static Map<String, LønnsendringScenario> kodeMap() {
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
