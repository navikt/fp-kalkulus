package no.nav.folketrygdloven.kalkulus.håndtering.v1.fakta;


import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_ABSENT;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.folketrygdloven.kalkulus.kodeverk.Inntektskategori;


@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = NON_ABSENT, content = NON_EMPTY)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
public class FastsettMånedsinntektUtenInntektsmeldingAndelDto {

    @JsonProperty("andelsnr")
    @Valid
    @NotNull
    private Long andelsnr;

    @JsonProperty("fastsattBeløp")
    @Valid
    @NotNull
    private Integer fastsattBeløp;

    @JsonProperty("inntektskategori")
    @Valid
    @NotNull
    private Inntektskategori inntektskategori;

    public FastsettMånedsinntektUtenInntektsmeldingAndelDto() {
    }

    public FastsettMånedsinntektUtenInntektsmeldingAndelDto(@Valid @NotNull Long andelsnr, @Valid @NotNull Integer fastsattBeløp, @Valid @NotNull Inntektskategori inntektskategori) {
        this.andelsnr = andelsnr;
        this.fastsattBeløp = fastsattBeløp;
        this.inntektskategori = inntektskategori;
    }

    public Long getAndelsnr() {
        return andelsnr;
    }


    public Integer getFastsattBeløp() {
        return fastsattBeløp;
    }

    public Inntektskategori getInntektskategori() {
        return inntektskategori;
    }

}
