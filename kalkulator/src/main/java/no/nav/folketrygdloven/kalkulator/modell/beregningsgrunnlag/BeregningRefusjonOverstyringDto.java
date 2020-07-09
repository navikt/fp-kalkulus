package no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import no.nav.folketrygdloven.kalkulator.modell.virksomhet.Arbeidsgiver;


public class BeregningRefusjonOverstyringDto {

    private Arbeidsgiver arbeidsgiver;
    private LocalDate førsteMuligeRefusjonFom;
    private BeregningRefusjonOverstyringerDto refusjonOverstyringer;
    private List<BeregningRefusjonPeriodeDto> refusjonPerioder = new ArrayList<>();

    BeregningRefusjonOverstyringDto() {
        // Hibernate
    }

    public BeregningRefusjonOverstyringDto(Arbeidsgiver arbeidsgiver, LocalDate førsteMuligeRefusjonFom) {
        Objects.requireNonNull(arbeidsgiver, "arbeidsgiver");
        this.førsteMuligeRefusjonFom = førsteMuligeRefusjonFom;
        this.arbeidsgiver = arbeidsgiver;
    }

    public BeregningRefusjonOverstyringDto(Arbeidsgiver arbeidsgiver, LocalDate førsteMuligeRefusjonFom, List<BeregningRefusjonPeriodeDto> refusjonPerioder) {
        Objects.requireNonNull(arbeidsgiver, "arbeidsgiver");
        this.førsteMuligeRefusjonFom = førsteMuligeRefusjonFom;
        this.refusjonPerioder = refusjonPerioder;
        this.arbeidsgiver = arbeidsgiver;
    }

    void setRefusjonOverstyringerEntitet(BeregningRefusjonOverstyringerDto refusjonOverstyringer) {
        this.refusjonOverstyringer = refusjonOverstyringer;
    }

    public Arbeidsgiver getArbeidsgiver() {
        return arbeidsgiver;
    }

    public Optional<LocalDate> getFørsteMuligeRefusjonFom() {
        return Optional.ofNullable(førsteMuligeRefusjonFom);
    }

    public List<BeregningRefusjonPeriodeDto> getRefusjonPerioder() {
        return refusjonPerioder;
    }
}
