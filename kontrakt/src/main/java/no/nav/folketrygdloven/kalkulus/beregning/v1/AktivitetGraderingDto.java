package no.nav.folketrygdloven.kalkulus.beregning.v1;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
public class AktivitetGraderingDto {

    @JsonProperty(value = "andelGraderingDto")
    @Valid
    @NotEmpty
    private List<AndelGraderingDto> andelGraderingDto;

    protected AktivitetGraderingDto() {
        // default ctor
    }

    public AktivitetGraderingDto(@Valid @NotEmpty List<AndelGraderingDto> andelGraderingDto) {
        this.andelGraderingDto = andelGraderingDto;
    }

    public List<AndelGraderingDto> getAndelGraderingDto() {
        return andelGraderingDto;
    }
}
