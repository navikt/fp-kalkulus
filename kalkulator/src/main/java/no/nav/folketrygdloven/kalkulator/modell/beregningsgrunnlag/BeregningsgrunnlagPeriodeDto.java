package no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag;


import static no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.kodeverk.BeregningsgrunnlagPeriodeRegelType.FASTSETT;
import static no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.kodeverk.BeregningsgrunnlagPeriodeRegelType.FINN_GRENSEVERDI;
import static no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.kodeverk.BeregningsgrunnlagPeriodeRegelType.FORDEL;
import static no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.kodeverk.BeregningsgrunnlagPeriodeRegelType.FORESLÅ;
import static no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.kodeverk.BeregningsgrunnlagPeriodeRegelType.OPPDATER_GRUNNLAG_SVP;
import static no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.kodeverk.BeregningsgrunnlagPeriodeRegelType.VILKÅR_VURDERING;
import static no.nav.vedtak.konfig.Tid.TIDENES_ENDE;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.kodeverk.BeregningsgrunnlagPeriodeRegelType;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.kodeverk.PeriodeÅrsak;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;

public class BeregningsgrunnlagPeriodeDto {

    private BeregningsgrunnlagDto beregningsgrunnlag;
    private List<BeregningsgrunnlagPrStatusOgAndelDto> beregningsgrunnlagPrStatusOgAndelList = new ArrayList<>();
    private Intervall periode;
    private BigDecimal bruttoPrÅr;
    private BigDecimal avkortetPrÅr;
    private BigDecimal redusertPrÅr;
    private Long dagsats;
    private Map<BeregningsgrunnlagPeriodeRegelType, BeregningsgrunnlagPeriodeRegelSporingDto> regelSporingMap = new HashMap<>();
    private List<BeregningsgrunnlagPeriodeÅrsakDto> beregningsgrunnlagPeriodeÅrsaker = new ArrayList<>();

    private BeregningsgrunnlagPeriodeDto() {
    }

    public BeregningsgrunnlagPeriodeDto(BeregningsgrunnlagPeriodeDto kopiereFra) {
        this.beregningsgrunnlagPrStatusOgAndelList = kopiereFra.beregningsgrunnlagPrStatusOgAndelList.stream().map(a ->
            {
                BeregningsgrunnlagPrStatusOgAndelDto kopi = new BeregningsgrunnlagPrStatusOgAndelDto(a);
                kopi.setBeregningsgrunnlagPeriode(this);
                return kopi;
            }
        ).collect(Collectors.toList());

        this.beregningsgrunnlagPeriodeÅrsaker = kopiereFra.beregningsgrunnlagPeriodeÅrsaker.stream().map(o ->
            BeregningsgrunnlagPeriodeÅrsakDto.Builder.kopier(o).build(this)
        ).collect(Collectors.toList());

        this.regelSporingMap = kopiereFra.regelSporingMap.entrySet().stream().map(o -> {
            BeregningsgrunnlagPeriodeRegelSporingDto.Builder builder = BeregningsgrunnlagPeriodeRegelSporingDto.Builder.kopier(o.getValue());
            BeregningsgrunnlagPeriodeRegelSporingDto build = builder.build(this);
            return Map.entry(o.getKey(), build);
        }).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        this.periode = kopiereFra.periode;
        this.bruttoPrÅr = kopiereFra.bruttoPrÅr;
        this.avkortetPrÅr = kopiereFra.avkortetPrÅr;
        this.redusertPrÅr = kopiereFra.redusertPrÅr;
        this.dagsats = kopiereFra.dagsats;
    }

    public List<BeregningsgrunnlagPrStatusOgAndelDto> getBeregningsgrunnlagPrStatusOgAndelList() {
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
                .map(BeregningsgrunnlagPrStatusOgAndelDto::getBeregnetPrÅr)
                .reduce(BigDecimal::add)
                .orElse(BigDecimal.ZERO);
    }

    void updateBruttoPrÅr() {
        bruttoPrÅr = beregningsgrunnlagPrStatusOgAndelList.stream()
                .filter(bgpsa -> bgpsa.getBruttoPrÅr() != null)
                .map(BeregningsgrunnlagPrStatusOgAndelDto::getBruttoPrÅr)
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

    public List<BeregningsgrunnlagPeriodeÅrsakDto> getBeregningsgrunnlagPeriodeÅrsaker() {
        return Collections.unmodifiableList(beregningsgrunnlagPeriodeÅrsaker);
    }

    public List<PeriodeÅrsak> getPeriodeÅrsaker() {
        return beregningsgrunnlagPeriodeÅrsaker.stream().map(BeregningsgrunnlagPeriodeÅrsakDto::getPeriodeÅrsak).collect(Collectors.toList());
    }

    void addBeregningsgrunnlagPrStatusOgAndel(BeregningsgrunnlagPrStatusOgAndelDto bgPrStatusOgAndel) {
        Objects.requireNonNull(bgPrStatusOgAndel, "beregningsgrunnlagPrStatusOgAndel");
        if (!beregningsgrunnlagPrStatusOgAndelList.contains(bgPrStatusOgAndel)) { // NOSONAR Class defines List based fields but uses them like Sets: Ingening å tjene på å bytte til Set ettersom det er små lister
            beregningsgrunnlagPrStatusOgAndelList.add(bgPrStatusOgAndel);
        }
    }

    void addBeregningsgrunnlagPeriodeÅrsak(BeregningsgrunnlagPeriodeÅrsakDto bgPeriodeÅrsak) {
        Objects.requireNonNull(bgPeriodeÅrsak, "beregningsgrunnlagPeriodeÅrsak");
        if (!beregningsgrunnlagPeriodeÅrsaker.contains(bgPeriodeÅrsak)) { // NOSONAR Class defines List based fields but uses them like Sets: Ingening å tjene på å bytte til Set ettersom det er små lister
            beregningsgrunnlagPeriodeÅrsaker.add(bgPeriodeÅrsak);
        }
    }

    void leggTilBeregningsgrunnlagPeriodeRegel(BeregningsgrunnlagPeriodeRegelSporingDto beregningsgrunnlagPeriodeRegelSporing) {
        Objects.requireNonNull(beregningsgrunnlagPeriodeRegelSporing, "beregningsgrunnlagPeriodeRegelSporing");
        regelSporingMap.put(beregningsgrunnlagPeriodeRegelSporing.getRegelType(), beregningsgrunnlagPeriodeRegelSporing);
    }

    public Beløp getTotaltRefusjonkravIPeriode() {
        return new Beløp(beregningsgrunnlagPrStatusOgAndelList.stream()
            .map(BeregningsgrunnlagPrStatusOgAndelDto::getBgAndelArbeidsforhold)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .map(BGAndelArbeidsforholdDto::getRefusjonskravPrÅr)
            .filter(Objects::nonNull)
            .reduce(BigDecimal::add)
            .orElse(BigDecimal.ZERO));
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (!(obj instanceof BeregningsgrunnlagPeriodeDto)) {
            return false;
        }
        BeregningsgrunnlagPeriodeDto other = (BeregningsgrunnlagPeriodeDto) obj;
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

    public static Builder builder(BeregningsgrunnlagPeriodeDto eksisterendeBeregningsgrunnlagPeriode) {
        return new Builder(eksisterendeBeregningsgrunnlagPeriode);
    }

    public static Builder oppdater(BeregningsgrunnlagPeriodeDto eksisterendeBeregningsgrunnlagPeriode) {
        return new Builder(eksisterendeBeregningsgrunnlagPeriode, true);
    }

    public String getRegelEvaluering() {
        return regelSporingMap.containsKey(FORESLÅ) ?  regelSporingMap.get(FORESLÅ).getRegelEvaluering() : null;
    }

    public String getRegelEvalueringFastsett() {
        return regelSporingMap.containsKey(FASTSETT) ?  regelSporingMap.get(FASTSETT).getRegelEvaluering() : null;
    }

    public String getRegelInput() {
        return regelSporingMap.containsKey(FORESLÅ)  ? regelSporingMap.get(FORESLÅ).getRegelInput() : null;
    }

    public String getRegelInputFastsett() {
        return regelSporingMap.containsKey(FASTSETT) ? regelSporingMap.get(FASTSETT).getRegelInput() : null;
    }

    public String getRegelInputFordel() {
        return regelSporingMap.containsKey(FORDEL) ? regelSporingMap.get(FORDEL).getRegelInput() : null;
    }
    public String getRegelEvalueringFordel() {
        return regelSporingMap.containsKey(FORDEL) ? regelSporingMap.get(FORDEL).getRegelEvaluering() : null;
    }


    public String getRegelInputFinnGrenseverdi() {
        return regelSporingMap.containsKey(FINN_GRENSEVERDI) ? regelSporingMap.get(FINN_GRENSEVERDI).getRegelInput() : null;
    }

    public String getRegelInputVilkårvurdering() {
        return regelSporingMap.containsKey(VILKÅR_VURDERING) ?  regelSporingMap.get(VILKÅR_VURDERING).getRegelInput() : null;
    }

    public String getRegelEvalueringVilkårvurdering() {
        return regelSporingMap.containsKey(VILKÅR_VURDERING) ?  regelSporingMap.get(VILKÅR_VURDERING).getRegelEvaluering() : null;
    }

    public String getRegelInputOppdatereGrunnlagSVP() {
        return regelSporingMap.containsKey(OPPDATER_GRUNNLAG_SVP) ?  regelSporingMap.get(OPPDATER_GRUNNLAG_SVP).getRegelInput() : null;
    }

    public String getRegelEvalueringFinnGrenseverdi() {
        return regelSporingMap.containsKey(FINN_GRENSEVERDI) ? regelSporingMap.get(FINN_GRENSEVERDI).getRegelEvaluering() : null;
    }

    public static class Builder {
        private BeregningsgrunnlagPeriodeDto kladd;
        private boolean built;
        private boolean oppdater;

        public static Builder kopier(BeregningsgrunnlagPeriodeDto p) {
            return new Builder(p);
        }

        public Builder(BeregningsgrunnlagPeriodeDto eksisterendeBeregningsgrunnlagPeriod, boolean oppdater) {
            this.oppdater = oppdater;
            this.kladd = eksisterendeBeregningsgrunnlagPeriod;
        }

        public Builder() {
            kladd = new BeregningsgrunnlagPeriodeDto();
        }

        public Builder(BeregningsgrunnlagPeriodeDto eksisterendeBeregningsgrunnlagPeriod) {
            this.kladd = new BeregningsgrunnlagPeriodeDto(eksisterendeBeregningsgrunnlagPeriod);
        }

        public Builder leggTilBeregningsgrunnlagPrStatusOgAndel(BeregningsgrunnlagPrStatusOgAndelDto beregningsgrunnlagPrStatusOgAndel) {
            verifiserKanModifisere();
            kladd.beregningsgrunnlagPrStatusOgAndelList.add(beregningsgrunnlagPrStatusOgAndel);
            return this;
        }

        public Builder fjernBeregningsgrunnlagPrStatusOgAndelerSomIkkeLiggerIListeAvAndelsnr(List<Long> listeAvAndelsnr) {
            verifiserKanModifisere();
            List<BeregningsgrunnlagPrStatusOgAndelDto> andelerSomSkalFjernes = new ArrayList<>();
            for (BeregningsgrunnlagPrStatusOgAndelDto andel : kladd.getBeregningsgrunnlagPrStatusOgAndelList()) {
                if (!listeAvAndelsnr.contains(andel.getAndelsnr()) && andel.getLagtTilAvSaksbehandler()) {
                    andelerSomSkalFjernes.add(andel);
                }
            }
            kladd.beregningsgrunnlagPrStatusOgAndelList.removeAll(andelerSomSkalFjernes);
            return this;
        }

        public Builder leggTilBeregningsgrunnlagPrStatusOgAndel(BeregningsgrunnlagPrStatusOgAndelDto.Builder prStatusOgAndelBuilder) {
            verifiserKanModifisere();
            prStatusOgAndelBuilder.build(kladd);
            return this;
        }

        public Builder medBeregningsgrunnlagPrStatusOgAndel(List<BeregningsgrunnlagPrStatusOgAndelDto> beregningsgrunnlagPrStatusOgAndeler) {
            verifiserKanModifisere();
            kladd.beregningsgrunnlagPrStatusOgAndelList = beregningsgrunnlagPrStatusOgAndeler;
            return this;
        }

        public Builder fjernAlleBeregningsgrunnlagPrStatusOgAndeler() {
            verifiserKanModifisere();
            kladd.beregningsgrunnlagPrStatusOgAndelList = new ArrayList<>();
            return this;
        }

        public Builder fjernBeregningsgrunnlagPrStatusOgAndel(BeregningsgrunnlagPrStatusOgAndelDto beregningsgrunnlagPrStatusOgAndel) {
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

        public Builder medRegelEvalueringForeslå(String regelInput, String regelEvaluering) {
            verifiserKanModifisere();
            BeregningsgrunnlagPeriodeRegelSporingDto.ny()
                .medRegelInput(regelInput)
                .medRegelEvaluering(regelEvaluering)
                .medRegelType(FORESLÅ)
                .build(kladd);
            return this;
        }

        public Builder medRegelEvalueringFordel(String regelInput, String regelEvaluering) {
            verifiserKanModifisere();
            BeregningsgrunnlagPeriodeRegelSporingDto.ny()
                .medRegelInput(regelInput)
                .medRegelEvaluering(regelEvaluering)
                .medRegelType(FORDEL)
                .build(kladd);
            return this;
        }

        public Builder medRegelEvalueringFastsett(String regelInput, String regelEvaluering) {
            verifiserKanModifisere();
            BeregningsgrunnlagPeriodeRegelSporingDto.ny()
                .medRegelInput(regelInput)
                .medRegelEvaluering(regelEvaluering)
                .medRegelType(FASTSETT)
                .build(kladd);
            return this;
        }

        public Builder medRegelEvalueringVilkårsvurdering(String regelInput, String regelEvaluering) {
            verifiserKanModifisere();
            BeregningsgrunnlagPeriodeRegelSporingDto.ny()
                .medRegelInput(regelInput)
                .medRegelEvaluering(regelEvaluering)
                .medRegelType(VILKÅR_VURDERING)
                .build(kladd);
            return this;
        }

        public Builder medRegelEvalueringFinnGrenseverdi(String regelInput, String regelEvaluering) {
            verifiserKanModifisere();
            BeregningsgrunnlagPeriodeRegelSporingDto.ny()
                .medRegelInput(regelInput)
                .medRegelEvaluering(regelEvaluering)
                .medRegelType(FINN_GRENSEVERDI)
                .build(kladd);
            return this;
        }


        public Builder leggTilPeriodeÅrsak(PeriodeÅrsak periodeÅrsak) {
            verifiserKanModifisere();
            if (!kladd.getPeriodeÅrsaker().contains(periodeÅrsak)) {
                BeregningsgrunnlagPeriodeÅrsakDto.Builder bgPeriodeÅrsakBuilder = new BeregningsgrunnlagPeriodeÅrsakDto.Builder();
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

        public BeregningsgrunnlagPeriodeDto build() {
            Long dagsatsSum = kladd.beregningsgrunnlagPrStatusOgAndelList.stream()
                .filter(bgpsa -> bgpsa.getDagsats() != null)
                .map(BeregningsgrunnlagPrStatusOgAndelDto::getDagsats)
                .reduce(Long::sum)
                .orElse(null);
            kladd.dagsats = dagsatsSum;
            built = true;
            return kladd;
        }

        public BeregningsgrunnlagPeriodeDto build(BeregningsgrunnlagDto beregningsgrunnlag) {
            kladd.beregningsgrunnlag = beregningsgrunnlag;
            verifyStateForBuild();

            kladd.beregningsgrunnlag.leggTilBeregningsgrunnlagPeriode(kladd);

            Long dagsatsSum = kladd.beregningsgrunnlagPrStatusOgAndelList.stream()
                .filter(bgpsa -> bgpsa.getDagsats() != null)
                .map(BeregningsgrunnlagPrStatusOgAndelDto::getDagsats)
                .reduce(Long::sum)
                .orElse(null);
            kladd.dagsats = dagsatsSum;
            built = true;
            return kladd;
        }

        public BeregningsgrunnlagPeriodeDto buildForKopi() {
            Long dagsatsSum = kladd.beregningsgrunnlagPrStatusOgAndelList.stream()
                .filter(bgpsa -> bgpsa.getDagsats() != null)
                .map(BeregningsgrunnlagPrStatusOgAndelDto::getDagsats)
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
