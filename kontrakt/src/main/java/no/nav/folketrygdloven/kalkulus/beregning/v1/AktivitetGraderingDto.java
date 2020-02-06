package no.nav.folketrygdloven.kalkulus.beregning.v1;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class AktivitetGraderingDto {

    @JsonProperty(value = "andelGraderingDto")
    @Valid
    @NotEmpty
    List<AndelGraderingDto> andelGraderingDto;

    @JsonCreator
    public AktivitetGraderingDto(@Valid @NotEmpty List<AndelGraderingDto> andelGraderingDto) {
        this.andelGraderingDto = andelGraderingDto;
    }

    public List<AndelGraderingDto> getAndelGraderingDto() {
        return andelGraderingDto;
    }
}
