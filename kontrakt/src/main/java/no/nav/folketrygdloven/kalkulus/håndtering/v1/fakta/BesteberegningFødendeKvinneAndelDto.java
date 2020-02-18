package no.nav.folketrygdloven.kalkulus.håndtering.v1.fakta;


import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.folketrygdloven.kalkulus.kodeverk.Inntektskategori;

public class BesteberegningFødendeKvinneAndelDto {

    @JsonProperty("andelsnr")
    @Valid
    @NotNull
    private Long andelsnr;

    @JsonProperty("lagtTilAvSaksbehandler")
    @Valid
    @NotNull
    private Boolean lagtTilAvSaksbehandler;

    @JsonProperty("fastsatteVerdier")
    @Valid
    @NotNull
    private FastsatteVerdierForBesteberegningDto fastsatteVerdier;

    public BesteberegningFødendeKvinneAndelDto(Long andelsnr, Integer inntektPrMnd, Inntektskategori inntektskategori,
                                               boolean lagtTilAvSaksbehandler) {
        this.lagtTilAvSaksbehandler = lagtTilAvSaksbehandler;
        this.andelsnr = andelsnr;
        fastsatteVerdier = new FastsatteVerdierForBesteberegningDto(inntektPrMnd, inntektskategori);
    }

    public FastsatteVerdierForBesteberegningDto getFastsatteVerdier() {
        return fastsatteVerdier;
    }

    public Long getAndelsnr() {
        return andelsnr;
    }

    public Boolean getLagtTilAvSaksbehandler() {
        return lagtTilAvSaksbehandler;
    }

}
