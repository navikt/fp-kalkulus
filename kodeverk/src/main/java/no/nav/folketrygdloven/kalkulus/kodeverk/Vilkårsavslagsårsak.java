package no.nav.folketrygdloven.kalkulus.kodeverk;

import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonCreator.Mode;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonFormat(shape = Shape.OBJECT)
@JsonAutoDetect(getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE, fieldVisibility = Visibility.ANY)
public enum Vilkårsavslagsårsak implements Kodeverdi{

    ATFL_SAMME_ORG("ATFL_SAMME_ORG"),
    SØKT_FL_INGEN_FL_INNTEKT("SØKT_FL_INGEN_FL_INNTEKT"),
    FOR_LAVT_BG("FOR_LAVT_BG"),
    FOR_LAVT_BG_8_47("FOR_LAVT_BG_8_47"),
    AVKORTET_GRUNNET_ANNEN_INNTEKT("AVKORTET_GRUNNET_ANNEN_INNTEKT");

    private static final String KODEVERK = "AVSLAGSÅRSAK";
    private static final Map<String, Vilkårsavslagsårsak> KODER = new LinkedHashMap<>();

    static {
        for (var v : values()) {
            if (KODER.putIfAbsent(v.kode, v) != null) {
                throw new IllegalArgumentException("Duplikat : " + v.kode);
            }
        }
    }

    private final String kode;

    Vilkårsavslagsårsak(String kode) {
        this.kode = kode;
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

    @JsonCreator(mode = Mode.DELEGATING)
    public static Vilkårsavslagsårsak fraKode(Object node) {
        if (node == null) {
            return null;
        }
        String kode = TempAvledeKode.getVerdi(Vilkårsavslagsårsak.class, node, "kode");
        var ad = KODER.get(kode);
        if (ad == null) {
            throw new IllegalArgumentException("Ukjent Vilkårsavslagsårsak: " + kode);
        }
        return ad;
    }

}
