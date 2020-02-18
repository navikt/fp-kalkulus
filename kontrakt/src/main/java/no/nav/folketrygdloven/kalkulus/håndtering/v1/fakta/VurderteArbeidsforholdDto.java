package no.nav.folketrygdloven.kalkulus.håndtering.v1.fakta;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

public class VurderteArbeidsforholdDto  {

    @JsonProperty("andelsnr")
    @Valid
    @NotNull
    private Long andelsnr;

    @JsonProperty("tidsbegrensetArbeidsforhold")
    @Valid
    @NotNull
    private Boolean tidsbegrensetArbeidsforhold;

    @JsonProperty("opprinneligVerdi")
    @Valid
    private Boolean opprinneligVerdi;

    public VurderteArbeidsforholdDto(Long andelsnr,
                                     boolean tidsbegrensetArbeidsforhold,
                                     Boolean opprinneligVerdi) {
        this.andelsnr = andelsnr;
        this.tidsbegrensetArbeidsforhold = tidsbegrensetArbeidsforhold;
        this.opprinneligVerdi = opprinneligVerdi;
    }

    public Long getAndelsnr() {
        return andelsnr;
    }

    public boolean isTidsbegrensetArbeidsforhold() {
        return tidsbegrensetArbeidsforhold;
    }

    public Boolean isOpprinneligVerdi() {
        return opprinneligVerdi;
    }
}
