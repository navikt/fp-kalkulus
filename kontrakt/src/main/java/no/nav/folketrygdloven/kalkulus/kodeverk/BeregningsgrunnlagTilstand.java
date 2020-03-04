package no.nav.folketrygdloven.kalkulus.kodeverk;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_ABSENT;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

import java.util.Objects;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = NON_ABSENT, content = NON_EMPTY)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
public class BeregningsgrunnlagTilstand extends Kodeverk{
    static final String KODEVERK = "BEREGNINGSGRUNNLAG_TILSTAND";

    public static final BeregningsgrunnlagTilstand OPPRETTET = new BeregningsgrunnlagTilstand("OPPRETTET");
    public static final BeregningsgrunnlagTilstand FASTSATT_BEREGNINGSAKTIVITETER = new BeregningsgrunnlagTilstand("FASTSATT_BEREGNINGSAKTIVITETER");
    public static final BeregningsgrunnlagTilstand OPPDATERT_MED_ANDELER = new BeregningsgrunnlagTilstand("OPPDATERT_MED_ANDELER");
    public static final BeregningsgrunnlagTilstand KOFAKBER_UT = new BeregningsgrunnlagTilstand("KOFAKBER_UT");
    public static final BeregningsgrunnlagTilstand FORESLÅTT = new BeregningsgrunnlagTilstand("FORESLÅTT");
    public static final BeregningsgrunnlagTilstand FORESLÅTT_UT = new BeregningsgrunnlagTilstand("FORESLÅTT_UT");
    public static final BeregningsgrunnlagTilstand OPPDATERT_MED_REFUSJON_OG_GRADERING = new BeregningsgrunnlagTilstand("OPPDATERT_MED_REFUSJON_OG_GRADERING");
    public static final BeregningsgrunnlagTilstand FASTSATT_INN = new BeregningsgrunnlagTilstand("FASTSATT_INN");
    public static final BeregningsgrunnlagTilstand FASTSATT = new BeregningsgrunnlagTilstand("FASTSATT");
    public static final BeregningsgrunnlagTilstand UDEFINERT = new BeregningsgrunnlagTilstand("-");

    @JsonProperty(value = "kode", required = true, index = 1)
    @Pattern(regexp = "^[\\p{L}\\p{N}_\\.\\-]+$", message="Kode '${validatedValue}' matcher ikke tillatt pattern '{regexp}'")
    @Size(min = 2, max = 50)
    @NotNull
    private String kode;

    @JsonCreator
    public BeregningsgrunnlagTilstand(@JsonProperty(value = "kode", required = true) String kode) {
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
