package no.nav.folketrygdloven.kalkulus.kodeverk;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_ABSENT;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = NON_ABSENT, content = NON_EMPTY)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
public class StegType extends Kodeverk{
    static final String KODEVERK = "STEG_TYPE";

    public static final StegType FASTSETT_STP_BER = new StegType("FASTSETT_STP_BER");
    public static final StegType KOFAKBER = new StegType("KOFAKBER");
    public static final StegType FORS_BESTEBEREGNING = new StegType("FORS_BESTEBEREGNING");
    public static final StegType FORS_BERGRUNN = new StegType("FORS_BERGRUNN");
    public static final StegType FORS_BERGRUNN_2 = new StegType("FORS_BERGRUNN_2");
    public static final StegType VURDER_VILKAR_BERGRUNN = new StegType("VURDER_VILKAR_BERGRUNN");
    public static final StegType VURDER_REF_BERGRUNN = new StegType("VURDER_REF_BERGRUNN");
    public static final StegType FORDEL_BERGRUNN = new StegType("FORDEL_BERGRUNN");
    public static final StegType FAST_BERGRUNN = new StegType("FAST_BERGRUNN");

    @JsonProperty(value = "kode", required = true, index = 1)
    @Pattern(regexp = "^[\\p{L}\\p{N}_\\.\\-]+$", message="Kode '${validatedValue}' matcher ikke tillatt pattern '{regexp}'")
    @Size(min = 3, max = 50)
    @NotNull
    private String kode;

    @JsonCreator
    public StegType(@JsonProperty(value = "kode", required = true) String kode) {
        Objects.requireNonNull(kode, "kode");
        this.kode = kode;
    }

    @Override
    public String getKode() {
        return kode;
    }

    @Override
    public String getKodeverk() {
        return KODEVERK;
    }
}
