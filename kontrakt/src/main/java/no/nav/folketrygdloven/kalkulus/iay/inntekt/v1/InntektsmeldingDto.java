package no.nav.folketrygdloven.kalkulus.iay.inntekt.v1;

import java.time.LocalDate;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.folketrygdloven.kalkulus.felles.v1.Aktør;
import no.nav.folketrygdloven.kalkulus.felles.v1.BeløpDto;
import no.nav.folketrygdloven.kalkulus.felles.v1.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulus.felles.v1.JournalpostId;

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
    private BeløpDto inntektBeløp;

    @JsonProperty(value = "naturalYtelser")
    @Valid
    @Size
    private List<NaturalYtelseDto> naturalYtelser;

    @JsonProperty(value = "endringerRefusjon")
    @Valid
    @Size
    private List<RefusjonDto> endringerRefusjon;

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
    private BeløpDto refusjonBeløpPerMnd;

    /** JournalpostId - for sporing. */
    @JsonProperty(value = "journalpostId")
    @Valid
    private JournalpostId journalpostId;

    /** Opprinnelig kanalreferanse (fra Altinn). for sporing. */
    @JsonProperty(value = "kanalreferanse")
    @Pattern(regexp = "^[\\p{Graph}\\s\\t\\p{Sc}\\p{L}\\p{M}\\p{N}]+$", message = "Inntektsmelding kanalreferanse [${validatedValue}] matcher ikke tillatt pattern [{regexp}]")
    private String kanalreferanse;

    protected InntektsmeldingDto() {
        // default ctor
    }

    public InntektsmeldingDto(@Valid @NotNull Aktør arbeidsgiver, 
                              @Valid @NotNull BeløpDto inntektBeløp, 
                              @Valid List<NaturalYtelseDto> naturalYtelser,
                              @Valid List<RefusjonDto> endringerRefusjon, 
                              @Valid InternArbeidsforholdRefDto arbeidsforholdRef,
                              @Valid LocalDate startDatoPermisjon, 
                              @Valid LocalDate refusjonOpphører, 
                              @Valid BeløpDto refusjonBeløpPerMnd,
                              @Valid JournalpostId journalpostId,
                              @Valid String kanalreferanse
                              ) {
        this.arbeidsgiver = arbeidsgiver;
        this.inntektBeløp = inntektBeløp;
        this.naturalYtelser = naturalYtelser;
        this.endringerRefusjon = endringerRefusjon;
        this.arbeidsforholdRef = arbeidsforholdRef;
        this.startDatoPermisjon = startDatoPermisjon;
        this.refusjonOpphører = refusjonOpphører;
        this.refusjonBeløpPerMnd = refusjonBeløpPerMnd;
        this.journalpostId = journalpostId;
        this.kanalreferanse = kanalreferanse;
    }

    public Aktør getArbeidsgiver() {
        return arbeidsgiver;
    }

    public BeløpDto getInntektBeløp() {
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

    public BeløpDto getRefusjonBeløpPerMnd() {
        return refusjonBeløpPerMnd;
    }

    public JournalpostId getJournalpostId() {
        return journalpostId;
    }

    public String getKanalreferanse() {
        return kanalreferanse;
    }
}
