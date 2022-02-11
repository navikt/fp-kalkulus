package no.nav.folketrygdloven.kalkulus.response.v1.håndtering;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import java.time.LocalDate;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import no.nav.folketrygdloven.kalkulus.kodeverk.OpptjeningAktivitetType;


@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = JsonInclude.Include.NON_ABSENT, content = JsonInclude.Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = NONE, setterVisibility = NONE, isGetterVisibility = NONE, creatorVisibility = NONE)
public class BeregningAktivitetNøkkel {

    @JsonProperty(value = "opptjeningAktivitetType")
    @NotNull
    @Valid
    private OpptjeningAktivitetType opptjeningAktivitetType;

    @JsonProperty(value = "fom")
    @NotNull
    @Valid
    private LocalDate fom;

    @JsonProperty(value = "arbeidsgiverIdentifikator")
    @Pattern(regexp = "^[\\p{Graph}\\p{Space}\\p{Sc}\\p{L}\\p{M}\\p{N}]+$", message = "'${validatedValue}' matcher ikke tillatt pattern '{regexp}'")
    @Valid
    private String arbeidsgiverIdentifikator;

    @JsonProperty(value = "arbeidsforholdRef")
    @Pattern(regexp = "^[\\p{Graph}\\p{Space}\\p{Sc}\\p{L}\\p{M}\\p{N}]+$", message = "'${validatedValue}' matcher ikke tillatt pattern '{regexp}'")
    @Valid
    private String arbeidsforholdRef;

    private BeregningAktivitetNøkkel() {
    }

    public BeregningAktivitetNøkkel(OpptjeningAktivitetType opptjeningAktivitetType,
                                    LocalDate fom,
                                    String arbeidsgiverIdentifikator,
                                    String arbeidsforholdRef) {
        this.opptjeningAktivitetType = opptjeningAktivitetType;
        this.fom = fom;
        this.arbeidsgiverIdentifikator = arbeidsgiverIdentifikator;
        this.arbeidsforholdRef = arbeidsforholdRef;
    }

    public OpptjeningAktivitetType getOpptjeningAktivitetType() {
        return opptjeningAktivitetType;
    }

    public LocalDate getFom() {
        return fom;
    }

    public String getArbeidsgiverIdentifikator() {
        return arbeidsgiverIdentifikator;
    }

    public String getArbeidsforholdRef() {
        return arbeidsforholdRef;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof BeregningAktivitetNøkkel)) {
            return false;
        }
        BeregningAktivitetNøkkel that = (BeregningAktivitetNøkkel) o;
        return Objects.equals(opptjeningAktivitetType, that.opptjeningAktivitetType)
                && Objects.equals(fom, that.fom)
                && Objects.equals(arbeidsgiverIdentifikator, that.arbeidsgiverIdentifikator)
                && Objects.equals(arbeidsforholdRef, that.arbeidsforholdRef);
    }

    @Override
    public int hashCode() {
        return Objects.hash(opptjeningAktivitetType, fom, arbeidsgiverIdentifikator, arbeidsforholdRef);
    }

}
