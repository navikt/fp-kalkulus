package no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import java.math.BigDecimal;

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

    @JsonProperty(value = "belopReadOnly")
    private BigDecimal belopReadOnly;

    @JsonProperty(value = "fastsattBelop")
    private BigDecimal fastsattBelop;

    @JsonProperty(value = "inntektskategori")
    private Inntektskategori inntektskategori;

    @JsonProperty(value = "aktivitetStatus")
    private AktivitetStatus aktivitetStatus;

    @JsonProperty(value = "refusjonskrav")
    private BigDecimal refusjonskrav;

    @JsonProperty(value = "visningsnavn")
    private String visningsnavn;

    @JsonProperty(value = "arbeidsforhold")
    private BeregningsgrunnlagArbeidsforholdDto arbeidsforhold;

    @JsonProperty(value = "andelsnr")
    private Long andelsnr;

    @JsonProperty(value = "skalKunneEndreAktivitet")
    private Boolean skalKunneEndreAktivitet;

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
