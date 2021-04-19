package no.nav.folketrygdloven.kalkulus.opptjening.v1;


import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;

import no.nav.folketrygdloven.kalkulus.kodeverk.Kodeverdi;
import no.nav.folketrygdloven.kalkulus.kodeverk.TempAvledeKode;

import java.util.LinkedHashMap;
import java.util.Map;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
@JsonAutoDetect(getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, fieldVisibility = JsonAutoDetect.Visibility.ANY)
public enum MidlertidigInaktivType implements Kodeverdi {

    A("8-47 A"), B("8-47 B");

    public static final String KODEVERK = "MIDLERTIDIG_INAKTIV_TYPE";
    private static final Map<String, MidlertidigInaktivType> KODER = new LinkedHashMap<>();

    private String kode;

    static {
        for (var v : values()) {
            if (KODER.putIfAbsent(v.kode, v) != null) {
                throw new IllegalArgumentException("Duplikat : " + v.kode);
            }
        }
    }

    MidlertidigInaktivType(String s) {
        kode = s;
    }

    @Override
    public String getKode() {
        return kode;
    }

    @Override
    public String getKodeverk() {
        return KODEVERK;
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static MidlertidigInaktivType fraKode(Object node) {
        if (node == null) {
            return null;
        }
        String kode = TempAvledeKode.getVerdi(MidlertidigInaktivType.class, node, "kode");
        var ad = KODER.get(kode);
        if (ad == null) {
            throw new IllegalArgumentException("Ukjent MidlertidigInaktivType: " + kode);
        }
        return ad;
    }
}
