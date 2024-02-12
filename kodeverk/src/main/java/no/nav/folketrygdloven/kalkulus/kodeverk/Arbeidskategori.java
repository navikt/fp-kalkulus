package no.nav.folketrygdloven.kalkulus.kodeverk;

import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonCreator.Mode;
import com.fasterxml.jackson.annotation.JsonValue;


@JsonAutoDetect(getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE, fieldVisibility = Visibility.ANY)
public enum Arbeidskategori implements Kodeverdi, KontraktKode {

    FISKER, // Selvstendig næringsdrivende - Fisker
    ARBEIDSTAKER,
    SELVSTENDIG_NÆRINGSDRIVENDE,
    KOMBINASJON_ARBEIDSTAKER_OG_SELVSTENDIG_NÆRINGSDRIVENDE,
    SJØMANN, // Arbeidstaker - sjømann
    JORDBRUKER, // Selvstendig næringsdrivende - Jordbruker
    DAGPENGER, // Tilstøtende ytelse - dagpenger
    INAKTIV,
    KOMBINASJON_ARBEIDSTAKER_OG_JORDBRUKER, //"Kombinasjon arbeidstaker og selvstendig næringsdrivende - jordbruker
    KOMBINASJON_ARBEIDSTAKER_OG_FISKER, // Kombinasjon arbeidstaker og selvstendig næringsdrivende - fisker
    FRILANSER,
    KOMBINASJON_ARBEIDSTAKER_OG_FRILANSER,
    KOMBINASJON_ARBEIDSTAKER_OG_DAGPENGER,
    DAGMAMMA, // Selvstendig næringsdrivende - Dagmamma
    UGYLDIG,
    UDEFINERT,
    ;

    private static final Map<String, Arbeidskategori> KODER = new LinkedHashMap<>();

    static {
        KODER.putIfAbsent(KodeKonstanter.UDEFINERT, UDEFINERT);
        for (var v : values()) {
            if (KODER.putIfAbsent(v.name(), v) != null) {
                throw new IllegalArgumentException("Duplikat : " + v.name());
            }
        }
    }


    @JsonCreator(mode = Mode.DELEGATING)
    public static Arbeidskategori fraKode(Object node) {
        if (node == null) {
            return null;
        }
        String kode = TempAvledeKode.getVerdi(Arbeidskategori.class, node, "kode");
        var ad = KODER.get(kode);
        if (ad == null) {
            throw new IllegalArgumentException("Ukjent Arbeidskategori: " + kode);
        }
        return ad;
    }

    @Override
    @JsonValue
    public String getKode() {
        return this == UDEFINERT ? KodeKonstanter.UDEFINERT : name();
    }


}
