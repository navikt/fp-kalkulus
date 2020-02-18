package no.nav.folketrygdloven.kalkulus.håndtering.v1.fakta;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.folketrygdloven.kalkulus.kodeverk.Inntektskategori;

public class FastsatteVerdierForBesteberegningDto {

    @JsonProperty("fastsattBeløp")
    @Valid
    @NotNull
    private Integer fastsattBeløp;

    @JsonProperty("inntektskategori")
    @Valid
    private Inntektskategori inntektskategori;

    public FastsatteVerdierForBesteberegningDto(@Valid @NotNull Integer fastsattBeløp, @Valid Inntektskategori inntektskategori) {
        this.fastsattBeløp = fastsattBeløp;
        this.inntektskategori = inntektskategori;
    }

    public Integer getFastsattBeløp() {
        return fastsattBeløp;
    }

    public Inntektskategori getInntektskategori() {
        return inntektskategori;
    }

}
