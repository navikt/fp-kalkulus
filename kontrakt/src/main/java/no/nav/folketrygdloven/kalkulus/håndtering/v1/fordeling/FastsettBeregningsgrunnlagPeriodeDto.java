package no.nav.folketrygdloven.kalkulus.håndtering.v1.fordeling;

import java.time.LocalDate;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

public class FastsettBeregningsgrunnlagPeriodeDto {

    @JsonProperty("andeler")
    @Valid
    @NotNull
    private List<FastsettBeregningsgrunnlagAndelDto> andeler;

    @JsonProperty("fom")
    @Valid
    @NotNull
    private LocalDate fom;

    @JsonProperty("tom")
    @Valid
    @NotNull
    private LocalDate tom;


    public FastsettBeregningsgrunnlagPeriodeDto(List<FastsettBeregningsgrunnlagAndelDto> andeler, LocalDate fom, LocalDate tom) { // NOSONAR
        this.andeler = andeler;
        this.fom = fom;
        this.tom = tom;
    }

    public LocalDate getFom() {
        return fom;
    }

    public LocalDate getTom() {
        return tom;
    }

    public List<FastsettBeregningsgrunnlagAndelDto> getAndeler() {
        return andeler;
    }

}
