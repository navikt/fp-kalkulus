package no.nav.folketrygdloven.kalkulus.håndtering.v1.fakta;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

public class VurderEtterlønnSluttpakkeDto {

    @JsonProperty("vurderEtterlønnSluttpakke")
    @Valid
    @NotNull
    private Boolean erEtterlønnSluttpakke;

    public VurderEtterlønnSluttpakkeDto(@Valid @NotNull Boolean erEtterlønnSluttpakke) {
        this.erEtterlønnSluttpakke = erEtterlønnSluttpakke;
    }

    public Boolean getErEtterlønnSluttpakke() {
        return erEtterlønnSluttpakke;
    }

}
