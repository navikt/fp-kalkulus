package no.nav.folketrygdloven.kalkulator.ytelse.frisinn;

import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.folketrygdloven.kalkulus.felles.kodeverk.Kodeverdi;

public enum FrisinnBehandlingType implements Kodeverdi {

    REVURDERING("REVURDERING", "Revurdering"),
    NY_SØKNADSPERIODE("NY_SØKNADSPERIODE", "Ny søknadsperiode ");

    public static final String KODEVERK = "FRISINN_BEHANDLING_TYPE";

    private static final Map<String, FrisinnBehandlingType> KODER = new LinkedHashMap<>();

    static {
        for (var v : values()) {
            if (KODER.putIfAbsent(v.kode, v) != null) {
                throw new IllegalArgumentException("Duplikat : " + v.kode);
            }
        }
    }

    private String kode;
    private String navn;

    FrisinnBehandlingType(String kode, String navn) {
        this.kode = kode;
        this.navn = navn;
    }

    @Override
    public String getKode() {
        return kode;
    }

    @Override
    public String getOffisiellKode() {
        return null;
    }

    @Override
    public String getKodeverk() {
        return KODEVERK;
    }

    @Override
    public String getNavn() {
        return navn;
    }



    @JsonCreator
    public static FrisinnBehandlingType fraKode(@JsonProperty("kode") String kode) {
        if (kode == null) {
            return null;
        }
        var ad = KODER.get(kode);
        if (ad == null) {
            throw new IllegalArgumentException("Ukjent ArbeidType: " + kode);
        }
        return ad;
    }


}
