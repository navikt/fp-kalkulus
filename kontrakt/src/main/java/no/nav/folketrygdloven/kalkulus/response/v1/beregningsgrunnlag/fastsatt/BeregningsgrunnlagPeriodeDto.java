package no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.fastsatt;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.folketrygdloven.kalkulus.felles.v1.Periode;
import no.nav.folketrygdloven.kalkulus.kodeverk.PeriodeÅrsak;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = Include.NON_ABSENT, content = Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = NONE, setterVisibility = NONE, isGetterVisibility = NONE, creatorVisibility = NONE)
public class BeregningsgrunnlagPeriodeDto {

    @JsonProperty(value = "beregningsgrunnlagPrStatusOgAndelList")
    @NotNull
    @Valid
    private List<BeregningsgrunnlagPrStatusOgAndelDto> beregningsgrunnlagPrStatusOgAndelList;

    @JsonProperty(value = "periode")
    @NotNull
    @Valid
    private Periode periode;

    @JsonProperty(value = "bruttoPrÅr")
    @Valid
    private BigDecimal bruttoPrÅr;

    @JsonProperty(value = "avkortetPrÅr")
    @Valid
    private BigDecimal avkortetPrÅr;

    @JsonProperty(value = "redusertPrÅr")
    @Valid
    private BigDecimal redusertPrÅr;

    @JsonProperty(value = "dagsats")
    @Valid
    private Long dagsats;

    @JsonProperty(value = "periodeÅrsaker")
    @NotNull
    @Valid
    private List<PeriodeÅrsak> periodeÅrsaker;

    public BeregningsgrunnlagPeriodeDto() {
    }

    public BeregningsgrunnlagPeriodeDto(@JsonProperty(value = "beregningsgrunnlagPrStatusOgAndelList") @NotNull @Valid List<BeregningsgrunnlagPrStatusOgAndelDto> beregningsgrunnlagPrStatusOgAndelList,
                                        @JsonProperty(value = "periode") @NotNull @Valid Periode periode,
                                        @JsonProperty(value = "bruttoPrÅr") @Valid BigDecimal bruttoPrÅr,
                                        @JsonProperty(value = "avkortetPrÅr") @Valid BigDecimal avkortetPrÅr,
                                        @JsonProperty(value = "redusertPrÅr") @Valid BigDecimal redusertPrÅr,
                                        @JsonProperty(value = "dagsats") @Valid Long dagsats,
                                        @JsonProperty(value = "periodeÅrsaker") @NotNull @Valid List<PeriodeÅrsak> periodeÅrsaker) {

        this.beregningsgrunnlagPrStatusOgAndelList = beregningsgrunnlagPrStatusOgAndelList;
        this.periode = periode;
        this.bruttoPrÅr = bruttoPrÅr;
        this.avkortetPrÅr = avkortetPrÅr;
        this.redusertPrÅr = redusertPrÅr;
        this.dagsats = dagsats;
        this.periodeÅrsaker = periodeÅrsaker;
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
}
