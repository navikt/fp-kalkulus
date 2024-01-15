package no.nav.folketrygdloven.kalkulus.kodeverk;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonCreator.Mode;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonValue;


@JsonAutoDetect(getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE, fieldVisibility = Visibility.ANY)
public enum FagsakYtelseType implements Kodeverdi {

    /**
     * Folketrygdloven K4 ytelser.
     */
    DAGPENGER("DAG", "DAGPENGER"),

    /**
     * Ny ytelse for kompenasasjon for koronatiltak for Selvstendig næringsdrivende og Frilansere (Anmodning 10).
     */
    FRISINN("FRISINN"),

    /**
     * Folketrygdloven K8 ytelser.
     */
    SYKEPENGER("SP", "SYKEPENGER"),

    /**
     * Folketrygdloven K9 ytelser.
     */
    PLEIEPENGER_SYKT_BARN("PSB"),
    PLEIEPENGER_NÆRSTÅENDE("PPN"),
    OMSORGSPENGER("OMP"),
    OPPLÆRINGSPENGER("OLP"),

    /**
     * Folketrygdloven K11 ytelser.
     */
    ARBEIDSAVKLARINGSPENGER("AAP", "ARBEIDSAVKLARINGSPENGER"),

    /**
     * Folketrygdloven K14 ytelser.
     */
    ENGANGSTØNAD("ES", "ENGANGSSTØNAD"),
    FORELDREPENGER("FP", "FORELDREPENGER"),
    SVANGERSKAPSPENGER("SVP", "SVANGERSKAPSPENGER"),

    /**
     * Folketrygdloven K15 ytelser.
     */
    ENSLIG_FORSØRGER("EF", "ENSLIG_FORSØRGER"),

    UDEFINERT("-", "Ikke definert"),
    ;

    private static final Map<String, FagsakYtelseType> KODER_FPSAK = new LinkedHashMap<>();

    private static final Map<String, FagsakYtelseType> KODER = new LinkedHashMap<>();

    private static final Set<FagsakYtelseType> ARENA_YTELSER = new HashSet<>(Arrays.asList(DAGPENGER,
            ARBEIDSAVKLARINGSPENGER));

    public static final Set<FagsakYtelseType> K9_YTELSER = Set.of(
            OMSORGSPENGER,
            PLEIEPENGER_SYKT_BARN,
            PLEIEPENGER_NÆRSTÅENDE,
            OPPLÆRINGSPENGER);

    static {
        for (var v : values()) {
            if (KODER.putIfAbsent(v.kode, v) != null) {
                throw new IllegalArgumentException("Duplikat : " + v.kode);
            }
        }
    }

    static {
        for (var v : values()) {
            if (v.fpsakKode != null && KODER_FPSAK.putIfAbsent(v.fpsakKode, v) != null) {
                throw new IllegalArgumentException("Duplikat : " + v.kode);
            }
        }
    }

    @JsonIgnore
    private final String fpsakKode;
    @JsonValue
    private final String kode;

    FagsakYtelseType(String kode) {
        this(kode, kode);
    }

    FagsakYtelseType(String kode, String fpsakKode) {
        this.kode = kode;
        this.fpsakKode = fpsakKode;
    }

    @JsonCreator(mode = Mode.DELEGATING)
    public static FagsakYtelseType fraKode(Object node) {
        if (node == null) {
            return null;
        }
        String kode = TempAvledeKode.getVerdi(FagsakYtelseType.class, node, "kode");
        var ad = KODER.get(kode);
        if (ad == null) {
            // håndter diff mellom k9 og fpsak
            ad = KODER_FPSAK.get(kode);
            if (ad == null) {
                throw new IllegalArgumentException("Ukjent FagsakYtelseType: " + kode);
            }
        }
        return ad;
    }

    public boolean erArenaytelse() {
        return ARENA_YTELSER.contains(this);
    }

    @Override
    public String getKode() {
        return kode;
    }


}
