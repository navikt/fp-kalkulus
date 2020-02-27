package no.nav.folketrygdloven.kalkulus.håndtering.v1.foreslå;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_ABSENT;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.folketrygdloven.kalkulus.håndtering.v1.HåndterBeregningDto;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = NON_ABSENT, content = NON_EMPTY)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
public class FastsettBruttoBeregningsgrunnlagSNHåndteringDto extends HåndterBeregningDto {

    public static final String IDENT_TYPE = "5042";

    @JsonProperty("fastsettBruttoBeregningsgrunnlagSNDto")
    @Valid
    @NotNull
    private FastsettBruttoBeregningsgrunnlagSNDto fastsettBruttoBeregningsgrunnlagSNDto;

    public FastsettBruttoBeregningsgrunnlagSNHåndteringDto() {
        // default ctor
    }

    @Override
    public String getIdentType() {
        return IDENT_TYPE;
    }

    public FastsettBruttoBeregningsgrunnlagSNHåndteringDto(@Valid @NotNull FastsettBruttoBeregningsgrunnlagSNDto fastsettBruttoBeregningsgrunnlagSNDto) {
        this.fastsettBruttoBeregningsgrunnlagSNDto = fastsettBruttoBeregningsgrunnlagSNDto;
    }

    public FastsettBruttoBeregningsgrunnlagSNDto getFastsettBruttoBeregningsgrunnlagSNDto() {
        return fastsettBruttoBeregningsgrunnlagSNDto;
    }

}
