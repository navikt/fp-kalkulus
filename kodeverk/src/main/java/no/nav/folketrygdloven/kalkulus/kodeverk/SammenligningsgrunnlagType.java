package no.nav.folketrygdloven.kalkulus.kodeverk;

/**
 * <h3>Internt kodeverk</h3>
 * Definerer status/type av {@link SammenligningsgrunnlagPrStatus}
 * <p>
 */

import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonCreator.Mode;
import com.fasterxml.jackson.annotation.JsonValue;


@JsonAutoDetect(getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE, fieldVisibility = Visibility.ANY)
public enum SammenligningsgrunnlagType implements Kodeverdi, DatabaseKode, KontraktKode {

    SAMMENLIGNING_AT("SAMMENLIGNING_AT"),
    SAMMENLIGNING_FL("SAMMENLIGNING_FL"),
    SAMMENLIGNING_AT_FL("SAMMENLIGNING_AT_FL"),
    SAMMENLIGNING_SN("SAMMENLIGNING_SN"),
    SAMMENLIGNING_ATFL_SN("SAMMENLIGNING_ATFL_SN"),
    SAMMENLIGNING_MIDL_INAKTIV("SAMMENLIGNING_MIDL_INAKTIV"),

    ;

    private static final Map<String, SammenligningsgrunnlagType> KODER = new LinkedHashMap<>();

    static {
        for (var v : values()) {
            if (KODER.putIfAbsent(v.kode, v) != null) {
                throw new IllegalArgumentException("Duplikat : " + v.kode);
            }
        }
    }

    @JsonValue
    private final String kode;

    SammenligningsgrunnlagType(String kode) {
        this.kode = kode;
    }

    @JsonCreator(mode = Mode.DELEGATING)
    public static SammenligningsgrunnlagType fraKode(Object node) {
        if (node == null) {
            return null;
        }
        String kode = TempAvledeKode.getVerdi(SammenligningsgrunnlagType.class, node, "kode");
        var ad = KODER.get(kode);
        if (ad == null) {
            throw new IllegalArgumentException("Ukjent SammenligningsgrunnlagType: " + kode);
        }
        return ad;
    }

    @Override
    public String getKode() {
        return kode;
    }

    public static SammenligningsgrunnlagType fraDatabaseKode(String databaseKode) {
        return fraKode(databaseKode);
    }

}
