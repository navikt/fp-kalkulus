package no.nav.folketrygdloven.kalkulator.steg.besteberegning;

import java.math.BigDecimal;

import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.OpptjeningAktivitetType;

public class Inntekt {

    private Arbeidsgiver arbeidsgiver;
    private InternArbeidsforholdRefDto arbeidsforholdRef;
    private OpptjeningAktivitetType opptjeningAktivitetType;
    private final BigDecimal inntekt;

    public Inntekt(OpptjeningAktivitetType opptjeningAktivitetType, BigDecimal inntekt) {
        this.opptjeningAktivitetType = opptjeningAktivitetType;
        this.inntekt = inntekt;
    }

    public Inntekt(Arbeidsgiver arbeidsgiver, InternArbeidsforholdRefDto arbeidsforholdRef, BigDecimal inntekt) {
        this.arbeidsgiver = arbeidsgiver;
        this.arbeidsforholdRef = arbeidsforholdRef;
        this.inntekt = inntekt;
    }

    public Arbeidsgiver getArbeidsgiver() {
        return arbeidsgiver;
    }

    public InternArbeidsforholdRefDto getArbeidsforholdRef() {
        return arbeidsforholdRef;
    }

    public OpptjeningAktivitetType getOpptjeningAktivitetType() {
        return opptjeningAktivitetType;
    }

    public BigDecimal getInntekt() {
        return inntekt;
    }
}
