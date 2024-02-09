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
import com.fasterxml.jackson.annotation.JsonValue;

/*
 * Alle ytelser som kalkulus forholder seg til i prosessen og som kan være innhentet i IAY-grunnlag
 */
@JsonAutoDetect(getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE, fieldVisibility = Visibility.ANY)
public enum YtelseType implements Kodeverdi, KontraktKode {

    /**
     * Folketrygdloven K4 ytelser.
     */
    DAGPENGER("DAG"),

    /**
     * Ny ytelse for kompenasasjon for koronatiltak for Selvstendig næringsdrivende og Frilansere (Anmodning 10).
     */
    FRISINN(KodeKonstanter.YT_FRISINN),

    /**
     * Folketrygdloven K8 ytelser.
     */
    SYKEPENGER("SP"),

    /**
     * Folketrygdloven K9 ytelser.
     */
    PLEIEPENGER_SYKT_BARN(KodeKonstanter.YT_PLEIEPENGER_SYKT_BARN),
    PLEIEPENGER_NÆRSTÅENDE(KodeKonstanter.YT_PLEIEPENGER_NÆRSTÅENDE),
    OMSORGSPENGER(KodeKonstanter.YT_OMSORGSPENGER),
    OPPLÆRINGSPENGER(KodeKonstanter.YT_OPPLÆRINGSPENGER),

    /**
     * Folketrygdloven K11 ytelser.
     */
    ARBEIDSAVKLARINGSPENGER("AAP"),

    /**
     * Folketrygdloven K14 ytelser.
     */
    ENGANGSTØNAD("ES"),
    FORELDREPENGER(KodeKonstanter.YT_FORELDREPENGER),
    SVANGERSKAPSPENGER(KodeKonstanter.YT_SVANGERSKAPSPENGER),

    /**
     * Folketrygdloven K15 ytelser.
     */
    ENSLIG_FORSØRGER("EF"),

    UDEFINERT(KodeKonstanter.UDEFINERT),
    ;

    private static final Map<String, YtelseType> KODER = new LinkedHashMap<>();

    private static final Set<YtelseType> ARENA_YTELSER = new HashSet<>(Arrays.asList(DAGPENGER,
            ARBEIDSAVKLARINGSPENGER));

    public static final Set<YtelseType> K9_YTELSER = Set.of(
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


    @JsonValue
    private final String kode;

    YtelseType(String kode) {
        this.kode = kode;
    }

    @JsonCreator(mode = Mode.DELEGATING)
    public static YtelseType fraKode(Object node) {
        if (node == null) {
            return null;
        }
        String kode = TempAvledeKode.getVerdi(YtelseType.class, node, "kode");
        var ad = KODER.get(kode);
        if (ad == null) {
            throw new IllegalArgumentException("Ukjent FagsakYtelseType: " + kode);
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
