package no.nav.folketrygdloven.kalkulus.beregning.v1;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.folketrygdloven.kalkulus.felles.v1.Aktør;
import no.nav.folketrygdloven.kalkulus.felles.v1.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;

public class AndelGraderingDto {


    @JsonProperty
    @Valid
    @NotNull
    private AktivitetStatus aktivitetStatus;

    @JsonProperty
    @Valid
    @NotNull
    private Aktør arbeidsgiver;

    @JsonProperty
    @Valid
    private InternArbeidsforholdRefDto arbeidsforholdRef;

    @JsonProperty
    @Valid
    @NotEmpty
    private List<GraderingDto> graderinger;

    public AndelGraderingDto(@Valid @NotNull AktivitetStatus aktivitetStatus,
                             @Valid @NotNull Aktør arbeidsgiver,
                             @Valid InternArbeidsforholdRefDto arbeidsforholdRef,
                             @Valid @NotEmpty List<GraderingDto> graderinger) {

        this.aktivitetStatus = aktivitetStatus;
        this.arbeidsgiver = arbeidsgiver;
        this.arbeidsforholdRef = arbeidsforholdRef;
        this.graderinger = graderinger;
    }

    public AktivitetStatus getAktivitetStatus() {
        return aktivitetStatus;
    }

    public Aktør getArbeidsgiver() {
        return arbeidsgiver;
    }

    public InternArbeidsforholdRefDto getArbeidsforholdRef() {
        return arbeidsforholdRef;
    }

    public List<GraderingDto> getGraderinger() {
        return graderinger;
    }
}
