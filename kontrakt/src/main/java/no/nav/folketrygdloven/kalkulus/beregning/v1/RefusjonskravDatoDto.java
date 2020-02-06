package no.nav.folketrygdloven.kalkulus.beregning.v1;

import java.time.LocalDate;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.folketrygdloven.kalkulus.felles.v1.Aktør;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = Visibility.NONE, getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE, isGetterVisibility = Visibility.NONE, creatorVisibility = Visibility.NONE)
@JsonInclude(value = Include.NON_ABSENT, content = Include.NON_EMPTY)
public class RefusjonskravDatoDto {

    @JsonProperty
    @Valid
    @NotNull
    private Aktør arbeidsgiver;

    @JsonProperty
    @Valid
    @NotNull
    private LocalDate førsteDagMedRefusjonskrav;

    @JsonProperty
    @Valid
    @NotNull
    private LocalDate førsteInnsendingAvRefusjonskrav;

    @JsonCreator
    public RefusjonskravDatoDto(@Valid @NotNull Aktør arbeidsgiver,
                                @Valid @NotNull LocalDate førsteDagMedRefusjonskrav,
                                @Valid @NotNull LocalDate førsteInnsendingAvRefusjonskrav) {

        this.arbeidsgiver = arbeidsgiver;
        this.førsteDagMedRefusjonskrav = førsteDagMedRefusjonskrav;
        this.førsteInnsendingAvRefusjonskrav = førsteInnsendingAvRefusjonskrav;
    }

    public Aktør getArbeidsgiver() {
        return arbeidsgiver;
    }

    public LocalDate getFørsteDagMedRefusjonskrav() {
        return førsteDagMedRefusjonskrav;
    }

    public LocalDate getFørsteInnsendingAvRefusjonskrav() {
        return førsteInnsendingAvRefusjonskrav;
    }
}
