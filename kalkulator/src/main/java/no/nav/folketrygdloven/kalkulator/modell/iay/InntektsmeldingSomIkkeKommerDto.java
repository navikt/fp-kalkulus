package no.nav.folketrygdloven.kalkulator.modell.iay;

import java.util.Objects;

import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;


public class InntektsmeldingSomIkkeKommerDto {

    private Arbeidsgiver arbeidsgiver;
    private InternArbeidsforholdRefDto internRef;

    public InntektsmeldingSomIkkeKommerDto(Arbeidsgiver arbeidsgiver,
                                           InternArbeidsforholdRefDto internRef) {
        this.arbeidsgiver = arbeidsgiver;
        this.internRef = internRef;
    }

    public Arbeidsgiver getArbeidsgiver() {
        return arbeidsgiver;
    }

    public InternArbeidsforholdRefDto getRef() {
        return internRef;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        InntektsmeldingSomIkkeKommerDto that = (InntektsmeldingSomIkkeKommerDto) o;
        return Objects.equals(arbeidsgiver, that.arbeidsgiver)
            && Objects.equals(internRef, that.internRef);
    }

    @Override
    public int hashCode() {
        return Objects.hash(arbeidsgiver, internRef);
    }

    @Override
    public String toString() {
        return "InntektsmeldingSomIkkeKommer{" +
            "arbeidsgiver=" + arbeidsgiver +
            ", internRef=" + internRef +
            '}';
    }
}
