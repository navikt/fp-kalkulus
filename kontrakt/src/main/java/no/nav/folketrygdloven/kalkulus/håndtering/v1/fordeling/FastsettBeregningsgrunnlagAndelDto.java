package no.nav.folketrygdloven.kalkulus.håndtering.v1.fordeling;


import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.folketrygdloven.kalkulus.håndtering.v1.fakta.FastsatteVerdierDto;
import no.nav.folketrygdloven.kalkulus.håndtering.v1.fakta.RedigerbarAndelDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.Inntektskategori;


public class FastsettBeregningsgrunnlagAndelDto extends RedigerbarAndelDto {

    @JsonProperty("fastsatteVerdier")
    @Valid
    @NotNull
    private FastsatteVerdierDto fastsatteVerdier;

    @JsonProperty("forrigeInntektskategori")
    @Valid
    private Inntektskategori forrigeInntektskategori;

    @JsonProperty("forrigeRefusjonPrÅr")
    @Valid
    @NotNull
    private Integer forrigeRefusjonPrÅr;

    @JsonProperty("forrigeArbeidsinntektPrÅr")
    @Valid
    private Integer forrigeArbeidsinntektPrÅr;

    FastsettBeregningsgrunnlagAndelDto() { // NOSONAR
        // Jackson
    }


    public FastsettBeregningsgrunnlagAndelDto(@NotNull @Valid RedigerbarAndelDto andelDto,
                                              @NotNull @Valid FastsatteVerdierDto fastsatteVerdier,
                                              @Valid Inntektskategori forrigeInntektskategori,
                                              @Valid Integer forrigeRefusjonPrÅr,
                                              @Valid Integer forrigeArbeidsinntektPrÅr) {
        super(andelDto.getAndelsnr(), andelDto.getArbeidsgiverId(), andelDto.getArbeidsforholdId().getReferanse(),
                andelDto.getNyAndel(), andelDto.getAktivitetStatus(), andelDto.getArbeidsforholdType(), andelDto.getLagtTilAvSaksbehandler(),
                andelDto.getBeregningsperiodeFom(), andelDto.getBeregningsperiodeTom());
        this.fastsatteVerdier = fastsatteVerdier;
        this.forrigeArbeidsinntektPrÅr = forrigeArbeidsinntektPrÅr;
        this.forrigeInntektskategori = forrigeInntektskategori;
        this.forrigeRefusjonPrÅr = forrigeRefusjonPrÅr;
    }

    public FastsatteVerdierDto getFastsatteVerdier() {
        return fastsatteVerdier;
    }

    public Inntektskategori getForrigeInntektskategori() {
        return forrigeInntektskategori;
    }

    public Integer getForrigeRefusjonPrÅr() {
        return forrigeRefusjonPrÅr;
    }

    public Integer getForrigeArbeidsinntektPrÅr() {
        return forrigeArbeidsinntektPrÅr;
    }
}
