package no.nav.folketrygdloven.kalkulus.kodeverk;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonCreator.Mode;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonFormat(shape = Shape.OBJECT)
@JsonAutoDetect(getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE, fieldVisibility = Visibility.ANY)
public enum YtelseTyperKalkulusStøtterKontrakt implements Kodeverdi {

    /** Folketrygdloven K4 ytelser. */
    DAGPENGER("DAG", "Dagpenger"),

    /** Ytelse for kompenasasjon for koronatiltak for Selvstendig næringsdrivende og Frilansere (Anmodning 10). */
    FRISINN("FRISINN", "Frilans og selsvstendig næringsdrivende inntektskompensasjon"),

    /** Folketrygdloven K8 ytelser. */
    SYKEPENGER("SP", "Sykepenger"),

    /** Folketrygdloven K9 ytelser. */
    PLEIEPENGER_SYKT_BARN("PSB", "Pleiepenger sykt barn"),
    PLEIEPENGER_NÆRSTÅENDE("PPN", "Pleiepenger nærstående"),
    OMSORGSPENGER("OMP", "Omsorgspenger"),
    OPPLÆRINGSPENGER("OLP", "Opplæringspenger"),

    /** @deprecated Legacy infotrygd K9 ytelse type (må tolkes sammen med TemaUnderkategori). */
    PÅRØRENDESYKDOM("PS", "Pårørende sykdom"),

    /** Folketrygdloven K11 ytelser. */
    ARBEIDSAVKLARINGSPENGER("AAP"),

    /** Folketrygdloven K14 ytelser. */
    ENGANGSTØNAD("ES", "Engangsstønad"),
    FORELDREPENGER("FP", "Foreldrepenger"),
    SVANGERSKAPSPENGER("SVP", "Svangerskapspenger"),

    /** Folketrygdloven K15 ytelser. */
    ENSLIG_FORSØRGER("EF", "Enslig forsørger");

    public static final String KODEVERK = "FAGSAK_YTELSE_TYPE"; //$NON-NLS-1$

    private static final Map<String, YtelseTyperKalkulusStøtterKontrakt> KODER = new LinkedHashMap<>();

    static {
        for (var v : values()) {
            if (KODER.putIfAbsent(v.kode, v) != null) {
                throw new IllegalArgumentException("Duplikat : " + v.kode);
            }
        }
    }

    @JsonIgnore
    private String navn;

    private String kode;

    private YtelseTyperKalkulusStøtterKontrakt(String kode) {
        this.kode = kode;
    }

    private YtelseTyperKalkulusStøtterKontrakt(String kode, String navn) {
        this.kode = kode;
        this.navn = navn;
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

    public static Map<String, YtelseTyperKalkulusStøtterKontrakt> kodeMap() {
        return Collections.unmodifiableMap(KODER);
    }

    @JsonProperty
    @Override
    public String getKode() {
        return kode;
    }

    @JsonProperty
    @Override
    public String getKodeverk() {
        return KODEVERK;
    }
    
}
