package no.nav.folketrygdloven.kalkulus.kodeverk;

import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonCreator.Mode;
import com.fasterxml.jackson.annotation.JsonValue;

@JsonAutoDetect(getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE, fieldVisibility = Visibility.ANY)
public enum YtelseTyperKalkulusStøtterKontrakt implements Kodeverdi, DatabaseKode, KontraktKode {

    /**
     * Folketrygdloven K4 ytelser.
     */
    DAGPENGER("DAG"),

    /**
     * Ytelse for kompenasasjon for koronatiltak for Selvstendig næringsdrivende og Frilansere (Anmodning 10).
     */
    FRISINN("FRISINN"),

    /**
     * Folketrygdloven K8 ytelser.
     */
    SYKEPENGER("SP"),

    /**
     * Folketrygdloven K9 ytelser.
     */
    PLEIEPENGER_SYKT_BARN("PSB"),
    PLEIEPENGER_NÆRSTÅENDE("PPN"),
    OMSORGSPENGER("OMP"),
    OPPLÆRINGSPENGER("OLP"),

    /**
     * @deprecated Legacy infotrygd K9 ytelse type (må tolkes sammen med TemaUnderkategori).
     */
    PÅRØRENDESYKDOM("PS"),

    /**
     * Folketrygdloven K11 ytelser.
     */
    ARBEIDSAVKLARINGSPENGER("AAP"),

    /**
     * Folketrygdloven K14 ytelser.
     */
    ENGANGSTØNAD("ES"),
    FORELDREPENGER("FP"),
    SVANGERSKAPSPENGER("SVP"),

    /**
     * Folketrygdloven K15 ytelser.
     */
    ENSLIG_FORSØRGER("EF");

    private static final Map<String, YtelseTyperKalkulusStøtterKontrakt> KODER = new LinkedHashMap<>();

    static {
        for (var v : values()) {
            if (KODER.putIfAbsent(v.kode, v) != null) {
                throw new IllegalArgumentException("Duplikat : " + v.kode);
            }
        }
    }

    @JsonValue
    private final String kode;

    YtelseTyperKalkulusStøtterKontrakt(String kode) {
        this.kode = kode;
    }

    @JsonCreator(mode = Mode.DELEGATING)
    public static YtelseTyperKalkulusStøtterKontrakt fraKode(Object node) {
        if (node == null) {
            return null;
        }
        String kode = TempAvledeKode.getVerdi(YtelseTyperKalkulusStøtterKontrakt.class, node, "kode");
        var ad = KODER.get(kode);
        if (ad == null) {
            throw new IllegalArgumentException("Ukjent YtelseTyperKalkulusStøtter: " + kode);
        }
        return ad;
    }

    @Override
    public String getKode() {
        return kode;
    }

    public static YtelseTyperKalkulusStøtterKontrakt fraDatabaseKode(String databaseKode) {
        return fraKode(databaseKode);
    }

}

