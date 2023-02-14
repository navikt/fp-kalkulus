package no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.folketrygdloven.kalkulus.kodeverk.PeriodeÅrsak;


@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = JsonInclude.Include.NON_ABSENT, content = JsonInclude.Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = NONE, setterVisibility = NONE, isGetterVisibility = NONE, creatorVisibility = NONE)
public class BeregningsgrunnlagPeriodeDto {

    @Valid
    @JsonProperty("beregningsgrunnlagPeriodeFom")
    private LocalDate beregningsgrunnlagPeriodeFom;

    @Valid
    @JsonProperty("beregningsgrunnlagPeriodeTom")
    private LocalDate beregningsgrunnlagPeriodeTom;

    @Valid
    @JsonProperty("beregnetPrAar")
    @Digits(integer = 8, fraction = 2)
    @DecimalMin("0.00")
    @DecimalMax("10000000.00")
    private BigDecimal beregnetPrAar;

    @Valid
    @JsonProperty("bruttoPrAar")
    @Digits(integer = 8, fraction = 2)
    @DecimalMin("0.00")
    @DecimalMax("10000000.00")
    private BigDecimal bruttoPrAar;

    @Valid
    @JsonProperty("bruttoInkludertBortfaltNaturalytelsePrAar")
    @Digits(integer = 8, fraction = 2)
    @DecimalMin("0.00")
    @DecimalMax("10000000.00")
    private BigDecimal bruttoInkludertBortfaltNaturalytelsePrAar;

    @Valid
    @JsonProperty("avkortetPrAar")
    @Digits(integer = 8, fraction = 2)
    @DecimalMin("0.00")
    @DecimalMax("10000000.00")
    private BigDecimal avkortetPrAar;

    @Valid
    @JsonProperty("redusertPrAar")
    @Digits(integer = 8, fraction = 2)
    @DecimalMin("0.00")
    @DecimalMax("10000000.00")
    private BigDecimal redusertPrAar;

    @Valid
    @Size()
    @JsonProperty("periodeAarsaker")
    private Set<PeriodeÅrsak> periodeAarsaker = new HashSet<>();

    @Valid
    @JsonProperty("dagsats")
    @Digits(integer = 8, fraction = 2)
    @DecimalMin("0.00")
    @DecimalMax("10000000.00")
    private Long dagsats;

    @Valid
    @Size()
    @JsonProperty("beregningsgrunnlagPrStatusOgAndel")
    private List<BeregningsgrunnlagPrStatusOgAndelDto> beregningsgrunnlagPrStatusOgAndel;

    public BeregningsgrunnlagPeriodeDto() {
        // trengs for deserialisering av JSON
    }

    public LocalDate getBeregningsgrunnlagPeriodeFom() {
        return beregningsgrunnlagPeriodeFom;
    }

    public LocalDate getBeregningsgrunnlagPeriodeTom() {
        return beregningsgrunnlagPeriodeTom;
    }

    public BigDecimal getBeregnetPrAar() {
        return beregnetPrAar;
    }

    public BigDecimal getBruttoPrAar() {
        return bruttoPrAar;
    }

    public BigDecimal getBruttoInkludertBortfaltNaturalytelsePrAar() {
        return bruttoInkludertBortfaltNaturalytelsePrAar;
    }

    public BigDecimal getAvkortetPrAar() {
        return avkortetPrAar;
    }

    public BigDecimal getRedusertPrAar() {
        return redusertPrAar;
    }

    public Long getDagsats() {
        return dagsats;
    }

    public List<BeregningsgrunnlagPrStatusOgAndelDto> getBeregningsgrunnlagPrStatusOgAndel() {
        return beregningsgrunnlagPrStatusOgAndel;
    }

    public void setBeregningsgrunnlagPeriodeFom(LocalDate beregningsgrunnlagPeriodeFom) {
        this.beregningsgrunnlagPeriodeFom = beregningsgrunnlagPeriodeFom;
    }

    public void setBeregningsgrunnlagPeriodeTom(LocalDate beregningsgrunnlagPeriodeTom) {
        this.beregningsgrunnlagPeriodeTom = beregningsgrunnlagPeriodeTom;
    }

    public void setBeregnetPrAar(BigDecimal beregnetPrAar) {
        this.beregnetPrAar = beregnetPrAar;
    }

    public void setBruttoPrAar(BigDecimal bruttoPrAar) {
        this.bruttoPrAar = bruttoPrAar;
    }

    public void setBruttoInkludertBortfaltNaturalytelsePrAar(BigDecimal bruttoInkludertBortfaltNaturalytelsePrAar) {
        this.bruttoInkludertBortfaltNaturalytelsePrAar = bruttoInkludertBortfaltNaturalytelsePrAar;
    }

    public void setAvkortetPrAar(BigDecimal avkortetPrAar) {
        this.avkortetPrAar = avkortetPrAar;
    }

    public void setRedusertPrAar(BigDecimal redusertPrAar) {
        this.redusertPrAar = redusertPrAar;
    }

    public void setAndeler(List<BeregningsgrunnlagPrStatusOgAndelDto> andeler) {
        this.beregningsgrunnlagPrStatusOgAndel = andeler;
    }

    public void setDagsats(Long dagsats) {
        this.dagsats = dagsats;
    }

    void leggTilPeriodeAarsak(PeriodeÅrsak periodeAarsak) {
        periodeAarsaker.add(periodeAarsak);
    }

    public void leggTilPeriodeAarsaker(List<PeriodeÅrsak> periodeAarsaker) {
        for (PeriodeÅrsak aarsak : periodeAarsaker) {
            leggTilPeriodeAarsak(aarsak);
        }
    }

    public void setPeriodeAarsaker(Set<PeriodeÅrsak> periodeAarsaker) {
        this.periodeAarsaker = periodeAarsaker;
    }

    public Set<PeriodeÅrsak> getPeriodeAarsaker() {
        return periodeAarsaker;
    }

}
