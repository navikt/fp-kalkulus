package no.nav.folketrygdloven.kalkulus.kodeverk;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
public enum InntektsFilter implements Kodeverdi {

    BEREGNINGSGRUNNLAG("BEREGNINGSGRUNNLAG", "Beregningsgrunnlag", "8-28",
            InntektskildeType.INNTEKT_BEREGNING, InntektsFormål.FORMAAL_FORELDREPENGER),
    OPPTJENINGSGRUNNLAG("OPPTJENINGSGRUNNLAG", "Pensjonsgivende inntekt", "PensjonsgivendeA-Inntekt",
            InntektskildeType.INNTEKT_OPPTJENING, InntektsFormål.FORMAAL_PGI),
    SAMMENLIGNINGSGRUNNLAG("SAMMENLIGNINGSGRUNNLAG", "Sammenligningsgrunnlag", "8-30",
            InntektskildeType.INNTEKT_SAMMENLIGNING, InntektsFormål.FORMAAL_FORELDREPENGER),
    UDEFINERT("-", "Ikke definert", null,
            InntektskildeType.UDEFINERT, InntektsFormål.UDEFINERT),
            ;

    private static final Map<InntektsFilter, InntektsFormål> INNTEKTSFILTER_TIL_INNTEKTSFORMÅL = new LinkedHashMap<>();
    private static final Map<InntektskildeType, InntektsFilter> INNTEKTSKILDE_TIL_INNTEKTSFILTER = new LinkedHashMap<>();

    private static final Map<String, InntektsFilter> KODER = new LinkedHashMap<>();

    static {
        for (var v : values()) {
            if (KODER.putIfAbsent(v.kode, v) != null) {
                throw new IllegalArgumentException("Duplikat : " + v.kode);
            }
        }

        List.of(values()).stream()
            .forEach(v -> INNTEKTSKILDE_TIL_INNTEKTSFILTER.putIfAbsent(v.getInntektsKilde(), v));

        List.of(values()).stream()
            .forEach(v -> INNTEKTSFILTER_TIL_INNTEKTSFORMÅL.putIfAbsent(v, v.getInntektsFormål()));

    }

    @JsonIgnore
    private InntektsFormål inntektsFormål;
    @JsonIgnore
    private InntektskildeType inntektsKilde;

    private String kode;

    @JsonIgnore
    private String navn;

    @JsonIgnore
    private String offisiellKode;

    private InntektsFilter(String kode) {
        this.kode = kode;
    }

    private InntektsFilter(String kode, String navn, String offisiellKode, InntektskildeType inntektsKilde, InntektsFormål inntektsFormål) {
        this.kode = kode;
        this.navn = navn;
        this.offisiellKode = offisiellKode;
        this.inntektsKilde = inntektsKilde;
        this.inntektsFormål = inntektsFormål;
    }

    public static InntektsFilter finnForKodeverkEiersKode(String offisiellDokumentType) {
        return List.of(values()).stream().filter(k -> Objects.equals(k.offisiellKode, offisiellDokumentType)).findFirst().orElse(UDEFINERT);
    }

    @JsonCreator(mode = Mode.DELEGATING)
    public static InntektsFilter fraKode(Object node) {
        if (node == null) {
            return null;
        }
        String kode = TempAvledeKode.getVerdi(InntektsFilter.class, node, "kode");
        var ad = KODER.get(kode);
        if (ad == null) {
            throw new IllegalArgumentException("Ukjent InntektsFilter: " + kode);
        }
        return ad;
    }

    public static Map<String, InntektsFilter> kodeMap() {
        return Collections.unmodifiableMap(KODER);
    }

    public static Map<InntektsFilter, InntektsFormål> mapInntektsFilterTilFormål() {
        return Collections.unmodifiableMap(INNTEKTSFILTER_TIL_INNTEKTSFORMÅL);
    }

    public static Map<InntektskildeType, InntektsFilter> mapInntektsKildeTilFilter() {
        return Collections.unmodifiableMap(INNTEKTSKILDE_TIL_INNTEKTSFILTER);
    }

    public InntektsFormål getInntektsFormål() {
        return inntektsFormål;
    }

    @JsonProperty
    @Override
    public String getKode() {
        return kode;
    }
    
    @JsonProperty
    @Override
    public String getKodeverk() {
        return "INNTEKTSFILTER";
    }

    private InntektskildeType getInntektsKilde() {
        return inntektsKilde;
    }

}
