package no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.folketrygdloven.kalkulus.felles.kodeverk.Kodeverdi;


@JsonFormat(shape = JsonFormat.Shape.OBJECT)
@JsonAutoDetect(getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE, fieldVisibility = Visibility.ANY)
public enum AndelKilde implements Kodeverdi {

    SAKSBEHANDLER_KOFAKBER("SAKSBEHANDLER_KOFAKBER", "Saksbehandler i steg kontroller fakta beregning"),
    SAKSBEHANDLER_FORDELING("SAKSBEHANDLER_FORDELING", "Saksbehandler i steg for fordeling"),
    PROSESS_PERIODISERING("PROSESS_PERIODISERING", "Prosess for periodisering grunnet refusjon/gradering/utbetalingsgrad"),
    PROSESS_OMFORDELING("PROSESS_OMFORDELING", "Prosess for automatisk omfordeling"),
    PROSESS_START("PROSESS_START", "Start av beregning"),
    ;
    private static final Map<String, AndelKilde> KODER = new LinkedHashMap<>();

    public static final String KODEVERK = "ANDEL_KILDE";

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

    AndelKilde(String kode, String navn) {
        this.kode = kode;
        this.navn = navn;
    }

    @JsonCreator
    public static AndelKilde fraKode(@JsonProperty("kode") String kode) {
        if (kode == null) {
            return null;
        }
        var ad = KODER.get(kode);
        if (ad == null) {
            throw new IllegalArgumentException("Ukjent andelkilde: " + kode);
        }
        return ad;
    }
    public static Map<String, AndelKilde> kodeMap() {
        return Collections.unmodifiableMap(KODER);
    }

    @Override
    public String getNavn() {
        return navn;
    }

    @JsonProperty
    @Override
    public String getKodeverk() {
        return KODEVERK;
    }

    @JsonProperty
    @Override
    public String getKode() {
        return kode;
    }

    @Override
    public String getOffisiellKode() {
        return getKode();
    }

    public static void main(String[] args) {
        System.out.println(KODER.keySet().stream().map(k -> "'" + k + "'").collect(Collectors.toList()));
    }

    @Converter(autoApply = true)
    public static class KodeverdiConverter implements AttributeConverter<AndelKilde, String> {
        @Override
        public String convertToDatabaseColumn(AndelKilde attribute) {
            return attribute == null ? null : attribute.getKode();
        }

        @Override
        public AndelKilde convertToEntityAttribute(String dbData) {
            return dbData == null ? null : fraKode(dbData);
        }

    }
}
