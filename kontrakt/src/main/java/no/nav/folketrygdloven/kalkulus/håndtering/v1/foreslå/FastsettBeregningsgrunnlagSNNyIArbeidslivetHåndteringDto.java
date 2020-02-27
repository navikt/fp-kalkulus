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
public class FastsettBeregningsgrunnlagSNNyIArbeidslivetHåndteringDto extends HåndterBeregningDto {

    public static final String IDENT_TYPE = "5049";

    @JsonProperty("fastsettBruttoBeregningsgrunnlagSNforNyIArbeidslivetDto")
    @Valid
    @NotNull
    private FastsettBruttoBeregningsgrunnlagSNforNyIArbeidslivetDto fastsettBruttoBeregningsgrunnlagSNforNyIArbeidslivetDto;

    public FastsettBeregningsgrunnlagSNNyIArbeidslivetHåndteringDto() {
        // default ctor
    }

    @Override
    public String getIdentType() {
        return IDENT_TYPE;
    }

    public FastsettBeregningsgrunnlagSNNyIArbeidslivetHåndteringDto(@Valid @NotNull FastsettBruttoBeregningsgrunnlagSNforNyIArbeidslivetDto fastsettBruttoBeregningsgrunnlagSNforNyIArbeidslivetDto) {
        this.fastsettBruttoBeregningsgrunnlagSNforNyIArbeidslivetDto = fastsettBruttoBeregningsgrunnlagSNforNyIArbeidslivetDto;
    }

    public FastsettBruttoBeregningsgrunnlagSNforNyIArbeidslivetDto getFastsettBruttoBeregningsgrunnlagSNforNyIArbeidslivetDto() {
        return fastsettBruttoBeregningsgrunnlagSNforNyIArbeidslivetDto;
    }
}
