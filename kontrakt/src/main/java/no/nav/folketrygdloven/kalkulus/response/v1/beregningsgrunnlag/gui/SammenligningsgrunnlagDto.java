package no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.folketrygdloven.kalkulus.kodeverk.SammenligningsgrunnlagType;


@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = JsonInclude.Include.NON_ABSENT, content = JsonInclude.Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = NONE, setterVisibility = NONE, isGetterVisibility = NONE, creatorVisibility = NONE)
public class SammenligningsgrunnlagDto {

    @JsonProperty(value = "sammenligningsgrunnlagFom")
    private LocalDate sammenligningsgrunnlagFom;

    @JsonProperty(value = "sammenligningsgrunnlagTom")
    private LocalDate sammenligningsgrunnlagTom;

    @JsonProperty(value = "rapportertPrAar")
    private BigDecimal rapportertPrAar;

    @JsonProperty(value = "avvikPromille")
    private BigDecimal avvikPromille;

    @JsonProperty(value = "avvikProsent")
    private BigDecimal avvikProsent;

    @JsonProperty(value = "sammenligningsgrunnlagType")
    private SammenligningsgrunnlagType sammenligningsgrunnlagType;

    @JsonProperty(value = "differanseBeregnet")
    private BigDecimal differanseBeregnet;

    public SammenligningsgrunnlagDto() {
        // trengs for deserialisering av JSON
    }

    public LocalDate getSammenligningsgrunnlagFom() {
        return sammenligningsgrunnlagFom;
    }

    public LocalDate getSammenligningsgrunnlagTom() {
        return sammenligningsgrunnlagTom;
    }

    public BigDecimal getRapportertPrAar() {
        return rapportertPrAar;
    }

    public BigDecimal getAvvikPromille() {
        return avvikPromille;
    }

    public SammenligningsgrunnlagType getSammenligningsgrunnlagType() {
        return sammenligningsgrunnlagType;
    }

    public BigDecimal getDifferanseBeregnet() {
        return differanseBeregnet;
    }

    public void setSammenligningsgrunnlagFom(LocalDate sammenligningsgrunnlagFom) {
        this.sammenligningsgrunnlagFom = sammenligningsgrunnlagFom;
    }

    public void setSammenligningsgrunnlagTom(LocalDate sammenligningsgrunnlagTom) {
        this.sammenligningsgrunnlagTom = sammenligningsgrunnlagTom;
    }

    public void setRapportertPrAar(BigDecimal rapportertPrAar) {
        this.rapportertPrAar = rapportertPrAar;
    }

    public void setAvvikPromille(BigDecimal avvikPromille) {
        this.avvikPromille = avvikPromille;
    }

    public BigDecimal getAvvikProsent() {
        return avvikProsent;
    }

    public void setAvvikProsent(BigDecimal avvikProsent) {
        this.avvikProsent = avvikProsent;
    }

    public void setSammenligningsgrunnlagType(SammenligningsgrunnlagType sammenligningsgrunnlagType) {
        this.sammenligningsgrunnlagType = sammenligningsgrunnlagType;
    }

    public void setDifferanseBeregnet(BigDecimal differanseBeregnet) {
        this.differanseBeregnet = differanseBeregnet;
    }
}
