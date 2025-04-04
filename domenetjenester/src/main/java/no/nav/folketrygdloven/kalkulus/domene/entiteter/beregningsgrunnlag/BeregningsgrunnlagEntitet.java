package no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

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
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import org.hibernate.annotations.BatchSize;

import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.Beløp;
import no.nav.folketrygdloven.kalkulus.felles.diff.ChangeTracked;
import no.nav.folketrygdloven.kalkulus.felles.jpa.BaseEntitet;
import no.nav.folketrygdloven.kalkulus.kodeverk.FaktaOmBeregningTilfelle;

@SequenceGenerator(name = "GLOBAL_PK_SEQ_GENERATOR", sequenceName = "SEQ_GLOBAL_PK")
@Entity(name = "BeregningsgrunnlagEntitet")
@Table(name = "BEREGNINGSGRUNNLAG")
public class BeregningsgrunnlagEntitet extends BaseEntitet {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "GLOBAL_PK_SEQ_GENERATOR")
    private Long id;

    @Version
    @Column(name = "versjon", nullable = false)
    private int versjon;

    @Column(name = "skjaeringstidspunkt", nullable = false)
    private LocalDate skjæringstidspunkt;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "beregningsgrunnlag", cascade = CascadeType.PERSIST)
    @BatchSize(size = 20)
    private final List<BeregningsgrunnlagAktivitetStatusEntitet> aktivitetStatuser = new ArrayList<>();

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "beregningsgrunnlag", cascade = CascadeType.PERSIST)
    @BatchSize(size = 20)
    private final List<BeregningsgrunnlagPeriodeEntitet> beregningsgrunnlagPerioder = new ArrayList<>();

    @OneToOne(mappedBy = "beregningsgrunnlag", cascade = CascadeType.PERSIST)
    private BesteberegninggrunnlagEntitet besteberegninggrunnlag;

    @OneToMany(mappedBy = "beregningsgrunnlag")
    @BatchSize(size = 20)
    private final List<SammenligningsgrunnlagPrStatusEntitet> sammenligningsgrunnlagPrStatusListe = new ArrayList<>();

    @Embedded
    @AttributeOverrides(@AttributeOverride(name = "verdi", column = @Column(name = "grunnbeloep")))
    @ChangeTracked
    private Beløp grunnbeløp;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "beregningsgrunnlag", cascade = CascadeType.PERSIST)
    @BatchSize(size = 20)
    private final List<FaktaOmBeregningTilfelleEntitet> faktaOmBeregningTilfeller = new ArrayList<>();

    @Column(name = "overstyrt", nullable = false)
    private boolean overstyrt = false;

    public BeregningsgrunnlagEntitet(BeregningsgrunnlagEntitet kopi) {
        this.grunnbeløp = kopi.getGrunnbeløp();
        this.overstyrt = kopi.isOverstyrt();
        this.skjæringstidspunkt = kopi.getSkjæringstidspunkt();
        kopi.getSammenligningsgrunnlagPrStatusListe()
            .stream()
            .map(SammenligningsgrunnlagPrStatusEntitet::new)
            .forEach(this::leggTilSammenligningsgrunnlagPrStatus);
        kopi.getBesteberegninggrunnlag().map(BesteberegninggrunnlagEntitet::new).ifPresent(this::setBesteberegninggrunnlag);
        kopi.getFaktaOmBeregningTilfeller().stream().map(FaktaOmBeregningTilfelleEntitet::new).forEach(this::leggTilFaktaOmBeregningTilfelle);
        kopi.getAktivitetStatuser()
            .stream()
            .map(BeregningsgrunnlagAktivitetStatusEntitet::new)
            .forEach(this::leggTilBeregningsgrunnlagAktivitetStatus);
        kopi.getBeregningsgrunnlagPerioder().stream().map(BeregningsgrunnlagPeriodeEntitet::new).forEach(this::leggTilBeregningsgrunnlagPeriode);
    }

    public BeregningsgrunnlagEntitet() {
        // Hibernate
    }

    public Long getId() {
        return id;
    }

    public LocalDate getSkjæringstidspunkt() {
        return skjæringstidspunkt;
    }

    public List<BeregningsgrunnlagAktivitetStatusEntitet> getAktivitetStatuser() {
        return aktivitetStatuser.stream().sorted(Comparator.comparing(BeregningsgrunnlagAktivitetStatusEntitet::getAktivitetStatus)).toList();
    }

    public List<BeregningsgrunnlagPeriodeEntitet> getBeregningsgrunnlagPerioder() {
        return beregningsgrunnlagPerioder.stream()
            .sorted(Comparator.comparing(BeregningsgrunnlagPeriodeEntitet::getBeregningsgrunnlagPeriodeFom))
            .toList();
    }

    public Optional<BesteberegninggrunnlagEntitet> getBesteberegninggrunnlag() {
        return Optional.ofNullable(besteberegninggrunnlag);
    }


    public Beløp getGrunnbeløp() {
        return grunnbeløp;
    }

    void leggTilBeregningsgrunnlagAktivitetStatus(BeregningsgrunnlagAktivitetStatusEntitet aktivitetStatus) {
        Objects.requireNonNull(aktivitetStatus, "beregningsgrunnlagAktivitetStatus");
        aktivitetStatus.setBeregningsgrunnlag(this);
        // Aktivitetstatuser burde implementeres som eit Set
        if (!aktivitetStatuser.contains(aktivitetStatus)) {
            aktivitetStatuser.add(aktivitetStatus);
        }
    }

    void leggTilBeregningsgrunnlagPeriode(BeregningsgrunnlagPeriodeEntitet periode) {
        Objects.requireNonNull(periode, "beregningsgrunnlagPeriode");
        if (!beregningsgrunnlagPerioder.contains(periode)) { // NOSONAR
            periode.setBeregningsgrunnlag(this);
            beregningsgrunnlagPerioder.add(periode);
        }
    }

    public List<FaktaOmBeregningTilfelle> getFaktaOmBeregningTilfeller() {
        return faktaOmBeregningTilfeller.stream()
            .sorted(Comparator.comparing(FaktaOmBeregningTilfelleEntitet::getFaktaOmBeregningTilfelle))
            .map(FaktaOmBeregningTilfelleEntitet::getFaktaOmBeregningTilfelle)
            .toList();
    }


    void leggTilFaktaOmBeregningTilfelle(FaktaOmBeregningTilfelleEntitet faktaOmBeregningTilfelle) {
        Objects.requireNonNull(faktaOmBeregningTilfelle, "faktaOmBeregningTilfelle");
        // Aktivitetstatuser burde implementeres som eit Set
        if (!faktaOmBeregningTilfeller.contains(faktaOmBeregningTilfelle)) {
            faktaOmBeregningTilfelle.setBeregningsgrunnlag(this);
            faktaOmBeregningTilfeller.add(faktaOmBeregningTilfelle);
        }
    }

    public List<SammenligningsgrunnlagPrStatusEntitet> getSammenligningsgrunnlagPrStatusListe() {
        return sammenligningsgrunnlagPrStatusListe.stream()
            .sorted(Comparator.comparing(SammenligningsgrunnlagPrStatusEntitet::getSammenligningsgrunnlagType))
            .toList();
    }

    void leggTilSammenligningsgrunnlagPrStatus(SammenligningsgrunnlagPrStatusEntitet sammenligningsgrunnlagPrStatus) {
        Objects.requireNonNull(sammenligningsgrunnlagPrStatus, "sammenligningsgrunnlagPrStatus");
        var finnesFraFør = sammenligningsgrunnlagPrStatusListe.stream()
            .anyMatch(sg -> sg.getSammenligningsgrunnlagType().equals(sammenligningsgrunnlagPrStatus.getSammenligningsgrunnlagType()));
        if (finnesFraFør) {
            throw new IllegalStateException(
                "Feil: Kan ikke legge til sammenligningsgrunnlag for " + sammenligningsgrunnlagPrStatus.getSammenligningsgrunnlagType()
                    + " fordi det allerede er lagt til.");
        }
        sammenligningsgrunnlagPrStatusListe.add(sammenligningsgrunnlagPrStatus);
        sammenligningsgrunnlagPrStatus.setBeregningsgrunnlag(this);
    }

    void setBesteberegninggrunnlag(BesteberegninggrunnlagEntitet besteberegninggrunnlag) {
        besteberegninggrunnlag.setBeregningsgrunnlag(this);
        this.besteberegninggrunnlag = besteberegninggrunnlag;
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
        return getClass().getSimpleName() + "<" + "id=" + id + ", " //$NON-NLS-2$
            + "skjæringstidspunkt=" + skjæringstidspunkt + ", " //$NON-NLS-2$
            + "grunnbeløp=" + grunnbeløp + ", " //$NON-NLS-2$
            + ">";
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Builder kopiere(BeregningsgrunnlagEntitet mal) {
        return new Builder(mal);
    }

    public static class Builder {
        private boolean built;
        private BeregningsgrunnlagEntitet kladd;

        private Builder() {
            kladd = new BeregningsgrunnlagEntitet();
        }

        private Builder(BeregningsgrunnlagEntitet mal) {
            kladd = new BeregningsgrunnlagEntitet(mal);
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

        public Builder leggTilSammenligningsgrunnlag(SammenligningsgrunnlagPrStatusEntitet sammenligningsgrunnlagPrStatus) {
            kladd.leggTilSammenligningsgrunnlagPrStatus(sammenligningsgrunnlagPrStatus);
            return this;
        }

        public Builder medOverstyring(boolean overstyrt) {
            verifiserKanModifisere();
            kladd.overstyrt = overstyrt;
            return this;
        }

        public Builder leggTilBeregningsgrunnlagPeriode(BeregningsgrunnlagPeriodeEntitet beregningsgrunnlagperiode) {
            verifiserKanModifisere();
            kladd.leggTilBeregningsgrunnlagPeriode(beregningsgrunnlagperiode);
            return this;
        }

        public Builder leggTilAktivitetstatus(BeregningsgrunnlagAktivitetStatusEntitet aktivitetStatusEntitet) {
            verifiserKanModifisere();
            kladd.leggTilBeregningsgrunnlagAktivitetStatus(aktivitetStatusEntitet);
            return this;
        }

        public Builder leggTilFaktaTilfelle(FaktaOmBeregningTilfelle tilfelle) {
            verifiserKanModifisere();
            var tilfelleEnt = FaktaOmBeregningTilfelleEntitet.builder().medFaktaOmBeregningTilfelle(tilfelle).build();
            kladd.leggTilFaktaOmBeregningTilfelle(tilfelleEnt);
            return this;
        }

        public Builder medBesteberegninggrunnlag(BesteberegninggrunnlagEntitet besteberegninggrunnlag) {
            verifiserKanModifisere();
            if (besteberegninggrunnlag == null) {
                return this;
            }
            kladd.setBesteberegninggrunnlag(besteberegninggrunnlag);
            return this;
        }

        public BeregningsgrunnlagEntitet build() {
            verifyStateForBuild();
            built = true;
            return kladd;
        }

        private void verifiserKanModifisere() {
            if (built) {
                throw new IllegalStateException("Er allerede bygd, kan ikke oppdatere videre: " + this.kladd);
            }
        }

        public void verifyStateForBuild() {
            Objects.requireNonNull(kladd.skjæringstidspunkt, "skjæringstidspunkt");
        }
    }
}
