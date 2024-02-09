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

    ARBEIDSTAKER("ARBEIDSTAKER"),
    FRILANSER("FRILANSER"),
    SELVSTENDIG_NÆRINGSDRIVENDE("SELVSTENDIG_NÆRINGSDRIVENDE"),
    DAGPENGER("DAGPENGER"),
    ARBEIDSAVKLARINGSPENGER("ARBEIDSAVKLARINGSPENGER"),
    SJØMANN("SJØMANN"),
    DAGMAMMA("DAGMAMMA"),
    JORDBRUKER("JORDBRUKER"),
    FISKER("FISKER"),
    ARBEIDSTAKER_UTEN_FERIEPENGER("ARBEIDSTAKER_UTEN_FERIEPENGER"),
    UDEFINERT(KodeKonstanter.UDEFINERT),
    ;

    private static final Map<String, Inntektskategori> KODER = new LinkedHashMap<>();

    static {
        for (var v : values()) {
            if (KODER.putIfAbsent(v.kode, v) != null) {
                throw new IllegalArgumentException("Duplikat : " + v.kode);
            }
        }
    }

    @JsonValue
    private final String kode;

    Inntektskategori(String kode) {
        this.kode = kode;
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
    public String getKode() {
        return kode;
    }

    public static Inntektskategori fraDatabaseKode(String databaseKode) {
        return fraKode(databaseKode);
    }

}
