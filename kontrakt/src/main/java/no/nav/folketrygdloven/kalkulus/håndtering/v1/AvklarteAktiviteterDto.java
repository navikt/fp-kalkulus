package no.nav.folketrygdloven.kalkulus.håndtering.v1;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AvklarteAktiviteterDto {

    @JsonProperty("beregningsaktivitetLagreDtoList")
    @Valid
    @NotNull
    private List<BeregningsaktivitetLagreDto> beregningsaktivitetLagreDtoList;

    public AvklarteAktiviteterDto(List<BeregningsaktivitetLagreDto> beregningsaktivitetLagreDtoList) { // NOSONAR
        this.beregningsaktivitetLagreDtoList = beregningsaktivitetLagreDtoList;
    }

    public List<BeregningsaktivitetLagreDto> getBeregningsaktivitetLagreDtoList() {
        return beregningsaktivitetLagreDtoList;
    }
}
