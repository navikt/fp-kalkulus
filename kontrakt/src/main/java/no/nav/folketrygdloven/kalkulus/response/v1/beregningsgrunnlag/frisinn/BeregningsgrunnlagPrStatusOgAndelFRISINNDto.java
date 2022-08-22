package no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.frisinn;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import java.math.BigDecimal;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.Inntektskategori;


@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = Include.NON_ABSENT, content = Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = NONE, setterVisibility = NONE, isGetterVisibility = NONE, creatorVisibility = NONE)
public class BeregningsgrunnlagPrStatusOgAndelFRISINNDto {

    @JsonProperty(value = "aktivitetStatus")
    @NotNull
    @Valid
    private AktivitetStatus aktivitetStatus;

    @JsonProperty(value = "bruttoPrÅr")
    @NotNull
    @Valid
    @DecimalMin(value = "0.00", message = "verdien ${validatedValue} må være >= {value}")
    @DecimalMax(value = "1000000000.00", message = "verdien ${validatedValue} må være <= {value}")
    @Digits(integer = 10, fraction = 2)
    private BigDecimal bruttoPrÅr;

    @JsonProperty(value = "redusertPrÅr")
    @NotNull
    @Valid
    @DecimalMin(value = "0.00", message = "verdien ${validatedValue} må være >= {value}")
    @DecimalMax(value = "1000000000.00", message = "verdien ${validatedValue} må være <= {value}")
    @Digits(integer = 10, fraction = 2)
    private BigDecimal redusertPrÅr;

    @JsonProperty(value = "avkortetPrÅr")
    @NotNull
    @Valid
    @DecimalMin(value = "0.00", message = "verdien ${validatedValue} må være >= {value}")
    @DecimalMax(value = "1000000000.00", message = "verdien ${validatedValue} må være <= {value}")
    @Digits(integer = 10, fraction = 2)
    private BigDecimal avkortetPrÅr;

    @JsonProperty(value = "løpendeInntektPrÅr")
    @NotNull
    @Valid
    @DecimalMin(value = "0.00", message = "verdien ${validatedValue} må være >= {value}")
    @DecimalMax(value = "1000000000.00", message = "verdien ${validatedValue} må være <= {value}")
    @Digits(integer = 10, fraction = 2)
    private BigDecimal løpendeInntektPrÅr;

    @JsonProperty(value = "bgFratrukketInntektstak")
    @NotNull
    @Valid
    @DecimalMin(value = "0.00", message = "verdien ${validatedValue} må være >= {value}")
    @DecimalMax(value = "1000000000.00", message = "verdien ${validatedValue} må være <= {value}")
    @Digits(integer = 10, fraction = 2)
    private BigDecimal bgFratrukketInntektstak;

    @JsonProperty(value = "dagsats")
    @NotNull
    @Valid
    @Min(0)
    @Max(178956970)
    private Long dagsats;

    @JsonProperty(value = "inntektskategori")
    @NotNull
    @Valid
    private Inntektskategori inntektskategori;

    @JsonProperty(value = "avslagsårsak")
    @Valid
    private Avslagsårsak avslagsårsak;

    public BeregningsgrunnlagPrStatusOgAndelFRISINNDto(@NotNull @Valid AktivitetStatus aktivitetStatus,
                                                       @NotNull @Valid @DecimalMin(value = "0.00", message = "verdien ${validatedValue} må være >= {value}") @DecimalMax(value = "1000000000.00", message = "verdien ${validatedValue} må være <= {value}") @Digits(integer = 10, fraction = 2) BigDecimal bruttoPrÅr,
                                                       @NotNull @Valid @DecimalMin(value = "0.00", message = "verdien ${validatedValue} må være >= {value}") @DecimalMax(value = "1000000000.00", message = "verdien ${validatedValue} må være <= {value}") @Digits(integer = 10, fraction = 2) BigDecimal redusertPrÅr,
                                                       @NotNull @Valid @DecimalMin(value = "0.00", message = "verdien ${validatedValue} må være >= {value}") @DecimalMax(value = "1000000000.00", message = "verdien ${validatedValue} må være <= {value}") @Digits(integer = 10, fraction = 2) BigDecimal avkortetPrÅr,
                                                       @NotNull @Valid @DecimalMin(value = "0.00", message = "verdien ${validatedValue} må være >= {value}") @DecimalMax(value = "1000000000.00", message = "verdien ${validatedValue} må være <= {value}") @Digits(integer = 10, fraction = 2) BigDecimal løpendeInntektPrÅr,
                                                       @NotNull @Valid @DecimalMin(value = "0.00", message = "verdien ${validatedValue} må være >= {value}") @DecimalMax(value = "1000000000.00", message = "verdien ${validatedValue} må være <= {value}") @Digits(integer = 10, fraction = 2) BigDecimal bgFratrukketInntektstak,
                                                       @NotNull @Valid @Min(0) @Max(178956970) Long dagsats,
                                                       @NotNull @Valid Inntektskategori inntektskategori, Avslagsårsak avslagsårsak) {
        this.aktivitetStatus = aktivitetStatus;
        this.bruttoPrÅr = bruttoPrÅr;
        this.redusertPrÅr = redusertPrÅr;
        this.avkortetPrÅr = avkortetPrÅr;
        this.løpendeInntektPrÅr = løpendeInntektPrÅr;
        this.dagsats = dagsats;
        this.inntektskategori = inntektskategori;
        this.avslagsårsak = avslagsårsak;
        this.bgFratrukketInntektstak = bgFratrukketInntektstak;
    }

    public BeregningsgrunnlagPrStatusOgAndelFRISINNDto() {
    }

    public AktivitetStatus getAktivitetStatus() {
        return aktivitetStatus;
    }

    public BigDecimal getBruttoPrÅr() {
        return bruttoPrÅr;
    }

    public BigDecimal getRedusertPrÅr() {
        return redusertPrÅr;
    }

    public BigDecimal getAvkortetPrÅr() {
        return avkortetPrÅr;
    }

    public BigDecimal getLøpendeInntektPrÅr() {
        return løpendeInntektPrÅr;
    }

    public Long getDagsats() {
        return dagsats;
    }

    public Inntektskategori getInntektskategori() {
        return inntektskategori;
    }

    public Avslagsårsak getAvslagsårsak() {
        return avslagsårsak;
    }

    public BigDecimal getBgFratrukketInntektstak() {
        return bgFratrukketInntektstak;
    }
}
