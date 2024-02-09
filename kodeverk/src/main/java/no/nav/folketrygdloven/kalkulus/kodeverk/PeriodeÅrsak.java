package no.nav.folketrygdloven.kalkulus.kodeverk;

import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonCreator.Mode;
import com.fasterxml.jackson.annotation.JsonValue;


@JsonAutoDetect(getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE, fieldVisibility = Visibility.ANY)
public enum PeriodeÅrsak implements Kodeverdi, DatabaseKode, KontraktKode {

    NATURALYTELSE_BORTFALT("NATURALYTELSE_BORTFALT"),
    ARBEIDSFORHOLD_AVSLUTTET("ARBEIDSFORHOLD_AVSLUTTET"),
    NATURALYTELSE_TILKOMMER("NATURALYTELSE_TILKOMMER"),
    ENDRING_I_REFUSJONSKRAV("ENDRING_I_REFUSJONSKRAV"),
    REFUSJON_OPPHØRER("REFUSJON_OPPHØRER"),
    GRADERING("GRADERING"),
    GRADERING_OPPHØRER("GRADERING_OPPHØRER"),
    ENDRING_I_AKTIVITETER_SØKT_FOR("ENDRING_I_AKTIVITETER_SØKT_FOR"),
    TILKOMMET_INNTEKT("TILKOMMET_INNTEKT"),
    TILKOMMET_INNTEKT_MANUELT("TILKOMMET_INNTEKT_MANUELT"),
    TILKOMMET_INNTEKT_AVSLUTTET("TILKOMMET_INNTEKT_AVSLUTTET"),
    REFUSJON_AVSLÅTT("REFUSJON_AVSLÅTT"),
    REPRESENTERER_STORTINGET("REPRESENTERER_STORTINGET"),
    REPRESENTERER_STORTINGET_AVSLUTTET("REPRESENTERER_STORTINGET_AVSLUTTET"),

    UDEFINERT(KodeKonstanter.UDEFINERT),
    ;

    private static final Map<String, PeriodeÅrsak> KODER = new LinkedHashMap<>();

    static {
        for (var v : values()) {
            if (KODER.putIfAbsent(v.kode, v) != null) {
                throw new IllegalArgumentException("Duplikat : " + v.kode);
            }
        }
    }

    @JsonValue
    private final String kode;

    PeriodeÅrsak(String kode) {
        this.kode = kode;
    }

    @JsonCreator(mode = Mode.DELEGATING)
    public static PeriodeÅrsak fraKode(Object node) {
        if (node == null) {
            return null;
        }
        String kode = TempAvledeKode.getVerdi(PeriodeÅrsak.class, node, "kode");
        var ad = KODER.get(kode);
        if (ad == null) {
            throw new IllegalArgumentException("Ukjent PeriodeÅrsak: " + kode);
        }
        return ad;
    }


    @Override
    public String getKode() {
        return kode;
    }

    public static PeriodeÅrsak fraDatabaseKode(String databaseKode) {
        return fraKode(databaseKode);
    }

}
