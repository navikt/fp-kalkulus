package no.nav.folketrygdloven.kalkulus.kodeverk;


import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonCreator.Mode;
import com.fasterxml.jackson.annotation.JsonValue;


@JsonAutoDetect(getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE, fieldVisibility = Visibility.ANY)
public enum PermisjonsbeskrivelseType implements Kodeverdi, KontraktKode {

    UDEFINERT("-"),
    PERMISJON("PERMISJON"),
    UTDANNINGSPERMISJON("UTDANNINGSPERMISJON"),
    UTDANNINGSPERMISJON_IKKE_LOVFESTET("UTDANNINGSPERMISJON_IKKE_LOVFESTET"),
    UTDANNINGSPERMISJON_LOVFESTET("UTDANNINGSPERMISJON_LOVFESTET"),
    VELFERDSPERMISJON("VELFERDSPERMISJON"),
    ANNEN_PERMISJON_IKKE_LOVFESTET("ANNEN_PERMISJON_IKKE_LOVFESTET"),
    ANNEN_PERMISJON_LOVFESTET("ANNEN_PERMISJON_LOVFESTET"),
    PERMISJON_MED_FORELDREPENGER("PERMISJON_MED_FORELDREPENGER"),
    PERMITTERING("PERMITTERING"),
    PERMISJON_VED_MILITÆRTJENESTE("PERMISJON_VED_MILITÆRTJENESTE"),
    ;


    public static final Set<PermisjonsbeskrivelseType> K9_VELFERDSPERMISJON = Set.of(
            PermisjonsbeskrivelseType.VELFERDSPERMISJON,
            PermisjonsbeskrivelseType.ANNEN_PERMISJON_LOVFESTET
    );


    private static final Map<String, PermisjonsbeskrivelseType> KODER = new LinkedHashMap<>();

    static {
        for (var v : values()) {
            if (KODER.putIfAbsent(v.kode, v) != null) {
                throw new IllegalArgumentException("Duplikat : " + v.kode);
            }
        }
    }

    @JsonValue
    private final String kode;

    PermisjonsbeskrivelseType(String kode) {
        this.kode = kode;
    }

    @JsonCreator(mode = Mode.DELEGATING)
    public static PermisjonsbeskrivelseType fraKode(Object node) {
        if (node == null) {
            return null;
        }
        String kode = TempAvledeKode.getVerdi(PermisjonsbeskrivelseType.class, node, "kode");
        var ad = KODER.get(kode);
        if (ad == null) {
            throw new IllegalArgumentException("Ukjent PermisjonsbeskrivelseType: " + kode);
        }
        return ad;
    }


    @Override
    public String getKode() {
        return kode;
    }

}
