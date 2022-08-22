package no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.brev;

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

import no.nav.folketrygdloven.kalkulus.felles.v1.Periode;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.Inntektskategori;
import no.nav.folketrygdloven.kalkulus.kodeverk.OpptjeningAktivitetType;
import no.nav.folketrygdloven.kalkulus.response.v1.Arbeidsgiver;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = Include.NON_ABSENT, content = Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = NONE, setterVisibility = NONE, isGetterVisibility = NONE, creatorVisibility = NONE)
public class BeregningsgrunnlagPrStatusOgAndelDto {

    @JsonProperty(value = "andelsnr")
    @Min(1)
    @Max(100)
    @NotNull
    @Valid
    private Long andelsnr;

    @JsonProperty(value = "aktivitetStatus")
    @NotNull
    @Valid
    private AktivitetStatus aktivitetStatus;

    @JsonProperty(value = "arbeidsforholdType")
    @NotNull
    @Valid
    private OpptjeningAktivitetType arbeidsforholdType;

    @JsonProperty(value = "beregningsperiode")
    @NotNull
    @Valid
    private Periode beregningsperiode;

    @JsonProperty(value = "bruttoPrÅr")
    @Valid
    @DecimalMin(value = "0.00", message = "verdien ${validatedValue} må være >= {value}")
    @DecimalMax(value = "1000000000.00", message = "verdien ${validatedValue} må være <= {value}")
    @Digits(integer = 10, fraction = 2)
    @NotNull
    private BigDecimal bruttoPrÅr;


    @JsonProperty(value = "dagsatsBruker")
    @Valid
    @Min(0)
    @Max(178956970)
    @NotNull
    private Long dagsatsBruker;

    @JsonProperty(value = "dagsatsArbeidsgiver")
    @Valid
    @Min(0)
    @Max(178956970)
    @NotNull
    private Long dagsatsArbeidsgiver;


    @JsonProperty(value = "ugradertDagsatsBruker")
    @Valid
    @Min(0)
    @Max(178956970)
    @NotNull
    private Long ugradertDagsatsBruker;

    @JsonProperty(value = "ugradertDagsatsArbeidsgiver")
    @Valid
    @Min(0)
    @Max(178956970)
    @NotNull
    private Long ugradertDagsatsArbeidsgiver;


    @JsonProperty(value = "inntektskategori")
    @NotNull
    @Valid
    private Inntektskategori inntektskategori;

    @JsonProperty(value = "bgAndelArbeidsforhold")
    @Valid
    private BGAndelArbeidsforhold bgAndelArbeidsforhold;

    @JsonProperty(value = "avkortetPrÅr")
    @Valid
    @DecimalMin(value = "0.00", message = "verdien ${validatedValue} må være >= {value}")
    @DecimalMax(value = "1000000000.00", message = "verdien ${validatedValue} må være <= {value}")
    @Digits(integer = 10, fraction = 2)
    @NotNull
    private BigDecimal avkortetPrÅr;

    @JsonProperty(value = "overstyrtPrÅr")
    @Valid
    @DecimalMin(value = "0.00", message = "verdien ${validatedValue} må være >= {value}")
    @DecimalMax(value = "1000000000.00", message = "verdien ${validatedValue} må være <= {value}")
    @Digits(integer = 10, fraction = 2)
    private BigDecimal overstyrtPrÅr;

    @JsonProperty(value = "redusertPrÅr")
    @Valid
    @DecimalMin(value = "0.00", message = "verdien ${validatedValue} må være >= {value}")
    @DecimalMax(value = "1000000000.00", message = "verdien ${validatedValue} må være <= {value}")
    @Digits(integer = 10, fraction = 2)
    private BigDecimal redusertPrÅr;

    @JsonProperty(value = "beregnetPrÅr")
    @Valid
    @DecimalMin(value = "0.00", message = "verdien ${validatedValue} må være >= {value}")
    @DecimalMax(value = "1000000000.00", message = "verdien ${validatedValue} må være <= {value}")
    @Digits(integer = 10, fraction = 2)
    private BigDecimal beregnetPrÅr;

    @JsonProperty(value = "avkortetFørGraderingPrÅr")
    @DecimalMin(value = "0.00", message = "verdien ${validatedValue} må være >= {value}")
    @DecimalMax(value = "1000000000.00", message = "verdien ${validatedValue} må være <= {value}")
    @Digits(integer = 10, fraction = 2)
    @NotNull
    @Valid
    private BigDecimal avkortetFørGraderingPrÅr;

    @JsonProperty(value = "fastsattAvSaksbehandler")
    @NotNull
    @Valid
    private Boolean fastsattAvSaksbehandler;

    public BeregningsgrunnlagPrStatusOgAndelDto() {
    }

    public BeregningsgrunnlagPrStatusOgAndelDto(Long andelsnr,
                                                AktivitetStatus aktivitetStatus,
                                                OpptjeningAktivitetType arbeidsforholdType, Periode beregningsperiode, BigDecimal bruttoPrÅr,
                                                Long dagsatsBruker,
                                                Long dagsatsArbeidsgiver,
                                                Long ugradertDagsatsBruker, Long ugradertDagsatsArbeidsgiver, Inntektskategori inntektskategori,
                                                BGAndelArbeidsforhold bgAndelArbeidsforhold,
                                                BigDecimal avkortetFørGraderingPrÅr,
                                                BigDecimal avkortetPrÅr,
                                                BigDecimal overstyrtPrÅr,
                                                BigDecimal redusertPrÅr,
                                                BigDecimal beregnetPrÅr,
                                                Boolean fastsattAvSaksbehandler) {
        this.andelsnr = andelsnr;
        this.aktivitetStatus = aktivitetStatus;
        this.arbeidsforholdType = arbeidsforholdType;
        this.beregningsperiode = beregningsperiode;
        this.bruttoPrÅr = bruttoPrÅr;
        this.dagsatsBruker = dagsatsBruker;
        this.dagsatsArbeidsgiver = dagsatsArbeidsgiver;
        this.ugradertDagsatsBruker = ugradertDagsatsBruker;
        this.ugradertDagsatsArbeidsgiver = ugradertDagsatsArbeidsgiver;
        this.inntektskategori = inntektskategori;
        this.bgAndelArbeidsforhold = bgAndelArbeidsforhold;
        this.avkortetFørGraderingPrÅr = avkortetFørGraderingPrÅr;
        this.avkortetPrÅr = avkortetPrÅr;
        this.overstyrtPrÅr = overstyrtPrÅr;
        this.redusertPrÅr = redusertPrÅr;
        this.beregnetPrÅr = beregnetPrÅr;
        this.fastsattAvSaksbehandler = fastsattAvSaksbehandler;
    }

    public Long getAndelsnr() {
        return andelsnr;
    }

    public AktivitetStatus getAktivitetStatus() {
        return aktivitetStatus;
    }

    public BigDecimal getBruttoPrÅr() {
        return bruttoPrÅr;
    }

    public Inntektskategori getInntektskategori() {
        return inntektskategori;
    }

    public Long getDagsatsBruker() {
        return dagsatsBruker;
    }

    public Long getDagsatsArbeidsgiver() {
        return dagsatsArbeidsgiver;
    }

    public Long getUgradertDagsatsBruker() {
        return ugradertDagsatsBruker;
    }

    public Long getUgradertDagsatsArbeidsgiver() {
        return ugradertDagsatsArbeidsgiver;
    }

    public Long getDagsats() {
        if (dagsatsBruker == null) {
            return dagsatsArbeidsgiver;
        }
        if (dagsatsArbeidsgiver == null) {
            return dagsatsBruker;
        }
        return dagsatsBruker + dagsatsArbeidsgiver;
    }

    public BGAndelArbeidsforhold getBgAndelArbeidsforhold() {
        return bgAndelArbeidsforhold;
    }


    public Arbeidsgiver getArbeidsgiver() {
        BGAndelArbeidsforhold beregningArbeidsforhold = getBgAndelArbeidsforhold();
        return beregningArbeidsforhold == null ? null : beregningArbeidsforhold.getArbeidsgiver();
    }

    public BigDecimal getAvkortetPrÅr() {
        return avkortetPrÅr;
    }

    public BigDecimal getAvkortetFørGraderingPrÅr() {
        return avkortetFørGraderingPrÅr;
    }

    public BigDecimal getOverstyrtPrÅr() {
        return overstyrtPrÅr;
    }

    public BigDecimal getRedusertPrÅr() {
        return redusertPrÅr;
    }

    public BigDecimal getBeregnetPrÅr() {
        return beregnetPrÅr;
    }

    public Boolean getFastsattAvSaksbehandler() {
        return fastsattAvSaksbehandler;
    }

    public OpptjeningAktivitetType getArbeidsforholdType() {
        return arbeidsforholdType;
    }

    public Periode getBeregningsperiode() {
        return beregningsperiode;
    }
}
