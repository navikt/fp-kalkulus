package no.nav.folketrygdloven.kalkulator.steg.refusjon.modell;

import java.math.BigDecimal;
import java.util.Objects;

import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.modell.virksomhet.Arbeidsgiver;

public class RefusjonAndel {
    private Arbeidsgiver arbeidsgiver;
    private InternArbeidsforholdRefDto arbeidsforholdRef;
    private BigDecimal brutto;
    private BigDecimal refusjon;

    public RefusjonAndel(Arbeidsgiver arbeidsgiver, InternArbeidsforholdRefDto arbeidsforholdRef, BigDecimal brutto, BigDecimal refusjon) {
        Objects.requireNonNull(arbeidsgiver, "arbeidsgiver");
        Objects.requireNonNull(brutto, "brutto");
        Objects.requireNonNull(refusjon, "refusjon");

        this.arbeidsgiver = arbeidsgiver;
        this.arbeidsforholdRef = arbeidsforholdRef;
        this.brutto = brutto;
        this.refusjon = refusjon;
    }


    public BigDecimal getBrutto() {
        return brutto;
    }

    public BigDecimal getRefusjon() {
        return refusjon;
    }

    public Arbeidsgiver getArbeidsgiver() {
        return arbeidsgiver;
    }

    public InternArbeidsforholdRefDto getArbeidsforholdRef() {
        return arbeidsforholdRef == null ? InternArbeidsforholdRefDto.nullRef() : arbeidsforholdRef;
    }

    public boolean matcher(RefusjonAndel other) {
        return other.getArbeidsgiver().equals(this.arbeidsgiver)
                && other.getArbeidsforholdRef().gjelderFor(arbeidsforholdRef);
    }

    @Override
    public String toString() {
        return "RefusjonAndel{" +
                "arbeidsgiver=" + arbeidsgiver +
                ", brutto=" + brutto +
                ", refusjon=" + refusjon +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RefusjonAndel that = (RefusjonAndel) o;
        return Objects.equals(arbeidsgiver, that.arbeidsgiver) &&
                Objects.equals(arbeidsforholdRef, that.arbeidsforholdRef);
    }

    @Override
    public int hashCode() {
        return Objects.hash(arbeidsgiver, arbeidsforholdRef);
    }
}
