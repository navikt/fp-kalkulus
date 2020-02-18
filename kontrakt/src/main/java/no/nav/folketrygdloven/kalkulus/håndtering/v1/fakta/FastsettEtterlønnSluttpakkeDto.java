package no.nav.folketrygdloven.kalkulus.håndtering.v1.fakta;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

public class FastsettEtterlønnSluttpakkeDto {

    @JsonProperty("fastsettEtterlønnSluttpakke")
    @Valid
    @NotNull
    private Integer fastsattPrMnd;

    public FastsettEtterlønnSluttpakkeDto(@Valid @NotNull Integer fastsattPrMnd) {
        this.fastsattPrMnd = fastsattPrMnd;
    }

    public Integer getFastsattPrMnd() {
        return fastsattPrMnd;
    }
}
