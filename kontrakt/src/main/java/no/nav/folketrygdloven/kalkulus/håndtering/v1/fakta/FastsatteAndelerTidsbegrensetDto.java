package no.nav.folketrygdloven.kalkulus.håndtering.v1.fakta;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

public class FastsatteAndelerTidsbegrensetDto {

    @JsonProperty("andelsnr")
    @Valid
    @NotNull
    private Long andelsnr;

    @JsonProperty("bruttoFastsattInntekt")
    @Valid
    @NotNull
    private Integer bruttoFastsattInntekt;

    public FastsatteAndelerTidsbegrensetDto(@Valid @NotNull Long andelsnr, @Valid @NotNull Integer bruttoFastsattInntekt) {
        this.andelsnr = andelsnr;
        this.bruttoFastsattInntekt = bruttoFastsattInntekt;
    }

    public Long getAndelsnr() { return andelsnr; }

    public Integer getBruttoFastsattInntekt() {
        return bruttoFastsattInntekt;
    }

}
