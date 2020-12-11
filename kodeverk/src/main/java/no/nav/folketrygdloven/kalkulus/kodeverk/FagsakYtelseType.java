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
public enum FagsakYtelseType implements Kodeverdi {

    /**
     * Folketrygdloven K4 ytelser.
     */
    DAGPENGER("DAG", "Dagpenger", "DAGPENGER"),

    /** Ny ytelse for kompenasasjon for koronatiltak for Selvstendig næringsdrivende og Frilansere (Anmodning 10). */
    FRISINN("FRISINN", "FRIlansere og Selstendig næringsdrivendes INNtektskompensasjon", "FRISINN"),

    /**
     * Folketrygdloven K8 ytelser.
     */
    SYKEPENGER("SP", "Sykepenger", "SYKEPENGER"),

    /**
     * Folketrygdloven K9 ytelser.
     */
    PLEIEPENGER_SYKT_BARN("PSB", "Pleiepenger sykt barn", "PSB"),
    PLEIEPENGER_NÆRSTÅENDE("PPN", "Pleiepenger nærstående", "PPN"),
    OMSORGSPENGER("OMP", "Omsorgspenger", "OMP"),
    OPPLÆRINGSPENGER("OLP", "Opplæringspenger", "OLP"),

    /**
     * @deprecated Gammel infotrygd kode for K9 ytelser. Må tolkes om til ovenstående sammen med TemaUnderkategori.
     */
    @Deprecated
    PÅRØRENDESYKDOM("PS", "Pårørende sykdom", "PÅRØRENDESYKDOM"),

    /**
     * Folketrygdloven K11 ytelser.
     */
    ARBEIDSAVKLARINGSPENGER("AAP", "Arbeidsavklaringspenger", "ARBEIDSAVKLARINGSPENGER"),

    /**
     * Folketrygdloven K14 ytelser.
     */
    ENGANGSTØNAD("ES", "Engangsstønad", "ENGANGSSTØNAD"),
    FORELDREPENGER("FP", "Foreldrepenger", "FORELDREPENGER"),
    SVANGERSKAPSPENGER("SVP", "Svangerskapspenger", "SVANGERSKAPSPENGER"),

    /**
     * Folketrygdloven K15 ytelser.
     */
    ENSLIG_FORSØRGER("EF", "Enslig forsørger", "ENSLIG_FORSØRGER"),

    UDEFINERT("-", "Ikke definert"),
    ;

    public static final String KODEVERK = "FAGSAK_YTELSE"; //$NON-NLS-1$

    private static final Map<String, FagsakYtelseType> KODER_FPSAK = new LinkedHashMap<>();

    private static final Map<String, FagsakYtelseType> KODER = new LinkedHashMap<>();

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
    private String navn;
    
    @JsonIgnore
    private String fpsakKode;
    
    private String kode;

    private FagsakYtelseType(String kode) {
        this.kode = kode;
    }

    private FagsakYtelseType(String kode, String navn) {
        this.kode = kode;
        this.navn = navn;
    }

    private FagsakYtelseType(String kode, String navn, String fpsakKode) {
        this.kode = kode;
        this.navn = navn;
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

    public static Map<String, FagsakYtelseType> kodeMap() {
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
