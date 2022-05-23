package no.nav.folketrygdloven.kalkulus.rest.forvaltning;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import no.nav.k9.felles.sikkerhet.abac.AbacDataAttributter;
import no.nav.k9.felles.sikkerhet.abac.AbacDto;

public class FjernRegelsporingLimit implements AbacDto {

    @JsonProperty("limit")
    @Valid
    private final Integer limit;

    public FjernRegelsporingLimit(@JsonProperty("limit") Integer limit) {
        this.limit = limit;
    }

    @Override
    public AbacDataAttributter abacAttributter() {
        return AbacDataAttributter.opprett();
    }

    public Integer getLimit() {
        return limit;
    }
}
