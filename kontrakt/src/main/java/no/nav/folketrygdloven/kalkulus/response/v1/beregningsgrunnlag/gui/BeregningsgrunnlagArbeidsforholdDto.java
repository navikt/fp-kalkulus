package no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Pattern;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.folketrygdloven.kalkulus.kodeverk.OpptjeningAktivitetType;
import no.nav.folketrygdloven.kalkulus.kodeverk.Organisasjonstype;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = JsonInclude.Include.NON_ABSENT, content = JsonInclude.Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = NONE, setterVisibility = NONE, isGetterVisibility = NONE, creatorVisibility = NONE)
public class BeregningsgrunnlagArbeidsforholdDto {


    @Valid
    @JsonProperty(value = "arbeidsgiverIdent")
    @Pattern(regexp = "^[\\p{Graph}\\p{Space}\\p{Sc}\\p{L}\\p{M}\\p{N}]+$", message = "'${validatedValue}' matcher ikke tillatt pattern '{regexp}'")
    private String arbeidsgiverIdent;

    @Valid
    @JsonProperty(value = "startdato")
    private LocalDate startdato;

    @Valid
    @JsonProperty(value = "opphoersdato")
    private LocalDate opphoersdato;

    @Valid
    @JsonProperty(value = "arbeidsforholdId")
    @Pattern(regexp = "^[\\p{Graph}\\p{Space}\\p{Sc}\\p{L}\\p{M}\\p{N}]+$", message = "'${validatedValue}' matcher ikke tillatt pattern '{regexp}'")
    private String arbeidsforholdId;

    @Valid
    @JsonProperty(value = "eksternArbeidsforholdId")
    @Pattern(regexp = "^[\\p{Graph}\\p{Space}\\p{Sc}\\p{L}\\p{M}\\p{N}]+$", message = "'${validatedValue}' matcher ikke tillatt pattern '{regexp}'")
    private String eksternArbeidsforholdId;

    @Valid
    @JsonProperty(value = "arbeidsforholdType")
    private OpptjeningAktivitetType arbeidsforholdType;

    @Valid
    @JsonProperty(value = "refusjonPrAar")
    @Digits(integer = 8, fraction = 2)
    @DecimalMin("0.00")
    @DecimalMax("10000000.00")
    private BigDecimal refusjonPrAar;

    @Valid
    @JsonProperty(value = "belopFraInntektsmeldingPrMnd")
    @Digits(integer = 8, fraction = 2)
    @DecimalMin("0.00")
    @DecimalMax("10000000.00")
    private BigDecimal belopFraInntektsmeldingPrMnd;

    @Valid
    @JsonProperty(value = "organisasjonstype")
    private Organisasjonstype organisasjonstype;

    @Valid
    @JsonProperty(value = "naturalytelseBortfaltPrÅr")
    @Digits(integer = 8, fraction = 2)
    @DecimalMin("0.00")
    @DecimalMax("10000000.00")
    private BigDecimal naturalytelseBortfaltPrÅr;

    @Valid
    @JsonProperty(value = "naturalytelseTilkommetPrÅr")
    @Digits(integer = 8, fraction = 2)
    @DecimalMin("0.00")
    @DecimalMax("10000000.00")
    private BigDecimal naturalytelseTilkommetPrÅr;

    public BeregningsgrunnlagArbeidsforholdDto() {
        // Hibernate
    }

    public String getArbeidsforholdId() {
        return arbeidsforholdId;
    }

    public void setArbeidsforholdId(String arbeidsforholdId) {
        this.arbeidsforholdId = arbeidsforholdId;
    }

    public LocalDate getStartdato() {
        return startdato;
    }

    public void setStartdato(LocalDate startdato) {
        this.startdato = startdato;
    }

    public LocalDate getOpphoersdato() {
        return opphoersdato;
    }

    public void setOpphoersdato(LocalDate opphoersdato) {
        this.opphoersdato = opphoersdato;
    }

    public OpptjeningAktivitetType getArbeidsforholdType() {
        return arbeidsforholdType;
    }

    public void setArbeidsforholdType(OpptjeningAktivitetType arbeidsforholdType) {
        this.arbeidsforholdType = arbeidsforholdType;
    }

    public String getArbeidsgiverIdent() {
        return arbeidsgiverIdent;
    }

    public void setArbeidsgiverIdent(String arbeidsgiverIdent) {
        this.arbeidsgiverIdent = arbeidsgiverIdent;
    }

    public BigDecimal getRefusjonPrAar() {
        return refusjonPrAar;
    }

    public void setRefusjonPrAar(BigDecimal refusjonPrAar) {
        this.refusjonPrAar = refusjonPrAar;
    }

    public Organisasjonstype getOrganisasjonstype() {
        return organisasjonstype;
    }

    public void setOrganisasjonstype(Organisasjonstype organisasjonstype) {
        this.organisasjonstype = organisasjonstype;
    }

    public BigDecimal getNaturalytelseBortfaltPrÅr() {
        return naturalytelseBortfaltPrÅr;
    }

    public void setNaturalytelseBortfaltPrÅr(BigDecimal naturalytelseBortfaltPrÅr) {
        this.naturalytelseBortfaltPrÅr = naturalytelseBortfaltPrÅr;
    }

    public BigDecimal getNaturalytelseTilkommetPrÅr() {
        return naturalytelseTilkommetPrÅr;
    }

    public void setNaturalytelseTilkommetPrÅr(BigDecimal naturalytelseTilkommetPrÅr) {
        this.naturalytelseTilkommetPrÅr = naturalytelseTilkommetPrÅr;
    }

    public void setEksternArbeidsforholdId(String eksternArbeidsforholdId) {
        this.eksternArbeidsforholdId = eksternArbeidsforholdId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BeregningsgrunnlagArbeidsforholdDto that = (BeregningsgrunnlagArbeidsforholdDto) o;
        return Objects.equals(startdato, that.startdato) &&
                Objects.equals(opphoersdato, that.opphoersdato) &&
                Objects.equals(arbeidsforholdId, that.arbeidsforholdId) &&
                Objects.equals(arbeidsgiverIdent, that.arbeidsgiverIdent) &&
                Objects.equals(eksternArbeidsforholdId, that.eksternArbeidsforholdId) &&
                Objects.equals(arbeidsforholdType, that.arbeidsforholdType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(arbeidsgiverIdent, startdato, opphoersdato, arbeidsforholdId, eksternArbeidsforholdId, arbeidsforholdType);
    }

    public BigDecimal getBelopFraInntektsmeldingPrMnd() {
        return belopFraInntektsmeldingPrMnd;
    }

    public void setBelopFraInntektsmeldingPrMnd(BigDecimal belopFraInntektsmeldingPrMnd) {
        this.belopFraInntektsmeldingPrMnd = belopFraInntektsmeldingPrMnd;
    }
}
