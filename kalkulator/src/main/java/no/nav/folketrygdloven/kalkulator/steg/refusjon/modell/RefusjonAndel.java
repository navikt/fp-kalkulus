package no.nav.folketrygdloven.kalkulator.steg.refusjon.modell;

import java.math.BigDecimal;
import java.util.Objects;

import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.modell.virksomhet.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.AktivitetStatus;

public class RefusjonAndel {
    private AktivitetStatus aktivitetStatus;
    private Arbeidsgiver arbeidsgiver;
    private InternArbeidsforholdRefDto arbeidsforholdRef;
    private BigDecimal brutto;
    private BigDecimal refusjon;

    public RefusjonAndel(AktivitetStatus aktivitetStatus, Arbeidsgiver arbeidsgiver, InternArbeidsforholdRefDto arbeidsforholdRef, BigDecimal brutto, BigDecimal refusjon) {
        Objects.requireNonNull(aktivitetStatus, "aktivitetStatus");
        Objects.requireNonNull(brutto, "brutto");
        Objects.requireNonNull(refusjon, "refusjon");

        this.aktivitetStatus = aktivitetStatus;
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

    public boolean matcherEksakt(RefusjonAndel other) {
        return Objects.equals(other.getAktivitetStatus(), this.aktivitetStatus)
                && Objects.equals(other.getArbeidsgiver(), this.arbeidsgiver)
                && other.getArbeidsforholdRef().gjelderFor(arbeidsforholdRef);
    }

    public AktivitetStatus getAktivitetStatus() {
        return aktivitetStatus;
    }

    public RefusjonAndelNøkkel getNøkkel() {
        return new RefusjonAndelNøkkel(aktivitetStatus, arbeidsgiver);
    }

    @Override
    public String toString() {
        return "RefusjonAndel{" +
                "aktivitetStatus=" + aktivitetStatus +
                "arbeidsgiver=" + arbeidsgiver +
                "arbeidsforholdRef=" + arbeidsforholdRef +
                ", brutto=" + brutto +
                ", refusjon=" + refusjon +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RefusjonAndel that = (RefusjonAndel) o;
        return Objects.equals(aktivitetStatus, that.aktivitetStatus) &&
                Objects.equals(arbeidsgiver, that.arbeidsgiver) &&
                Objects.equals(arbeidsforholdRef, that.arbeidsforholdRef);
    }

    @Override
    public int hashCode() {
        return Objects.hash(aktivitetStatus, arbeidsgiver, arbeidsforholdRef);
    }


}
