package no.nav.folketrygdloven.kalkulus.håndtering.v1.fakta;


import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.folketrygdloven.kalkulus.kodeverk.Inntektskategori;


public class FastsettMånedsinntektUtenInntektsmeldingAndelDto {

    @JsonProperty("erLønnsendringIBeregningsperioden")
    @Valid
    @NotNull
    private Long andelsnr;

    @JsonProperty("erLønnsendringIBeregningsperioden")
    @Valid
    @NotNull
    private Integer fastsattBeløp;

    @JsonProperty("erLønnsendringIBeregningsperioden")
    @Valid
    @NotNull
    private Inntektskategori inntektskategori;

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
