package no.nav.folketrygdloven.kalkulus.domene.felles.jpa;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;

@Embeddable
public class IntervallEntitet extends AbstractIntervall {


    @NotNull
    @Column(name = "fom", nullable = false)
    private LocalDate fomDato;

    @NotNull
    @Column(name = "tom", nullable = false)
    private LocalDate tomDato;


    protected IntervallEntitet() {
        //hibernate
    }
    private IntervallEntitet(LocalDate fomDato, LocalDate tomDato) {
        if (fomDato == null) {
            throw new IllegalArgumentException("Fra og med dato må være satt.");
        } else if (tomDato == null) {
            throw new IllegalArgumentException("Til og med dato må være satt.");
        } else if (tomDato.isBefore(fomDato)) {
            throw new IllegalArgumentException("Til og med dato før fra og med dato.");
        } else {
            this.fomDato = fomDato;
            this.tomDato = tomDato;
        }
    }

    public static IntervallEntitet fraOgMedTilOgMed(LocalDate fomDato, LocalDate tomDato) {
        return new IntervallEntitet(fomDato, tomDato);
    }

    public static IntervallEntitet fraOgMed(LocalDate fomDato) {
        return new IntervallEntitet(fomDato, TIDENES_ENDE);
    }

    @Override
    public LocalDate getFomDato() {
        return this.fomDato;
    }

    @Override
    public LocalDate getTomDato() {
        return this.tomDato;
    }

    protected IntervallEntitet lagNyPeriode(LocalDate fomDato, LocalDate tomDato) {
        return fraOgMedTilOgMed(fomDato, tomDato);
    }
}
