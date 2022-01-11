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
import no.nav.folketrygdloven.kalkulus.felles.jpa.BaseEntitet;

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

    @Column(name = "er_frist_utvidet")
    private Boolean erFristUtvidet;

    @JsonBackReference
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "br_overstyringer_id", nullable = false, updatable = false)
    private BeregningRefusjonOverstyringerEntitet refusjonOverstyringer;

    @OneToMany(mappedBy = "refusjonOverstyring")
    private List<BeregningRefusjonPeriodeEntitet> refusjonPerioder = new ArrayList<>();

    public BeregningRefusjonOverstyringEntitet(BeregningRefusjonOverstyringEntitet beregningRefusjonOverstyringEntitet) {
        this.arbeidsgiver = beregningRefusjonOverstyringEntitet.getArbeidsgiver();
        this.førsteMuligeRefusjonFom = beregningRefusjonOverstyringEntitet.getFørsteMuligeRefusjonFom().orElse(null);
        this.erFristUtvidet =  beregningRefusjonOverstyringEntitet.getErFristUtvidet();
        beregningRefusjonOverstyringEntitet.getRefusjonPerioder().stream().map(BeregningRefusjonPeriodeEntitet::new)
                .forEach(this::leggTilBeregningRefusjonPeriode);
    }

    private BeregningRefusjonOverstyringEntitet() {
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

    public Boolean getErFristUtvidet() {
        return erFristUtvidet;
    }

    public List<BeregningRefusjonPeriodeEntitet> getRefusjonPerioder() {
        return refusjonPerioder;
    }

    void leggTilBeregningRefusjonPeriode(BeregningRefusjonPeriodeEntitet beregningRefusjonPeriodeEntitet) {
        if (!refusjonPerioder.contains(beregningRefusjonPeriodeEntitet)) {
            beregningRefusjonPeriodeEntitet.setRefusjonOverstyringEntitet(this);
            refusjonPerioder.add(beregningRefusjonPeriodeEntitet);
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

        public BeregningRefusjonOverstyringEntitet.Builder medArbeidsgiver(Arbeidsgiver arbeidsgiver) {
            kladd.arbeidsgiver = arbeidsgiver;
            return this;
        }


        public BeregningRefusjonOverstyringEntitet.Builder medFørsteMuligeRefusjonFom(LocalDate førsteMuligeRefusjonFom) {
            kladd.førsteMuligeRefusjonFom = førsteMuligeRefusjonFom;
            return this;
        }

        public BeregningRefusjonOverstyringEntitet.Builder medErFristUtvidet(Boolean erFristUtvidet) {
            kladd.erFristUtvidet = erFristUtvidet;
            return this;
        }


        public BeregningRefusjonOverstyringEntitet build() {
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
