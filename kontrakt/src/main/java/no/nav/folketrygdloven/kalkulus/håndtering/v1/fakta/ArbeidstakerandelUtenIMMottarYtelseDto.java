package no.nav.folketrygdloven.kalkulus.håndtering.v1.fakta;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ArbeidstakerandelUtenIMMottarYtelseDto {

    @JsonProperty("andelsnr")
    @Valid
    @NotNull
    private long andelsnr;

    @JsonProperty("mottarYtelse")
    @Valid
    @NotNull
    private Boolean mottarYtelse;

    public ArbeidstakerandelUtenIMMottarYtelseDto(long andelsnr, Boolean mottarYtelse) {
        this.andelsnr = andelsnr;
        this.mottarYtelse = mottarYtelse;
    }

    public long getAndelsnr() {
        return andelsnr;
    }

    public Boolean getMottarYtelse() {
        return mottarYtelse;
    }
}
