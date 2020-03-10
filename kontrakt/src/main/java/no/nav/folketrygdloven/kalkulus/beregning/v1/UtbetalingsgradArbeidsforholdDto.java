package no.nav.folketrygdloven.kalkulus.beregning.v1;

import java.util.Objects;
import java.util.Optional;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.folketrygdloven.kalkulus.felles.v1.Aktør;
import no.nav.folketrygdloven.kalkulus.felles.v1.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.UttakArbeidType;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = Visibility.NONE, getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE, isGetterVisibility = Visibility.NONE, creatorVisibility = Visibility.NONE)
@JsonInclude(value = Include.NON_ABSENT, content = Include.NON_EMPTY)
public final class UtbetalingsgradArbeidsforholdDto {

    @JsonProperty(value = "arbeidsgiver", required = true)
    @Valid
    @NotNull
    private Aktør arbeidsgiver;

    @JsonProperty(value = "internArbeidsforholdRef", required = true)
    @Valid
    @NotNull
    private InternArbeidsforholdRefDto internArbeidsforholdRef;


    @JsonProperty(value = "uttakArbeidType", required = true)
    @Valid
    @NotNull
    private UttakArbeidType uttakArbeidType;

    public UtbetalingsgradArbeidsforholdDto() {
    }

    public UtbetalingsgradArbeidsforholdDto(@Valid @NotNull Aktør arbeidsgiver, @Valid @NotNull InternArbeidsforholdRefDto internArbeidsforholdRef, @Valid @NotNull UttakArbeidType uttakArbeidType) {
        this.arbeidsgiver = arbeidsgiver;
        this.internArbeidsforholdRef = internArbeidsforholdRef;
        this.uttakArbeidType = uttakArbeidType;
    }

    public Optional<Aktør> getArbeidsgiver() {
        return Optional.ofNullable(arbeidsgiver);
    }

    public InternArbeidsforholdRefDto getInternArbeidsforholdRef() {
        return internArbeidsforholdRef;
    }

    public UttakArbeidType getUttakArbeidType() {
        return uttakArbeidType;
    }


}
