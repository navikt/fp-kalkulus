package no.nav.foreldrepenger.kalkulus.kontrakt.request.håndtering.fakta;


import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_ABSENT;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.folketrygdloven.kalkulus.kodeverk.Inntektskategori;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = NON_ABSENT, content = NON_EMPTY)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
public class FastsettBeregningsgrunnlagAndelDto {

    @JsonProperty("andelsnr")
    @Valid
    @Min(1)
    @Max(100)
    private Long andelsnr;

    @JsonProperty("lagtTilAvSaksbehandler")
    @Valid
    private Boolean lagtTilAvSaksbehandler;

    @JsonProperty("fastsatteVerdier")
    @Valid
    @NotNull
    private FastsatteVerdierDto fastsatteVerdier;

    @JsonProperty("forrigeInntektskategori")
    @Valid
    private Inntektskategori forrigeInntektskategori;

    @JsonProperty("forrigeRefusjonPrÅr")
    @Valid
    @Min(0)
    @Max(178956970)
    private Integer forrigeRefusjonPrÅr;

    @JsonProperty("forrigeArbeidsinntektPrÅr")
    @Valid
    @Min(0)
    @Max(178956970)
    private Integer forrigeArbeidsinntektPrÅr;

    FastsettBeregningsgrunnlagAndelDto() { // NOSONAR
        // Jackson
    }


    public FastsettBeregningsgrunnlagAndelDto(@Valid @Min(1) @Max(100) Long andelsnr,
                                              @Valid Boolean lagtTilAvSaksbehandler,
                                              @NotNull @Valid FastsatteVerdierDto fastsatteVerdier,
                                              @Valid Inntektskategori forrigeInntektskategori,
                                              @Valid Integer forrigeRefusjonPrÅr,
                                              @Valid Integer forrigeArbeidsinntektPrÅr) {
        this.andelsnr = andelsnr;
        this.lagtTilAvSaksbehandler = lagtTilAvSaksbehandler;
        this.fastsatteVerdier = fastsatteVerdier;
        this.forrigeArbeidsinntektPrÅr = forrigeArbeidsinntektPrÅr;
        this.forrigeInntektskategori = forrigeInntektskategori;
        this.forrigeRefusjonPrÅr = forrigeRefusjonPrÅr;
    }

    public Long getAndelsnr() {
        return andelsnr;
    }

    public Boolean getLagtTilAvSaksbehandler() {
        return lagtTilAvSaksbehandler;
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
