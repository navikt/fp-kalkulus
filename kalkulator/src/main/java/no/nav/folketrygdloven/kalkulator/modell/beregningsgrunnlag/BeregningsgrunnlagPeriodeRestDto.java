package no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag;


import static no.nav.vedtak.konfig.Tid.TIDENES_ENDE;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.kodeverk.PeriodeÅrsak;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;

public class BeregningsgrunnlagPeriodeRestDto {

    private BeregningsgrunnlagRestDto beregningsgrunnlag;
    private List<BeregningsgrunnlagPrStatusOgAndelRestDto> beregningsgrunnlagPrStatusOgAndelList = new ArrayList<>();
    private Intervall periode;
    private BigDecimal bruttoPrÅr;
    private BigDecimal avkortetPrÅr;
    private BigDecimal redusertPrÅr;
    private Long dagsats;
    private List<BeregningsgrunnlagPeriodeÅrsakRestDto> beregningsgrunnlagPeriodeÅrsaker = new ArrayList<>();

    private BeregningsgrunnlagPeriodeRestDto() {
    }

    public BeregningsgrunnlagPeriodeRestDto(BeregningsgrunnlagPeriodeRestDto kopiereFra) {
        this.beregningsgrunnlagPrStatusOgAndelList = kopiereFra.beregningsgrunnlagPrStatusOgAndelList.stream().map(a ->
            {
                BeregningsgrunnlagPrStatusOgAndelRestDto kopi = new BeregningsgrunnlagPrStatusOgAndelRestDto(a);
                kopi.setBeregningsgrunnlagPeriode(this);
                return kopi;
            }
        ).collect(Collectors.toList());

        this.beregningsgrunnlagPeriodeÅrsaker = kopiereFra.beregningsgrunnlagPeriodeÅrsaker.stream().map(o ->
            BeregningsgrunnlagPeriodeÅrsakRestDto.Builder.kopier(o).build(this)
        ).collect(Collectors.toList());

        this.periode = kopiereFra.periode;
        this.bruttoPrÅr = kopiereFra.bruttoPrÅr;
        this.avkortetPrÅr = kopiereFra.avkortetPrÅr;
        this.redusertPrÅr = kopiereFra.redusertPrÅr;
        this.dagsats = kopiereFra.dagsats;
    }

    public BeregningsgrunnlagRestDto getBeregningsgrunnlag() {
        return beregningsgrunnlag;
    }

    public List<BeregningsgrunnlagPrStatusOgAndelRestDto> getBeregningsgrunnlagPrStatusOgAndelList() {
        return Collections.unmodifiableList(beregningsgrunnlagPrStatusOgAndelList);
    }

    public Intervall getPeriode() {
        if (periode.getTomDato() == null) {
            return Intervall.fraOgMedTilOgMed(periode.getFomDato(), TIDENES_ENDE);
        }
        return periode;
    }

    public LocalDate getBeregningsgrunnlagPeriodeFom() {
        return periode.getFomDato();
    }

    public LocalDate getBeregningsgrunnlagPeriodeTom() {
        return periode.getTomDato();
    }

    public BigDecimal getBeregnetPrÅr() {
        return beregningsgrunnlagPrStatusOgAndelList.stream()
                .filter(bgpsa -> bgpsa.getBeregnetPrÅr() != null)
                .map(BeregningsgrunnlagPrStatusOgAndelRestDto::getBeregnetPrÅr)
                .reduce(BigDecimal::add)
                .orElse(BigDecimal.ZERO);
    }

    void updateBruttoPrÅr() {
        bruttoPrÅr = beregningsgrunnlagPrStatusOgAndelList.stream()
                .filter(bgpsa -> bgpsa.getBruttoPrÅr() != null)
                .map(BeregningsgrunnlagPrStatusOgAndelRestDto::getBruttoPrÅr)
                .reduce(BigDecimal::add)
                .orElse(BigDecimal.ZERO);
    }

    public BigDecimal getBruttoPrÅr() {
        return bruttoPrÅr;
    }

    public BigDecimal getAvkortetPrÅr() {
        return avkortetPrÅr;
    }

    public BigDecimal getRedusertPrÅr() {
        return redusertPrÅr;
    }

    public Long getDagsats() {
        return dagsats;
    }

    public List<BeregningsgrunnlagPeriodeÅrsakRestDto> getBeregningsgrunnlagPeriodeÅrsaker() {
        return Collections.unmodifiableList(beregningsgrunnlagPeriodeÅrsaker);
    }

    public List<PeriodeÅrsak> getPeriodeÅrsaker() {
        return beregningsgrunnlagPeriodeÅrsaker.stream().map(BeregningsgrunnlagPeriodeÅrsakRestDto::getPeriodeÅrsak).collect(Collectors.toList());
    }

    void addBeregningsgrunnlagPrStatusOgAndel(BeregningsgrunnlagPrStatusOgAndelRestDto bgPrStatusOgAndel) {
        Objects.requireNonNull(bgPrStatusOgAndel, "beregningsgrunnlagPrStatusOgAndel");
        if (!beregningsgrunnlagPrStatusOgAndelList.contains(bgPrStatusOgAndel)) { // NOSONAR Class defines List based fields but uses them like Sets: Ingening å tjene på å bytte til Set ettersom det er små lister
            beregningsgrunnlagPrStatusOgAndelList.add(bgPrStatusOgAndel);
        }
    }

    void addBeregningsgrunnlagPeriodeÅrsak(BeregningsgrunnlagPeriodeÅrsakRestDto bgPeriodeÅrsak) {
        Objects.requireNonNull(bgPeriodeÅrsak, "beregningsgrunnlagPeriodeÅrsak");
        if (!beregningsgrunnlagPeriodeÅrsaker.contains(bgPeriodeÅrsak)) { // NOSONAR Class defines List based fields but uses them like Sets: Ingening å tjene på å bytte til Set ettersom det er små lister
            beregningsgrunnlagPeriodeÅrsaker.add(bgPeriodeÅrsak);
        }
    }

    public Beløp getTotaltRefusjonkravIPeriode() {
        return new Beløp(beregningsgrunnlagPrStatusOgAndelList.stream()
            .map(BeregningsgrunnlagPrStatusOgAndelRestDto::getBgAndelArbeidsforhold)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .map(BGAndelArbeidsforholdRestDto::getRefusjonskravPrÅr)
            .filter(Objects::nonNull)
            .reduce(BigDecimal::add)
            .orElse(BigDecimal.ZERO));
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (!(obj instanceof BeregningsgrunnlagPeriodeRestDto)) {
            return false;
        }
        BeregningsgrunnlagPeriodeRestDto other = (BeregningsgrunnlagPeriodeRestDto) obj;
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
                "periode=" + periode + ", " // $NON-NLS-1$ //$NON-NLS-2$
                + "bruttoPrÅr=" + bruttoPrÅr + ", " //$NON-NLS-1$ //$NON-NLS-2$
                + "avkortetPrÅr=" + avkortetPrÅr + ", " //$NON-NLS-1$ //$NON-NLS-2$
                + "redusertPrÅr=" + redusertPrÅr + ", " //$NON-NLS-1$ //$NON-NLS-2$
                + "dagsats=" + dagsats + ", " //$NON-NLS-1$ //$NON-NLS-2$
                + ">"; //$NON-NLS-1$
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Builder builder(BeregningsgrunnlagPeriodeRestDto eksisterendeBeregningsgrunnlagPeriode) {
        return new Builder(eksisterendeBeregningsgrunnlagPeriode);
    }

    public static Builder oppdater(BeregningsgrunnlagPeriodeRestDto eksisterendeBeregningsgrunnlagPeriode) {
        return new Builder(eksisterendeBeregningsgrunnlagPeriode, true);
    }

    public static class Builder {
        private BeregningsgrunnlagPeriodeRestDto kladd;
        private boolean built;
        private boolean oppdater;

        public static Builder kopier(BeregningsgrunnlagPeriodeRestDto p) {
            return new Builder(p);
        }

        public Builder(BeregningsgrunnlagPeriodeRestDto eksisterendeBeregningsgrunnlagPeriod, boolean oppdater) {
            this.oppdater = oppdater;
            this.kladd = eksisterendeBeregningsgrunnlagPeriod;
        }

        public Builder() {
            kladd = new BeregningsgrunnlagPeriodeRestDto();
        }

        public Builder(BeregningsgrunnlagPeriodeRestDto eksisterendeBeregningsgrunnlagPeriod) {
            this.kladd = new BeregningsgrunnlagPeriodeRestDto(eksisterendeBeregningsgrunnlagPeriod);
        }

        public Builder leggTilBeregningsgrunnlagPrStatusOgAndel(BeregningsgrunnlagPrStatusOgAndelRestDto beregningsgrunnlagPrStatusOgAndel) {
            verifiserKanModifisere();
            kladd.beregningsgrunnlagPrStatusOgAndelList.add(beregningsgrunnlagPrStatusOgAndel);
            return this;
        }

        public Builder fjernBeregningsgrunnlagPrStatusOgAndelerSomIkkeLiggerIListeAvAndelsnr(List<Long> listeAvAndelsnr) {
            verifiserKanModifisere();
            List<BeregningsgrunnlagPrStatusOgAndelRestDto> andelerSomSkalFjernes = new ArrayList<>();
            for (BeregningsgrunnlagPrStatusOgAndelRestDto andel : kladd.getBeregningsgrunnlagPrStatusOgAndelList()) {
                if (!listeAvAndelsnr.contains(andel.getAndelsnr()) && andel.getLagtTilAvSaksbehandler()) {
                    andelerSomSkalFjernes.add(andel);
                }
            }
            kladd.beregningsgrunnlagPrStatusOgAndelList.removeAll(andelerSomSkalFjernes);
            return this;
        }

        public Builder leggTilBeregningsgrunnlagPrStatusOgAndel(BeregningsgrunnlagPrStatusOgAndelRestDto.Builder prStatusOgAndelBuilder) {
            verifiserKanModifisere();
            prStatusOgAndelBuilder.build(kladd);
            return this;
        }

        public Builder medBeregningsgrunnlagPrStatusOgAndel(List<BeregningsgrunnlagPrStatusOgAndelRestDto> beregningsgrunnlagPrStatusOgAndeler) {
            verifiserKanModifisere();
            kladd.beregningsgrunnlagPrStatusOgAndelList = beregningsgrunnlagPrStatusOgAndeler;
            return this;
        }

        public Builder fjernAlleBeregningsgrunnlagPrStatusOgAndeler() {
            verifiserKanModifisere();
            kladd.beregningsgrunnlagPrStatusOgAndelList = new ArrayList<>();
            return this;
        }

        public Builder fjernBeregningsgrunnlagPrStatusOgAndel(BeregningsgrunnlagPrStatusOgAndelRestDto beregningsgrunnlagPrStatusOgAndel) {
            verifiserKanModifisere();
            kladd.beregningsgrunnlagPrStatusOgAndelList.remove(beregningsgrunnlagPrStatusOgAndel);
            return this;
        }

        public Builder medBeregningsgrunnlagPeriode(LocalDate fraOgMed, LocalDate tilOgMed) {
            verifiserKanModifisere();
            kladd.periode = tilOgMed == null ? Intervall.fraOgMed(fraOgMed) : Intervall.fraOgMedTilOgMed(fraOgMed, tilOgMed);
            return this;
        }

        public Builder medBruttoPrÅr(BigDecimal bruttoPrÅr) {
            verifiserKanModifisere();
            kladd.bruttoPrÅr = bruttoPrÅr;
            return this;
        }

        public Builder medAvkortetPrÅr(BigDecimal avkortetPrÅr) {
            verifiserKanModifisere();
            kladd.avkortetPrÅr = avkortetPrÅr;
            return this;
        }

        public Builder medRedusertPrÅr(BigDecimal redusertPrÅr) {
            verifiserKanModifisere();
            kladd.redusertPrÅr = redusertPrÅr;
            return this;
        }


        public Builder leggTilPeriodeÅrsak(PeriodeÅrsak periodeÅrsak) {
            verifiserKanModifisere();
            if (!kladd.getPeriodeÅrsaker().contains(periodeÅrsak)) {
                BeregningsgrunnlagPeriodeÅrsakRestDto.Builder bgPeriodeÅrsakBuilder = new BeregningsgrunnlagPeriodeÅrsakRestDto.Builder();
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

        public Builder tilbakestillPeriodeÅrsaker() {
            verifiserKanModifisere();
            kladd.beregningsgrunnlagPeriodeÅrsaker.clear();
            return this;
        }

        public BeregningsgrunnlagPeriodeRestDto build(BeregningsgrunnlagRestDto beregningsgrunnlag) {
            kladd.beregningsgrunnlag = beregningsgrunnlag;
            verifyStateForBuild();

            kladd.beregningsgrunnlag.leggTilBeregningsgrunnlagPeriode(kladd);

            Long dagsatsSum = kladd.beregningsgrunnlagPrStatusOgAndelList.stream()
                .filter(bgpsa -> bgpsa.getDagsats() != null)
                .map(BeregningsgrunnlagPrStatusOgAndelRestDto::getDagsats)
                .reduce(Long::sum)
                .orElse(null);
            kladd.dagsats = dagsatsSum;
            built = true;
            return kladd;
        }

        public BeregningsgrunnlagPeriodeRestDto buildForKopi() {
            Long dagsatsSum = kladd.beregningsgrunnlagPrStatusOgAndelList.stream()
                .filter(bgpsa -> bgpsa.getDagsats() != null)
                .map(BeregningsgrunnlagPrStatusOgAndelRestDto::getDagsats)
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
            Objects.requireNonNull(kladd.beregningsgrunnlag, "beregningsgrunnlag");
            Objects.requireNonNull(kladd.beregningsgrunnlagPrStatusOgAndelList, "beregningsgrunnlagPrStatusOgAndelList");
            Objects.requireNonNull(kladd.periode, "beregningsgrunnlagPeriodeFom");
        }
    }
}
