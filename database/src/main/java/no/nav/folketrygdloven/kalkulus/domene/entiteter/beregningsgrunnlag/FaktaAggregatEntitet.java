package no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Version;

import org.hibernate.annotations.BatchSize;

import no.nav.folketrygdloven.kalkulus.felles.jpa.BaseEntitet;


@Entity(name = "FaktaAggregatEntitet")
@Table(name = "FAKTA_AGGREGAT")
public class FaktaAggregatEntitet extends BaseEntitet {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_FAKTA_AGGREGAT")
    private Long id;

    @Version
    @Column(name = "versjon", nullable = false)
    private long versjon;

    @OneToMany(mappedBy = "faktaAggregat")
    @BatchSize(size=20)
    private List<FaktaArbeidsforholdEntitet> faktaArbeidsforholdListe = new ArrayList<>();

    @OneToOne(mappedBy = "faktaAggregat", cascade = CascadeType.PERSIST)
    private FaktaAktørEntitet faktaAktør;

    public FaktaAggregatEntitet() {
        // NOSONAR
    }

    public Long getId() {
        return id;
    }

    public List<FaktaArbeidsforholdEntitet> getFaktaArbeidsforhold() {
        return faktaArbeidsforholdListe.stream()
                .collect(Collectors.toUnmodifiableList());
    }

    public FaktaAktørEntitet getFaktaAktør() {
        return faktaAktør;
    }

    private void leggTilFaktaArbeidsforhold(FaktaArbeidsforholdEntitet faktaArbeidsforhold) {
        faktaArbeidsforhold.setBeregningAktiviteter(this);
        this.faktaArbeidsforholdListe.add(faktaArbeidsforhold);
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
        private final FaktaAggregatEntitet kladd;

        private Builder() {
            kladd = new FaktaAggregatEntitet();
        }


        public Builder leggTilFaktaArbeidsforhold(FaktaArbeidsforholdEntitet faktaArbeidsforhold) { // NOSONAR
            kladd.leggTilFaktaArbeidsforhold(faktaArbeidsforhold);
            return this;
        }

        public Builder medFaktaAktør(FaktaAktørEntitet faktaAktør) { // NOSONAR
            kladd.faktaAktør = faktaAktør;
            return this;
        }

        public FaktaAggregatEntitet build() {
            verifyStateForBuild();
            return kladd;
        }

        private void verifyStateForBuild() {
            if (kladd.faktaArbeidsforholdListe.isEmpty() && kladd.faktaAktør == null) {
                throw new IllegalStateException("Må ha satt enten faktaArbeidsforhold eller faktaAktør");
            }
        }
    }
}
