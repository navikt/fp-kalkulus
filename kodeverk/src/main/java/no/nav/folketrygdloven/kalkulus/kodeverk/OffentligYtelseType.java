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
public enum OffentligYtelseType implements Kodeverdi, YtelseType {

    UDEFINERT("-"),
    AAP("AAP"),
    DAGPENGER_FISKER("DAGPENGER_FISKER"),
    DAGPENGER_ARBEIDSLØS("DAGPENGER_ARBEIDSLØS"),
    DAGPENGER("DAGPENGER"),
    FORELDREPENGER("FORELDREPENGER"),
    OVERGANGSSTØNAD_ENSLIG("OVERGANGSSTØNAD_ENSLIG"),
    SVANGERSKAPSPENGER("SVANGERSKAPSPENGER"),
    SYKEPENGER("SYKEPENGER"),
    SYKEPENGER_FISKER("SYKEPENGER_FISKER"),
    UFØRETRYGD("UFØRETRYGD"),
    UFØRETRYGD_ETTEROPPGJØR("UFØRETRYGD_ETTEROPPGJØR"),
    UNDERHOLDNINGSBIDRAG_BARN("UNDERHOLDNINGSBIDRAG_BARN"),
    VENTELØNN("VENTELØNN"),
    ;

    private static final Map<String, OffentligYtelseType> KODER = new LinkedHashMap<>();

    public static final String KODEVERK = "YTELSE_FRA_OFFENTLIGE";

    static {
        for (var v : values()) {
            if (KODER.putIfAbsent(v.kode, v) != null) {
                throw new IllegalArgumentException("Duplikat : " + v.kode);
            }
        }
    }

    @JsonValue
    private final String kode;


    OffentligYtelseType(String kode) {
        this.kode = kode;
    }

    @JsonCreator(mode = Mode.DELEGATING)
    public static OffentligYtelseType fraKode(Object node) {
        if (node == null) {
            return null;
        }
        String kode = TempAvledeKode.getVerdi(OffentligYtelseType.class, node, "kode");
        var ad = KODER.get(kode);
        if (ad == null) {
            throw new IllegalArgumentException("Ukjent OffentligYtelseType: " + kode);
        }
        return ad;
    }

    public static Map<String, OffentligYtelseType> kodeMap() {
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
