package no.nav.folketrygdloven.kalkulus.kodeverk;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonCreator.Mode;
import com.fasterxml.jackson.annotation.JsonValue;


@JsonAutoDetect(getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE, fieldVisibility = Visibility.ANY)
public enum NæringsinntektType implements Kodeverdi, YtelseType {

    VEDERLAG_DAGMAMMA_I_EGETHJEM("VEDERLAG_DAGMAMMA_I_EGETHJEM"),
    VEDERLAG("VEDERLAG"),
    SYKEPENGER_TIL_JORD_OG_SKOGBRUKERE("SYKEPENGER_TIL_JORD_OG_SKOGBRUKERE"),
    SYKEPENGER_TIL_FISKER("SYKEPENGER_TIL_FISKER"),
    SYKEPENGER_TIL_DAGMAMMA("SYKEPENGER_TIL_DAGMAMMA"),
    SYKEPENGER("SYKEPENGER"),
    SYKEPENGER_NÆRING("SYKEPENGER_NÆRING"),
    LOTT_KUN_TRYGDEAVGIFT("LOTT_KUN_TRYGDEAVGIFT"),
    DAGPENGER_VED_ARBEIDSLØSHET("DAGPENGER_VED_ARBEIDSLØSHET"),
    DAGPENGER_TIL_FISKER("DAGPENGER_TIL_FISKER"),
    DAGPENGER_NÆRING("DAGPENGER_NÆRING"),
    ANNET("ANNET"),
    KOMPENSASJON_FOR_TAPT_PERSONINNTEKT("KOMPENSASJON_FOR_TAPT_PERSONINNTEKT"),
    UDEFINERT("-"),

    ;

    private static final Map<String, NæringsinntektType> KODER = new LinkedHashMap<>();

    public static final String KODEVERK = "NÆRINGSINNTEKT_TYPE";

    static {
        for (var v : values()) {
            if (KODER.putIfAbsent(v.kode, v) != null) {
                throw new IllegalArgumentException("Duplikat : " + v.kode);
            }
        }
    }

    @JsonValue
    private final String kode;


    NæringsinntektType(String kode) {
        this.kode = kode;
    }

    @JsonCreator(mode = Mode.DELEGATING)
    public static NæringsinntektType fraKode(Object node) {
        if (node == null) {
            return null;
        }
        String kode = TempAvledeKode.getVerdi(NæringsinntektType.class, node, "kode");
        var ad = KODER.get(kode);
        if (ad == null) {
            throw new IllegalArgumentException("Ukjent NæringsinntektType: " + kode);
        }
        return ad;
    }

    public static Map<String, NæringsinntektType> kodeMap() {
        return Collections.unmodifiableMap(KODER);
    }

    @Override
    public String getKodeverk() {
        return KODEVERK;
    }

    @Override
    public String getKode() {
        return kode;
    }

}
