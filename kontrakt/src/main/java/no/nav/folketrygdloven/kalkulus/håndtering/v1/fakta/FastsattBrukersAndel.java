package no.nav.folketrygdloven.kalkulus.håndtering.v1.fakta;


import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.Inntektskategori;

public class FastsattBrukersAndel {

    @JsonProperty("andelsnr")
    @Valid
    @NotNull
    private Long andelsnr;

    @JsonProperty("nyAndel")
    @Valid
    @NotNull
    private Boolean nyAndel;

    @JsonProperty("lagtTilAvSaksbehandler")
    @Valid
    @NotNull
    private Boolean lagtTilAvSaksbehandler;

    @JsonProperty("fastsattBeløp")
    @Valid
    @NotNull
    private Integer fastsattBeløp;

    @JsonProperty("inntektskategori")
    @Valid
    @NotNull
    private Inntektskategori inntektskategori;

    public FastsattBrukersAndel(@Valid @NotNull Long andelsnr, @Valid @NotNull Boolean nyAndel, @Valid @NotNull Boolean lagtTilAvSaksbehandler, @Valid @NotNull Integer fastsattBeløp, @Valid @NotNull Inntektskategori inntektskategori) {
        this.andelsnr = andelsnr;
        this.nyAndel = nyAndel;
        this.lagtTilAvSaksbehandler = lagtTilAvSaksbehandler;
        this.fastsattBeløp = fastsattBeløp;
        this.inntektskategori = inntektskategori;
    }

    public Long getAndelsnr() {
        return andelsnr;
    }

    public Boolean getNyAndel() {
        return nyAndel;
    }

    public Boolean getLagtTilAvSaksbehandler() {
        return lagtTilAvSaksbehandler;
    }

    public Integer getFastsattBeløp() {
        return fastsattBeløp;
    }

    public Inntektskategori getInntektskategori() {
        return inntektskategori;
    }
}
