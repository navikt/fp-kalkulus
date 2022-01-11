package no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.detaljert;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import javax.validation.Valid;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.FaktaOmBeregningTilfelle;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = Include.NON_ABSENT, content = Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = NONE, setterVisibility = NONE, isGetterVisibility = NONE, creatorVisibility = NONE)
public class BeregningsgrunnlagDto {

    @JsonProperty(value = "skjæringstidspunkt")
    @NotNull
    @Valid
    private LocalDate skjæringstidspunkt;

    @JsonProperty(value = "aktivitetStatuser")
    @NotNull
    @Size(min = 1, max = 20)
    @Valid
    private List<AktivitetStatus> aktivitetStatuser;

    @JsonProperty(value = "beregningsgrunnlagPerioder")
    @NotNull
    @Size(min = 1, max = 100)
    @Valid
    private List<BeregningsgrunnlagPeriodeDto> beregningsgrunnlagPerioder;

    @JsonProperty(value = "sammenligningsgrunnlag")
    @Valid
    private Sammenligningsgrunnlag sammenligningsgrunnlag;

    @JsonProperty(value = "sammenligningsgrunnlagPrStatusListe")
    @Size(max = 10)
    @Valid
    private List<SammenligningsgrunnlagPrStatusDto> sammenligningsgrunnlagPrStatusListe;

    @JsonProperty(value = "faktaOmBeregningTilfeller")
    @Size(max = 50)
    @Valid
    private List<FaktaOmBeregningTilfelle> faktaOmBeregningTilfeller;

    @JsonProperty(value = "overstyrt")
    private boolean overstyrt;

    @JsonProperty(value = "grunnbeløp")
    @Valid
    @DecimalMin(value = "0.00", message = "verdien ${validatedValue} må være >= {value}")
    @DecimalMax(value = "1000000000.00", message = "verdien ${validatedValue} må være <= {value}")
    @Digits(integer = 10, fraction = 2)
    private BigDecimal grunnbeløp;

    public BeregningsgrunnlagDto() {
    }

    public BeregningsgrunnlagDto(@NotNull @Valid LocalDate skjæringstidspunkt,
                                 @NotNull @Size(min = 1, max = 20) @Valid List<AktivitetStatus> aktivitetStatuser,
                                 @NotNull @Size(min = 1, max = 100) @Valid List<BeregningsgrunnlagPeriodeDto> beregningsgrunnlagPerioder,
                                 @Valid Sammenligningsgrunnlag sammenligningsgrunnlag,
                                 @Size(max = 10) @Valid List<SammenligningsgrunnlagPrStatusDto> sammenligningsgrunnlagPrStatusListe,
                                 @Size(max = 50) @Valid List<FaktaOmBeregningTilfelle> faktaOmBeregningTilfeller,
                                 boolean overstyrt,
                                 @Valid BigDecimal grunnbeløp) {
        this.skjæringstidspunkt = skjæringstidspunkt;
        this.aktivitetStatuser = aktivitetStatuser;
        this.beregningsgrunnlagPerioder = beregningsgrunnlagPerioder;
        this.sammenligningsgrunnlag = sammenligningsgrunnlag;
        this.sammenligningsgrunnlagPrStatusListe = sammenligningsgrunnlagPrStatusListe;
        this.faktaOmBeregningTilfeller = faktaOmBeregningTilfeller;
        this.overstyrt = overstyrt;
        this.grunnbeløp = grunnbeløp;
    }

    public LocalDate getSkjæringstidspunkt() {
        return skjæringstidspunkt;
    }

    public List<AktivitetStatus> getAktivitetStatuser() {
        return Collections.unmodifiableList(aktivitetStatuser);
    }

    public List<BeregningsgrunnlagPeriodeDto> getBeregningsgrunnlagPerioder() {
        return beregningsgrunnlagPerioder
            .stream()
            .sorted(Comparator.comparing(BeregningsgrunnlagPeriodeDto::getBeregningsgrunnlagPeriodeFom))
            .collect(Collectors.toUnmodifiableList());
    }

    public Sammenligningsgrunnlag getSammenligningsgrunnlag() {
        return sammenligningsgrunnlag;
    }

    public List<FaktaOmBeregningTilfelle> getFaktaOmBeregningTilfeller() {
        return faktaOmBeregningTilfeller;
    }

    public List<SammenligningsgrunnlagPrStatusDto> getSammenligningsgrunnlagPrStatusListe() {
        return sammenligningsgrunnlagPrStatusListe;
    }

    public boolean isOverstyrt() {
        return overstyrt;
    }

    public BigDecimal getGrunnbeløp() {
        return grunnbeløp;
    }
}
