package no.nav.folketrygdloven.kalkulator.steg.besteberegning;

import java.math.BigDecimal;

import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.OpptjeningAktivitetType;

public class Inntekt {

    private Arbeidsgiver arbeidsgiver;
    private InternArbeidsforholdRefDto arbeidsforholdRefDto;
    private OpptjeningAktivitetType opptjeningAktivitetType;
    private final BigDecimal inntekt;

    public Inntekt(OpptjeningAktivitetType opptjeningAktivitetType, BigDecimal inntekt) {
        this.opptjeningAktivitetType = opptjeningAktivitetType;
        this.inntekt = inntekt;
    }

    public Inntekt(Arbeidsgiver arbeidsgiver, InternArbeidsforholdRefDto arbeidsforholdRefDto, BigDecimal inntekt) {
        this.arbeidsgiver = arbeidsgiver;
        this.arbeidsforholdRefDto = arbeidsforholdRefDto;
        this.inntekt = inntekt;
    }

    public Arbeidsgiver getArbeidsgiver() {
        return arbeidsgiver;
    }

    public OpptjeningAktivitetType getOpptjeningAktivitetType() {
        return opptjeningAktivitetType;
    }

    public BigDecimal getInntekt() {
        return inntekt;
    }
}
