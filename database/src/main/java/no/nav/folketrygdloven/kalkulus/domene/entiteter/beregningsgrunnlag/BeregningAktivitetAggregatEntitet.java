package no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Version;

import no.nav.folketrygdloven.kalkulus.felles.jpa.BaseEntitet;


@Entity(name = "BeregningAktiviteter")
@Table(name = "BG_AKTIVITETER")
public class BeregningAktivitetAggregatEntitet extends BaseEntitet {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_BG_AKTIVITETER")
    private Long id;

    @Version
    @Column(name = "versjon", nullable = false)
    private long versjon;

    @OneToMany(mappedBy = "beregningAktiviteter")
    private List<BeregningAktivitetEntitet> aktiviteter = new ArrayList<>();

    @Column(name = "skjaringstidspunkt_opptjening", nullable = false)
    private LocalDate skjæringstidspunktOpptjening;

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
        beregningAktivitet.setBeregningAktiviteter(this);
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
