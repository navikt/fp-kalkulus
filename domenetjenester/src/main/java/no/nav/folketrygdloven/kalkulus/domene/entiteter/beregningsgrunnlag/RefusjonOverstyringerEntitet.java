package no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import org.hibernate.annotations.BatchSize;

import no.nav.folketrygdloven.kalkulus.domene.felles.jpa.BaseEntitet;

@Entity(name = "RefusjonOverstyringerEntitet")
@Table(name = "REFUSJON_OVERSTYRINGER")
public class RefusjonOverstyringerEntitet extends BaseEntitet {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "GLOBAL_PK_SEQ_GENERATOR")
    private Long id;

    @Version
    @Column(name = "versjon", nullable = false)
    private int versjon;

    @OneToMany(mappedBy = "refusjonOverstyringer")
    @BatchSize(size = 20)
    private List<RefusjonOverstyringEntitet> overstyringer = new ArrayList<>();

    public RefusjonOverstyringerEntitet(RefusjonOverstyringerEntitet refusjonOverstyringerEntitet) {
        refusjonOverstyringerEntitet.getRefusjonOverstyringer()
            .stream()
            .map(RefusjonOverstyringEntitet::new)
            .forEach(this::leggTilRefusjonOverstyring);
    }

    public RefusjonOverstyringerEntitet() {
        // Hibernate
    }

    public List<RefusjonOverstyringEntitet> getRefusjonOverstyringer() {
        return overstyringer.stream().sorted(Comparator.comparing(RefusjonOverstyringEntitet::getArbeidsgiver)).toList();
    }

    void leggTilRefusjonOverstyring(RefusjonOverstyringEntitet refusjonOverstyringEntitet) {
        if (!overstyringer.contains(refusjonOverstyringEntitet)) {
            refusjonOverstyringEntitet.setRefusjonOverstyringerEntitet(this);
            overstyringer.add(refusjonOverstyringEntitet);
        }
    }

    @Override
    public String toString() {
        return "BeregningRefusjonOverstyringerEntitet{" + "id=" + id + '}';
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final RefusjonOverstyringerEntitet kladd;

        private Builder() {
            kladd = new RefusjonOverstyringerEntitet();
        }

        public Builder leggTilOverstyring(RefusjonOverstyringEntitet beregningRefusjonOverstyring) {
            kladd.leggTilRefusjonOverstyring(beregningRefusjonOverstyring);
            return this;
        }

        public RefusjonOverstyringerEntitet build() {
            return kladd;
        }
    }
}
