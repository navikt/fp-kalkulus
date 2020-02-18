package no.nav.folketrygdloven.kalkulus.håndtering.v1.fakta;

import java.util.List;

import javax.validation.Valid;

import com.fasterxml.jackson.annotation.JsonProperty;


public class MottarYtelseDto {

    @JsonProperty("frilansMottarYtelse")
    @Valid
    private Boolean frilansMottarYtelse;

    @JsonProperty("arbeidstakerUtenIMMottarYtelse")
    @Valid
    private List<ArbeidstakerandelUtenIMMottarYtelseDto> arbeidstakerUtenIMMottarYtelse;

    public MottarYtelseDto(@Valid Boolean frilansMottarYtelse, @Valid List<ArbeidstakerandelUtenIMMottarYtelseDto> arbeidstakerUtenIMMottarYtelse) {
        this.frilansMottarYtelse = frilansMottarYtelse;
        this.arbeidstakerUtenIMMottarYtelse = arbeidstakerUtenIMMottarYtelse;
    }

    public Boolean getFrilansMottarYtelse() {
        return frilansMottarYtelse;
    }

    public List<ArbeidstakerandelUtenIMMottarYtelseDto> getArbeidstakerUtenIMMottarYtelse() {
        return arbeidstakerUtenIMMottarYtelse;
    }
}
