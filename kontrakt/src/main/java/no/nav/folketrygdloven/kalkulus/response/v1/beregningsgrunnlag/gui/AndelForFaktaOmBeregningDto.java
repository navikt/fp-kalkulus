package no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import java.math.BigDecimal;

import javax.validation.Valid;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Pattern;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.Inntektskategori;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = JsonInclude.Include.NON_ABSENT, content = JsonInclude.Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = NONE, setterVisibility = NONE, isGetterVisibility = NONE, creatorVisibility = NONE)
public class AndelForFaktaOmBeregningDto {

    @Valid
    @JsonProperty(value = "belopReadOnly")
    @Digits(integer = 8, fraction = 2)
    @DecimalMin("0.00")
    @DecimalMax("10000000.00")
    private BigDecimal belopReadOnly;

    @Valid
    @JsonProperty(value = "fastsattBelop")
    @Digits(integer = 8, fraction = 2)
    @DecimalMin("0.00")
    @DecimalMax("10000000.00")
    private BigDecimal fastsattBelop;

    @Valid
    @JsonProperty(value = "inntektskategori")
    private Inntektskategori inntektskategori;

    @Valid
    @JsonProperty(value = "aktivitetStatus")
    private AktivitetStatus aktivitetStatus;

    @Valid
    @JsonProperty(value = "refusjonskrav")
    @Digits(integer = 8, fraction = 2)
    @DecimalMin("0.00")
    @DecimalMax("10000000.00")
    private BigDecimal refusjonskrav;

    @Valid
    @JsonProperty(value = "visningsnavn")
    @Pattern(regexp = "^[\\p{Graph}\\p{Space}\\p{Sc}\\p{L}\\p{M}\\p{N}]+$", message="'${validatedValue}' matcher ikke tillatt pattern '{regexp}'")
    private String visningsnavn;

    @Valid
    @JsonProperty(value = "arbeidsforhold")
    private BeregningsgrunnlagArbeidsforholdDto arbeidsforhold;

    @Valid
    @JsonProperty(value = "andelsnr")
    @Min(0)
    @Max(1000)
    private Long andelsnr;

    @Valid
    @JsonProperty(value = "skalKunneEndreAktivitet")
    private Boolean skalKunneEndreAktivitet;

    @Valid
    @JsonProperty(value = "lagtTilAvSaksbehandler")
    private Boolean lagtTilAvSaksbehandler;

    public BeregningsgrunnlagArbeidsforholdDto getArbeidsforhold() {
        return arbeidsforhold;
    }

    public void setArbeidsforhold(BeregningsgrunnlagArbeidsforholdDto arbeidsforhold) {
        this.arbeidsforhold = arbeidsforhold;
    }

    public AktivitetStatus getAktivitetStatus() {
        return aktivitetStatus;
    }

    public void setAktivitetStatus(AktivitetStatus aktivitetStatus) {
        this.aktivitetStatus = aktivitetStatus;
    }

    public Long getAndelsnr() {
        return andelsnr;
    }

    public void setAndelsnr(Long andelsnr) {
        this.andelsnr = andelsnr;
    }

    public String getVisningsnavn() {
        return visningsnavn;
    }

    public void setVisningsnavn(String visningsnavn) {
        this.visningsnavn = visningsnavn;
    }

    public BigDecimal getRefusjonskrav() {
        return refusjonskrav;
    }

    public void setRefusjonskrav(BigDecimal refusjonskrav) {
        this.refusjonskrav = refusjonskrav;
    }

    public Inntektskategori getInntektskategori() {
        return inntektskategori;
    }

    public void setInntektskategori(Inntektskategori inntektskategori) {
        this.inntektskategori = inntektskategori;
    }

    public BigDecimal getBelopReadOnly() {
        return belopReadOnly;
    }

    public void setBelopReadOnly(BigDecimal belopReadOnly) {
        this.belopReadOnly = belopReadOnly;
    }

    public BigDecimal getFastsattBelop() {
        return fastsattBelop;
    }

    public void setFastsattBelop(BigDecimal fastsattBelop) {
        this.fastsattBelop = fastsattBelop;
    }

    public Boolean getSkalKunneEndreAktivitet() {
        return skalKunneEndreAktivitet;
    }

    public void setSkalKunneEndreAktivitet(Boolean skalKunneEndreAktivitet) {
        this.skalKunneEndreAktivitet = skalKunneEndreAktivitet;
    }

    public Boolean getLagtTilAvSaksbehandler() {
        return lagtTilAvSaksbehandler;
    }

    public void setLagtTilAvSaksbehandler(Boolean lagtTilAvSaksbehandler) {
        this.lagtTilAvSaksbehandler = lagtTilAvSaksbehandler;
    }
}
