package no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Version;

import org.hibernate.annotations.BatchSize;

import no.nav.folketrygdloven.kalkulus.felles.jpa.BaseEntitet;

@Entity(name = "BeregningRefusjonOverstyringer")
@Table(name = "BG_REFUSJON_OVERSTYRINGER")
public class BeregningRefusjonOverstyringerEntitet extends BaseEntitet {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_BG_REFUSJON_OVERSTYRINGER")
    private Long id;

    @Version
    @Column(name = "versjon", nullable = false)
    private long versjon;

    @OneToMany(mappedBy = "refusjonOverstyringer")
    @BatchSize(size=20)
    private List<BeregningRefusjonOverstyringEntitet> overstyringer = new ArrayList<>();

    public BeregningRefusjonOverstyringerEntitet() {
        // Hibernate
    }

    public List<BeregningRefusjonOverstyringEntitet> getRefusjonOverstyringer() {
        return overstyringer.stream()
                .sorted(Comparator.comparing(BeregningRefusjonOverstyringEntitet::getArbeidsgiver))
                .collect(Collectors.toUnmodifiableList());
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final BeregningRefusjonOverstyringerEntitet kladd;

        private Builder() {
            kladd = new BeregningRefusjonOverstyringerEntitet();
        }

        public Builder leggTilOverstyring(BeregningRefusjonOverstyringEntitet beregningRefusjonOverstyring) {
            BeregningRefusjonOverstyringEntitet entitet = beregningRefusjonOverstyring;
            entitet.setRefusjonOverstyringerEntitet(kladd);
            kladd.overstyringer.add(entitet);
            return this;
        }

        public BeregningRefusjonOverstyringerEntitet build() {
            return kladd;
        }
    }
}
