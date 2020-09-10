package no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.refusjon;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.response.v1.Arbeidsgiver;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = JsonInclude.Include.NON_ABSENT, content = JsonInclude.Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = NONE, setterVisibility = NONE, isGetterVisibility = NONE, creatorVisibility = NONE)
public class RefusjonAndelTilVurderingDto {

    @Valid
    @JsonProperty("aktivitetStatus")
    @NotNull
    private AktivitetStatus aktivitetStatus;

    @Valid
    @JsonProperty("tidligereUtbetalinger")
    @Size(min = 1)
    private List<TidligereUtbetalingDto> tidligereUtbetalinger;

    @Valid
    @JsonProperty("nyttRefusjonskravFom")
    private LocalDate nyttRefusjonskravFom;

    @Valid
    @JsonProperty("fastsattNyttRefusjonskravFom")
    private LocalDate fastsattNyttRefusjonskravFom;

    @Valid
    @JsonProperty("tidligsteMuligeRefusjonsdato")
    private LocalDate tidligsteMuligeRefusjonsdato;

    @Valid
    @JsonProperty("arbeidsgiverId")
    private Arbeidsgiver arbeidsgiverId;

    @Valid
    @JsonProperty("arbeidsgiverNavn")
    @Pattern(regexp = "^[\\p{Graph}\\p{Space}\\p{Sc}\\p{L}\\p{M}\\p{N}]+$", message="'${validatedValue}' matcher ikke tillatt pattern '{regexp}'")
    private String arbeidsgiverNavn;

    @JsonProperty(value = "internArbeidsforholdRef")
    @Valid
    @Pattern(regexp = "^[\\p{Graph}\\p{Space}\\p{Sc}\\p{L}\\p{M}\\p{N}]+$", message="'${validatedValue}' matcher ikke tillatt pattern '{regexp}'")
    private String internArbeidsforholdRef;

    @JsonProperty(value = "eksternArbeidsforholdRef")
    @Valid
    @Pattern(regexp = "^[\\p{Graph}\\p{Space}\\p{Sc}\\p{L}\\p{M}\\p{N}]+$", message="'${validatedValue}' matcher ikke tillatt pattern '{regexp}'")
    private String eksternArbeidsforholdRef;

    public AktivitetStatus getAktivitetStatus() {
        return aktivitetStatus;
    }

    public void setAktivitetStatus(AktivitetStatus aktivitetStatus) {
        this.aktivitetStatus = aktivitetStatus;
    }

    public List<TidligereUtbetalingDto> getTidligereUtbetalinger() {
        return tidligereUtbetalinger;
    }

    public void setTidligereUtbetalinger(List<TidligereUtbetalingDto> tidligereUtbetalinger) {
        this.tidligereUtbetalinger = tidligereUtbetalinger;
    }

    public LocalDate getNyttRefusjonskravFom() {
        return nyttRefusjonskravFom;
    }

    public void setNyttRefusjonskravFom(LocalDate nyttRefusjonskravFom) {
        this.nyttRefusjonskravFom = nyttRefusjonskravFom;
    }

    public LocalDate getFastsattNyttRefusjonskravFom() {
        return fastsattNyttRefusjonskravFom;
    }

    public void setFastsattNyttRefusjonskravFom(LocalDate fastsattNyttRefusjonskravFom) {
        this.fastsattNyttRefusjonskravFom = fastsattNyttRefusjonskravFom;
    }

    public Arbeidsgiver getArbeidsgiverId() {
        return arbeidsgiverId;
    }

    public void setArbeidsgiverId(Arbeidsgiver arbeidsgiverId) {
        this.arbeidsgiverId = arbeidsgiverId;
    }

    public String getInternArbeidsforholdRef() {
        return internArbeidsforholdRef;
    }

    public void setInternArbeidsforholdRef(String internArbeidsforholdRef) {
        this.internArbeidsforholdRef = internArbeidsforholdRef;
    }

    public String getEksternArbeidsforholdRef() {
        return eksternArbeidsforholdRef;
    }

    public void setEksternArbeidsforholdRef(String eksternArbeidsforholdRef) {
        this.eksternArbeidsforholdRef = eksternArbeidsforholdRef;
    }

    public String getArbeidsgiverNavn() {
        return arbeidsgiverNavn;
    }

    public void setArbeidsgiverNavn(String arbeidsgiverNavn) {
        this.arbeidsgiverNavn = arbeidsgiverNavn;
    }

    public LocalDate getTidligsteMuligeRefusjonsdato() {
        return tidligsteMuligeRefusjonsdato;
    }

    public void setTidligsteMuligeRefusjonsdato(LocalDate tidligsteMuligeRefusjonsdato) {
        this.tidligsteMuligeRefusjonsdato = tidligsteMuligeRefusjonsdato;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RefusjonAndelTilVurderingDto that = (RefusjonAndelTilVurderingDto) o;
        return Objects.equals(aktivitetStatus, that.aktivitetStatus) &&
                Objects.equals(tidligereUtbetalinger, that.tidligereUtbetalinger) &&
                Objects.equals(nyttRefusjonskravFom, that.nyttRefusjonskravFom) &&
                Objects.equals(arbeidsgiverNavn, that.arbeidsgiverNavn) &&
                Objects.equals(fastsattNyttRefusjonskravFom, that.fastsattNyttRefusjonskravFom) &&
                Objects.equals(arbeidsgiverId, that.arbeidsgiverId) &&
                Objects.equals(tidligsteMuligeRefusjonsdato, that.tidligsteMuligeRefusjonsdato) &&
                Objects.equals(internArbeidsforholdRef, that.internArbeidsforholdRef) &&
                Objects.equals(eksternArbeidsforholdRef, that.eksternArbeidsforholdRef);
    }

    @Override
    public int hashCode() {
        return Objects.hash(aktivitetStatus,
                tidligereUtbetalinger,
                nyttRefusjonskravFom,
                arbeidsgiverNavn,
                fastsattNyttRefusjonskravFom,
                arbeidsgiverId,
                internArbeidsforholdRef,
                eksternArbeidsforholdRef,
                tidligsteMuligeRefusjonsdato);
    }
}
