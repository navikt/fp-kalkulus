package no.nav.folketrygdloven.kalkulus.kodeverk;

import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonCreator.Mode;
import com.fasterxml.jackson.annotation.JsonValue;


/**
 * Mulige statuser for grunnbeløpet på en kobling.
 * NØDVENDIG - Grunnbeløpet som er brukt på koblingen avviker fra nylig utledet grunnbeløp og det kan påvirke beregningen, betyr typisk at gregulering er nødvendig
 * IKKE_NØDVENDIG - Grunnbeløpet som er brukt på koblingen avviker ikke fra nylig utledet grunnbeløp eller nytt grunnbeløp påvirker ikke beregningen, gregulering er ikke nødvendig
 * IKKE_VURDERT - Beregningen har ikke hensyntatt grunnbeløp på koblingen enda
 */
@JsonAutoDetect(getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE, fieldVisibility = Visibility.ANY)
public enum GrunnbeløpReguleringStatus implements Kodeverdi, KontraktKode {

    NØDVENDIG,
    IKKE_NØDVENDIG,
    IKKE_VURDERT;

    private static final Map<String, GrunnbeløpReguleringStatus> KODER = new LinkedHashMap<>();

    static {
        for (var v : values()) {
            if (KODER.putIfAbsent(v.name(), v) != null) {
                throw new IllegalArgumentException("Duplikat : " + v.name());
            }
        }
    }

    @Override
    @JsonValue
    public String getKode() {
        return name();
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
}
