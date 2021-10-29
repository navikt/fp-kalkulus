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
public enum FaktaVurderingKilde implements Kodeverdi {

    SAKSBEHANDLER("SAKSBEHANDLER", "Saksbehandler"),
    KALKULATOR("KALKULATOR", "Kalkulator"),
    UDEFINERT("-", "Ingen inntektskategori (default)"),
    ;

    private static final Map<String, FaktaVurderingKilde> KODER = new LinkedHashMap<>();

    public static final String KODEVERK = "FAKTA_VURDERING_KILDE";

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

    FaktaVurderingKilde(String kode, String navn) {
        this.kode = kode;
        this.navn = navn;
    }

    @JsonCreator(mode = Mode.DELEGATING)
    public static FaktaVurderingKilde fraKode(Object node) {
        if (node == null) {
            return null;
        }
        String kode = TempAvledeKode.getVerdi(FaktaVurderingKilde.class, node, "kode");
        var ad = KODER.get(kode);
        if (ad == null) {
            throw new IllegalArgumentException("Ukjent Inntektskategori: " + kode);
        }
        return ad;
    }

    public static Map<String, FaktaVurderingKilde> kodeMap() {
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
