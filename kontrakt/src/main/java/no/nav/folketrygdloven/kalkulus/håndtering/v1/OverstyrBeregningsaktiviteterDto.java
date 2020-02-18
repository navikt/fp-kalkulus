package no.nav.folketrygdloven.kalkulus.håndtering.v1;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.Size;

public class OverstyrBeregningsaktiviteterDto extends HåndterBeregningDto {

    @Valid
    @Size(max = 1000)
    private List<BeregningsaktivitetLagreDto> beregningsaktivitetLagreDtoList;

    public OverstyrBeregningsaktiviteterDto(@Valid @Size(max = 1000) List<BeregningsaktivitetLagreDto> beregningsaktivitetLagreDtoList) {
        this.beregningsaktivitetLagreDtoList = beregningsaktivitetLagreDtoList;
    }

    public List<BeregningsaktivitetLagreDto> getBeregningsaktivitetLagreDtoList() {
        return beregningsaktivitetLagreDtoList;
    }
}
