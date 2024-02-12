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

    NATURALYTELSE_BORTFALT,
    ARBEIDSFORHOLD_AVSLUTTET,
    NATURALYTELSE_TILKOMMER,
    ENDRING_I_REFUSJONSKRAV,
    REFUSJON_OPPHØRER,
    GRADERING,
    GRADERING_OPPHØRER,
    ENDRING_I_AKTIVITETER_SØKT_FOR,
    TILKOMMET_INNTEKT,
    TILKOMMET_INNTEKT_MANUELT,
    TILKOMMET_INNTEKT_AVSLUTTET,
    REFUSJON_AVSLÅTT,
    REPRESENTERER_STORTINGET,
    REPRESENTERER_STORTINGET_AVSLUTTET,
    UDEFINERT,
    ;

    private static final Map<String, PeriodeÅrsak> KODER = new LinkedHashMap<>();

    static {
        KODER.putIfAbsent(KodeKonstanter.UDEFINERT, UDEFINERT);
        for (var v : values()) {
            if (KODER.putIfAbsent(v.name(), v) != null) {
                throw new IllegalArgumentException("Duplikat : " + v.name());
            }
        }
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
    @JsonValue
    public String getKode() {
        return this == UDEFINERT ? KodeKonstanter.UDEFINERT : name();
    }

    public static PeriodeÅrsak fraDatabaseKode(String databaseKode) {
        return fraKode(databaseKode);
    }

}
