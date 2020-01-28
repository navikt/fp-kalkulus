package no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag;

import java.time.LocalDate;
import java.util.Objects;

import no.nav.folketrygdloven.kalkulator.modell.virksomhet.Arbeidsgiver;


public class BeregningRefusjonOverstyringDto {

    private Arbeidsgiver arbeidsgiver;
    private LocalDate førsteMuligeRefusjonFom;
    private BeregningRefusjonOverstyringerDto refusjonOverstyringer;

    BeregningRefusjonOverstyringDto() {
        // Hibernate
    }

    public BeregningRefusjonOverstyringDto(Arbeidsgiver arbeidsgiver, LocalDate førsteMuligeRefusjonFom) {
        Objects.requireNonNull(arbeidsgiver);
        Objects.requireNonNull(førsteMuligeRefusjonFom);
        this.førsteMuligeRefusjonFom = førsteMuligeRefusjonFom;
        this.arbeidsgiver = arbeidsgiver;
    }

    void setRefusjonOverstyringerEntitet(BeregningRefusjonOverstyringerDto refusjonOverstyringer) {
        this.refusjonOverstyringer = refusjonOverstyringer;
    }

    public Arbeidsgiver getArbeidsgiver() {
        return arbeidsgiver;
    }

    public LocalDate getFørsteMuligeRefusjonFom() {
        return førsteMuligeRefusjonFom;
    }
}
