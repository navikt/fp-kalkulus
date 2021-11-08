package no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;


public class BeregningRefusjonOverstyringDto {

    private Arbeidsgiver arbeidsgiver;
    private LocalDate førsteMuligeRefusjonFom;
    /**
     * Perioder der refusjonskravet skal anses å ha kommet inn i tide (overstyring av frist).
     * Tom liste betyr at alle perioder er gyldige.
     */
    private List<Intervall> refusjonGyldighetsperioder = new ArrayList<>();
    private BeregningRefusjonOverstyringerDto refusjonOverstyringer;
    private List<BeregningRefusjonPeriodeDto> refusjonPerioder = new ArrayList<>();

    BeregningRefusjonOverstyringDto() {
        // Hibernate
    }

    public BeregningRefusjonOverstyringDto(Arbeidsgiver arbeidsgiver, LocalDate førsteMuligeRefusjonFom) {
        Objects.requireNonNull(arbeidsgiver, "arbeidsgiver");
        this.førsteMuligeRefusjonFom = førsteMuligeRefusjonFom;
        this.arbeidsgiver = arbeidsgiver;
        this.refusjonGyldighetsperioder.add(Intervall.fraOgMed(førsteMuligeRefusjonFom));
    }

    public BeregningRefusjonOverstyringDto(Arbeidsgiver arbeidsgiver, LocalDate førsteMuligeRefusjonFom,
                                           List<Intervall> refusjonGyldighetsperioder) {
        Objects.requireNonNull(arbeidsgiver, "arbeidsgiver");
        this.førsteMuligeRefusjonFom = førsteMuligeRefusjonFom;
        this.arbeidsgiver = arbeidsgiver;
        this.refusjonGyldighetsperioder = refusjonGyldighetsperioder;
    }

    public BeregningRefusjonOverstyringDto(Arbeidsgiver arbeidsgiver, LocalDate førsteMuligeRefusjonFom,
                                           List<Intervall> refusjonGyldighetsperioder, List<BeregningRefusjonPeriodeDto> refusjonPerioder) {
        this.refusjonGyldighetsperioder = refusjonGyldighetsperioder;
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
        if (førsteMuligeRefusjonFom != null) {
            return Optional.of(førsteMuligeRefusjonFom);
        }
        if (!refusjonGyldighetsperioder.isEmpty()) {
            return refusjonGyldighetsperioder.stream()
                    .sorted(Comparator.naturalOrder())
                    .map(Intervall::getFomDato)
                    .findFirst();
        }
        return Optional.empty();
    }

    public List<BeregningRefusjonPeriodeDto> getRefusjonPerioder() {
        return Collections.unmodifiableList(refusjonPerioder);
    }

    public List<Intervall> getRefusjonGyldighetsperioder() {
        return refusjonGyldighetsperioder;
    }
}
