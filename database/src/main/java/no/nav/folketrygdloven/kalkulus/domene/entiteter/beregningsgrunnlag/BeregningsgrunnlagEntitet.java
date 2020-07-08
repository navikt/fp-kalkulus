package no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag;

import static no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.BeregningsgrunnlagRegelType.BRUKERS_STATUS;
import static no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.BeregningsgrunnlagRegelType.PERIODISERING;
import static no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.BeregningsgrunnlagRegelType.SKJÆRINGSTIDSPUNKT;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
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
import javax.persistence.MapKey;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Version;

import org.hibernate.annotations.BatchSize;

import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.Beløp;
import no.nav.folketrygdloven.kalkulus.felles.diff.ChangeTracked;
import no.nav.folketrygdloven.kalkulus.felles.jpa.BaseEntitet;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.BeregningsgrunnlagRegelType;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.FaktaOmBeregningTilfelle;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.Hjemmel;

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
    private List<BeregningsgrunnlagAktivitetStatus> aktivitetStatuser = new ArrayList<>();

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "beregningsgrunnlag", cascade = CascadeType.PERSIST)
    @BatchSize(size=20)
    private List<BeregningsgrunnlagPeriode> beregningsgrunnlagPerioder = new ArrayList<>();

    @OneToOne(mappedBy = "beregningsgrunnlag", cascade = CascadeType.PERSIST)
    private Sammenligningsgrunnlag sammenligningsgrunnlag;

    @OneToMany(mappedBy = "beregningsgrunnlag")
    @BatchSize(size=20)
    private List<SammenligningsgrunnlagPrStatus> sammenligningsgrunnlagPrStatusListe = new ArrayList<>();

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "beregningsgrunnlag", cascade = CascadeType.PERSIST, orphanRemoval = true)
    @MapKey(name = "regelType")
    private Map<BeregningsgrunnlagRegelType, BeregningsgrunnlagRegelSporing> regelSporingMap = new HashMap<>();

    @Embedded
    @AttributeOverrides(@AttributeOverride(name = "verdi", column = @Column(name = "grunnbeloep")))
    @ChangeTracked
    private Beløp grunnbeløp;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "beregningsgrunnlag", cascade = CascadeType.PERSIST)
    @BatchSize(size=20)
    private List<BeregningsgrunnlagFaktaOmBeregningTilfelle> faktaOmBeregningTilfeller = new ArrayList<>();

    @Column(name = "overstyrt", nullable = false)
    private boolean overstyrt = false;

    public Long getId() {
        return id;
    }

    public LocalDate getSkjæringstidspunkt() {
        return skjæringstidspunkt;
    }

    public List<BeregningsgrunnlagAktivitetStatus> getAktivitetStatuser() {
        return aktivitetStatuser.stream()
                .sorted(Comparator.comparing(BeregningsgrunnlagAktivitetStatus::getAktivitetStatus))
                .collect(Collectors.toUnmodifiableList());
    }

    public List<BeregningsgrunnlagPeriode> getBeregningsgrunnlagPerioder() {
        return beregningsgrunnlagPerioder
                .stream()
                .sorted(Comparator.comparing(BeregningsgrunnlagPeriode::getBeregningsgrunnlagPeriodeFom))
                .collect(Collectors.toUnmodifiableList());
    }

    public Sammenligningsgrunnlag getSammenligningsgrunnlag() {
        return sammenligningsgrunnlag;
    }

    public Beløp getGrunnbeløp() {
        return grunnbeløp;
    }

    public void leggTilBeregningsgrunnlagAktivitetStatus(BeregningsgrunnlagAktivitetStatus bgAktivitetStatus) {
        Objects.requireNonNull(bgAktivitetStatus, "beregningsgrunnlagAktivitetStatus");
        aktivitetStatuser.remove(bgAktivitetStatus); // NOSONAR
        aktivitetStatuser.add(bgAktivitetStatus);
    }

    public void leggTilBeregningsgrunnlagPeriode(BeregningsgrunnlagPeriode bgPeriode) {
        Objects.requireNonNull(bgPeriode, "beregningsgrunnlagPeriode");
        if (!beregningsgrunnlagPerioder.contains(bgPeriode)) { // NOSONAR
            beregningsgrunnlagPerioder.add(bgPeriode);
        }
    }

    public Map<BeregningsgrunnlagRegelType, BeregningsgrunnlagRegelSporing> getRegelSporingMap() {
        return regelSporingMap;
    }

    public BeregningsgrunnlagRegelSporing getRegelsporing(BeregningsgrunnlagRegelType regelType) {
        return regelSporingMap.getOrDefault(regelType, null);
    }

    public String getRegelinputPeriodisering() {
        return regelSporingMap.containsKey(PERIODISERING) ? regelSporingMap.get(PERIODISERING).getRegelInput() : null;
    }

    public String getRegelInputSkjæringstidspunkt() {
        return regelSporingMap.containsKey(SKJÆRINGSTIDSPUNKT) ? regelSporingMap.get(SKJÆRINGSTIDSPUNKT).getRegelInput() : null;
    }

    public String getRegelloggSkjæringstidspunkt() {
        return regelSporingMap.containsKey(SKJÆRINGSTIDSPUNKT) ? regelSporingMap.get(SKJÆRINGSTIDSPUNKT).getRegelEvaluering() : null;
    }

    public String getRegelInputBrukersStatus() {
        return regelSporingMap.containsKey(SKJÆRINGSTIDSPUNKT) ? regelSporingMap.get(SKJÆRINGSTIDSPUNKT).getRegelInput() : null;
    }

    public String getRegelloggBrukersStatus() {
        return regelSporingMap.containsKey(BRUKERS_STATUS) ? regelSporingMap.get(BRUKERS_STATUS).getRegelEvaluering() : null;
    }

    public Hjemmel getHjemmel() {
        if (aktivitetStatuser.isEmpty()) {
            return Hjemmel.UDEFINERT;
        }
        if (aktivitetStatuser.size() == 1) {
            return aktivitetStatuser.get(0).getHjemmel();
        }
        Optional<BeregningsgrunnlagAktivitetStatus> dagpenger = aktivitetStatuser.stream()
                .filter(as -> Hjemmel.F_14_7_8_49.equals(as.getHjemmel()))
                .findFirst();
        if (dagpenger.isPresent()) {
            return dagpenger.get().getHjemmel();
        }
        Optional<BeregningsgrunnlagAktivitetStatus> gjelder = aktivitetStatuser.stream()
                .filter(as -> !Hjemmel.F_14_7.equals(as.getHjemmel()))
                .findFirst();
        return gjelder.isPresent() ? gjelder.get().getHjemmel() : Hjemmel.F_14_7;
    }

    public List<FaktaOmBeregningTilfelle> getFaktaOmBeregningTilfeller() {
        return faktaOmBeregningTilfeller
                .stream()
                .sorted(Comparator.comparing(BeregningsgrunnlagFaktaOmBeregningTilfelle::getFaktaOmBeregningTilfelle))
                .map(BeregningsgrunnlagFaktaOmBeregningTilfelle::getFaktaOmBeregningTilfelle)
                .collect(Collectors.toUnmodifiableList());
    }

    public List<SammenligningsgrunnlagPrStatus> getSammenligningsgrunnlagPrStatusListe() {
        return sammenligningsgrunnlagPrStatusListe.stream()
                .sorted(Comparator.comparing(SammenligningsgrunnlagPrStatus::getSammenligningsgrunnlagType))
                .collect(Collectors.toUnmodifiableList());
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

    public static Builder builder(BeregningsgrunnlagEntitet original) {
        return new Builder(original);
    }

    void leggTilBeregningsgrunnlagRegel(BeregningsgrunnlagRegelSporing beregningsgrunnlagRegelSporing) {
        Objects.requireNonNull(beregningsgrunnlagRegelSporing, "beregningsgrunnlagRegelSporing");
        regelSporingMap.put(beregningsgrunnlagRegelSporing.getRegelType(), beregningsgrunnlagRegelSporing);
    }

    public static class Builder {
        private boolean built;
        private BeregningsgrunnlagEntitet kladd;

        private Builder() {
            kladd = new BeregningsgrunnlagEntitet();
        }

        private Builder(BeregningsgrunnlagEntitet original) {
            kladd = original;
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

        public Builder fjernAllePerioder() {
            verifiserKanModifisere();
            kladd.beregningsgrunnlagPerioder = new ArrayList<>();
            return this;
        }

        public Builder fjernAktivitetstatus(AktivitetStatus status) {
            verifiserKanModifisere();
            List<BeregningsgrunnlagAktivitetStatus> statuserSomSkalFjernes = kladd.aktivitetStatuser.stream().filter(a -> Objects.equals(a.getAktivitetStatus(), status)).collect(Collectors.toList());
            if (statuserSomSkalFjernes.size() != 1) {
                throw new IllegalStateException("Ikke entydig hvilken status som skal fjernes fra beregningsgrunnlaget.");
            }
            kladd.aktivitetStatuser.remove(statuserSomSkalFjernes.get(0));
            return this;
        }

        public Builder leggTilFaktaOmBeregningTilfeller(List<FaktaOmBeregningTilfelle> faktaOmBeregningTilfeller) {
            verifiserKanModifisere();
            faktaOmBeregningTilfeller.forEach(this::leggTilFaktaOmBeregningTilfeller);
            return this;
        }

        private void leggTilFaktaOmBeregningTilfeller(FaktaOmBeregningTilfelle tilfelle) {
            verifiserKanModifisere();
            BeregningsgrunnlagFaktaOmBeregningTilfelle b = BeregningsgrunnlagFaktaOmBeregningTilfelle.builder().medFaktaOmBeregningTilfelle(tilfelle).build(kladd);
            this.kladd.faktaOmBeregningTilfeller.add(b);
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

        public Builder leggTilSammenligningsgrunnlag(SammenligningsgrunnlagPrStatus.Builder sammenligningsgrunnlagPrStatusBuilder) { // NOSONAR
            kladd.sammenligningsgrunnlagPrStatusListe.add(sammenligningsgrunnlagPrStatusBuilder.medBeregningsgrunnlag(kladd).build());
            return this;
        }

        public Builder medRegelloggSkjæringstidspunkt(String regelInput, String regelEvaluering) {
            verifiserKanModifisere();
            BeregningsgrunnlagRegelSporing.ny()
                .medRegelInput(regelInput)
                .medRegelEvaluering(regelEvaluering)
                .medRegelType(SKJÆRINGSTIDSPUNKT)
                .build(kladd);
            return this;
        }

        public Builder medRegelloggBrukersStatus(String regelInput, String regelEvaluering) {
            verifiserKanModifisere();
            BeregningsgrunnlagRegelSporing.ny()
                .medRegelInput(regelInput)
                .medRegelEvaluering(regelEvaluering)
                .medRegelType(BRUKERS_STATUS)
                .build(kladd);
            return this;
        }

        public Builder medRegelinputPeriodisering(String regelInput) {
            verifiserKanModifisere();
            if (regelInput != null) {
                BeregningsgrunnlagRegelSporing.ny()
                    .medRegelInput(regelInput)
                    .medRegelType(PERIODISERING)
                    .build(kladd);
            }
            return this;
        }

        public Builder medRegellogg(String regelInput, String regelEvaluering, BeregningsgrunnlagRegelType regelType) {
            verifiserKanModifisere();
            if (regelInput != null) {
                BeregningsgrunnlagRegelSporing.ny()
                        .medRegelInput(regelInput)
                        .medRegelEvaluering(regelEvaluering)
                        .medRegelType(regelType)
                        .build(kladd);
            }
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
