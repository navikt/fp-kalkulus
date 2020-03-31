package no.nav.folketrygdloven.kalkulator.refusjon.modell;

import no.nav.folketrygdloven.kalkulator.modell.virksomhet.Arbeidsgiver;

import java.math.BigDecimal;

public class RefusjonAndel {
    private Arbeidsgiver arbeidsgiver;
    private BigDecimal brutto;
    private BigDecimal refusjon;

    public RefusjonAndel(Arbeidsgiver arbeidsgiver, BigDecimal brutto, BigDecimal refusjon) {
        this.arbeidsgiver = arbeidsgiver;
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

    public boolean matcher(RefusjonAndel other) {
        return other.getArbeidsgiver().equals(this.arbeidsgiver);
    }

    @Override
    public String toString() {
        return "RefusjonAndel{" +
                "arbeidsgiver=" + arbeidsgiver +
                ", brutto=" + brutto +
                ", refusjon=" + refusjon +
                '}';
    }
}
