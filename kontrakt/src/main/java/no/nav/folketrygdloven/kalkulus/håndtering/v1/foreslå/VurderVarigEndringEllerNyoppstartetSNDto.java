package no.nav.folketrygdloven.kalkulus.håndtering.v1.foreslå;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_ABSENT;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = NON_ABSENT, content = NON_EMPTY)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
public class VurderVarigEndringEllerNyoppstartetSNDto {

    @JsonProperty("erVarigEndretNaering")
    @Valid
    @NotNull
    private boolean erVarigEndretNaering;

    @JsonProperty("bruttoBeregningsgrunnlag")
    @Valid
    @NotNull
    @Min(0)
    @Max(Long.MAX_VALUE)
    private Integer bruttoBeregningsgrunnlag;

    public VurderVarigEndringEllerNyoppstartetSNDto() {
        // For Json deserialisering
    }

    public VurderVarigEndringEllerNyoppstartetSNDto(@Valid @NotNull boolean erVarigEndretNaering, @Valid @NotNull @Min(0) @Max(Long.MAX_VALUE) Integer bruttoBeregningsgrunnlag) {
        this.erVarigEndretNaering = erVarigEndretNaering;
        this.bruttoBeregningsgrunnlag = bruttoBeregningsgrunnlag;
    }

    public boolean getErVarigEndretNaering() {
        return erVarigEndretNaering;
    }

    public Integer getBruttoBeregningsgrunnlag() {
        return bruttoBeregningsgrunnlag;
    }
}
