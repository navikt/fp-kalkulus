package no.nav.folketrygdloven.kalkulus.håndtering.v1.fakta;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

public class VurderATogFLiSammeOrganisasjonAndelDto {

    @JsonProperty("andelsnr")
    @Valid
    @NotNull
    private Long andelsnr;

    @JsonProperty("arbeidsinntekt")
    @Valid
    @NotNull
    private Integer arbeidsinntekt;

    public VurderATogFLiSammeOrganisasjonAndelDto(@Valid @NotNull Long andelsnr, @Valid @NotNull Integer arbeidsinntekt) {
        this.andelsnr = andelsnr;
        this.arbeidsinntekt = arbeidsinntekt;
    }

    public Long getAndelsnr() {
        return andelsnr;
    }

    public Integer getArbeidsinntekt() {
        return arbeidsinntekt;
    }
}
