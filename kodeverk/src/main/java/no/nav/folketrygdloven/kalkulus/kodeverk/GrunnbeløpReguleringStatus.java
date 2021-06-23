package no.nav.folketrygdloven.kalkulus.kodeverk;

import java.util.Collections;
import java.util.LinkedHashMap;
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
 * Mulige statuser for grunnbeløpet på en kobling.
 * NØDVENDIG - Grunnbeløpet som er brukt på koblingen avviker fra nylig utledet grunnbeløp og det kan påvirke beregningen, betyr typisk at gregulering er nødvendig
 * IKKE_NØDVENDIG - Grunnbeløpet som er brukt på koblingen avviker ikke fra nylig utledet grunnbeløp eller nytt grunnbeløp påvirker ikke beregningen, gregulering er ikke nødvendig
 * IKKE_VURDERT - Beregningen har ikke hensyntatt grunnbeløp på koblingen enda
 */
@JsonFormat(shape = Shape.OBJECT)
@JsonAutoDetect(getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE, fieldVisibility = Visibility.ANY)
public
enum GrunnbeløpReguleringStatus implements Kodeverdi {

    NØDVENDIG("NØDVENDIG", "Gregulering er nødvendig"),
    IKKE_NØDVENDIG("IKKE_NØDVENDIG", "Gregulering er ikke nødvendig"),
    IKKE_VURDERT("IKKE_VURDERT", "Ikke vurdert");

    public static final String KODEVERK = "GRUNNBELØP_REGULERING_STATUS";
    private static final Map<String, GrunnbeløpReguleringStatus> KODER = new LinkedHashMap<>();

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

    private GrunnbeløpReguleringStatus(String kode) {
        this.kode = kode;
    }

    private GrunnbeløpReguleringStatus(String kode, String navn) {
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
    public static GrunnbeløpReguleringStatus fraKode(Object node) {
        if (node == null) {
            return null;
        }
        String kode = TempAvledeKode.getVerdi(GrunnbeløpReguleringStatus.class, node, "kode");
        var ad = KODER.get(kode);
        if (ad == null) {
            throw new IllegalArgumentException("Ukjent GrunnbeløpReguleringStatus: " + kode);
        }
        return ad;
    }

    public static Map<String, GrunnbeløpReguleringStatus> kodeMap() {
        return Collections.unmodifiableMap(KODER);
    }

    public static void main(String[] args) {
        System.out.println(KODER.keySet());
    }
}
