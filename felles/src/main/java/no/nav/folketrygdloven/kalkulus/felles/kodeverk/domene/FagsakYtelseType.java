package no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.folketrygdloven.kalkulus.felles.kodeverk.Kodeverdi;


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

    @JsonCreator
    public static FagsakYtelseType fraKode(@JsonProperty("kode") String kode) {
        if (kode == null) {
            return null;
        }
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

    public static void main(String[] args) {
        System.out.println(KODER.keySet());
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

    @Override
    public String getOffisiellKode() {
        return getKode();
    }

    @Override
    public String getNavn() {
        return navn;
    }

    public enum YtelseType {
        ES, FP, SVP;
    }

    @Converter(autoApply = true)
    public static class KodeverdiConverter implements AttributeConverter<FagsakYtelseType, String> {
        @Override
        public String convertToDatabaseColumn(FagsakYtelseType attribute) {
            return attribute == null ? null : attribute.getKode();
        }

        @Override
        public FagsakYtelseType convertToEntityAttribute(String dbData) {
            return dbData == null ? null : fraKode(dbData);
        }
    }

}
