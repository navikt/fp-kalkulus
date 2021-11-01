package no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag;

import static no.nav.folketrygdloven.kalkulus.felles.tid.AbstractIntervall.TIDENES_ENDE;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.CascadeType;
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

import org.hibernate.annotations.BatchSize;

import com.fasterxml.jackson.annotation.JsonBackReference;

import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.Beløp;
import no.nav.folketrygdloven.kalkulus.felles.jpa.BaseEntitet;
import no.nav.folketrygdloven.kalkulus.felles.jpa.IntervallEntitet;
import no.nav.folketrygdloven.kalkulus.kodeverk.PeriodeÅrsak;

@Entity(name = "BeregningsgrunnlagPeriode")
@Table(name = "BEREGNINGSGRUNNLAG_PERIODE")
public class BeregningsgrunnlagPeriode extends BaseEntitet {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_BG_PERIODE")
    private Long id;

    @Version
    @Column(name = "versjon", nullable = false)
    private long versjon;

    @JsonBackReference
    @ManyToOne(optional = false)
    @JoinColumn(name = "beregningsgrunnlag_id", nullable = false, updatable = false)
    private BeregningsgrunnlagEntitet beregningsgrunnlag;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "beregningsgrunnlagPeriode", cascade = CascadeType.PERSIST, orphanRemoval = true)
    @BatchSize(size=20)
    private final List<BeregningsgrunnlagPrStatusOgAndel> beregningsgrunnlagPrStatusOgAndelList = new ArrayList<>();

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "fomDato", column = @Column(name = "bg_periode_fom")),
        @AttributeOverride(name = "tomDato", column = @Column(name = "bg_periode_tom"))
    })
    private IntervallEntitet periode;

    @Embedded
    @AttributeOverrides(@AttributeOverride(name = "verdi", column = @Column(name = "brutto_pr_aar")))
    private Beløp bruttoPrÅr;

    @Embedded
    @AttributeOverrides(@AttributeOverride(name = "verdi", column = @Column(name = "avkortet_pr_aar")))
    private Beløp avkortetPrÅr;

    @Embedded
    @AttributeOverrides(@AttributeOverride(name = "verdi", column = @Column(name = "redusert_pr_aar")))
    private Beløp redusertPrÅr;

    @Column(name = "dagsats")
    private Long dagsats;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "beregningsgrunnlagPeriode", cascade = CascadeType.PERSIST, orphanRemoval = true)
    @BatchSize(size=20)
    private final List<BeregningsgrunnlagPeriodeÅrsak> beregningsgrunnlagPeriodeÅrsaker = new ArrayList<>();

    public BeregningsgrunnlagPeriode(BeregningsgrunnlagPeriode beregningsgrunnlagPeriode) {
        this.avkortetPrÅr = beregningsgrunnlagPeriode.getAvkortetPrÅr();
        this.bruttoPrÅr = beregningsgrunnlagPeriode.getBruttoPrÅr();
        this.dagsats = beregningsgrunnlagPeriode.getDagsats();
        this.periode = beregningsgrunnlagPeriode.getPeriode();
        this.redusertPrÅr = beregningsgrunnlagPeriode.getRedusertPrÅr();
        beregningsgrunnlagPeriode.getBeregningsgrunnlagPeriodeÅrsaker().stream().map(BeregningsgrunnlagPeriodeÅrsak::new)
                .forEach(this::addBeregningsgrunnlagPeriodeÅrsak);
        beregningsgrunnlagPeriode.getBeregningsgrunnlagPrStatusOgAndelList().stream().map(BeregningsgrunnlagPrStatusOgAndel::new)
                .forEach(this::addBeregningsgrunnlagPrStatusOgAndel);
    }

    private BeregningsgrunnlagPeriode() { }

    public Long getId() {
        return id;
    }

    public BeregningsgrunnlagEntitet getBeregningsgrunnlag() {
        return beregningsgrunnlag;
    }

    public List<BeregningsgrunnlagPrStatusOgAndel> getBeregningsgrunnlagPrStatusOgAndelList() {
        return beregningsgrunnlagPrStatusOgAndelList.stream()
                .sorted(Comparator.comparing(BeregningsgrunnlagPrStatusOgAndel::getAndelsnr))
                .collect(Collectors.toUnmodifiableList());
    }

    public IntervallEntitet getPeriode() {
        if (periode.getTomDato() == null) {
            return IntervallEntitet.fraOgMedTilOgMed(periode.getFomDato(), TIDENES_ENDE);
        }
        return periode;
    }

    public LocalDate getBeregningsgrunnlagPeriodeFom() {
        return periode.getFomDato();
    }

    public LocalDate getBeregningsgrunnlagPeriodeTom() {
        return periode.getTomDato();
    }

    void updateBruttoPrÅr() {
        bruttoPrÅr = beregningsgrunnlagPrStatusOgAndelList.stream()
                .filter(bgpsa -> bgpsa.getBruttoPrÅr() != null)
                .map(BeregningsgrunnlagPrStatusOgAndel::getBruttoPrÅr)
                .reduce(Beløp::adder)
                .orElse(Beløp.ZERO);
    }

    public Beløp getBruttoPrÅr() {
        return bruttoPrÅr;
    }

    public Beløp getAvkortetPrÅr() {
        return avkortetPrÅr;
    }

    public Beløp getRedusertPrÅr() {
        return redusertPrÅr;
    }

    public Long getDagsats() {
        return dagsats;
    }

    public List<BeregningsgrunnlagPeriodeÅrsak> getBeregningsgrunnlagPeriodeÅrsaker() {
        return beregningsgrunnlagPeriodeÅrsaker.stream()
                .sorted(Comparator.comparing(BeregningsgrunnlagPeriodeÅrsak::getPeriodeÅrsak))
                .collect(Collectors.toUnmodifiableList());
    }

    public List<PeriodeÅrsak> getPeriodeÅrsaker() {
        return getBeregningsgrunnlagPeriodeÅrsaker().stream()
                .map(BeregningsgrunnlagPeriodeÅrsak::getPeriodeÅrsak)
                .collect(Collectors.toUnmodifiableList());
    }

    void addBeregningsgrunnlagPrStatusOgAndel(BeregningsgrunnlagPrStatusOgAndel bgPrStatusOgAndel) {
        Objects.requireNonNull(bgPrStatusOgAndel, "beregningsgrunnlagPrStatusOgAndel");
        if (!beregningsgrunnlagPrStatusOgAndelList.contains(bgPrStatusOgAndel)) { // NOSONAR Class defines List based fields but uses them like Sets: Ingening å tjene på å bytte til Set ettersom det er små lister
            bgPrStatusOgAndel.setBeregningsgrunnlagPeriode(this);
            beregningsgrunnlagPrStatusOgAndelList.add(bgPrStatusOgAndel);
        }
    }

    void addBeregningsgrunnlagPeriodeÅrsak(BeregningsgrunnlagPeriodeÅrsak bgPeriodeÅrsak) {
        Objects.requireNonNull(bgPeriodeÅrsak, "beregningsgrunnlagPeriodeÅrsak");
        if (!beregningsgrunnlagPeriodeÅrsaker.contains(bgPeriodeÅrsak)) { // NOSONAR Class defines List based fields but uses them like Sets: Ingening å tjene på å bytte til Set ettersom det er små lister
            bgPeriodeÅrsak.setBeregningsgrunnlagPeriode(this);
            beregningsgrunnlagPeriodeÅrsaker.add(bgPeriodeÅrsak);
        }
    }

    void setBeregningsgrunnlag(BeregningsgrunnlagEntitet beregningsgrunnlag) {
        this.beregningsgrunnlag = beregningsgrunnlag;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (!(obj instanceof BeregningsgrunnlagPeriode)) {
            return false;
        }
        BeregningsgrunnlagPeriode other = (BeregningsgrunnlagPeriode) obj;
        return Objects.equals(this.periode.getFomDato(), other.periode.getFomDato())
                && Objects.equals(this.periode.getTomDato(), other.periode.getTomDato())
                && Objects.equals(this.getBruttoPrÅr(), other.getBruttoPrÅr())
                && Objects.equals(this.getAvkortetPrÅr(), other.getAvkortetPrÅr())
                && Objects.equals(this.getRedusertPrÅr(), other.getRedusertPrÅr())
                && Objects.equals(this.getDagsats(), other.getDagsats());
    }

    @Override
    public int hashCode() {
        return Objects.hash(periode, bruttoPrÅr, avkortetPrÅr, redusertPrÅr, dagsats);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "<" + //$NON-NLS-1$
                "id=" + id + ", " //$NON-NLS-2$
                + "periode=" + periode + ", " // $NON-NLS-1$ //$NON-NLS-2$
                + "bruttoPrÅr=" + bruttoPrÅr + ", " //$NON-NLS-1$ //$NON-NLS-2$
                + "avkortetPrÅr=" + avkortetPrÅr + ", " //$NON-NLS-1$ //$NON-NLS-2$
                + "redusertPrÅr=" + redusertPrÅr + ", " //$NON-NLS-1$ //$NON-NLS-2$
                + "dagsats=" + dagsats + ", " //$NON-NLS-1$ //$NON-NLS-2$
                + ">"; //$NON-NLS-1$
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Builder builder(BeregningsgrunnlagPeriode eksisterendeBeregningsgrunnlagPeriode) {
        return new Builder(eksisterendeBeregningsgrunnlagPeriode);
    }


    public static class Builder {
        private final BeregningsgrunnlagPeriode kladd;
        private boolean built;

        public Builder() {
            kladd = new BeregningsgrunnlagPeriode();
        }

        public Builder(BeregningsgrunnlagPeriode eksisterendeBeregningsgrunnlagPeriod) {
            if (Objects.nonNull(eksisterendeBeregningsgrunnlagPeriod.getId())) {
                throw new IllegalArgumentException("Kan ikke bygge på et lagret grunnlag");
            }
            kladd = eksisterendeBeregningsgrunnlagPeriod;
        }

        public Builder leggTilBeregningsgrunnlagPrStatusOgAndel(BeregningsgrunnlagPrStatusOgAndel.Builder prStatusOgAndelBuilder) {
            verifiserKanModifisere();
            prStatusOgAndelBuilder.build(kladd);
            return this;
        }

        public Builder medBeregningsgrunnlagPeriode(LocalDate fraOgMed, LocalDate tilOgMed) {
            verifiserKanModifisere();
            kladd.periode = IntervallEntitet.fraOgMedTilOgMed(fraOgMed, tilOgMed);
            return this;
        }

        public Builder medBruttoPrÅr(Beløp bruttoPrÅr) {
            verifiserKanModifisere();
            kladd.bruttoPrÅr = bruttoPrÅr;
            return this;
        }

        public Builder medAvkortetPrÅr(Beløp avkortetPrÅr) {
            verifiserKanModifisere();
            kladd.avkortetPrÅr = avkortetPrÅr;
            return this;
        }

        public Builder medRedusertPrÅr(Beløp redusertPrÅr) {
            verifiserKanModifisere();
            kladd.redusertPrÅr = redusertPrÅr;
            return this;
        }

        public Builder leggTilPeriodeÅrsak(PeriodeÅrsak periodeÅrsak) {
            verifiserKanModifisere();
            if (!kladd.getPeriodeÅrsaker().contains(periodeÅrsak)) {
                BeregningsgrunnlagPeriodeÅrsak.Builder bgPeriodeÅrsakBuilder = new BeregningsgrunnlagPeriodeÅrsak.Builder();
                bgPeriodeÅrsakBuilder.medPeriodeÅrsak(periodeÅrsak);
                bgPeriodeÅrsakBuilder.build(kladd);
            }
            return this;
        }

        public Builder leggTilPeriodeÅrsaker(Collection<PeriodeÅrsak> periodeÅrsaker) {
            verifiserKanModifisere();
            periodeÅrsaker.forEach(this::leggTilPeriodeÅrsak);
            return this;
        }

        public BeregningsgrunnlagPeriode build(BeregningsgrunnlagEntitet beregningsgrunnlag) {
            verifyStateForBuild();
            beregningsgrunnlag.leggTilBeregningsgrunnlagPeriode(kladd);

            Long dagsatsSum = kladd.beregningsgrunnlagPrStatusOgAndelList.stream()
                    .map(BeregningsgrunnlagPrStatusOgAndel::getDagsats)
                    .filter(Objects::nonNull)
                    .reduce(Long::sum)
                    .orElse(null);
            kladd.dagsats = dagsatsSum;
            built = true;
            return kladd;
        }

        private void verifiserKanModifisere() {
            if(built) {
                throw new IllegalStateException("Er allerede bygd, kan ikke oppdatere videre: " + this.kladd);
            }
        }

        private void verifyStateForBuild() {
            Objects.requireNonNull(kladd.beregningsgrunnlagPrStatusOgAndelList, "beregningsgrunnlagPrStatusOgAndelList");
            Objects.requireNonNull(kladd.periode, "beregningsgrunnlagPeriodeFom");
        }
    }
}
