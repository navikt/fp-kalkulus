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
public enum AndelKilde implements Kodeverdi, DatabaseKode, KontraktKode {

    SAKSBEHANDLER_KOFAKBER("SAKSBEHANDLER_KOFAKBER", "Saksbehandler i steg kontroller fakta beregning"),
    PROSESS_BESTEBEREGNING("PROSESS_BESTEBEREGNING", "Prosess for besteberegning"),
    SAKSBEHANDLER_FORDELING("SAKSBEHANDLER_FORDELING", "Saksbehandler i steg for fordeling"),
    PROSESS_PERIODISERING("PROSESS_PERIODISERING", "Prosess for periodisering grunnet refusjon/gradering/utbetalingsgrad"),
    PROSESS_OMFORDELING("PROSESS_OMFORDELING", "Prosess for automatisk omfordeling"),
    PROSESS_START("PROSESS_START", "Start av beregning"),
    PROSESS_PERIODISERING_TILKOMMET_INNTEKT("PROSESS_PERIODISERING_TILKOMMET_INNTEKT", "Periodisering for tilkommet inntekt"),

    ;
    private static final Map<String, AndelKilde> KODER = new LinkedHashMap<>();

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

    AndelKilde(String kode, String navn) {
        this.kode = kode;
        this.navn = navn;
    }

    @JsonCreator(mode = Mode.DELEGATING)
    public static AndelKilde fraKode(Object node) {
        if (node == null) {
            return null;
        }
        String kode = TempAvledeKode.getVerdi(AndelKilde.class, node, "kode");
        var ad = KODER.get(kode);
        if (ad == null) {
            throw new IllegalArgumentException("Ukjent AndelKilde: " + kode);
        }
        return ad;
    }

    @Override
    public String getKode() {
        return kode;
    }

    public static AndelKilde fraDatabaseKode(String databaseKode) {
        return fraKode(databaseKode);
    }

}
