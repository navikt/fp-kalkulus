package no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import com.fasterxml.jackson.annotation.JsonBackReference;

import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulus.domene.felles.jpa.BaseEntitet;

@Entity(name = "RefusjonOverstyringEntitet")
@Table(name = "REFUSJON_OVERSTYRING")
public class RefusjonOverstyringEntitet extends BaseEntitet {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "GLOBAL_PK_SEQ_GENERATOR")
    private Long id;

    @Version
    @Column(name = "versjon", nullable = false)
    private int versjon;

    @Embedded
    private Arbeidsgiver arbeidsgiver;

    @Column(name = "fom")
    private LocalDate førsteMuligeRefusjonFom;

    @Column(name = "er_frist_utvidet")
    private Boolean erFristUtvidet;

    @JsonBackReference
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "refusjon_overstyringer_id", nullable = false, updatable = false)
    private RefusjonOverstyringerEntitet refusjonOverstyringer;

    @OneToMany(mappedBy = "refusjonOverstyring")
    private List<RefusjonPeriodeEntitet> refusjonPerioder = new ArrayList<>();


    public RefusjonOverstyringEntitet(RefusjonOverstyringEntitet refusjonOverstyringEntitet) {
        this.arbeidsgiver = refusjonOverstyringEntitet.getArbeidsgiver();
        this.førsteMuligeRefusjonFom = refusjonOverstyringEntitet.getFørsteMuligeRefusjonFom().orElse(null);
        this.erFristUtvidet = refusjonOverstyringEntitet.getErFristUtvidet();
        refusjonOverstyringEntitet.getRefusjonPerioder().stream().map(RefusjonPeriodeEntitet::new).forEach(this::leggTilRefusjonPeriode);
    }

    protected RefusjonOverstyringEntitet() {
        // Hibernate
    }

    void setRefusjonOverstyringerEntitet(RefusjonOverstyringerEntitet refusjonOverstyringer) {
        this.refusjonOverstyringer = refusjonOverstyringer;
    }

    public Arbeidsgiver getArbeidsgiver() {
        return arbeidsgiver;
    }

    public Optional<LocalDate> getFørsteMuligeRefusjonFom() {
        return Optional.ofNullable(førsteMuligeRefusjonFom);
    }

    public Boolean getErFristUtvidet() {
        return erFristUtvidet;
    }

    public List<RefusjonPeriodeEntitet> getRefusjonPerioder() {
        return refusjonPerioder;
    }

    void leggTilRefusjonPeriode(RefusjonPeriodeEntitet refusjonPeriodeEntitet) {
        if (!refusjonPerioder.contains(refusjonPeriodeEntitet)) {
            refusjonPeriodeEntitet.setRefusjonOverstyringEntitet(this);
            refusjonPerioder.add(refusjonPeriodeEntitet);
        }
    }

    public static RefusjonOverstyringEntitet.Builder builder() {
        return new RefusjonOverstyringEntitet.Builder();
    }

    public static class Builder {
        private final RefusjonOverstyringEntitet kladd;

        private Builder() {
            kladd = new RefusjonOverstyringEntitet();
        }

        public RefusjonOverstyringEntitet.Builder leggTilRefusjonPeriode(RefusjonPeriodeEntitet refusjonStart) {
            kladd.leggTilRefusjonPeriode(refusjonStart);
            return this;
        }

        public RefusjonOverstyringEntitet.Builder medArbeidsgiver(Arbeidsgiver arbeidsgiver) {
            kladd.arbeidsgiver = arbeidsgiver;
            return this;
        }


        public RefusjonOverstyringEntitet.Builder medFørsteMuligeRefusjonFom(LocalDate førsteMuligeRefusjonFom) {
            kladd.førsteMuligeRefusjonFom = førsteMuligeRefusjonFom;
            return this;
        }

        public RefusjonOverstyringEntitet.Builder medErFristUtvidet(Boolean erFristUtvidet) {
            kladd.erFristUtvidet = erFristUtvidet;
            return this;
        }


        public RefusjonOverstyringEntitet build() {
            kladd.verifiserTilstand();
            return kladd;
        }
    }

    private void verifiserTilstand() {
        Objects.requireNonNull(arbeidsgiver, "arbeidsgiver");
        if (førsteMuligeRefusjonFom == null && erFristUtvidet == null && refusjonPerioder.isEmpty()) {
            throw new IllegalStateException("Objektet inneholder ingen informasjon om refusjon, ugyldig tilstand");
        }
    }
}
