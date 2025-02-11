package no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import org.hibernate.annotations.BatchSize;

import no.nav.folketrygdloven.kalkulus.felles.jpa.BaseEntitet;


@Entity(name = "AktivitetAggregatEntitet")
@Table(name = "AKTIVITETER")
public class AktivitetAggregatEntitet extends BaseEntitet {

    @SuppressWarnings("unused")
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "GLOBAL_PK_SEQ_GENERATOR")
    private Long id;

    @Version
    @Column(name = "versjon", nullable = false)
    private int versjon;

    @OneToMany
    @JoinColumn(name = "aktiviteter_id", nullable = false, updatable = false)
    @BatchSize(size = 20)
    private List<AktivitetEntitet> aktiviteter = new ArrayList<>();

    @Column(name = "skjaeringstidspunkt_opptjening", nullable = false)
    private LocalDate skjæringstidspunktOpptjening;

    public AktivitetAggregatEntitet(AktivitetAggregatEntitet aktivitetAggregatEntitet) {
        this.skjæringstidspunktOpptjening = aktivitetAggregatEntitet.getSkjæringstidspunktOpptjening();
        aktivitetAggregatEntitet.getAktiviteter().stream().map(AktivitetEntitet::new).forEach(this::leggTilAktivitet);
    }

    public AktivitetAggregatEntitet() {
        // NOSONAR
    }

    public Long getId() {
        return id;
    }

    public List<AktivitetEntitet> getAktiviteter() {
        return aktiviteter.stream().sorted().toList();
    }

    public LocalDate getSkjæringstidspunktOpptjening() {
        return skjæringstidspunktOpptjening;
    }

    private void leggTilAktivitet(AktivitetEntitet beregningAktivitet) {
        aktiviteter.add(beregningAktivitet);
    }

    @Override
    public String toString() {
        return "BeregningAktivitetAggregatEntitet{" + "id=" + id + '}';
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final AktivitetAggregatEntitet kladd;

        private Builder() {
            kladd = new AktivitetAggregatEntitet();
        }

        public Builder medSkjæringstidspunktOpptjening(LocalDate skjæringstidspunktOpptjening) {
            kladd.skjæringstidspunktOpptjening = skjæringstidspunktOpptjening;
            return this;
        }

        public Builder leggTilAktivitet(AktivitetEntitet beregningAktivitet) { // NOSONAR
            kladd.leggTilAktivitet(beregningAktivitet);
            return this;
        }

        public AktivitetAggregatEntitet build() {
            verifyStateForBuild();
            return kladd;
        }

        private void verifyStateForBuild() {
            Objects.requireNonNull(kladd.skjæringstidspunktOpptjening, "skjæringstidspunktOpptjening");
        }
    }
}
