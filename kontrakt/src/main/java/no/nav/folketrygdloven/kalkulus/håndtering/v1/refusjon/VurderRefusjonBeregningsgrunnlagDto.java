package no.nav.folketrygdloven.kalkulus.håndtering.v1.refusjon;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_ABSENT;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = NON_ABSENT, content = NON_EMPTY)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
public class VurderRefusjonBeregningsgrunnlagDto {

    @JsonProperty("fastsatteAndeler")
    @Valid
    @Size(min = 1)
    private List<VurderRefusjonAndelBeregningsgrunnlagDto> fastsatteAndeler;

    public VurderRefusjonBeregningsgrunnlagDto() {
        // For Json deserialisering
    }

    public VurderRefusjonBeregningsgrunnlagDto(@Valid @Size(min = 1) List<VurderRefusjonAndelBeregningsgrunnlagDto> fastsatteAndeler) {
        this.fastsatteAndeler = fastsatteAndeler;
    }

    public List<VurderRefusjonAndelBeregningsgrunnlagDto> getFastsatteAndeler() {
        return fastsatteAndeler;
    }
}
