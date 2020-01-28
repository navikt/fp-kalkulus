package no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class BeregningAktivitetAggregatRestDto {


    private List<BeregningAktivitetRestDto> aktiviteter = new ArrayList<>();
    private LocalDate skjæringstidspunktOpptjening;

    public BeregningAktivitetAggregatRestDto() {
        // NOSONAR
    }

    public List<BeregningAktivitetRestDto> getBeregningAktiviteter() {
        return Collections.unmodifiableList(aktiviteter);
    }

    public LocalDate getSkjæringstidspunktOpptjening() {
        return skjæringstidspunktOpptjening;
    }

    private void leggTilAktivitet(BeregningAktivitetRestDto beregningAktivitet) {
        beregningAktivitet.setBeregningAktiviteter(this);
        aktiviteter.add(beregningAktivitet);
    }

    @Override
    public String toString() {
        return "BeregningAktivitetAggregat{" +
                "skjæringstidspunktOpptjening=" + skjæringstidspunktOpptjening +
                '}';
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final BeregningAktivitetAggregatRestDto kladd;

        private Builder() {
            kladd = new BeregningAktivitetAggregatRestDto();
        }

        public Builder medSkjæringstidspunktOpptjening(LocalDate skjæringstidspunktOpptjening) {
            kladd.skjæringstidspunktOpptjening = skjæringstidspunktOpptjening;
            return this;
        }

        public Builder leggTilAktivitet(BeregningAktivitetRestDto beregningAktivitet) { // NOSONAR
            kladd.leggTilAktivitet(beregningAktivitet);
            return this;
        }

        public BeregningAktivitetAggregatRestDto build() {
            verifyStateForBuild();
            return kladd;
        }

        private void verifyStateForBuild() {
            Objects.requireNonNull(kladd.skjæringstidspunktOpptjening, "skjæringstidspunktOpptjening");
        }
    }
}
