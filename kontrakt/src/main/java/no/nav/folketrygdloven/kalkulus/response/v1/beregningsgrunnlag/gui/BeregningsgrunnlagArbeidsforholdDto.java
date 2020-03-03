package no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.folketrygdloven.kalkulus.felles.v1.AktørId;
import no.nav.folketrygdloven.kalkulus.felles.v1.AktørIdPersonident;
import no.nav.folketrygdloven.kalkulus.kodeverk.OpptjeningAktivitetType;
import no.nav.folketrygdloven.kalkulus.kodeverk.Organisasjonstype;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = JsonInclude.Include.NON_ABSENT, content = JsonInclude.Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = NONE, setterVisibility = NONE, isGetterVisibility = NONE, creatorVisibility = NONE)
public class BeregningsgrunnlagArbeidsforholdDto {

    @JsonProperty(value = "arbeidsgiverNavn")
    private String arbeidsgiverNavn;

    @JsonProperty(value = "arbeidsgiverId")
    private String arbeidsgiverId;

    @JsonProperty(value = "startdato")
    private LocalDate startdato;

    @JsonProperty(value = "opphoersdato")
    private LocalDate opphoersdato;

    @JsonProperty(value = "arbeidsforholdId")
    private String arbeidsforholdId;

    @JsonProperty(value = "eksternArbeidsforholdId")
    private String eksternArbeidsforholdId;

    @JsonProperty(value = "arbeidsforholdType")
    private OpptjeningAktivitetType arbeidsforholdType;

    @JsonProperty(value = "aktørId")
    private AktørId aktørId;

    @JsonProperty(value = "aktørIdPersonIdent")
    private AktørIdPersonident aktørIdPersonIdent;

    @JsonProperty(value = "refusjonPrAar")
    private BigDecimal refusjonPrAar;

    @JsonProperty(value = "belopFraInntektsmeldingPrMnd")
    private BigDecimal belopFraInntektsmeldingPrMnd;

    @JsonProperty(value = "organisasjonstype")
    private Organisasjonstype organisasjonstype;

    @JsonProperty(value = "naturalytelseBortfaltPrÅr")
    private BigDecimal naturalytelseBortfaltPrÅr;

    @JsonProperty(value = "naturalytelseTilkommetPrÅr")
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

    public String getArbeidsgiverNavn() {
        return arbeidsgiverNavn;
    }

    public void setArbeidsgiverNavn(String arbeidsgiverNavn) {
        this.arbeidsgiverNavn = arbeidsgiverNavn;
    }

    public String getArbeidsgiverId() {
        return arbeidsgiverId;
    }

    public void setArbeidsgiverId(String arbeidsgiverId) {
        this.arbeidsgiverId = arbeidsgiverId;
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

    public void setAktørId(AktørId aktørId) {
        this.aktørId = aktørId;
    }

    public AktørId getAktørId() {
        return aktørId;
    }

    public AktørIdPersonident getAktørIdPersonIdent() {
        return aktørIdPersonIdent;
    }

    public void setAktørIdPersonIdent(AktørIdPersonident aktørIdPersonIdent) {
        this.aktørIdPersonIdent = aktørIdPersonIdent;
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
        return Objects.equals(arbeidsgiverNavn, that.arbeidsgiverNavn) &&
            Objects.equals(arbeidsgiverId, that.arbeidsgiverId) &&
            Objects.equals(startdato, that.startdato) &&
            Objects.equals(opphoersdato, that.opphoersdato) &&
            Objects.equals(arbeidsforholdId, that.arbeidsforholdId) &&
            Objects.equals(eksternArbeidsforholdId, that.eksternArbeidsforholdId) &&
            Objects.equals(arbeidsforholdType, that.arbeidsforholdType) &&
            Objects.equals(aktørId, that.aktørId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(arbeidsgiverNavn, arbeidsgiverId, startdato, opphoersdato, arbeidsforholdId, eksternArbeidsforholdId, arbeidsforholdType, aktørId);
    }

    public BigDecimal getBelopFraInntektsmeldingPrMnd() {
        return belopFraInntektsmeldingPrMnd;
    }

    public void setBelopFraInntektsmeldingPrMnd(BigDecimal belopFraInntektsmeldingPrMnd) {
        this.belopFraInntektsmeldingPrMnd = belopFraInntektsmeldingPrMnd;
    }
}
