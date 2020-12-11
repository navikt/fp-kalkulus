package no.nav.folketrygdloven.kalkulus.kodeverk;

import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonCreator.Mode;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;

@JsonFormat(shape = Shape.OBJECT)
@JsonAutoDetect(getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE, fieldVisibility = Visibility.ANY)
public enum FrisinnBehandlingType implements Kodeverdi {

    REVURDERING("REVURDERING", "Revurdering"),
    NY_SØKNADSPERIODE("NY_SØKNADSPERIODE", "Ny søknadsperiode ");

    public static final String KODEVERK = "FRISINN_BEHANDLING_TYPE";

    private static final Map<String, FrisinnBehandlingType> KODER = new LinkedHashMap<>();

    static {
        for (var v : values()) {
            if (KODER.putIfAbsent(v.kode, v) != null) {
                throw new IllegalArgumentException("Duplikat : " + v.kode);
            }
        }
    }

    @JsonIgnore
    private String kode;
    
    private String navn;

    FrisinnBehandlingType(String kode, String navn) {
        this.kode = kode;
        this.navn = navn;
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
    
    public String getNavn() {
        return navn;
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
