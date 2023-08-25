package no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.detaljert;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import no.nav.folketrygdloven.kalkulus.felles.v1.Periode;
import no.nav.folketrygdloven.kalkulus.kodeverk.PeriodeÅrsak;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = Include.NON_ABSENT, content = Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = NONE, setterVisibility = NONE, isGetterVisibility = NONE, creatorVisibility = NONE)
public class BeregningsgrunnlagPeriodeDto {

    @JsonProperty(value = "beregningsgrunnlagPrStatusOgAndelList")
    @Size(min = 1)
    @Valid
    private List<BeregningsgrunnlagPrStatusOgAndelDto> beregningsgrunnlagPrStatusOgAndelList;

    @JsonProperty(value = "periode")
    @NotNull
    @Valid
    private Periode periode;

    @JsonProperty(value = "bruttoPrÅr")
    @Valid
    @DecimalMin(value = "0.00", message = "verdien ${validatedValue} må være >= {value}")
    @DecimalMax(value = "1000000000.00", message = "verdien ${validatedValue} må være <= {value}")
    @Digits(integer = 10, fraction = 2)
    private BigDecimal bruttoPrÅr;

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

    @JsonProperty(value = "dagsats")
    @Valid
    @Min(0)
    @Max(178956970)
    private Long dagsats;

    @JsonProperty(value = "periodeÅrsaker")
    @Size(min = 1)
    @Valid
    private List<PeriodeÅrsak> periodeÅrsaker;


    @Deprecated(since = "2.5.0", forRemoval = true)// du vil sannsynligvis bruke noe av totalUtbetalingsgradFraUttak/totalUtbetalingsgradEtterReduksjonVedTilkommetInntekt
    /**
     * Gradering av beregningsgrunnlaget ved tilkommet inntekt (gradering mot inntekt)
     * Angir total bortfalt inntekt av totalt beregningsgrunnlag.
     */
    @JsonProperty(value = "inntektGraderingsprosent")
    @Valid
    @DecimalMin(value = "0.00", message = "verdien ${validatedValue} må være >= {value}")
    @DecimalMax(value = "100.00", message = "verdien ${validatedValue} må være <= {value}")
    @Digits(integer = 10, fraction = 2)
    private BigDecimal inntektGraderingsprosent;

    /**
     * utbetalingsgrad dersom det kun skulle vært gradert mot uttak
     */
    @JsonProperty(value = "totalUtbetalingsgradFraUttak")
    @Valid
    @DecimalMin(value = "0.00", message = "verdien ${validatedValue} må være >= {value}")
    @DecimalMax(value = "1.00", message = "verdien ${validatedValue} må være <= {value}")
    @Digits(integer = 10, fraction = 4)
    private BigDecimal totalUtbetalingsgradFraUttak;

    /**
     * utbetalingsgrad dersom det kun skulle vært gradert mot tilkommetInntekt
     */
    @JsonProperty(value = "totalUtbetalingsgradEtterReduksjonVedTilkommetInntekt")
    @Valid
    @DecimalMin(value = "0.00", message = "verdien ${validatedValue} må være >= {value}")
    @DecimalMax(value = "1.00", message = "verdien ${validatedValue} må være <= {value}")
    @Digits(integer = 10, fraction = 4)
    private BigDecimal totalUtbetalingsgradEtterReduksjonVedTilkommetInntekt;

    @Deprecated(since = "2.5.0", forRemoval = true)// du vil sannsynligvis bruke noe av totalUtbetalingsgradFraUttak/totalUtbetalingsgradEtterReduksjonVedTilkommetInntekt
    /**
     * Gradering av beregningsgrunnlaget ved tilkommet inntekt (gradering mot inntekt)
     * Angir reduksjon pga gradering mot inntekt sammenlignet med å kun gradere mot uttaksgraden (eksisterer som faktor i inntektGraderingsprosent)
     */
    @JsonProperty(value = "graderingsfaktorInntekt")
    @Valid
    @DecimalMin(value = "0.00", message = "verdien ${validatedValue} må være >= {value}")
    @DecimalMax(value = "100.00", message = "verdien ${validatedValue} må være <= {value}")
    @Digits(integer = 10, fraction = 2)
    private BigDecimal graderingsfaktorInntekt;

    @Deprecated(since = "2.5.0", forRemoval = true)// du vil sannsynligvis bruke noe av totalUtbetalingsgradFraUttak/totalUtbetalingsgradEtterReduksjonVedTilkommetInntekt
    /**
     * Gradering av beregningsgrunnlaget ved tilkommet inntekt (gradering mot inntekt)
     * Angir reduksjon pga gradering mot arbeidstid (uttaksgrad)
     */
    @JsonProperty(value = "graderingsfaktorTid")
    @Valid
    @DecimalMin(value = "0.00", message = "verdien ${validatedValue} må være >= {value}")
    @DecimalMax(value = "100.00", message = "verdien ${validatedValue} må være <= {value}")
    @Digits(integer = 10, fraction = 2)
    private BigDecimal graderingsfaktorTid;


    public BeregningsgrunnlagPeriodeDto() {
    }

    public BeregningsgrunnlagPeriodeDto(@NotNull @Valid List<BeregningsgrunnlagPrStatusOgAndelDto> beregningsgrunnlagPrStatusOgAndelList,
                                        @NotNull @Valid Periode periode,
                                        @Valid BigDecimal bruttoPrÅr,
                                        @Valid BigDecimal avkortetPrÅr,
                                        @Valid BigDecimal redusertPrÅr,
                                        @Valid Long dagsats,
                                        @NotNull @Valid List<PeriodeÅrsak> periodeÅrsaker,
                                        BigDecimal inntektGraderingsprosent,
                                        BigDecimal totalUtbetalingsgradFraUttak,
                                        BigDecimal totalUtbetalingsgradEtterReduksjonVedTilkommetInntekt,
                                        BigDecimal graderingsfaktorInntekt,
                                        BigDecimal graderingsfaktorTid) {
        this.beregningsgrunnlagPrStatusOgAndelList = beregningsgrunnlagPrStatusOgAndelList;
        this.periode = periode;
        this.bruttoPrÅr = bruttoPrÅr;
        this.avkortetPrÅr = avkortetPrÅr;
        this.redusertPrÅr = redusertPrÅr;
        this.dagsats = dagsats;
        this.periodeÅrsaker = periodeÅrsaker;
        this.inntektGraderingsprosent = inntektGraderingsprosent;
        this.totalUtbetalingsgradFraUttak = totalUtbetalingsgradFraUttak;
        this.totalUtbetalingsgradEtterReduksjonVedTilkommetInntekt = totalUtbetalingsgradEtterReduksjonVedTilkommetInntekt;
        this.graderingsfaktorInntekt = graderingsfaktorInntekt;
        this.graderingsfaktorTid = graderingsfaktorTid;
    }

    public LocalDate getBeregningsgrunnlagPeriodeFom() {
        return periode.getFom();
    }

    public LocalDate getBeregningsgrunnlagPeriodeTom() {
        return periode.getTom();
    }

    public BigDecimal getBruttoPrÅr() {
        return bruttoPrÅr;
    }

    public BigDecimal getAvkortetPrÅr() {
        return avkortetPrÅr;
    }

    public BigDecimal getRedusertPrÅr() {
        return redusertPrÅr;
    }

    public Long getDagsats() {
        return dagsats;
    }

    public List<PeriodeÅrsak> getPeriodeÅrsaker() {
        return periodeÅrsaker;
    }

    public List<BeregningsgrunnlagPrStatusOgAndelDto> getBeregningsgrunnlagPrStatusOgAndelList() {
        return beregningsgrunnlagPrStatusOgAndelList;
    }

    public Periode getPeriode() {
        return periode;
    }

    public BigDecimal getInntektGraderingsprosent() {
        return inntektGraderingsprosent;
    }


    public BigDecimal getGraderingsfaktorInntekt() {
        return graderingsfaktorInntekt;
    }

    public BigDecimal getGraderingsfaktorTid() {
        return graderingsfaktorTid;
    }

    public BigDecimal getTotalUtbetalingsgradFraUttak() {
        return totalUtbetalingsgradFraUttak;
    }

    public BigDecimal getTotalUtbetalingsgradEtterReduksjonVedTilkommetInntekt() {
        return totalUtbetalingsgradEtterReduksjonVedTilkommetInntekt;
    }
}
