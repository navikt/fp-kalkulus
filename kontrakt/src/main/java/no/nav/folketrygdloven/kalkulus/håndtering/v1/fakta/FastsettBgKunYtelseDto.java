package no.nav.folketrygdloven.kalkulus.håndtering.v1.fakta;

import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

public class FastsettBgKunYtelseDto {

    @JsonProperty("andeler")
    @Valid
    @NotNull
    private List<FastsattBrukersAndel> andeler;

    @JsonProperty("kunYtelseFordeling")
    @Valid
    private Boolean skalBrukeBesteberegning;

    public FastsettBgKunYtelseDto(List<FastsattBrukersAndel> andeler, Boolean skalBrukeBesteberegning) { // NOSONAR
        this.andeler = new ArrayList<>(andeler);
        this.skalBrukeBesteberegning = skalBrukeBesteberegning;
    }

    public List<FastsattBrukersAndel> getAndeler() {
        return andeler;
    }

    public Boolean getSkalBrukeBesteberegning() {
        return skalBrukeBesteberegning;
    }
}
