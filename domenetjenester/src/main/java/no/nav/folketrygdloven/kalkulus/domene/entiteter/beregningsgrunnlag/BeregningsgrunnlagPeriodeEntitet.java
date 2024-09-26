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
import java.util.stream.Collectors;

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
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_BG_PERIODE")
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
    private final List<BeregningsgrunnlagPrStatusOgAndelEntitet> beregningsgrunnlagPrStatusOgAndelList = new ArrayList<>();

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

    @Embedded
    @AttributeOverrides(@AttributeOverride(name = "verdi", column = @Column(name = "inntekt_graderingsprosent_brutto")))
    private Prosent inntektgraderingsprosentBrutto;

    @Column(name = "total_utbetalingsgrad_fra_uttak")
    private BigDecimal totalUtbetalingsgradFraUttak;

    @Column(name = "total_utbetalingsgrad_etter_reduksjon_ved_tilkommet_inntekt")
    private BigDecimal totalUtbetalingsgradEtterReduksjonVedTilkommetInntekt;

    @Column(name = "reduksjonsfaktor_inaktiv_type_a")
    private BigDecimal reduksjonsfaktorInaktivTypeA;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "beregningsgrunnlagPeriode", cascade = CascadeType.PERSIST, orphanRemoval = true)
    @BatchSize(size = 20)
    private final List<BeregningsgrunnlagPeriodeÅrsakEntitet> beregningsgrunnlagPeriodeÅrsaker = new ArrayList<>();

    public BeregningsgrunnlagPeriodeEntitet(BeregningsgrunnlagPeriodeEntitet beregningsgrunnlagPeriode) {
        this.avkortetPrÅr = beregningsgrunnlagPeriode.getAvkortetPrÅr();
        this.bruttoPrÅr = beregningsgrunnlagPeriode.getBruttoPrÅr();
        this.dagsats = beregningsgrunnlagPeriode.getDagsats();
        this.periode = beregningsgrunnlagPeriode.getPeriode();
        this.redusertPrÅr = beregningsgrunnlagPeriode.getRedusertPrÅr();
        this.inntektgraderingsprosentBrutto = beregningsgrunnlagPeriode.getInntektgraderingsprosentBrutto();
        this.totalUtbetalingsgradFraUttak = beregningsgrunnlagPeriode.getTotalUtbetalingsgradFraUttak();
        this.totalUtbetalingsgradEtterReduksjonVedTilkommetInntekt = beregningsgrunnlagPeriode.getTotalUtbetalingsgradEtterReduksjonVedTilkommetInntekt();
        this.reduksjonsfaktorInaktivTypeA = beregningsgrunnlagPeriode.getReduksjonsfaktorInaktivTypeA();
        beregningsgrunnlagPeriode.getBeregningsgrunnlagPeriodeÅrsaker().stream().map(BeregningsgrunnlagPeriodeÅrsakEntitet::new)
                .forEach(this::addBeregningsgrunnlagPeriodeÅrsak);
        beregningsgrunnlagPeriode.getBeregningsgrunnlagPrStatusOgAndelList().stream().map(BeregningsgrunnlagPrStatusOgAndelEntitet::new)
                .forEach(this::addBeregningsgrunnlagPrStatusOgAndel);
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

    public List<BeregningsgrunnlagPrStatusOgAndelEntitet> getBeregningsgrunnlagPrStatusOgAndelList() {
        return beregningsgrunnlagPrStatusOgAndelList.stream()
                .sorted(Comparator.comparing(BeregningsgrunnlagPrStatusOgAndelEntitet::getAndelsnr))
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
                .map(BeregningsgrunnlagPrStatusOgAndelEntitet::getBruttoPrÅr)
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

    public BigDecimal getTotalUtbetalingsgradEtterReduksjonVedTilkommetInntekt() {
        return totalUtbetalingsgradEtterReduksjonVedTilkommetInntekt;
    }

    public BigDecimal getReduksjonsfaktorInaktivTypeA() {
        return reduksjonsfaktorInaktivTypeA;
    }

    public List<BeregningsgrunnlagPeriodeÅrsakEntitet> getBeregningsgrunnlagPeriodeÅrsaker() {
        return beregningsgrunnlagPeriodeÅrsaker.stream()
                .sorted(Comparator.comparing(BeregningsgrunnlagPeriodeÅrsakEntitet::getPeriodeÅrsak))
                .collect(Collectors.toUnmodifiableList());
    }

    public List<PeriodeÅrsak> getPeriodeÅrsaker() {
        return getBeregningsgrunnlagPeriodeÅrsaker().stream()
                .map(BeregningsgrunnlagPeriodeÅrsakEntitet::getPeriodeÅrsak)
                .collect(Collectors.toUnmodifiableList());
    }

    void addBeregningsgrunnlagPrStatusOgAndel(BeregningsgrunnlagPrStatusOgAndelEntitet bgPrStatusOgAndel) {
        Objects.requireNonNull(bgPrStatusOgAndel, "beregningsgrunnlagPrStatusOgAndel");
        if (!beregningsgrunnlagPrStatusOgAndelList.contains(bgPrStatusOgAndel)) { // NOSONAR Class defines List based fields but uses them like Sets: Ingening å tjene på å bytte til Set ettersom det er små lister
            bgPrStatusOgAndel.setBeregningsgrunnlagPeriode(this);
            beregningsgrunnlagPrStatusOgAndelList.add(bgPrStatusOgAndel);
        }
    }

    void addBeregningsgrunnlagPeriodeÅrsak(BeregningsgrunnlagPeriodeÅrsakEntitet bgPeriodeÅrsak) {
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
        } else if (!(obj instanceof BeregningsgrunnlagPeriodeEntitet)) {
            return false;
        }
        BeregningsgrunnlagPeriodeEntitet other = (BeregningsgrunnlagPeriodeEntitet) obj;
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
        return getClass().getSimpleName() + "<" +
                "id=" + id + ", " //$NON-NLS-2$
                + "periode=" + periode + ", " // $NON-NLS-1$ //$NON-NLS-2$
                + "bruttoPrÅr=" + bruttoPrÅr + ", " //$NON-NLS-2$
                + "avkortetPrÅr=" + avkortetPrÅr + ", " //$NON-NLS-2$
                + "redusertPrÅr=" + redusertPrÅr + ", " //$NON-NLS-2$
                + "dagsats=" + dagsats + ", " //$NON-NLS-2$
                + ">";
    }

    private long finnNesteAndelsnr() {
        var forrigeAndelsnr = beregningsgrunnlagPrStatusOgAndelList.stream().mapToLong(BeregningsgrunnlagPrStatusOgAndelEntitet::getAndelsnr).max().orElse(0L);
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

        public Builder leggTilBeregningsgrunnlagPrStatusOgAndel(BeregningsgrunnlagPrStatusOgAndelEntitet.Builder prStatusOgAndelBuilder) {
            verifiserKanModifisere();
            var andelsnr = kladd.finnNesteAndelsnr();
            var andel = prStatusOgAndelBuilder.medAndelsnr(andelsnr).build();
            andel.setBeregningsgrunnlagPeriode(kladd);
            kladd.addBeregningsgrunnlagPrStatusOgAndel(andel);
            kladd.updateBruttoPrÅr();
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
                    throw new IllegalStateException("totalUtbetalingsgradFraUttak må vere større enn eller lik 0. Var " + totalUtbetalingsgradFraUttak);
                }
                if (totalUtbetalingsgradFraUttak.compareTo(BigDecimal.valueOf(1)) > 0) {
                    throw new IllegalStateException("totalUtbetalingsgradFraUttak må vere mindre enn eller lik 1. Var " + totalUtbetalingsgradFraUttak);
                }
            }
            kladd.totalUtbetalingsgradFraUttak = totalUtbetalingsgradFraUttak;
            return this;
        }

        public Builder medTotalUtbetalingsgradEtterReduksjonVedTilkommetInntekt(BigDecimal totalUtbetalingsgradEtterReduksjonVedTilkommetInntekt) {
            verifiserKanModifisere();

            if (totalUtbetalingsgradEtterReduksjonVedTilkommetInntekt != null) {
                if (totalUtbetalingsgradEtterReduksjonVedTilkommetInntekt.compareTo(BigDecimal.ZERO) < 0) {
                    throw new IllegalStateException("totalUtbetalingsgradEtterReduksjonVedTilkommetInntekt må vere større enn eller lik 0. Var " + totalUtbetalingsgradEtterReduksjonVedTilkommetInntekt);
                }
                if (totalUtbetalingsgradEtterReduksjonVedTilkommetInntekt.compareTo(BigDecimal.valueOf(1)) > 0) {
                    throw new IllegalStateException("totalUtbetalingsgradEtterReduksjonVedTilkommetInntekt må vere mindre enn eller lik 1. Var " + totalUtbetalingsgradEtterReduksjonVedTilkommetInntekt);
                }
            }
            kladd.totalUtbetalingsgradEtterReduksjonVedTilkommetInntekt = totalUtbetalingsgradEtterReduksjonVedTilkommetInntekt;
            return this;
        }

        public Builder medReduksjonsfaktorInaktivTypeA(BigDecimal reduksjonsfaktorInaktivTypeA){
            verifiserKanModifisere();
            kladd.reduksjonsfaktorInaktivTypeA = reduksjonsfaktorInaktivTypeA;
            return this;
        }

        public Builder leggTilPeriodeÅrsak(PeriodeÅrsak periodeÅrsak) {
            verifiserKanModifisere();
            if (!kladd.getPeriodeÅrsaker().contains(periodeÅrsak)) {
                BeregningsgrunnlagPeriodeÅrsakEntitet.Builder bgPeriodeÅrsakBuilder = new BeregningsgrunnlagPeriodeÅrsakEntitet.Builder();
                bgPeriodeÅrsakBuilder.medPeriodeÅrsak(periodeÅrsak);
                kladd.addBeregningsgrunnlagPeriodeÅrsak(bgPeriodeÅrsakBuilder.build());
            }
            return this;
        }

        public BeregningsgrunnlagPeriodeEntitet build() {
            verifyStateForBuild();
            Long dagsatsSum = kladd.beregningsgrunnlagPrStatusOgAndelList.stream()
                    .map(BeregningsgrunnlagPrStatusOgAndelEntitet::getDagsats)
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
            Objects.requireNonNull(kladd.beregningsgrunnlagPrStatusOgAndelList, "beregningsgrunnlagPrStatusOgAndelList");
            Objects.requireNonNull(kladd.periode, "beregningsgrunnlagPeriodeFom");
        }

        private void verifiserAndelsnr() {
            Set<Long> andelsnrIBruk = new HashSet<>();
            kladd.beregningsgrunnlagPrStatusOgAndelList.stream()
                .map(BeregningsgrunnlagPrStatusOgAndelEntitet::getAndelsnr)
                .forEach(andelsnr -> {
                    if (andelsnrIBruk.contains(andelsnr)) {
                        throw new IllegalStateException("Utviklerfeil: Kan ikke bygge andel. Andelsnr eksisterer allerede på en annen andel i samme bgPeriode.");
                    }
                    andelsnrIBruk.add(andelsnr);
                });
        }


    }
}
