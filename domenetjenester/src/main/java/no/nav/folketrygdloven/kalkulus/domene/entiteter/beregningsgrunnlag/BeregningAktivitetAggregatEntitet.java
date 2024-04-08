package no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.hibernate.annotations.BatchSize;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import no.nav.folketrygdloven.kalkulus.felles.jpa.BaseEntitet;


@Entity(name = "BeregningAktiviteter")
@Table(name = "BG_AKTIVITETER")
public class BeregningAktivitetAggregatEntitet extends BaseEntitet {

    @SuppressWarnings("unused")
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_BG_AKTIVITETER")
    private Long id;

    @Version
    @Column(name = "versjon", nullable = false)
    private int versjon;

    @OneToMany
    @JoinColumn(name = "bg_aktiviteter_id", nullable = false, updatable = false)
    @BatchSize(size=20)
    private List<BeregningAktivitetEntitet> aktiviteter = new ArrayList<>();

    @Column(name = "skjaringstidspunkt_opptjening", nullable = false)
    private LocalDate skjæringstidspunktOpptjening;

    public BeregningAktivitetAggregatEntitet(BeregningAktivitetAggregatEntitet beregningAktivitetAggregatEntitet) {
        this.skjæringstidspunktOpptjening = beregningAktivitetAggregatEntitet.getSkjæringstidspunktOpptjening();
        beregningAktivitetAggregatEntitet.getBeregningAktiviteter().stream().map(BeregningAktivitetEntitet::new)
                .forEach(this::leggTilAktivitet);
    }

    public BeregningAktivitetAggregatEntitet() {
        // NOSONAR
    }

    public Long getId() {
        return id;
    }

    public List<BeregningAktivitetEntitet> getBeregningAktiviteter() {
        return aktiviteter.stream()
                .sorted()
                .collect(Collectors.toUnmodifiableList());
    }

    public LocalDate getSkjæringstidspunktOpptjening() {
        return skjæringstidspunktOpptjening;
    }

    private void leggTilAktivitet(BeregningAktivitetEntitet beregningAktivitet) {
        aktiviteter.add(beregningAktivitet);
    }

    @Override
    public String toString() {
        return "BeregningAktivitetAggregatEntitet{" +
                "id=" + id +
                '}';
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final BeregningAktivitetAggregatEntitet kladd;

        private Builder() {
            kladd = new BeregningAktivitetAggregatEntitet();
        }

        public Builder medSkjæringstidspunktOpptjening(LocalDate skjæringstidspunktOpptjening) {
            kladd.skjæringstidspunktOpptjening = skjæringstidspunktOpptjening;
            return this;
        }

        public Builder leggTilAktivitet(BeregningAktivitetEntitet beregningAktivitet) { // NOSONAR
            kladd.leggTilAktivitet(beregningAktivitet);
            return this;
        }

        public BeregningAktivitetAggregatEntitet build() {
            verifyStateForBuild();
            return kladd;
        }

        private void verifyStateForBuild() {
            Objects.requireNonNull(kladd.skjæringstidspunktOpptjening, "skjæringstidspunktOpptjening");
        }
    }
}
