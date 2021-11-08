package no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Version;

import com.fasterxml.jackson.annotation.JsonBackReference;

import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulus.felles.jpa.BaseEntitet;
import no.nav.folketrygdloven.kalkulus.felles.jpa.IntervallEntitet;

@Entity(name = "BeregningRefusjonOverstyring")
@Table(name = "BG_REFUSJON_OVERSTYRING")
public class BeregningRefusjonOverstyringEntitet extends BaseEntitet {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_BG_REFUSJON_OVERSTYRING")
    private Long id;

    @Version
    @Column(name = "versjon", nullable = false)
    private long versjon;

    @Embedded
    private Arbeidsgiver arbeidsgiver;

    @Column(name = "fom")
    private LocalDate førsteMuligeRefusjonFom;

    @OneToMany(mappedBy = "refusjonOverstyring")
    private List<RefusjonGyldighetsperiodeEntitet> bekreftetGyldighetsperioder = new ArrayList<>();

    @JsonBackReference
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "br_overstyringer_id", nullable = false, updatable = false)
    private BeregningRefusjonOverstyringerEntitet refusjonOverstyringer;

    @OneToMany(mappedBy = "refusjonOverstyring")
    private List<BeregningRefusjonPeriodeEntitet> refusjonPerioder = new ArrayList<>();

    public BeregningRefusjonOverstyringEntitet(BeregningRefusjonOverstyringEntitet beregningRefusjonOverstyringEntitet) {
        this.arbeidsgiver = beregningRefusjonOverstyringEntitet.getArbeidsgiver();
        this.førsteMuligeRefusjonFom = beregningRefusjonOverstyringEntitet.getFørsteMuligeRefusjonFom().orElse(null);
        beregningRefusjonOverstyringEntitet.getBekreftetGyldighetsperioder().stream().map(RefusjonGyldighetsperiodeEntitet::new)
                .forEach(this::leggTilBekreftetGyldighetsperiode);
        beregningRefusjonOverstyringEntitet.getRefusjonPerioder().stream().map(BeregningRefusjonPeriodeEntitet::new)
                .forEach(this::leggTilBeregningRefusjonPeriode);
    }

    protected BeregningRefusjonOverstyringEntitet() {
        // Hibernate
    }

    void setRefusjonOverstyringerEntitet(BeregningRefusjonOverstyringerEntitet refusjonOverstyringer) {
        this.refusjonOverstyringer = refusjonOverstyringer;
    }

    public Arbeidsgiver getArbeidsgiver() {
        return arbeidsgiver;
    }

    public Optional<LocalDate> getFørsteMuligeRefusjonFom() {
        return Optional.ofNullable(førsteMuligeRefusjonFom);
    }

    public List<BeregningRefusjonPeriodeEntitet> getRefusjonPerioder() {
        return refusjonPerioder;
    }

    public List<RefusjonGyldighetsperiodeEntitet> getBekreftetGyldighetsperioder() {
        return bekreftetGyldighetsperioder;
    }

    void leggTilBeregningRefusjonPeriode(BeregningRefusjonPeriodeEntitet beregningRefusjonPeriodeEntitet) {
        if (!refusjonPerioder.contains(beregningRefusjonPeriodeEntitet)) {
            beregningRefusjonPeriodeEntitet.setRefusjonOverstyringEntitet(this);
            refusjonPerioder.add(beregningRefusjonPeriodeEntitet);
        }
    }

    void leggTilBekreftetGyldighetsperiode(RefusjonGyldighetsperiodeEntitet refusjonGyldighetsperiodeEntitet) {
        if (!bekreftetGyldighetsperioder.contains(refusjonGyldighetsperiodeEntitet)) {
            refusjonGyldighetsperiodeEntitet.setRefusjonOverstyring(this);
            bekreftetGyldighetsperioder.add(refusjonGyldighetsperiodeEntitet);
        }
    }


    public static BeregningRefusjonOverstyringEntitet.Builder builder() {
        return new BeregningRefusjonOverstyringEntitet.Builder();
    }

    public static class Builder {
        private final BeregningRefusjonOverstyringEntitet kladd;

        private Builder() {
            kladd = new BeregningRefusjonOverstyringEntitet();
        }

        public BeregningRefusjonOverstyringEntitet.Builder leggTilRefusjonPeriode(BeregningRefusjonPeriodeEntitet beregningRefusjonStart) {
            kladd.leggTilBeregningRefusjonPeriode(beregningRefusjonStart);
            return this;
        }

        public BeregningRefusjonOverstyringEntitet.Builder leggTilBekreftetGyldighetsperiode(RefusjonGyldighetsperiodeEntitet refusjonGyldighetsperiodeEntitet) {
            kladd.leggTilBekreftetGyldighetsperiode(refusjonGyldighetsperiodeEntitet);
            return this;
        }

        public BeregningRefusjonOverstyringEntitet.Builder medArbeidsgiver(Arbeidsgiver arbeidsgiver) {
            kladd.arbeidsgiver = arbeidsgiver;
            return this;
        }


        public BeregningRefusjonOverstyringEntitet.Builder medFørsteMuligeRefusjonFom(LocalDate førsteMuligeRefusjonFom) {
            kladd.førsteMuligeRefusjonFom = førsteMuligeRefusjonFom;
            return this;
        }


        public BeregningRefusjonOverstyringEntitet build() {
            kladd.verifiserTilstand();
            return kladd;
        }
    }

    private void verifiserTilstand() {
        Objects.requireNonNull(arbeidsgiver, "arbeidsgiver");
        if (førsteMuligeRefusjonFom == null && refusjonPerioder.isEmpty()) {
            throw new IllegalStateException("Objektet inneholder ingen informasjon om refusjon, ugyldig tilstand");
        }
    }
}
