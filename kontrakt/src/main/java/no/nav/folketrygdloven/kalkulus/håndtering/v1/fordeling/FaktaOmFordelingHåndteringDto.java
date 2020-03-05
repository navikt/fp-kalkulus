package no.nav.folketrygdloven.kalkulus.håndtering.v1.fordeling;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_ABSENT;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.folketrygdloven.kalkulus.håndtering.v1.HåndterBeregningDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.HåndteringKode;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = NON_ABSENT, content = NON_EMPTY)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
public class FaktaOmFordelingHåndteringDto extends HåndterBeregningDto {

    public static final String IDENT_TYPE = "5046";

    @JsonProperty("fordelBeregningsgrunnlagDto")
    @Valid
    @NotNull
    private FordelBeregningsgrunnlagDto fordelBeregningsgrunnlagDto;

    @JsonCreator
    public FaktaOmFordelingHåndteringDto(@Valid @NotNull FordelBeregningsgrunnlagDto fordelBeregningsgrunnlagDto) {
        super(new HåndteringKode(IDENT_TYPE));
        this.fordelBeregningsgrunnlagDto = fordelBeregningsgrunnlagDto;
    }

    @Override
    public String getIdentType() {
        return IDENT_TYPE;
    }

    public FordelBeregningsgrunnlagDto getFordelBeregningsgrunnlagDto() {
        return fordelBeregningsgrunnlagDto;
    }
}
