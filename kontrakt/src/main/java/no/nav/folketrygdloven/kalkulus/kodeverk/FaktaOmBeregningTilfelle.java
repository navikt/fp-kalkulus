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
public class FaktaOmBeregningTilfelle extends Kodeverk{
    static final String KODEVERK = "FAKTA_OM_BEREGNING_TILFELLE";

    public static final FaktaOmBeregningTilfelle VURDER_TIDSBEGRENSET_ARBEIDSFORHOLD = new FaktaOmBeregningTilfelle("VURDER_TIDSBEGRENSET_ARBEIDSFORHOLD");
    public static final FaktaOmBeregningTilfelle VURDER_SN_NY_I_ARBEIDSLIVET = new FaktaOmBeregningTilfelle("VURDER_SN_NY_I_ARBEIDSLIVET");
    public static final FaktaOmBeregningTilfelle VURDER_NYOPPSTARTET_FL = new FaktaOmBeregningTilfelle("VURDER_NYOPPSTARTET_FL");
    public static final FaktaOmBeregningTilfelle FASTSETT_MAANEDSINNTEKT_FL = new FaktaOmBeregningTilfelle("FASTSETT_MAANEDSINNTEKT_FL");
    public static final FaktaOmBeregningTilfelle FASTSETT_BG_ARBEIDSTAKER_UTEN_INNTEKTSMELDING = new FaktaOmBeregningTilfelle("FASTSETT_BG_ARBEIDSTAKER_UTEN_INNTEKTSMELDING");
    public static final FaktaOmBeregningTilfelle VURDER_LØNNSENDRING = new FaktaOmBeregningTilfelle("VURDER_LØNNSENDRING");
    public static final FaktaOmBeregningTilfelle FASTSETT_MÅNEDSLØNN_ARBEIDSTAKER_UTEN_INNTEKTSMELDING = new FaktaOmBeregningTilfelle("FASTSETT_MÅNEDSLØNN_ARBEIDSTAKER_UTEN_INNTEKTSMELDING");
    public static final FaktaOmBeregningTilfelle VURDER_AT_OG_FL_I_SAMME_ORGANISASJON = new FaktaOmBeregningTilfelle("VURDER_AT_OG_FL_I_SAMME_ORGANISASJON");
    public static final FaktaOmBeregningTilfelle FASTSETT_BESTEBEREGNING_FØDENDE_KVINNE = new FaktaOmBeregningTilfelle("FASTSETT_BESTEBEREGNING_FØDENDE_KVINNE");
    public static final FaktaOmBeregningTilfelle VURDER_ETTERLØNN_SLUTTPAKKE = new FaktaOmBeregningTilfelle("VURDER_ETTERLØNN_SLUTTPAKKE");
    public static final FaktaOmBeregningTilfelle FASTSETT_ETTERLØNN_SLUTTPAKKE = new FaktaOmBeregningTilfelle("FASTSETT_ETTERLØNN_SLUTTPAKKE");
    public static final FaktaOmBeregningTilfelle VURDER_MOTTAR_YTELSE = new FaktaOmBeregningTilfelle("VURDER_MOTTAR_YTELSE");
    public static final FaktaOmBeregningTilfelle VURDER_BESTEBEREGNING = new FaktaOmBeregningTilfelle("VURDER_BESTEBEREGNING");
    public static final FaktaOmBeregningTilfelle VURDER_MILITÆR_SIVILTJENESTE = new FaktaOmBeregningTilfelle("VURDER_MILITÆR_SIVILTJENESTE");
    public static final FaktaOmBeregningTilfelle VURDER_REFUSJONSKRAV_SOM_HAR_KOMMET_FOR_SENT = new FaktaOmBeregningTilfelle("VURDER_REFUSJONSKRAV_SOM_HAR_KOMMET_FOR_SENT");
    public static final FaktaOmBeregningTilfelle FASTSETT_BG_KUN_YTELSE = new FaktaOmBeregningTilfelle("FASTSETT_BG_KUN_YTELSE");
    public static final FaktaOmBeregningTilfelle FASTSETT_ENDRET_BEREGNINGSGRUNNLAG = new FaktaOmBeregningTilfelle("FASTSETT_ENDRET_BEREGNINGSGRUNNLAG");

    @JsonProperty(value = "kode", required = true, index = 1)
    @Pattern(regexp = "^[\\p{L}\\p{N}_\\.\\-]+$", message="Kode '${validatedValue}' matcher ikke tillatt pattern '{regexp}'")
    @Size(min = 3, max = 100)
    @NotNull
    private String kode;

    @JsonCreator
    public FaktaOmBeregningTilfelle(@JsonProperty(value = "kode", required = true) String kode) {
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
