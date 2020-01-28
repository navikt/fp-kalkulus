package no.nav.folketrygdloven.kalkulator.tid;


import java.time.LocalDate;

import org.threeten.extra.Interval;

import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;

/** Skal ersatte {@link DatoIntervallEntitet} for kalkulus
 *
 *  Wrapper om {@link Interval} som sikrer riktig behandling av intervaller i beregning
 */
public class Intervall extends AbstractIntervall {

    private LocalDate fomDato;
    private LocalDate tomDato;

    @Override
    public LocalDate getFomDato() {
        return fomDato;
    }

    @Override
    public LocalDate getTomDato() {
        return tomDato;
    }

    private Intervall(LocalDate fomDato, LocalDate tomDato) {
        if (fomDato == null) {
            throw new IllegalArgumentException("Fra og med dato må være satt.");
        }
        if (tomDato == null) {
            throw new IllegalArgumentException("Til og med dato må være satt.");
        }
        if (tomDato.isBefore(fomDato)) {
            throw new IllegalArgumentException("Til og med dato før fra og med dato.");
        }
        this.fomDato = fomDato;
        this.tomDato = tomDato;
    }

    public static Intervall fraOgMedTilOgMed(LocalDate fomDato, LocalDate tomDato) {
        return new Intervall(fomDato, tomDato);
    }

    public static Intervall fraOgMed(LocalDate fomDato) {
        return new Intervall(fomDato, AbstractIntervall.TIDENES_ENDE);
    }
}
