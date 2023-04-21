package no.nav.folketrygdloven.kalkulus.kodeverk;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public enum LønnsinntektBeskrivelse implements Kodeverdi {
    KOMMUNAL_OMSORGSLOENN_OG_FOSTERHJEMSGODTGJOERELSE("KOMMUNAL_OMSORGSLOENN_OG_FOSTERHJEMSGODTGJOERELSE", "Kommunal omsorgslønn og fosterhjemsgodtgjørelse", "kommunalOmsorgsloennOgFosterhjemsgodtgjoerelse"),
    UDEFINERT("-", "Udefinert", null),
    ;
    public static final String KODEVERK = "LONNSINNTEKT_BESKRIVELSE";
    private static final Map<String, LønnsinntektBeskrivelse> KODER = new LinkedHashMap<>();

    static {
        for (var v : values()) {
            if (KODER.putIfAbsent(v.kode, v) != null) {
                throw new IllegalArgumentException("Duplikat : " + v.kode);
            }
        }
    }

    @JsonIgnore
    private String navn;

    @JsonIgnore
    private String offisiellKode;

    private String kode;


    private LønnsinntektBeskrivelse(String kode, String navn, String offisiellKode) {
        this.kode = kode;
        this.navn = navn;
        this.offisiellKode = offisiellKode;
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static LønnsinntektBeskrivelse fraKode(Object node) {
        if (node == null) {
            return null;
        }
        String kode = TempAvledeKode.getVerdi(LønnsinntektBeskrivelse.class, node, "kode");
        var ad = KODER.get(kode);
        if (ad == null) {
            throw new IllegalArgumentException("Ukjent LønnsinntektBeskrivelseType: " + kode);
        }
        return ad;
    }

    public static Map<String, LønnsinntektBeskrivelse> kodeMap() {
        return Collections.unmodifiableMap(KODER);
    }

    @Override
    public String getKodeverk() {
        return KODEVERK;
    }

    @JsonProperty
    @Override
    public String getKode() {
        return kode;
    }

}
