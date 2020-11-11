package no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.detaljert;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import java.math.BigDecimal;
import java.time.LocalDate;

import javax.validation.Valid;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

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
    @Valid
    private Long andelsnr;

    @JsonProperty(value = "aktivitetStatus")
    @NotNull
    @Valid
    private AktivitetStatus aktivitetStatus;

    @JsonProperty(value = "beregningsperiode")
    @NotNull
    @Valid
    private Periode beregningsperiode;

    @JsonProperty(value = "arbeidsforholdType")
    @NotNull
    @Valid
    private OpptjeningAktivitetType arbeidsforholdType;

    @JsonProperty(value = "bruttoPrÅr")
    @Valid
    @DecimalMin(value = "0.00", message = "verdien ${validatedValue} må være >= {value}")
    @DecimalMax(value = "1000000000.00", message = "verdien ${validatedValue} må være <= {value}")
    @Digits(integer = 10, fraction = 2)
    private BigDecimal bruttoPrÅr;

    @JsonProperty(value = "redusertRefusjonPrÅr")
    @Valid
    @DecimalMin(value = "0.00", message = "verdien ${validatedValue} må være >= {value}")
    @DecimalMax(value = "1000000000.00", message = "verdien ${validatedValue} må være <= {value}")
    @Digits(integer = 10, fraction = 2)
    private BigDecimal redusertRefusjonPrÅr;

    @JsonProperty(value = "redusertBrukersAndelPrÅr")
    @DecimalMin(value = "0.00", message = "verdien ${validatedValue} må være >= {value}")
    @DecimalMax(value = "1000000000.00", message = "verdien ${validatedValue} må være <= {value}")
    @Digits(integer = 10, fraction = 2)
    @Valid
    private BigDecimal redusertBrukersAndelPrÅr;

    @JsonProperty(value = "dagsatsBruker")
    @Valid
    @Min(0)
    @Max(Long.MAX_VALUE)
    private Long dagsatsBruker;

    @JsonProperty(value = "dagsatsArbeidsgiver")
    @Valid
    @Min(0)
    @Max(Long.MAX_VALUE)
    private Long dagsatsArbeidsgiver;

    @JsonProperty(value = "inntektskategori")
    @NotNull
    @Valid
    private Inntektskategori inntektskategori;

    @JsonProperty(value = "bgAndelArbeidsforhold")
    @Valid
    private BGAndelArbeidsforhold bgAndelArbeidsforhold;

    @JsonProperty(value = "overstyrtPrÅr")
    @Valid
    @DecimalMin(value = "0.00", message = "verdien ${validatedValue} må være >= {value}")
    @DecimalMax(value = "1000000000.00", message = "verdien ${validatedValue} må være <= {value}")
    @Digits(integer = 10, fraction = 2)
    private BigDecimal overstyrtPrÅr;

    @JsonProperty(value = "avkortetPrÅr")
    @Valid
    @DecimalMin(value = "0.00", message = "verdien ${validatedValue} må være >= {value}")
    @DecimalMax(value = "1000000000.00", message = "verdien ${validatedValue} må være <= {value}")
    @Digits(integer = 10, fraction = 2)
    private BigDecimal avkortetPrÅr;

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

    @JsonProperty(value = "fordeltPrÅr")
    @Valid
    @DecimalMin(value = "0.00", message = "verdien ${validatedValue} må være >= {value}")
    @DecimalMax(value = "1000000000.00", message = "verdien ${validatedValue} må være <= {value}")
    @Digits(integer = 10, fraction = 2)
    private BigDecimal fordeltPrÅr;

    @JsonProperty(value = "maksimalRefusjonPrÅr")
    @Valid
    @DecimalMin(value = "0.00", message = "verdien ${validatedValue} må være >= {value}")
    @DecimalMax(value = "1000000000.00", message = "verdien ${validatedValue} må være <= {value}")
    @Digits(integer = 10, fraction = 2)
    private BigDecimal maksimalRefusjonPrÅr;

    @JsonProperty(value = "avkortetRefusjonPrÅr")
    @Valid
    @DecimalMin(value = "0.00", message = "verdien ${validatedValue} må være >= {value}")
    @DecimalMax(value = "1000000000.00", message = "verdien ${validatedValue} må være <= {value}")
    @Digits(integer = 10, fraction = 2)
    private BigDecimal avkortetRefusjonPrÅr;

    @JsonProperty(value = "avkortetBrukersAndelPrÅr")
    @Valid
    @DecimalMin(value = "0.00", message = "verdien ${validatedValue} må være >= {value}")
    @DecimalMax(value = "1000000000.00", message = "verdien ${validatedValue} må være <= {value}")
    @Digits(integer = 10, fraction = 2)
    private BigDecimal avkortetBrukersAndelPrÅr;

    @JsonProperty(value = "pgiSnitt")
    @Valid
    @DecimalMin(value = "0.00", message = "verdien ${validatedValue} må være >= {value}")
    @DecimalMax(value = "1000000000.00", message = "verdien ${validatedValue} må være <= {value}")
    @Digits(integer = 10, fraction = 2)
    private BigDecimal pgiSnitt;

    @JsonProperty(value = "pgi1")
    @Valid
    @DecimalMin(value = "0.00", message = "verdien ${validatedValue} må være >= {value}")
    @DecimalMax(value = "1000000000.00", message = "verdien ${validatedValue} må være <= {value}")
    @Digits(integer = 10, fraction = 2)
    private BigDecimal pgi1;

    @JsonProperty(value = "pgi2")
    @Valid
    @DecimalMin(value = "0.00", message = "verdien ${validatedValue} må være >= {value}")
    @DecimalMax(value = "1000000000.00", message = "verdien ${validatedValue} må være <= {value}")
    @Digits(integer = 10, fraction = 2)
    private BigDecimal pgi2;

    @JsonProperty(value = "pgi3")
    @Valid
    @DecimalMin(value = "0.00", message = "verdien ${validatedValue} må være >= {value}")
    @DecimalMax(value = "1000000000.00", message = "verdien ${validatedValue} må være <= {value}")
    @Digits(integer = 10, fraction = 2)
    private BigDecimal pgi3;

    @JsonProperty(value = "årsbeløpFraTilstøtendeYtelse")
    @Valid
    @DecimalMin(value = "0.00", message = "verdien ${validatedValue} må være >= {value}")
    @DecimalMax(value = "1000000000.00", message = "verdien ${validatedValue} må være <= {value}")
    @Digits(integer = 10, fraction = 2)
    private BigDecimal årsbeløpFraTilstøtendeYtelse;

    @JsonProperty(value = "fastsattAvSaksbehandler")
    @Valid
    private Boolean fastsattAvSaksbehandler = false;

    @JsonProperty(value = "lagtTilAvSaksbehandler")
    @Valid
    private Boolean lagtTilAvSaksbehandler = false;

    @JsonProperty(value = "orginalDagsatsFraTilstøtendeYtelse")
    @Valid
    @Min(0)
    @Max(Long.MAX_VALUE)
    private Long orginalDagsatsFraTilstøtendeYtelse;

    public BeregningsgrunnlagPrStatusOgAndelDto() {
    }

    public BeregningsgrunnlagPrStatusOgAndelDto(Long andelsnr, @NotNull @Valid AktivitetStatus aktivitetStatus,
                                                @NotNull @Valid Periode beregningsperiode,
                                                @NotNull @Valid OpptjeningAktivitetType arbeidsforholdType,
                                                @NotNull @Valid BigDecimal bruttoPrÅr,
                                                @NotNull @Valid BigDecimal redusertRefusjonPrÅr,
                                                @NotNull @Valid BigDecimal redusertBrukersAndelPrÅr,
                                                @NotNull @Valid Long dagsatsBruker,
                                                @Valid Long dagsatsArbeidsgiver,
                                                @NotNull @Valid Inntektskategori inntektskategori,
                                                @Valid BGAndelArbeidsforhold bgAndelArbeidsforhold,
                                                BigDecimal overstyrtPrÅr,
                                                BigDecimal avkortetPrÅr,
                                                BigDecimal redusertPrÅr,
                                                BigDecimal beregnetPrÅr,
                                                BigDecimal fordeltPrÅr,
                                                BigDecimal maksimalRefusjonPrÅr,
                                                BigDecimal avkortetRefusjonPrÅr,
                                                BigDecimal avkortetBrukersAndelPrÅr,
                                                BigDecimal pgiSnitt,
                                                BigDecimal pgi1,
                                                BigDecimal pgi2,
                                                BigDecimal pgi3,
                                                BigDecimal årsbeløpFraTilstøtendeYtelse,
                                                Boolean fastsattAvSaksbehandler,
                                                Boolean lagtTilAvSaksbehandler,
                                                Long orginalDagsatsFraTilstøtendeYtelse) {
        this.andelsnr = andelsnr;
        this.aktivitetStatus = aktivitetStatus;
        this.beregningsperiode = beregningsperiode;
        this.arbeidsforholdType = arbeidsforholdType;
        this.bruttoPrÅr = bruttoPrÅr;
        this.redusertRefusjonPrÅr = redusertRefusjonPrÅr;
        this.redusertBrukersAndelPrÅr = redusertBrukersAndelPrÅr;
        this.dagsatsBruker = dagsatsBruker;
        this.dagsatsArbeidsgiver = dagsatsArbeidsgiver;
        this.inntektskategori = inntektskategori;
        this.bgAndelArbeidsforhold = bgAndelArbeidsforhold;
        this.overstyrtPrÅr = overstyrtPrÅr;
        this.avkortetPrÅr = avkortetPrÅr;
        this.redusertPrÅr = redusertPrÅr;
        this.beregnetPrÅr = beregnetPrÅr;
        this.fordeltPrÅr = fordeltPrÅr;
        this.maksimalRefusjonPrÅr = maksimalRefusjonPrÅr;
        this.avkortetRefusjonPrÅr = avkortetRefusjonPrÅr;
        this.avkortetBrukersAndelPrÅr = avkortetBrukersAndelPrÅr;
        this.pgiSnitt = pgiSnitt;
        this.pgi1 = pgi1;
        this.pgi2 = pgi2;
        this.pgi3 = pgi3;
        this.årsbeløpFraTilstøtendeYtelse = årsbeløpFraTilstøtendeYtelse;
        this.fastsattAvSaksbehandler = fastsattAvSaksbehandler;
        this.lagtTilAvSaksbehandler = lagtTilAvSaksbehandler;
        this.orginalDagsatsFraTilstøtendeYtelse = orginalDagsatsFraTilstøtendeYtelse;
    }

    public AktivitetStatus getAktivitetStatus() {
        return aktivitetStatus;
    }

    public LocalDate getBeregningsperiodeFom() {
        return beregningsperiode != null ? beregningsperiode.getFom() : null;
    }

    public LocalDate getBeregningsperiodeTom() {
        return beregningsperiode != null ? beregningsperiode.getTom() : null;
    }

    public OpptjeningAktivitetType getArbeidsforholdType() {
        return arbeidsforholdType;
    }

    public BigDecimal getBruttoPrÅr() {
        return bruttoPrÅr;
    }

    public Inntektskategori getInntektskategori() {
        return inntektskategori;
    }

    public BigDecimal getBruttoInkludertNaturalYtelser() {
        BGAndelArbeidsforhold bgAndelArbeidsforhold = getBgAndelArbeidsforhold();
        if (bgAndelArbeidsforhold == null) {
            return bruttoPrÅr;
        }
        BigDecimal naturalytelseBortfalt = bgAndelArbeidsforhold.getNaturalytelseBortfaltPrÅr() == null ? BigDecimal.ZERO : bgAndelArbeidsforhold.getNaturalytelseBortfaltPrÅr();
        BigDecimal naturalYtelseTilkommet = bgAndelArbeidsforhold.getNaturalytelseTilkommetPrÅr() == null ? BigDecimal.ZERO : bgAndelArbeidsforhold.getNaturalytelseTilkommetPrÅr();
        BigDecimal brutto = bruttoPrÅr != null ? bruttoPrÅr : BigDecimal.ZERO;
        return brutto.add(naturalytelseBortfalt).subtract(naturalYtelseTilkommet);
    }

    public Long getDagsatsBruker() {
        return dagsatsBruker;
    }

    public Long getDagsatsArbeidsgiver() {
        return dagsatsArbeidsgiver;
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

    public BigDecimal getRedusertBrukersAndelPrÅr() {
        return redusertBrukersAndelPrÅr;
    }

    public BigDecimal getRedusertRefusjonPrÅr() {
        return redusertRefusjonPrÅr;
    }

    public BigDecimal getOverstyrtPrÅr() {
        return overstyrtPrÅr;
    }

    public BigDecimal getAvkortetPrÅr() {
        return avkortetPrÅr;
    }

    public BigDecimal getRedusertPrÅr() {
        return redusertPrÅr;
    }

    public BigDecimal getBeregnetPrÅr() {
        return beregnetPrÅr;
    }

    public BigDecimal getFordeltPrÅr() {
        return fordeltPrÅr;
    }

    public BigDecimal getMaksimalRefusjonPrÅr() {
        return maksimalRefusjonPrÅr;
    }

    public BigDecimal getAvkortetRefusjonPrÅr() {
        return avkortetRefusjonPrÅr;
    }

    public BigDecimal getAvkortetBrukersAndelPrÅr() {
        return avkortetBrukersAndelPrÅr;
    }

    public BigDecimal getPgiSnitt() {
        return pgiSnitt;
    }

    public BigDecimal getPgi1() {
        return pgi1;
    }

    public BigDecimal getPgi2() {
        return pgi2;
    }

    public BigDecimal getPgi3() {
        return pgi3;
    }

    public BigDecimal getÅrsbeløpFraTilstøtendeYtelse() {
        return årsbeløpFraTilstøtendeYtelse;
    }

    public Boolean getFastsattAvSaksbehandler() {
        return fastsattAvSaksbehandler;
    }

    public Boolean getLagtTilAvSaksbehandler() {
        return lagtTilAvSaksbehandler;
    }

    public Long getOrginalDagsatsFraTilstøtendeYtelse() {
        return orginalDagsatsFraTilstøtendeYtelse;
    }

    public Long getAndelsnr() {
        return andelsnr;
    }
}
