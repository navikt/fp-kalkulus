package no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import org.hibernate.annotations.BatchSize;

import no.nav.folketrygdloven.kalkulus.domene.felles.jpa.BaseEntitet;


@Entity(name = "FaktaAggregatEntitet")
@Table(name = "FAKTA_AGGREGAT")
public class FaktaAggregatEntitet extends BaseEntitet {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "GLOBAL_PK_SEQ_GENERATOR")
    private Long id;

    @Version
    @Column(name = "versjon", nullable = false)
    private int versjon;

    @OneToMany(mappedBy = "faktaAggregat")
    @BatchSize(size = 20)
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
        return faktaArbeidsforholdListe.stream().toList();
    }

    public Optional<FaktaAktørEntitet> getFaktaAktør() {
        return Optional.ofNullable(faktaAktør);
    }

    private void leggTilFaktaArbeidsforholdIgnorerOmEksisterer(FaktaArbeidsforholdEntitet faktaArbeidsforhold) {
        faktaArbeidsforhold.setFaktaAggregat(this);
        var eksisterende = this.faktaArbeidsforholdListe.stream()
            .filter(fa -> fa.gjelderFor(faktaArbeidsforhold.getArbeidsgiver(), faktaArbeidsforhold.getArbeidsforholdRef()))
            .findFirst();
        if (eksisterende.isEmpty()) {
            faktaArbeidsforhold.setFaktaAggregat(this);
            this.faktaArbeidsforholdListe.add(faktaArbeidsforhold);
        }
    }

    void setFaktaAktør(FaktaAktørEntitet faktaAktør) {
        this.faktaAktør = faktaAktør;
        faktaAktør.setFaktaAggregat(this);
    }

    @Override
    public String toString() {
        return "BeregningAktivitetAggregatEntitet{" + "id=" + id + '}';
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final FaktaAggregatEntitet kladd;

        private Builder() {
            kladd = new FaktaAggregatEntitet();
        }


        public Builder leggTilFaktaArbeidsforholdIgnorerOmEksisterer(FaktaArbeidsforholdEntitet faktaArbeidsforhold) { // NOSONAR
            kladd.leggTilFaktaArbeidsforholdIgnorerOmEksisterer(faktaArbeidsforhold);
            return this;
        }

        public Builder medFaktaAktør(FaktaAktørEntitet faktaAktør) { // NOSONAR
            kladd.setFaktaAktør(faktaAktør);
            return this;
        }

        public FaktaAggregatEntitet build() {
            verifyStateForBuild();
            return kladd;
        }

        private void verifyStateForBuild() {
            if (manglerFakta()) {
                throw new IllegalStateException("Må ha satt enten faktaArbeidsforhold eller faktaAktør");
            }
        }

        public boolean manglerFakta() {
            return kladd.faktaArbeidsforholdListe.isEmpty() && kladd.faktaAktør == null;
        }
    }
}
