package no.nav.folketrygdloven.kalkulus.håndtering.v1.fakta;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_ABSENT;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = NON_ABSENT, content = NON_EMPTY)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
public class VurderATogFLiSammeOrganisasjonAndelDto {

    @JsonProperty("andelsnr")
    @Valid
    @NotNull
    private Long andelsnr;

    @JsonProperty("arbeidsinntekt")
    @Valid
    @NotNull
    private Integer arbeidsinntekt;

    public VurderATogFLiSammeOrganisasjonAndelDto() {
    }

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
