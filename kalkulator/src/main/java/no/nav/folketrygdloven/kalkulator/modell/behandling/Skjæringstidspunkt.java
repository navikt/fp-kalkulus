package no.nav.folketrygdloven.kalkulator.modell.behandling;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Inneholder relevante tidspunkter for en behandling
 */
public class Skjæringstidspunkt {
    private LocalDate skjæringstidspunktOpptjening;
    private LocalDate skjæringstidspunktBeregning;

    private Skjæringstidspunkt() {
        // hide constructor
    }

    private Skjæringstidspunkt(Skjæringstidspunkt other) {
        this.skjæringstidspunktOpptjening = other.skjæringstidspunktOpptjening;
        this.skjæringstidspunktBeregning = other.skjæringstidspunktBeregning;
    }


    /** Skjæringstidspunkt for opptjening er definert som dagen etter slutt av opptjeningsperiode. */
    public LocalDate getSkjæringstidspunktOpptjening() {
        return skjæringstidspunktOpptjening;
    }

    /** Skjæringstidspunkt for beregning er definert som dagen etter siste dag med godkjente aktiviteter. */
    public LocalDate getSkjæringstidspunktBeregning() {
        return skjæringstidspunktBeregning;
    }

    @Override
    public int hashCode() {
        return Objects.hash(skjæringstidspunktBeregning, skjæringstidspunktOpptjening);
    }

    @Override
    public boolean equals(Object obj) {
        if(obj==this) {
            return true;
        } else if (obj==null || !(obj.getClass().equals(this.getClass()))) {
            return false;
        }
        Skjæringstidspunkt other = (Skjæringstidspunkt) obj;
        return Objects.equals(this.skjæringstidspunktBeregning, other.skjæringstidspunktBeregning)
                && Objects.equals(this.skjæringstidspunktOpptjening, other.skjæringstidspunktOpptjening);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "<" + skjæringstidspunktBeregning + ">";
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Builder builder(Skjæringstidspunkt other) {
        return new Builder(other);
    }

    public static class Builder {
        private final Skjæringstidspunkt kladd;

        private Builder() {
            this.kladd = new Skjæringstidspunkt();
        }

        private Builder(Skjæringstidspunkt other) {
            this.kladd = new Skjæringstidspunkt(other);
        }


        public Builder medSkjæringstidspunktOpptjening(LocalDate dato) {
            kladd.skjæringstidspunktOpptjening = dato;
            return this;
        }

        public Builder medSkjæringstidspunktBeregning(LocalDate dato) {
            kladd.skjæringstidspunktBeregning = dato;
            return this;
        }


        public Skjæringstidspunkt build() {
            return kladd;
        }
    }
}
