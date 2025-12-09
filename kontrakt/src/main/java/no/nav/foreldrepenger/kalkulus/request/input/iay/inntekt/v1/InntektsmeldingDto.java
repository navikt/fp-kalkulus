package no.nav.foreldrepenger.kalkulus.request.input.iay.inntekt.v1;

import java.time.LocalDate;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.foreldrepenger.kalkulus.typer.Aktør;
import no.nav.foreldrepenger.kalkulus.typer.Beløp;
import no.nav.foreldrepenger.kalkulus.typer.InternArbeidsforholdRefDto;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = Include.ALWAYS, content = Include.ALWAYS)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
public class InntektsmeldingDto {

    @JsonProperty(value = "arbeidsgiver")
    @Valid
    @NotNull
    private Aktør arbeidsgiver;

    @JsonProperty(value = "inntektBeløp")
    @Valid
    @NotNull
    private Beløp inntektBeløp;

    @JsonProperty(value = "naturalYtelser")
    @Size
    private List<@Valid NaturalYtelseDto> naturalYtelser;

    @JsonProperty(value = "endringerRefusjon")
    @Size
    private List<@Valid RefusjonDto> endringerRefusjon;

    @JsonProperty(value = "arbeidsforholdRef")
    @Valid
    private InternArbeidsforholdRefDto arbeidsforholdRef;

    @JsonProperty(value = "startDatoPermisjon")
    @Valid
    private LocalDate startDatoPermisjon;

    @JsonProperty(value = "refusjonOpphører")
    @Valid
    private LocalDate refusjonOpphører;

    @JsonProperty(value = "refusjonBeløpPerMnd")
    @Valid
    private Beløp refusjonBeløpPerMnd;

    @JsonProperty(value = "innsendingsdato")
    @Valid
    private LocalDate innsendingsdato;

    protected InntektsmeldingDto() {
        // default ctor
    }

    public InntektsmeldingDto(Aktør arbeidsgiver,
                              Beløp inntektBeløp,
                              List<NaturalYtelseDto> naturalYtelser,
                              List<RefusjonDto> endringerRefusjon,
                              InternArbeidsforholdRefDto arbeidsforholdRef,
                              LocalDate startDatoPermisjon,
                              LocalDate refusjonOpphører,
                              Beløp refusjonBeløpPerMnd,
                              LocalDate innsendingsdato) {
        this.arbeidsgiver = arbeidsgiver;
        this.inntektBeløp = inntektBeløp;
        this.naturalYtelser = naturalYtelser;
        this.endringerRefusjon = endringerRefusjon;
        this.arbeidsforholdRef = arbeidsforholdRef;
        this.startDatoPermisjon = startDatoPermisjon;
        this.refusjonOpphører = refusjonOpphører;
        this.refusjonBeløpPerMnd = refusjonBeløpPerMnd;
        this.innsendingsdato = innsendingsdato;
    }

    public Aktør getArbeidsgiver() {
        return arbeidsgiver;
    }

    public Beløp getInntektBeløp() {
        return inntektBeløp;
    }

    public List<NaturalYtelseDto> getNaturalYtelser() {
        return naturalYtelser;
    }

    public List<RefusjonDto> getEndringerRefusjon() {
        return endringerRefusjon;
    }

    public InternArbeidsforholdRefDto getArbeidsforholdRef() {
        return arbeidsforholdRef;
    }

    public LocalDate getStartDatoPermisjon() {
        return startDatoPermisjon;
    }

    public LocalDate getRefusjonOpphører() {
        return refusjonOpphører;
    }

    public Beløp getRefusjonBeløpPerMnd() {
        return refusjonBeløpPerMnd;
    }

    public LocalDate getInnsendingsdato() {
        return innsendingsdato;
    }
}
