package no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import java.time.LocalDate;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.folketrygdloven.kalkulus.kodeverk.OpptjeningAktivitetType;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = JsonInclude.Include.NON_ABSENT, content = JsonInclude.Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = NONE, setterVisibility = NONE, isGetterVisibility = NONE, creatorVisibility = NONE)
public class BeregningAktivitetDto {

    @JsonProperty(value = "arbeidsgiverNavn")
    private String arbeidsgiverNavn;

    @JsonProperty(value = "arbeidsgiverId")
    private String arbeidsgiverId;

    @JsonProperty(value = "eksternArbeidsforholdId")
    private String eksternArbeidsforholdId;

    @JsonProperty(value = "fom")
    private LocalDate fom;

    @JsonProperty(value = "tom")
    private LocalDate tom;

    /** For virksomheter - orgnr. For personlige arbeidsgiver - aktørId. */
    @JsonProperty(value = "arbeidsforholdId")
    private String arbeidsforholdId;

    @JsonProperty(value = "arbeidsforholdType")
    private OpptjeningAktivitetType arbeidsforholdType;

    @JsonProperty(value = "aktørIdString")
    private String aktørIdString;

    @JsonProperty(value = "skalBrukes")
    private Boolean skalBrukes;

    public BeregningAktivitetDto() {
        // jackson
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

    public LocalDate getFom() {
        return fom;
    }

    public void setFom(LocalDate fom) {
        this.fom = fom;
    }

    public LocalDate getTom() {
        return tom;
    }

    public void setTom(LocalDate tom) {
        this.tom = tom;
    }

    public OpptjeningAktivitetType getArbeidsforholdType() {
        return arbeidsforholdType;
    }

    public void setArbeidsforholdType(OpptjeningAktivitetType arbeidsforholdType) {
        this.arbeidsforholdType = arbeidsforholdType;
    }

    public String getAktørIdString() {
        return aktørIdString;
    }

    public void setAktørIdString(String aktørIdString) {
        this.aktørIdString = aktørIdString;
    }

    public Boolean getSkalBrukes() {
        return skalBrukes;
    }

    public void setSkalBrukes(Boolean skalBrukes) {
        this.skalBrukes = skalBrukes;
    }

    public String getEksternArbeidsforholdId() {
        return eksternArbeidsforholdId;
    }

    public void setEksternArbeidsforholdId(String eksternArbeidsforholdId) {
        this.eksternArbeidsforholdId = eksternArbeidsforholdId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BeregningAktivitetDto that = (BeregningAktivitetDto) o;
        return Objects.equals(arbeidsgiverId, that.arbeidsgiverId) &&
            Objects.equals(fom, that.fom) &&
            Objects.equals(tom, that.tom) &&
            Objects.equals(arbeidsforholdId, that.arbeidsforholdId) &&
            Objects.equals(eksternArbeidsforholdId, that.eksternArbeidsforholdId) &&
            Objects.equals(arbeidsforholdType, that.arbeidsforholdType) &&
            Objects.equals(aktørIdString, that.aktørIdString);
    }

    @Override
    public int hashCode() {
        return Objects.hash(arbeidsgiverId, fom, tom, arbeidsforholdId, eksternArbeidsforholdId, arbeidsforholdType, aktørIdString);
    }
}
