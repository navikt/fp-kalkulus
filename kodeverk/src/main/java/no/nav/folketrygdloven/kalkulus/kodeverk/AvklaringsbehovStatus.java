package no.nav.folketrygdloven.kalkulus.kodeverk;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonCreator.Mode;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;


/**
 * Mulige statuser for avklaringsbehov.
 * OPPRETTET - Avklaringsbehovet er opprettet og ligger uløst på koblingen
 * UTFØRT - Avklaringsbehovet er opprettet og løst av saksbehandler
 * AVBRUTT - Avklaringsbehovet var før opprettet men er blitt avbrutt
 */
@JsonFormat(shape = Shape.OBJECT)
@JsonAutoDetect(getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE, fieldVisibility = Visibility.ANY)
public
enum AvklaringsbehovStatus implements Kodeverdi {

    OPPRETTET("OPPR", "Opprettet"),
    UTFØRT ("UTFO", "Utført"),
    AVBRUTT ("AVBR", "Avbrutt");

    public static final String KODEVERK = "AKSJONSPUNKT_STATUS";
    private static final Map<String, AvklaringsbehovStatus> KODER = new LinkedHashMap<>();

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

    private AvklaringsbehovStatus(String kode) {
        this.kode = kode;
    }

    private AvklaringsbehovStatus(String kode, String navn) {
        this.kode = kode;
        this.navn = navn;
    }

    @JsonProperty
    @Override
    public String getKode() {
        return kode;
    }

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Override
    public String getKodeverk() {
        return KODEVERK;
    }

    @JsonCreator(mode = Mode.DELEGATING)
    public static AvklaringsbehovStatus fraKode(Object node) {
        if (node == null) {
            return null;
        }
        String kode = TempAvledeKode.getVerdi(AvklaringsbehovStatus.class, node, "kode");
        var ad = KODER.get(kode);
        if (ad == null) {
            throw new IllegalArgumentException("Ukjent AvklaringsbehovStatus: " + kode);
        }
        return ad;
    }

    public static Map<String, AvklaringsbehovStatus> kodeMap() {
        return Collections.unmodifiableMap(KODER);
    }

    public static void main(String[] args) {
        System.out.println(KODER.keySet());
    }

}
