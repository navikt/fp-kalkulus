package no.nav.folketrygdloven.kalkulus.håndtering.v1.foreslå;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_ABSENT;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import no.nav.folketrygdloven.kalkulus.håndtering.v1.HåndterBeregningDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.AvklaringsbehovDefinisjon;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = NON_ABSENT, content = NON_EMPTY)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
public class FastsettBruttoBeregningsgrunnlagSNHåndteringDto extends HåndterBeregningDto implements FastsettBruttoBeregningsgrunnlag {

    public static final String AVKLARINGSBEHOV_KODE = "FASTSETT_BG_SN";

    @JsonProperty("fastsettBruttoBeregningsgrunnlagSNDto")
    @Valid
    @NotNull
    private FastsettBruttoBeregningsgrunnlagDto fastsettBruttoBeregningsgrunnlagSNDto;

    @JsonCreator
    public FastsettBruttoBeregningsgrunnlagSNHåndteringDto(@JsonProperty("fastsettBruttoBeregningsgrunnlagSNDto") @Valid @NotNull FastsettBruttoBeregningsgrunnlagDto fastsettBruttoBeregningsgrunnlagSNDto) {
        super(AvklaringsbehovDefinisjon.FASTSETT_BG_SN);
        this.fastsettBruttoBeregningsgrunnlagSNDto = fastsettBruttoBeregningsgrunnlagSNDto;
    }

    @Override
    public String getAvklaringsbehovKode() {
        return AVKLARINGSBEHOV_KODE;
    }

    public FastsettBruttoBeregningsgrunnlagDto getFastsettBruttoBeregningsgrunnlagSNDto() {
        return fastsettBruttoBeregningsgrunnlagSNDto;
    }

    @Override
    public Integer getBruttoBeregningsgrunnlag() {
        return fastsettBruttoBeregningsgrunnlagSNDto.getBruttoBeregningsgrunnlag();
    }
}
