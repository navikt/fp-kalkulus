package no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.Inntektskategori;


@JsonAutoDetect(getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE, fieldVisibility = Visibility.ANY)
@JsonTypeInfo(
        use=JsonTypeInfo.Id.NAME,
        include=JsonTypeInfo.As.PROPERTY,
        property="dtoType")
@JsonSubTypes({
        @JsonSubTypes.Type(value=BeregningsgrunnlagPrStatusOgAndelATDto.class, name= BeregningsgrunnlagPrStatusOgAndelATDto.DTO_TYPE),
        @JsonSubTypes.Type(value=BeregningsgrunnlagPrStatusOgAndelSNDto.class, name= BeregningsgrunnlagPrStatusOgAndelSNDto.DTO_TYPE),
        @JsonSubTypes.Type(value=BeregningsgrunnlagPrStatusOgAndelFLDto.class, name= BeregningsgrunnlagPrStatusOgAndelFLDto.DTO_TYPE),
        @JsonSubTypes.Type(value=BeregningsgrunnlagPrStatusOgAndelYtelseDto.class, name= BeregningsgrunnlagPrStatusOgAndelYtelseDto.DTO_TYPE),
        @JsonSubTypes.Type(value=BeregningsgrunnlagPrStatusOgAndelDtoFelles.class, name= BeregningsgrunnlagPrStatusOgAndelDtoFelles.DTO_TYPE)
})
public abstract class BeregningsgrunnlagPrStatusOgAndelDto {

    @Valid
    @JsonProperty("aktivitetStatus")
    private AktivitetStatus aktivitetStatus;

    @Valid
    @JsonProperty("beregningsperiodeFom")
    private LocalDate beregningsperiodeFom;

    @Valid
    @JsonProperty("beregningsperiodeTom")
    private LocalDate beregningsperiodeTom;

    @Valid
    @JsonProperty("beregnetPrAar")
    @Digits(integer = 8, fraction = 2)
    @DecimalMin("0.00")
    @DecimalMax("10000000.00")
    private BigDecimal beregnetPrAar;

    @Valid
    @JsonProperty("overstyrtPrAar")
    @Digits(integer = 8, fraction = 2)
    @DecimalMin("0.00")
    @DecimalMax("10000000.00")
    private BigDecimal overstyrtPrAar;

    @Valid
    @JsonProperty("bruttoPrAar")
    @Digits(integer = 8, fraction = 2)
    @DecimalMin("0.00")
    @DecimalMax("10000000.00")
    private BigDecimal bruttoPrAar;

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
    @JsonProperty("erTidsbegrensetArbeidsforhold")
    private Boolean erTidsbegrensetArbeidsforhold;

    @Valid
    @JsonProperty("erNyIArbeidslivet")
    private Boolean erNyIArbeidslivet;

    @Valid
    @JsonProperty("lonnsendringIBeregningsperioden")
    private Boolean lonnsendringIBeregningsperioden;

    @Valid
    @JsonProperty("andelsnr")
    @Min(1)
    @Max(1000)
    private Long andelsnr;

    @Valid
    @JsonProperty("besteberegningPrAar")
    @Digits(integer = 8, fraction = 2)
    @DecimalMin("0.00")
    @DecimalMax("10000000.00")
    private BigDecimal besteberegningPrAar;

    @Valid
    @JsonProperty("inntektskategori")
    private Inntektskategori inntektskategori;

    @Valid
    @JsonProperty("arbeidsforhold")
    private BeregningsgrunnlagArbeidsforholdDto arbeidsforhold;

    @Valid
    @JsonProperty("fastsattAvSaksbehandler")
    private Boolean fastsattAvSaksbehandler;

    @Valid
    @JsonProperty("lagtTilAvSaksbehandler")
    private Boolean lagtTilAvSaksbehandler;

    @Valid
    @JsonProperty("belopPrMndEtterAOrdningen")
    @Digits(integer = 8, fraction = 2)
    @DecimalMin("0.00")
    @DecimalMax("10000000.00")
    private BigDecimal belopPrMndEtterAOrdningen;

    @Valid
    @JsonProperty("belopPrAarEtterAOrdningen")
    @Digits(integer = 8, fraction = 2)
    @DecimalMin("0.00")
    @DecimalMax("10000000.00")
    private BigDecimal belopPrAarEtterAOrdningen;

    @Valid
    @JsonProperty("dagsats")
    @Min(0)
    @Max(1000000)
    private Long dagsats;

    @Valid
    @Min(0)
    @Max(1000000)
    @JsonProperty("originalDagsatsFraTilstøtendeYtelse")
    private Long originalDagsatsFraTilstøtendeYtelse;

    @Valid
    @JsonProperty("fordeltPrAar")
    @Digits(integer = 8, fraction = 2)
    @DecimalMin("0.00")
    @DecimalMax("10000000.00")
    private BigDecimal fordeltPrAar;

    @Valid
    @JsonProperty("erTilkommetAndel")
    private Boolean erTilkommetAndel;

    @Valid
    @JsonProperty("skalFastsetteGrunnlag")
    private Boolean skalFastsetteGrunnlag;

    public BeregningsgrunnlagPrStatusOgAndelDto() {
        // trengs for deserialisering av JSON
    }

    public BigDecimal getBeregnetPrAar() {
        return beregnetPrAar;
    }

    public BigDecimal getOverstyrtPrAar() {
        return overstyrtPrAar;
    }

    public BigDecimal getBruttoPrAar() {
        return bruttoPrAar;
    }

    public AktivitetStatus getAktivitetStatus() {
        return aktivitetStatus;
    }

    public BigDecimal getAvkortetPrAar() {
        return avkortetPrAar;
    }

    public BigDecimal getRedusertPrAar() {
        return redusertPrAar;
    }

    public Boolean getErTidsbegrensetArbeidsforhold() {
        return erTidsbegrensetArbeidsforhold;
    }

    public Boolean getErNyIArbeidslivet() {
        return erNyIArbeidslivet;
    }

    public Long getAndelsnr() {
        return andelsnr;
    }

    public Boolean getLonnsendringIBeregningsperioden() {
        return lonnsendringIBeregningsperioden;
    }

    public BigDecimal getBesteberegningPrAar() {
        return besteberegningPrAar;
    }

    public BeregningsgrunnlagArbeidsforholdDto getArbeidsforhold() {
        return arbeidsforhold;
    }

    public BigDecimal getFordeltPrAar() {
        return fordeltPrAar;
    }

    public Boolean getSkalFastsetteGrunnlag() {
        return skalFastsetteGrunnlag;
    }

    public LocalDate getBeregningsperiodeFom() {
        return beregningsperiodeFom;
    }

    public LocalDate getBeregningsperiodeTom() {
        return beregningsperiodeTom;
    }

    public void setFordeltPrAar(BigDecimal fordeltPrAar) {
        this.fordeltPrAar = fordeltPrAar;
    }

    public void setArbeidsforhold(BeregningsgrunnlagArbeidsforholdDto arbeidsforhold) {
        this.arbeidsforhold = arbeidsforhold;
    }

    public void setBesteberegningPrAar(BigDecimal besteberegningPrAar) {
        this.besteberegningPrAar = besteberegningPrAar;
    }

    public void setLonnsendringIBeregningsperioden(Boolean lonnsendringIBeregningsperioden) {
        this.lonnsendringIBeregningsperioden = lonnsendringIBeregningsperioden;
    }

    public void setBeregningsperiodeFom(LocalDate beregningsperiodeFom) {
        this.beregningsperiodeFom = beregningsperiodeFom;
    }

    public void setBeregningsperiodeTom(LocalDate beregningsperiodeTom) {
        this.beregningsperiodeTom = beregningsperiodeTom;
    }

    public void setBeregnetPrAar(BigDecimal beregnetPrAar) {
        this.beregnetPrAar = beregnetPrAar;
    }

    public void setOverstyrtPrAar(BigDecimal overstyrtPrAar) {
        this.overstyrtPrAar = overstyrtPrAar;
    }

    public void setBruttoPrAar(BigDecimal bruttoPrAar) {
        this.bruttoPrAar = bruttoPrAar;
    }

    public void setAktivitetStatus(AktivitetStatus aktivitetStatus) {
        this.aktivitetStatus = aktivitetStatus;
    }

    public void setAvkortetPrAar(BigDecimal avkortetPrAar) {
        this.avkortetPrAar = avkortetPrAar;
    }

    public void setRedusertPrAar(BigDecimal redusertPrAar) {
        this.redusertPrAar = redusertPrAar;
    }

    public void setErTidsbegrensetArbeidsforhold(Boolean erTidsbegrensetArbeidsforhold) {
        this.erTidsbegrensetArbeidsforhold = erTidsbegrensetArbeidsforhold;
    }

    public void setAndelsnr(Long andelsnr) {
        this.andelsnr = andelsnr;
    }

    public void setErNyIArbeidslivet(Boolean erNyIArbeidslivet) {
        this.erNyIArbeidslivet = erNyIArbeidslivet;
    }

    public Inntektskategori getInntektskategori() {
        return inntektskategori;
    }

    public void setInntektskategori(Inntektskategori inntektskategori) {
        this.inntektskategori = inntektskategori;
    }

    public Boolean getFastsattAvSaksbehandler() {
        return fastsattAvSaksbehandler;
    }

    public void setFastsattAvSaksbehandler(Boolean fastsattAvSaksbehandler) {
        this.fastsattAvSaksbehandler = fastsattAvSaksbehandler;
    }

    public Boolean getLagtTilAvSaksbehandler() {
        return lagtTilAvSaksbehandler;
    }

    public void setLagtTilAvSaksbehandler(Boolean lagtTilAvSaksbehandler) {
        this.lagtTilAvSaksbehandler = lagtTilAvSaksbehandler;
    }

    public Long getDagsats() {
        return dagsats;
    }

    public void setDagsats(Long dagsats) {
        this.dagsats = dagsats;
    }

    public Long getOriginalDagsatsFraTilstøtendeYtelse() {
        return originalDagsatsFraTilstøtendeYtelse;
    }

    public void setOriginalDagsatsFraTilstøtendeYtelse(Long originalDagsatsFraTilstøtendeYtelse) {
        this.originalDagsatsFraTilstøtendeYtelse = originalDagsatsFraTilstøtendeYtelse;
    }

    public Boolean getErTilkommetAndel() {
        return erTilkommetAndel;
    }

    public void setErTilkommetAndel(Boolean erTilkommetAndel) {
        this.erTilkommetAndel = erTilkommetAndel;
    }

    public void setSkalFastsetteGrunnlag(Boolean skalFastsetteGrunnlag) {
        this.skalFastsetteGrunnlag = skalFastsetteGrunnlag;
    }
}
