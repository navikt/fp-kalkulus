package no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.hibernate.annotations.BatchSize;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.Beløp;
import no.nav.folketrygdloven.kalkulus.felles.diff.ChangeTracked;
import no.nav.folketrygdloven.kalkulus.felles.jpa.BaseEntitet;
import no.nav.folketrygdloven.kalkulus.kodeverk.FaktaOmBeregningTilfelle;

@Entity(name = "Beregningsgrunnlag")
@Table(name = "BEREGNINGSGRUNNLAG")
public class BeregningsgrunnlagEntitet extends BaseEntitet {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_BEREGNINGSGRUNNLAG")
    private Long id;

    @Version
    @Column(name = "versjon", nullable = false)
    private long versjon;

    @Column(name = "skjaringstidspunkt", nullable = false)
    private LocalDate skjæringstidspunkt;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "beregningsgrunnlag", cascade = CascadeType.PERSIST)
    @BatchSize(size=20)
    private final List<BeregningsgrunnlagAktivitetStatus> aktivitetStatuser = new ArrayList<>();

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "beregningsgrunnlag", cascade = CascadeType.PERSIST)
    @BatchSize(size=20)
    private final List<BeregningsgrunnlagPeriode> beregningsgrunnlagPerioder = new ArrayList<>();

    @OneToOne(mappedBy = "beregningsgrunnlag", cascade = CascadeType.PERSIST)
    private Sammenligningsgrunnlag sammenligningsgrunnlag;

    @OneToMany(mappedBy = "beregningsgrunnlag")
    @BatchSize(size=20)
    private final List<SammenligningsgrunnlagPrStatus> sammenligningsgrunnlagPrStatusListe = new ArrayList<>();

    @Embedded
    @AttributeOverrides(@AttributeOverride(name = "verdi", column = @Column(name = "grunnbeloep")))
    @ChangeTracked
    private Beløp grunnbeløp;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "beregningsgrunnlag", cascade = CascadeType.PERSIST)
    @BatchSize(size=20)
    private final List<BeregningsgrunnlagFaktaOmBeregningTilfelle> faktaOmBeregningTilfeller = new ArrayList<>();

    @Column(name = "overstyrt", nullable = false)
    private boolean overstyrt = false;

    public BeregningsgrunnlagEntitet(BeregningsgrunnlagEntitet kopi) {
        this.grunnbeløp = kopi.getGrunnbeløp();
        this.overstyrt = kopi.isOverstyrt();
        this.skjæringstidspunkt = kopi.getSkjæringstidspunkt();
        kopi.getSammenligningsgrunnlag().map(Sammenligningsgrunnlag::new).ifPresent(this::setSammenligningsgrunnlag);
        kopi.getSammenligningsgrunnlagPrStatusListe().stream().map(SammenligningsgrunnlagPrStatus::new).forEach(this::leggTilSammenligningsgrunnlagPrStatus);
        kopi.faktaOmBeregningTilfeller.stream().map(BeregningsgrunnlagFaktaOmBeregningTilfelle::new).forEach(this::leggTilFaktaOmBeregningTilfelle);
        kopi.getAktivitetStatuser().stream().map(BeregningsgrunnlagAktivitetStatus::new).forEach(this::leggTilBeregningsgrunnlagAktivitetStatus);
        kopi.getBeregningsgrunnlagPerioder().stream().map(BeregningsgrunnlagPeriode::new)
                .forEach(this::leggTilBeregningsgrunnlagPeriode);
    }

    public BeregningsgrunnlagEntitet() {
    }

    public Long getId() {
        return id;
    }

    public LocalDate getSkjæringstidspunkt() {
        return skjæringstidspunkt;
    }

    public List<BeregningsgrunnlagAktivitetStatus> getAktivitetStatuser() {
        return aktivitetStatuser.stream()
                .sorted(Comparator.comparing(BeregningsgrunnlagAktivitetStatus::getAktivitetStatus))
                .toList();
    }

    public List<BeregningsgrunnlagPeriode> getBeregningsgrunnlagPerioder() {
        return beregningsgrunnlagPerioder
                .stream()
                .sorted(Comparator.comparing(BeregningsgrunnlagPeriode::getBeregningsgrunnlagPeriodeFom))
                .toList();
    }

    public Optional<Sammenligningsgrunnlag> getSammenligningsgrunnlag() {
        return Optional.ofNullable(sammenligningsgrunnlag);
    }

    void setSammenligningsgrunnlag(Sammenligningsgrunnlag sammenligningsgrunnlag) {
        sammenligningsgrunnlag.setBeregningsgrunnlag(this);
        this.sammenligningsgrunnlag = sammenligningsgrunnlag;
    }

    public Beløp getGrunnbeløp() {
        return grunnbeløp;
    }

    void leggTilBeregningsgrunnlagAktivitetStatus(BeregningsgrunnlagAktivitetStatus bgAktivitetStatus) {
        Objects.requireNonNull(bgAktivitetStatus, "beregningsgrunnlagAktivitetStatus");
        bgAktivitetStatus.setBeregningsgrunnlag(this);
        // Aktivitetstatuser burde implementeres som eit Set
        if (!aktivitetStatuser.contains(bgAktivitetStatus)) {
            aktivitetStatuser.add(bgAktivitetStatus);
        }
    }

    void leggTilBeregningsgrunnlagPeriode(BeregningsgrunnlagPeriode bgPeriode) {
        Objects.requireNonNull(bgPeriode, "beregningsgrunnlagPeriode");
        if (!beregningsgrunnlagPerioder.contains(bgPeriode)) { // NOSONAR
            bgPeriode.setBeregningsgrunnlag(this);
            beregningsgrunnlagPerioder.add(bgPeriode);
        }
    }

    public List<FaktaOmBeregningTilfelle> getFaktaOmBeregningTilfeller() {
        return faktaOmBeregningTilfeller
                .stream()
                .sorted(Comparator.comparing(BeregningsgrunnlagFaktaOmBeregningTilfelle::getFaktaOmBeregningTilfelle))
                .map(BeregningsgrunnlagFaktaOmBeregningTilfelle::getFaktaOmBeregningTilfelle)
                .toList();
    }


    void leggTilFaktaOmBeregningTilfelle(BeregningsgrunnlagFaktaOmBeregningTilfelle beregningsgrunnlagFaktaOmBeregningTilfelle) {
        Objects.requireNonNull(beregningsgrunnlagFaktaOmBeregningTilfelle, "beregningsgrunnlagFaktaOmBeregningTilfelle");
        // Aktivitetstatuser burde implementeres som eit Set
        if (!faktaOmBeregningTilfeller.contains(beregningsgrunnlagFaktaOmBeregningTilfelle)) {
            beregningsgrunnlagFaktaOmBeregningTilfelle.setBeregningsgrunnlag(this);
            faktaOmBeregningTilfeller.add(beregningsgrunnlagFaktaOmBeregningTilfelle);
        }
    }

    public List<SammenligningsgrunnlagPrStatus> getSammenligningsgrunnlagPrStatusListe() {
        return sammenligningsgrunnlagPrStatusListe.stream()
                .sorted(Comparator.comparing(SammenligningsgrunnlagPrStatus::getSammenligningsgrunnlagType))
                .toList();
    }

    void leggTilSammenligningsgrunnlagPrStatus(SammenligningsgrunnlagPrStatus sammenligningsgrunnlagPrStatus) {
        Objects.requireNonNull(sammenligningsgrunnlagPrStatus, "sammenligningsgrunnlagPrStatus");
        var finnesFraFør = sammenligningsgrunnlagPrStatusListe.stream()
                .anyMatch(sg -> sg.getSammenligningsgrunnlagType().equals(sammenligningsgrunnlagPrStatus.getSammenligningsgrunnlagType()));
        if (finnesFraFør) {
            throw new IllegalStateException("Feil: Kan ikke legge til sammenligningsgrunnlag for " + sammenligningsgrunnlagPrStatus.getSammenligningsgrunnlagType() +
                    " fordi det allerede er lagt til.");
        }
        sammenligningsgrunnlagPrStatusListe.add(sammenligningsgrunnlagPrStatus);
        sammenligningsgrunnlagPrStatus.setBeregningsgrunnlag(this);
    }

    public boolean isOverstyrt() {
        return overstyrt;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (!(obj instanceof BeregningsgrunnlagEntitet)) {
            return false;
        }
        BeregningsgrunnlagEntitet other = (BeregningsgrunnlagEntitet) obj;
        return Objects.equals(this.getSkjæringstidspunkt(), other.getSkjæringstidspunkt());
    }

    @Override
    public int hashCode() {
        return Objects.hash(skjæringstidspunkt);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "<" + //$NON-NLS-1$
                "id=" + id + ", " //$NON-NLS-2$
                + "skjæringstidspunkt=" + skjæringstidspunkt + ", " //$NON-NLS-1$ //$NON-NLS-2$
                + "grunnbeløp=" + grunnbeløp + ", " //$NON-NLS-1$ //$NON-NLS-2$
                + ">"; //$NON-NLS-1$
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private boolean built;
        private BeregningsgrunnlagEntitet kladd;

        private Builder() {
            kladd = new BeregningsgrunnlagEntitet();
        }

        public Builder medSkjæringstidspunkt(LocalDate skjæringstidspunkt) {
            verifiserKanModifisere();
            kladd.skjæringstidspunkt = skjæringstidspunkt;
            return this;
        }

        public Builder medGrunnbeløp(BigDecimal grunnbeløp) {
            verifiserKanModifisere();
            kladd.grunnbeløp = new Beløp(grunnbeløp);
            return this;
        }

        public Builder medGrunnbeløp(Beløp grunnbeløp) {
            verifiserKanModifisere();
            kladd.grunnbeløp = grunnbeløp;
            return this;
        }

        public Builder leggTilAktivitetStatus(BeregningsgrunnlagAktivitetStatus.Builder aktivitetStatusBuilder) {
            verifiserKanModifisere();
            aktivitetStatusBuilder.build(kladd);
            return this;
        }

        public Builder leggTilBeregningsgrunnlagPeriode(BeregningsgrunnlagPeriode.Builder beregningsgrunnlagPeriodeBuilder) {
            verifiserKanModifisere();
            beregningsgrunnlagPeriodeBuilder.build(kladd);
            return this;
        }

        public Builder leggTilFaktaOmBeregningTilfeller(List<FaktaOmBeregningTilfelle> faktaOmBeregningTilfeller) {
            verifiserKanModifisere();
            faktaOmBeregningTilfeller.forEach(tilfelle -> BeregningsgrunnlagFaktaOmBeregningTilfelle.builder().medFaktaOmBeregningTilfelle(tilfelle).build(kladd));
            return this;
        }

        /**
         * @deprecated bruk -> {@link SammenligningsgrunnlagPrStatus}
         */
        @Deprecated
        public Builder medSammenligningsgrunnlagOld(Sammenligningsgrunnlag sammenligningsgrunnlag) {
            verifiserKanModifisere();
            sammenligningsgrunnlag.setBeregningsgrunnlag(kladd);
            kladd.sammenligningsgrunnlag = sammenligningsgrunnlag;
            return this;
        }

        public Builder leggTilSammenligningsgrunnlag(SammenligningsgrunnlagPrStatus sammenligningsgrunnlagPrStatus) {
            kladd.leggTilSammenligningsgrunnlagPrStatus(sammenligningsgrunnlagPrStatus);
            return this;
        }

        public Builder medOverstyring(boolean overstyrt) {
            verifiserKanModifisere();
            kladd.overstyrt = overstyrt;
            return this;
        }

        public BeregningsgrunnlagEntitet build() {
            verifyStateForBuild();
            built = true;
            return kladd;
        }

        private void verifiserKanModifisere() {
            if(built) {
                throw new IllegalStateException("Er allerede bygd, kan ikke oppdatere videre: " + this.kladd);
            }
        }

        public void verifyStateForBuild() {
            Objects.requireNonNull(kladd.skjæringstidspunkt, "skjæringstidspunkt");
        }
    }
}
