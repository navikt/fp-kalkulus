package no.nav.folketrygdloven.kalkulus.felles.tid;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

import no.nav.vedtak.konfig.Tid;

public abstract class AbstractIntervall implements Comparable<AbstractIntervall>, Serializable {

    public static final LocalDate TIDENES_BEGYNNELSE = Tid.TIDENES_BEGYNNELSE;
    public static final LocalDate TIDENES_ENDE = Tid.TIDENES_ENDE;

    static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    public abstract LocalDate getFomDato();

    public abstract LocalDate getTomDato();

    public boolean overlapper(AbstractIntervall other) {
        boolean fomBeforeOrEqual = this.getFomDato().isBefore(other.getTomDato()) || this.getFomDato().isEqual(other.getTomDato());
        boolean tomAfterOrEqual = this.getTomDato().isAfter(other.getFomDato()) || this.getTomDato().isEqual(other.getFomDato());
        boolean overlapper = fomBeforeOrEqual && tomAfterOrEqual;
        return overlapper;
    }

    public boolean inkluderer(LocalDate dato) {
        Objects.requireNonNull(dato, "null dato, periode=" + this);
        return erEtterEllerLikPeriodestart(dato) && erFørEllerLikPeriodeslutt(dato);
    }

    private boolean erEtterEllerLikPeriodestart(LocalDate dato) {
        Objects.requireNonNull(dato, "null dato, periode=" + this);
        return (getFomDato().isBefore(dato) || getFomDato().isEqual(dato));
    }

    private boolean erFørEllerLikPeriodeslutt(LocalDate dato) {
        Objects.requireNonNull(dato, "null dato, periode=" + this);
        return (getTomDato() == null || getTomDato().isAfter(dato) || getTomDato().isEqual(dato));
    }

    @Override
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (!(object instanceof AbstractIntervall)) {
            return false;
        }
        AbstractIntervall annen = (AbstractIntervall) object;
        return Objects.equals(this.getFomDato(), annen.getFomDato())
            && Objects.equals(this.getTomDato(), annen.getTomDato());
    }

    @Override
    public int compareTo(AbstractIntervall periode) {
        return getFomDato().compareTo(periode.getFomDato());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getFomDato(), getTomDato());
    }

    @Override
    public String toString() {
        return String.format("Periode: %s - %s", getFomDato().format(FORMATTER), getTomDato().format(FORMATTER));
    }
}
