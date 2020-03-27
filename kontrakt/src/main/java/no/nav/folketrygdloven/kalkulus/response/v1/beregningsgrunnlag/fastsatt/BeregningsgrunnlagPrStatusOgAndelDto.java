package no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.fastsatt;

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
    @NotNull
    @Valid
    @DecimalMin(value = "0.00", message = "verdien ${validatedValue} må være >= {value}")
    @DecimalMax(value = "1000000000.00", message = "verdien ${validatedValue} må være <= {value}")
    @Digits(integer = 10, fraction = 2)
    private BigDecimal bruttoPrÅr;

    @JsonProperty(value = "redusertRefusjonPrÅr")
    @NotNull
    @Valid
    @DecimalMin(value = "0.00", message = "verdien ${validatedValue} må være >= {value}")
    @DecimalMax(value = "1000000000.00", message = "verdien ${validatedValue} må være <= {value}")
    @Digits(integer = 10, fraction = 2)
    private BigDecimal redusertRefusjonPrÅr;

    @JsonProperty(value = "redusertBrukersAndelPrÅr")
    @NotNull
    @Valid
    @DecimalMin(value = "0.00", message = "verdien ${validatedValue} må være >= {value}")
    @DecimalMax(value = "1000000000.00", message = "verdien ${validatedValue} må være <= {value}")
    @Digits(integer = 10, fraction = 2)
    private BigDecimal redusertBrukersAndelPrÅr;

    @JsonProperty(value = "dagsatsBruker")
    @NotNull
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

    public BeregningsgrunnlagPrStatusOgAndelDto() {
    }

    public BeregningsgrunnlagPrStatusOgAndelDto(@NotNull @Valid AktivitetStatus aktivitetStatus,
                                                @NotNull @Valid Periode beregningsperiode,
                                                @NotNull @Valid OpptjeningAktivitetType arbeidsforholdType,
                                                @NotNull @Valid BigDecimal bruttoPrÅr,
                                                @NotNull @Valid BigDecimal redusertRefusjonPrÅr,
                                                @NotNull @Valid BigDecimal redusertBrukersAndelPrÅr,
                                                @NotNull @Valid Long dagsatsBruker,
                                                @Valid Long dagsatsArbeidsgiver,
                                                @NotNull @Valid Inntektskategori inntektskategori,
                                                @Valid BGAndelArbeidsforhold bgAndelArbeidsforhold) {
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

    public BigDecimal getRedusertRefusjonPrÅr() {
        return redusertRefusjonPrÅr;
    }

    public BigDecimal getRedusertBrukersAndelPrÅr() {
        return redusertBrukersAndelPrÅr;
    }
}
