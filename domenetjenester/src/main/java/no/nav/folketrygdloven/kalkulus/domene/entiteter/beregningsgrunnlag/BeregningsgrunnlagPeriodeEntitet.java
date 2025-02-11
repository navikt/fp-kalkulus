package no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag;

import static no.nav.folketrygdloven.kalkulus.felles.jpa.AbstractIntervall.TIDENES_ENDE;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

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
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import org.hibernate.annotations.BatchSize;

import com.fasterxml.jackson.annotation.JsonBackReference;

import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.Beløp;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.Prosent;
import no.nav.folketrygdloven.kalkulus.felles.jpa.BaseEntitet;
import no.nav.folketrygdloven.kalkulus.felles.jpa.IntervallEntitet;
import no.nav.folketrygdloven.kalkulus.kodeverk.PeriodeÅrsak;

@Entity(name = "BeregningsgrunnlagPeriodeEntitet")
@Table(name = "BEREGNINGSGRUNNLAG_PERIODE")
public class BeregningsgrunnlagPeriodeEntitet extends BaseEntitet {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "GLOBAL_PK_SEQ_GENERATOR")
    private Long id;

    @Version
    @Column(name = "versjon", nullable = false)
    private int versjon;

    @JsonBackReference
    @ManyToOne(optional = false)
    @JoinColumn(name = "beregningsgrunnlag_id", nullable = false, updatable = false)
    private BeregningsgrunnlagEntitet beregningsgrunnlag;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "beregningsgrunnlagPeriode", cascade = CascadeType.PERSIST, orphanRemoval = true)
    @BatchSize(size = 20)
    private final List<BeregningsgrunnlagAndelEntitet> beregningsgrunnlagAndelList = new ArrayList<>();

    @Embedded
    @AttributeOverride(name = "fomDato", column = @Column(name = "periode_fom"))
    @AttributeOverride(name = "tomDato", column = @Column(name = "periode_tom"))
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

    @Embedded
    @AttributeOverrides(@AttributeOverride(name = "verdi", column = @Column(name = "inntekt_graderingsprosent_brutto")))
    private Prosent inntektgraderingsprosentBrutto;

    @Column(name = "total_utbetalingsgrad_fra_uttak")
    private BigDecimal totalUtbetalingsgradFraUttak;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "beregningsgrunnlagPeriode", cascade = CascadeType.PERSIST, orphanRemoval = true)
    @BatchSize(size = 20)
    private final List<PeriodeÅrsakEntitet> periodeårsaker = new ArrayList<>();

    public BeregningsgrunnlagPeriodeEntitet(BeregningsgrunnlagPeriodeEntitet beregningsgrunnlagPeriode) {
        this.avkortetPrÅr = beregningsgrunnlagPeriode.getAvkortetPrÅr();
        this.bruttoPrÅr = beregningsgrunnlagPeriode.getBruttoPrÅr();
        this.dagsats = beregningsgrunnlagPeriode.getDagsats();
        this.periode = beregningsgrunnlagPeriode.getPeriode();
        this.redusertPrÅr = beregningsgrunnlagPeriode.getRedusertPrÅr();
        this.inntektgraderingsprosentBrutto = beregningsgrunnlagPeriode.getInntektgraderingsprosentBrutto();
        this.totalUtbetalingsgradFraUttak = beregningsgrunnlagPeriode.getTotalUtbetalingsgradFraUttak();
        beregningsgrunnlagPeriode.getPeriodeårsaker().stream().map(PeriodeÅrsakEntitet::new).forEach(this::addPeriodeÅrsak);
        beregningsgrunnlagPeriode.getBeregningsgrunnlagAndelList()
            .stream()
            .map(BeregningsgrunnlagAndelEntitet::new)
            .forEach(this::addBeregningsgrunnlagAndel);
    }

    public BeregningsgrunnlagPeriodeEntitet() {
        // Hibernate
    }

    public Long getId() {
        return id;
    }

    public BeregningsgrunnlagEntitet getBeregningsgrunnlag() {
        return beregningsgrunnlag;
    }

    public List<BeregningsgrunnlagAndelEntitet> getBeregningsgrunnlagAndelList() {
        return beregningsgrunnlagAndelList.stream().sorted(Comparator.comparing(BeregningsgrunnlagAndelEntitet::getAndelsnr)).toList();
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
        bruttoPrÅr = beregningsgrunnlagAndelList.stream()
            .filter(bgpsa -> bgpsa.getBruttoPrÅr() != null)
            .map(BeregningsgrunnlagAndelEntitet::getBruttoPrÅr)
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

    public Prosent getInntektgraderingsprosentBrutto() {
        return inntektgraderingsprosentBrutto;
    }

    public BigDecimal getTotalUtbetalingsgradFraUttak() {
        return totalUtbetalingsgradFraUttak;
    }

    public List<PeriodeÅrsakEntitet> getPeriodeårsaker() {
        return periodeårsaker.stream().sorted(Comparator.comparing(PeriodeÅrsakEntitet::getPeriodeÅrsak)).toList();
    }

    public List<PeriodeÅrsak> getPeriodeÅrsaker() {
        return getPeriodeårsaker().stream().map(PeriodeÅrsakEntitet::getPeriodeÅrsak).toList();
    }

    void addBeregningsgrunnlagAndel(BeregningsgrunnlagAndelEntitet andel) {
        Objects.requireNonNull(andel, "andel");
        if (!beregningsgrunnlagAndelList.contains(
            andel)) { // NOSONAR Class defines List based fields but uses them like Sets: Ingening å tjene på å bytte til Set ettersom det er små lister
            andel.setBeregningsgrunnlagPeriode(this);
            beregningsgrunnlagAndelList.add(andel);
        }
    }

    void addPeriodeÅrsak(PeriodeÅrsakEntitet periodeÅrsak) {
        Objects.requireNonNull(periodeÅrsak, "periodeÅrsak");
        if (!periodeårsaker.contains(
            periodeÅrsak)) { // NOSONAR Class defines List based fields but uses them like Sets: Ingening å tjene på å bytte til Set ettersom det er små lister
            periodeÅrsak.setBeregningsgrunnlagPeriode(this);
            periodeårsaker.add(periodeÅrsak);
        }
    }

    void setBeregningsgrunnlag(BeregningsgrunnlagEntitet beregningsgrunnlag) {
        this.beregningsgrunnlag = beregningsgrunnlag;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (!(obj instanceof BeregningsgrunnlagPeriodeEntitet)) {
            return false;
        }
        BeregningsgrunnlagPeriodeEntitet other = (BeregningsgrunnlagPeriodeEntitet) obj;
        return Objects.equals(this.periode.getFomDato(), other.periode.getFomDato()) && Objects.equals(this.periode.getTomDato(),
            other.periode.getTomDato()) && Objects.equals(this.getBruttoPrÅr(), other.getBruttoPrÅr()) && Objects.equals(this.getAvkortetPrÅr(),
            other.getAvkortetPrÅr()) && Objects.equals(this.getRedusertPrÅr(), other.getRedusertPrÅr()) && Objects.equals(this.getDagsats(),
            other.getDagsats());
    }

    @Override
    public int hashCode() {
        return Objects.hash(periode, bruttoPrÅr, avkortetPrÅr, redusertPrÅr, dagsats);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "<" + "id=" + id + ", " //$NON-NLS-2$
            + "periode=" + periode + ", " // $NON-NLS-1$ //$NON-NLS-2$
            + "bruttoPrÅr=" + bruttoPrÅr + ", " //$NON-NLS-2$
            + "avkortetPrÅr=" + avkortetPrÅr + ", " //$NON-NLS-2$
            + "redusertPrÅr=" + redusertPrÅr + ", " //$NON-NLS-2$
            + "dagsats=" + dagsats + ", " //$NON-NLS-2$
            + ">";
    }

    private long finnNesteAndelsnr() {
        var forrigeAndelsnr = beregningsgrunnlagAndelList.stream().mapToLong(BeregningsgrunnlagAndelEntitet::getAndelsnr).max().orElse(0L);
        return forrigeAndelsnr + 1L;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Builder builder(BeregningsgrunnlagPeriodeEntitet eksisterendeBeregningsgrunnlagPeriode) {
        return new Builder(eksisterendeBeregningsgrunnlagPeriode);
    }


    public static class Builder {
        private final BeregningsgrunnlagPeriodeEntitet kladd;
        private boolean built;

        public Builder() {
            kladd = new BeregningsgrunnlagPeriodeEntitet();
        }

        public Builder(BeregningsgrunnlagPeriodeEntitet eksisterendeBeregningsgrunnlagPeriod) {
            if (Objects.nonNull(eksisterendeBeregningsgrunnlagPeriod.getId())) {
                throw new IllegalArgumentException("Kan ikke bygge på et lagret grunnlag");
            }
            kladd = eksisterendeBeregningsgrunnlagPeriod;
        }

        public Builder leggTilBeregningsgrunnlagAndel(BeregningsgrunnlagAndelEntitet.Builder andelBuilder) {
            verifiserKanModifisere();
            var andelsnr = kladd.finnNesteAndelsnr();
            var andel = andelBuilder.medAndelsnr(andelsnr).build();
            andel.setBeregningsgrunnlagPeriode(kladd);
            kladd.addBeregningsgrunnlagAndel(andel);
            kladd.updateBruttoPrÅr();
            return this;
        }

        public Builder leggTilBeregningsgrunnlagAndel(BeregningsgrunnlagAndelEntitet andel) {
            verifiserKanModifisere();
            andel.setBeregningsgrunnlagPeriode(kladd);
            kladd.addBeregningsgrunnlagAndel(andel);
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

        public Builder medInntektGraderingsprosentBrutto(Prosent inntektgraderingsprosent) {
            verifiserKanModifisere();
            if (inntektgraderingsprosent != null && inntektgraderingsprosent.getVerdi().compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalStateException("Graderingsprosent må vere større enn eller lik 0. Var " + inntektgraderingsprosent);
            }
            if (inntektgraderingsprosent != null && inntektgraderingsprosent.getVerdi().compareTo(BigDecimal.valueOf(100)) > 0) {
                throw new IllegalStateException("Graderingsprosent må vere mindre enn eller lik 100. Var " + inntektgraderingsprosent);
            }
            kladd.inntektgraderingsprosentBrutto = inntektgraderingsprosent;
            return this;
        }

        public Builder medTotalUtbetalingsgradFraUttak(BigDecimal totalUtbetalingsgradFraUttak) {
            verifiserKanModifisere();
            if (totalUtbetalingsgradFraUttak != null) {
                if (totalUtbetalingsgradFraUttak.compareTo(BigDecimal.ZERO) < 0) {
                    throw new IllegalStateException(
                        "totalUtbetalingsgradFraUttak må vere større enn eller lik 0. Var " + totalUtbetalingsgradFraUttak);
                }
                if (totalUtbetalingsgradFraUttak.compareTo(BigDecimal.valueOf(1)) > 0) {
                    throw new IllegalStateException(
                        "totalUtbetalingsgradFraUttak må vere mindre enn eller lik 1. Var " + totalUtbetalingsgradFraUttak);
                }
            }
            kladd.totalUtbetalingsgradFraUttak = totalUtbetalingsgradFraUttak;
            return this;
        }

        public Builder leggTilPeriodeÅrsak(PeriodeÅrsak periodeÅrsak) {
            verifiserKanModifisere();
            if (!kladd.getPeriodeÅrsaker().contains(periodeÅrsak)) {
                PeriodeÅrsakEntitet.Builder årsakBuilder = new PeriodeÅrsakEntitet.Builder();
                årsakBuilder.medPeriodeÅrsak(periodeÅrsak);
                kladd.addPeriodeÅrsak(årsakBuilder.build());
            }
            return this;
        }

        public BeregningsgrunnlagPeriodeEntitet build() {
            verifyStateForBuild();
            Long dagsatsSum = kladd.beregningsgrunnlagAndelList.stream()
                .map(BeregningsgrunnlagAndelEntitet::getDagsats)
                .filter(Objects::nonNull)
                .reduce(Long::sum)
                .orElse(null);
            kladd.dagsats = dagsatsSum;
            built = true;
            return kladd;
        }

        private void verifiserKanModifisere() {
            if (built) {
                throw new IllegalStateException("Er allerede bygd, kan ikke oppdatere videre: " + this.kladd);
            }
        }

        private void verifyStateForBuild() {
            verifiserAndelsnr();
            Objects.requireNonNull(kladd.beregningsgrunnlagAndelList, "beregningsgrunnlagAndelList");
            Objects.requireNonNull(kladd.periode, "beregningsgrunnlagPeriodeFom");
        }

        private void verifiserAndelsnr() {
            Set<Long> andelsnrIBruk = new HashSet<>();
            kladd.beregningsgrunnlagAndelList.stream().map(BeregningsgrunnlagAndelEntitet::getAndelsnr).forEach(andelsnr -> {
                if (andelsnrIBruk.contains(andelsnr)) {
                    throw new IllegalStateException(
                        "Utviklerfeil: Kan ikke bygge andel. Andelsnr eksisterer allerede på en annen andel i samme periode.");
                }
                andelsnrIBruk.add(andelsnr);
            });
        }


    }
}
