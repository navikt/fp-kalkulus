package no.nav.folketrygdloven.kalkulus.kodeverk;

import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonCreator.Mode;
import com.fasterxml.jackson.annotation.JsonValue;

@JsonAutoDetect(getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE, fieldVisibility = Visibility.ANY)
public enum Inntektskategori implements Kodeverdi, DatabaseKode, KontraktKode {

    ARBEIDSTAKER,
    FRILANSER,
    SELVSTENDIG_NÆRINGSDRIVENDE,
    DAGPENGER,
    ARBEIDSAVKLARINGSPENGER,
    SJØMANN,
    DAGMAMMA,
    JORDBRUKER,
    FISKER,
    ARBEIDSTAKER_UTEN_FERIEPENGER,
    UDEFINERT,
    ;

    private static final Map<String, Inntektskategori> KODER = new LinkedHashMap<>();

    static {
        KODER.putIfAbsent(KodeKonstanter.UDEFINERT, UDEFINERT);
        for (var v : values()) {
            if (KODER.putIfAbsent(v.name(), v) != null) {
                throw new IllegalArgumentException("Duplikat : " + v.name());
            }
        }
    }

    @JsonCreator(mode = Mode.DELEGATING)
    public static Inntektskategori fraKode(Object node) {
        if (node == null) {
            return null;
        }
        String kode = TempAvledeKode.getVerdi(Inntektskategori.class, node, "kode");
        var ad = KODER.get(kode);
        if (ad == null) {
            throw new IllegalArgumentException("Ukjent Inntektskategori: " + kode);
        }
        return ad;
    }

    @Override
    @JsonValue
    public String getKode() {
        return this == UDEFINERT ? KodeKonstanter.UDEFINERT : name();
    }

    public static Inntektskategori fraDatabaseKode(String databaseKode) {
        return fraKode(databaseKode);
    }

}
