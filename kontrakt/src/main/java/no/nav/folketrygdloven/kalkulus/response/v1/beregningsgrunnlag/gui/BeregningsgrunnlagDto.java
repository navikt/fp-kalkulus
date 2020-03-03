package no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.Hjemmel;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = JsonInclude.Include.NON_ABSENT, content = JsonInclude.Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = NONE, setterVisibility = NONE, isGetterVisibility = NONE, creatorVisibility = NONE)
public class BeregningsgrunnlagDto {

    @JsonProperty(value = "skjaeringstidspunktBeregning")
    @NotNull
    @Valid
    private LocalDate skjaeringstidspunktBeregning;

    @JsonProperty(value = "skjæringstidspunkt")
    @NotNull
    @Valid
    private LocalDate skjæringstidspunkt;

    @JsonProperty(value = "aktivitetStatus")
    @NotNull
    @Valid
    private List<AktivitetStatus> aktivitetStatus;

    @JsonProperty(value = "beregningsgrunnlagPeriode")
    @NotNull
    @Valid
    private List<BeregningsgrunnlagPeriodeDto> beregningsgrunnlagPeriode;

    @JsonProperty(value = "sammenligningsgrunnlag")
    @NotNull
    @Valid
    private SammenligningsgrunnlagDto sammenligningsgrunnlag;

    @JsonProperty(value = "sammenligningsgrunnlagPrStatus")
    @Valid
    private List<SammenligningsgrunnlagDto> sammenligningsgrunnlagPrStatus;

    @JsonProperty(value = "ledetekstBrutto")
    @Valid
    private String ledetekstBrutto;

    @JsonProperty(value = "ledetekstAvkortet")
    @Valid
    private String ledetekstAvkortet;

    @JsonProperty(value = "ledetekstRedusert")
    @NotNull
    @Valid
    private String ledetekstRedusert;

    @JsonProperty(value = "halvG")
    @Valid
    private Double halvG;

    @JsonProperty(value = "grunnbeløp")
    @Valid
    private BigDecimal grunnbeløp;

    @JsonProperty(value = "faktaOmBeregning")
    @Valid
    private FaktaOmBeregningDto faktaOmBeregning;

    @JsonProperty(value = "andelerMedGraderingUtenBG")
    @Valid
    private List<BeregningsgrunnlagPrStatusOgAndelDto> andelerMedGraderingUtenBG;

    @JsonProperty(value = "hjemmel")
    @Valid
    private Hjemmel hjemmel;

    @JsonProperty(value = "faktaOmFordeling")
    @Valid
    private FordelingDto faktaOmFordeling;

    @JsonProperty(value = "årsinntektVisningstall")
    @Valid
    private BigDecimal årsinntektVisningstall;

    @JsonProperty(value = "dekningsgrad")
    @Valid
    private int dekningsgrad;

    public BeregningsgrunnlagDto() {
        // trengs for deserialisering av JSON
    }

    public LocalDate getSkjaeringstidspunktBeregning() {
        return skjaeringstidspunktBeregning;
    }

    public List<AktivitetStatus> getAktivitetStatus() {
        return aktivitetStatus;
    }

    public List<BeregningsgrunnlagPeriodeDto> getBeregningsgrunnlagPeriode() {
        return beregningsgrunnlagPeriode;
    }

    public String getLedetekstBrutto() {
        return ledetekstBrutto;
    }

    public String getLedetekstAvkortet() {
        return ledetekstAvkortet;
    }

    public String getLedetekstRedusert() {
        return ledetekstRedusert;
    }

    public SammenligningsgrunnlagDto getSammenligningsgrunnlag() {
        return sammenligningsgrunnlag;
    }

    public Double getHalvG() {
        return halvG;
    }

    public FordelingDto getFaktaOmFordeling() {
        return faktaOmFordeling;
    }

    public FaktaOmBeregningDto getFaktaOmBeregning() {
        return faktaOmBeregning;
    }

    public Hjemmel getHjemmel() {
        return hjemmel;
    }

    public List<SammenligningsgrunnlagDto> getSammenligningsgrunnlagPrStatus() {
        return sammenligningsgrunnlagPrStatus;
    }

    public void setSkjaeringstidspunktBeregning(LocalDate skjaeringstidspunktBeregning) {
        this.skjaeringstidspunktBeregning = skjaeringstidspunktBeregning;
    }

    public void setAktivitetStatus(List<AktivitetStatus> aktivitetStatus) {
        this.aktivitetStatus = aktivitetStatus;
    }

    public void setBeregningsgrunnlagPeriode(List<BeregningsgrunnlagPeriodeDto> perioder) {
        this.beregningsgrunnlagPeriode = perioder;
    }

    public void setSammenligningsgrunnlag(SammenligningsgrunnlagDto sammenligningsgrunnlag) {
        this.sammenligningsgrunnlag = sammenligningsgrunnlag;
    }

    public void setLedetekstBrutto(String ledetekstBrutto) {
        this.ledetekstBrutto = ledetekstBrutto;
    }

    public void setLedetekstAvkortet(String ledetekstAvkortet) {
        this.ledetekstAvkortet = ledetekstAvkortet;
    }

    public void setLedetekstRedusert(String ledetekstRedusert) {
        this.ledetekstRedusert = ledetekstRedusert;
    }

    public void setHalvG(Double halvG) {
        this.halvG = halvG;
    }

    public void setFaktaOmBeregning(FaktaOmBeregningDto faktaOmBeregning) {
        this.faktaOmBeregning = faktaOmBeregning;
    }

    public List<BeregningsgrunnlagPrStatusOgAndelDto> getAndelerMedGraderingUtenBG() {
        return andelerMedGraderingUtenBG;
    }

    public void setAndelerMedGraderingUtenBG(List<BeregningsgrunnlagPrStatusOgAndelDto> andelerMedGraderingUtenBG) {
        this.andelerMedGraderingUtenBG = andelerMedGraderingUtenBG;
    }

    public void setHjemmel(Hjemmel hjemmel) {
        this.hjemmel = hjemmel;
    }

    public void setFaktaOmFordeling(FordelingDto faktaOmFordelingDto) {
        this.faktaOmFordeling = faktaOmFordelingDto;
    }

    public BigDecimal getÅrsinntektVisningstall() {
        return årsinntektVisningstall;
    }

    public void setÅrsinntektVisningstall(BigDecimal årsinntektVisningstall) {
        this.årsinntektVisningstall = årsinntektVisningstall;
    }

    public int getDekningsgrad() {
        return dekningsgrad;
    }

    public void setDekningsgrad(int dekningsgrad) {
        this.dekningsgrad = dekningsgrad;
    }

    public LocalDate getSkjæringstidspunkt() {
        return skjæringstidspunkt;
    }

    public void setSkjæringstidspunkt(LocalDate skjæringstidspunkt) {
        this.skjæringstidspunkt = skjæringstidspunkt;
    }

    public BigDecimal getGrunnbeløp() {
        return grunnbeløp;
    }

    public void setGrunnbeløp(BigDecimal grunnbeløp) {
        this.grunnbeløp = grunnbeløp;
    }

    public void setSammenligningsgrunnlagPrStatus(List<SammenligningsgrunnlagDto> sammenligningsgrunnlagPrStatus) {
        this.sammenligningsgrunnlagPrStatus = sammenligningsgrunnlagPrStatus;
    }
}
