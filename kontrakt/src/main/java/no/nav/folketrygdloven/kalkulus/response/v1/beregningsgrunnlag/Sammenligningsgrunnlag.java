package no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import java.math.BigDecimal;
import java.time.LocalDate;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.folketrygdloven.kalkulus.felles.jpa.IntervallEntitet;
import no.nav.folketrygdloven.kalkulus.felles.v1.Periode;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = Include.NON_ABSENT, content = Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = NONE, setterVisibility = NONE, isGetterVisibility = NONE, creatorVisibility = NONE)
public class Sammenligningsgrunnlag {

    @JsonProperty(value = "sammenligningsperiode")
    @NotNull
    @Valid
    private Periode sammenligningsperiode;

    @JsonProperty(value = "rapportertPrÅr")
    @NotNull
    @Valid
    private BigDecimal rapportertPrÅr;

    @JsonProperty(value = "avvikPromilleNy")
    @NotNull
    @Valid
    private BigDecimal avvikPromilleNy;

    public Sammenligningsgrunnlag(@NotNull @Valid Periode sammenligningsperiode, @NotNull @Valid BigDecimal rapportertPrÅr, @NotNull @Valid BigDecimal avvikPromilleNy) {
        this.sammenligningsperiode = sammenligningsperiode;
        this.rapportertPrÅr = rapportertPrÅr;
        this.avvikPromilleNy = avvikPromilleNy;
    }

    public LocalDate getSammenligningsperiodeFom() {
        return sammenligningsperiode.getFom();
    }

    public LocalDate getSammenligningsperiodeTom() {
        return sammenligningsperiode.getTom();
    }

    public BigDecimal getRapportertPrÅr() {
        return rapportertPrÅr;
    }

    public BigDecimal getAvvikPromilleNy() {
        return avvikPromilleNy;
    }

}
