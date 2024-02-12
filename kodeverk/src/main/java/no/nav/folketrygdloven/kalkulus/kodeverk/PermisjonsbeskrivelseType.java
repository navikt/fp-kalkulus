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

    UDEFINERT,
    PERMISJON,
    UTDANNINGSPERMISJON,
    UTDANNINGSPERMISJON_IKKE_LOVFESTET,
    UTDANNINGSPERMISJON_LOVFESTET,
    VELFERDSPERMISJON,
    ANNEN_PERMISJON_IKKE_LOVFESTET,
    ANNEN_PERMISJON_LOVFESTET,
    PERMISJON_MED_FORELDREPENGER,
    PERMITTERING,
    PERMISJON_VED_MILITÆRTJENESTE,
    ;


    public static final Set<PermisjonsbeskrivelseType> K9_VELFERDSPERMISJON = Set.of(
            PermisjonsbeskrivelseType.VELFERDSPERMISJON,
            PermisjonsbeskrivelseType.ANNEN_PERMISJON_LOVFESTET
    );


    private static final Map<String, PermisjonsbeskrivelseType> KODER = new LinkedHashMap<>();

    static {
        KODER.putIfAbsent(KodeKonstanter.UDEFINERT, UDEFINERT);
        for (var v : values()) {
            if (KODER.putIfAbsent(v.name(), v) != null) {
                throw new IllegalArgumentException("Duplikat : " + v.name());
            }
        }
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
    @JsonValue
    public String getKode() {
        return this == UDEFINERT ? KodeKonstanter.UDEFINERT : name();
    }

}
