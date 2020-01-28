package no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.kodeverk.FaktaOmBeregningTilfelle;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.kodeverk.Hjemmel;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;

public class BeregningsgrunnlagRestDto {

    private LocalDate skjæringstidspunkt;
    private List<BeregningsgrunnlagAktivitetStatusRestDto> aktivitetStatuser = new ArrayList<>();
    private List<BeregningsgrunnlagPeriodeRestDto> beregningsgrunnlagPerioder = new ArrayList<>();
    private SammenligningsgrunnlagRestDto sammenligningsgrunnlag;
    private List<SammenligningsgrunnlagPrStatusRestDto> sammenligningsgrunnlagPrStatusListe = new ArrayList<>();
    private Beløp grunnbeløp;
    private List<BeregningsgrunnlagFaktaOmBeregningTilfelleRestDto> faktaOmBeregningTilfeller = new ArrayList<>();
    private boolean overstyrt = false;

    public BeregningsgrunnlagRestDto() {
    }

    public BeregningsgrunnlagRestDto(BeregningsgrunnlagRestDto kopiereFra) {

        this.skjæringstidspunkt = kopiereFra.skjæringstidspunkt;

        this.sammenligningsgrunnlagPrStatusListe = kopiereFra.getSammenligningsgrunnlagPrStatusListe().stream().map(s -> {
            SammenligningsgrunnlagPrStatusRestDto.Builder builder = SammenligningsgrunnlagPrStatusRestDto.Builder.kopier(s);
            SammenligningsgrunnlagPrStatusRestDto build = builder.build();
            build.setBeregningsgrunnlagDto(this);
            return build;
        }).collect(Collectors.toList());

        if (kopiereFra.getSammenligningsgrunnlag() != null) {
            SammenligningsgrunnlagRestDto sammenligningsgrunnlagDto = new SammenligningsgrunnlagRestDto(kopiereFra.getSammenligningsgrunnlag());
            sammenligningsgrunnlagDto.setBeregningsgrunnlag(this);
            this.sammenligningsgrunnlag = sammenligningsgrunnlagDto;
        }

        this.beregningsgrunnlagPerioder = kopiereFra.getBeregningsgrunnlagPerioder().stream().map(p -> {
            BeregningsgrunnlagPeriodeRestDto.Builder builder = BeregningsgrunnlagPeriodeRestDto.Builder.kopier(p);
            return builder.build(this);
        }).collect(Collectors.toList());

        this.faktaOmBeregningTilfeller = kopiereFra.getFaktaOmBeregningTilfeller().stream().map(p -> {
            BeregningsgrunnlagFaktaOmBeregningTilfelleRestDto.Builder builder = BeregningsgrunnlagFaktaOmBeregningTilfelleRestDto.Builder.kopier(p);
            return builder.build(this);
        }).collect(Collectors.toList());

        this.aktivitetStatuser = kopiereFra.getAktivitetStatuser().stream().map(p -> {
            BeregningsgrunnlagAktivitetStatusRestDto.Builder builder = BeregningsgrunnlagAktivitetStatusRestDto.Builder.kopier(p);
            return builder.build(this);
        }).collect(Collectors.toList());

        this.grunnbeløp = kopiereFra.getGrunnbeløp();

        this.overstyrt = kopiereFra.overstyrt;
    }


    public LocalDate getSkjæringstidspunkt() {
        return skjæringstidspunkt;
    }

    public List<BeregningsgrunnlagAktivitetStatusRestDto> getAktivitetStatuser() {
        return Collections.unmodifiableList(aktivitetStatuser);
    }

    public List<BeregningsgrunnlagPeriodeRestDto> getBeregningsgrunnlagPerioder() {
        return beregningsgrunnlagPerioder
                .stream()
                .sorted(Comparator.comparing(BeregningsgrunnlagPeriodeRestDto::getBeregningsgrunnlagPeriodeFom))
                .collect(Collectors.toUnmodifiableList());
    }

    public SammenligningsgrunnlagRestDto getSammenligningsgrunnlag() {
        return sammenligningsgrunnlag;
    }

    public Beløp getGrunnbeløp() {
        return grunnbeløp;
    }

    public void leggTilBeregningsgrunnlagAktivitetStatus(BeregningsgrunnlagAktivitetStatusRestDto bgAktivitetStatus) {
        Objects.requireNonNull(bgAktivitetStatus, "beregningsgrunnlagAktivitetStatus");
        aktivitetStatuser.remove(bgAktivitetStatus); // NOSONAR
        aktivitetStatuser.add(bgAktivitetStatus);
    }

    public void leggTilBeregningsgrunnlagPeriode(BeregningsgrunnlagPeriodeRestDto bgPeriode) {
        Objects.requireNonNull(bgPeriode, "beregningsgrunnlagPeriode");
        if (!beregningsgrunnlagPerioder.contains(bgPeriode)) { // NOSONAR
            beregningsgrunnlagPerioder.add(bgPeriode);
        }
    }

    public Hjemmel getHjemmel() {
        if (aktivitetStatuser.isEmpty()) {
            return Hjemmel.UDEFINERT;
        }
        if (aktivitetStatuser.size() == 1) {
            return aktivitetStatuser.get(0).getHjemmel();
        }
        Optional<BeregningsgrunnlagAktivitetStatusRestDto> dagpenger = aktivitetStatuser.stream()
                .filter(as -> Hjemmel.F_14_7_8_49.equals(as.getHjemmel()))
                .findFirst();
        if (dagpenger.isPresent()) {
            return dagpenger.get().getHjemmel();
        }
        Optional<BeregningsgrunnlagAktivitetStatusRestDto> gjelder = aktivitetStatuser.stream()
                .filter(as -> !Hjemmel.F_14_7.equals(as.getHjemmel()))
                .findFirst();
        return gjelder.isPresent() ? gjelder.get().getHjemmel() : Hjemmel.F_14_7;
    }

    public List<FaktaOmBeregningTilfelle> getFaktaOmBeregningTilfeller() {
        return faktaOmBeregningTilfeller
                .stream()
                .map(BeregningsgrunnlagFaktaOmBeregningTilfelleRestDto::getFaktaOmBeregningTilfelle)
                .collect(Collectors.toUnmodifiableList());
    }

    public List<SammenligningsgrunnlagPrStatusRestDto> getSammenligningsgrunnlagPrStatusListe() {
        return sammenligningsgrunnlagPrStatusListe;
    }

    public boolean isOverstyrt() {
        return overstyrt;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (!(obj instanceof BeregningsgrunnlagRestDto)) {
            return false;
        }
        BeregningsgrunnlagRestDto other = (BeregningsgrunnlagRestDto) obj;
        return Objects.equals(this.getSkjæringstidspunkt(), other.getSkjæringstidspunkt());
    }

    @Override
    public int hashCode() {
        return Objects.hash(skjæringstidspunkt);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "<" + //$NON-NLS-1$
                "skjæringstidspunkt=" + skjæringstidspunkt + ", " //$NON-NLS-1$ //$NON-NLS-2$
                + "grunnbeløp=" + grunnbeløp + ", " //$NON-NLS-1$ //$NON-NLS-2$
                + ">"; //$NON-NLS-1$
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Builder builder(BeregningsgrunnlagRestDto original) {
        return new Builder(original);
    }

    public static class Builder {
        private boolean built;
        private BeregningsgrunnlagRestDto kladd;
        private boolean erOppdatering;

        private Builder() {
            kladd = new BeregningsgrunnlagRestDto();
        }

        private Builder(BeregningsgrunnlagRestDto original) {
            kladd = new BeregningsgrunnlagRestDto(original);
        }

        private Builder(BeregningsgrunnlagRestDto original, boolean erOppdatering) {
            kladd = original;
            this.erOppdatering = erOppdatering;
        }

        public static Builder oppdater(Optional<BeregningsgrunnlagRestDto> beregningsgrunnlag) {
            return beregningsgrunnlag.map(beregningsgrunnlagDto -> new Builder(beregningsgrunnlagDto, true)).orElse(new Builder());
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

        public Builder leggTilAktivitetStatus(BeregningsgrunnlagAktivitetStatusRestDto.Builder aktivitetStatusBuilder) {
            verifiserKanModifisere();
            aktivitetStatusBuilder.build(kladd);
            return this;
        }

        public Builder leggTilBeregningsgrunnlagPeriode(BeregningsgrunnlagPeriodeRestDto.Builder beregningsgrunnlagPeriodeBuilder) {
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
            List<BeregningsgrunnlagAktivitetStatusRestDto> statuserSomSkalFjernes = kladd.aktivitetStatuser.stream().filter(a -> Objects.equals(a.getAktivitetStatus(), status)).collect(Collectors.toList());
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
            BeregningsgrunnlagFaktaOmBeregningTilfelleRestDto b = BeregningsgrunnlagFaktaOmBeregningTilfelleRestDto.builder().medFaktaOmBeregningTilfelle(tilfelle).build(kladd);
            this.kladd.faktaOmBeregningTilfeller.add(b);
        }

        public Builder medSammenligningsgrunnlag(SammenligningsgrunnlagRestDto sammenligningsgrunnlag) {
            verifiserKanModifisere();
            kladd.sammenligningsgrunnlag = sammenligningsgrunnlag;
            return this;
        }

        public Builder leggTilSammenligningsgrunnlag(SammenligningsgrunnlagPrStatusRestDto.Builder sammenligningsgrunnlagPrStatusBuilder) { // NOSONAR
            kladd.sammenligningsgrunnlagPrStatusListe.add(sammenligningsgrunnlagPrStatusBuilder.medBeregningsgrunnlag(kladd).build());
            return this;
        }

        public Builder medOverstyring(boolean overstyrt) {
            verifiserKanModifisere();
            kladd.overstyrt = overstyrt;
            return this;
        }

        public BeregningsgrunnlagRestDto getBeregningsgrunnlag() {
            return kladd;
        }

        public BeregningsgrunnlagRestDto build() {
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

        public Builder medSammenligningsgrunnlag(SammenligningsgrunnlagRestDto.Builder builder) {
            SammenligningsgrunnlagRestDto sammenligningsgrunnlagDto = builder.build(kladd);
            kladd.sammenligningsgrunnlag = sammenligningsgrunnlagDto;
            return this;
        }
    }
}
