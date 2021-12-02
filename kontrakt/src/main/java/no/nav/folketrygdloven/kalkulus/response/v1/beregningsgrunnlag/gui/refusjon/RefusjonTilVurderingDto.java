package no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.refusjon;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import java.util.List;
import java.util.Objects;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = JsonInclude.Include.NON_ABSENT, content = JsonInclude.Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = NONE, setterVisibility = NONE, isGetterVisibility = NONE, creatorVisibility = NONE)
public class RefusjonTilVurderingDto {

    @Valid
    @JsonProperty("andeler")
    @Size(min = 1)
    @NotNull
    private List<RefusjonAndelTilVurderingDto> andeler;

    public RefusjonTilVurderingDto() {
    }

    public RefusjonTilVurderingDto(@Valid @Size(min = 1) @NotNull List<RefusjonAndelTilVurderingDto> andeler) {
        this.andeler = andeler;
    }

    public List<RefusjonAndelTilVurderingDto> getAndeler() {
        return andeler;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RefusjonTilVurderingDto that = (RefusjonTilVurderingDto) o;
        return Objects.equals(andeler, that.andeler);
    }

    @Override
    public int hashCode() {
        return Objects.hash(andeler);
    }
}
