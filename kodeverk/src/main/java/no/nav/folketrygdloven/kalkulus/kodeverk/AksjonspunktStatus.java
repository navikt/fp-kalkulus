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
 * Mulige statuser for aksjonspunkt.
 * OPPRETTET - Aksjonspunktet er opprettet og ligger uløst på koblingen
 * UTFØRT - Aksjonspunktet er opprettet og løst av saksbehandler
 * AVBRUTT - Aksjonspunktet var før opprettet men er blitt avbrutt
 */
@JsonFormat(shape = Shape.OBJECT)
@JsonAutoDetect(getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE, fieldVisibility = Visibility.ANY)
public
enum AksjonspunktStatus implements Kodeverdi {

    OPPRETTET("OPPR", "Opprettet"),
    UTFØRT ("UTFO", "Utført"),
    AVBRUTT ("AVBR", "Avbrutt");

    public static final String KODEVERK = "AKSJONSPUNKT_STATUS";
    private static final Map<String, AksjonspunktStatus> KODER = new LinkedHashMap<>();
    private static final List<AksjonspunktStatus> ÅPNE_AKSJONSPUNKT_KODER = List.of(OPPRETTET);

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

    private AksjonspunktStatus(String kode) {
        this.kode = kode;
    }

    private AksjonspunktStatus(String kode, String navn) {
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
    public static AksjonspunktStatus fraKode(Object node) {
        if (node == null) {
            return null;
        }
        String kode = TempAvledeKode.getVerdi(AksjonspunktStatus.class, node, "kode");
        var ad = KODER.get(kode);
        if (ad == null) {
            throw new IllegalArgumentException("Ukjent AksjonspunktStatus: " + kode);
        }
        return ad;
    }

    public static Map<String, AksjonspunktStatus> kodeMap() {
        return Collections.unmodifiableMap(KODER);
    }

    public static void main(String[] args) {
        System.out.println(KODER.keySet());
    }

    public boolean erÅpentAksjonspunkt() {
        return ÅPNE_AKSJONSPUNKT_KODER.contains(this);
    }

    public static List<AksjonspunktStatus> getÅpneAksjonspunktStatuser() {
        return new ArrayList<>(ÅPNE_AKSJONSPUNKT_KODER);
    }
}
