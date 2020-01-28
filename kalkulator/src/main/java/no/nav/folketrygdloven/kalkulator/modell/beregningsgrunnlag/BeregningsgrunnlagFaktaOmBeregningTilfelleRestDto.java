package no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag;

import java.util.Objects;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.kodeverk.FaktaOmBeregningTilfelle;

public class BeregningsgrunnlagFaktaOmBeregningTilfelleRestDto {

    private BeregningsgrunnlagRestDto beregningsgrunnlag;
    private FaktaOmBeregningTilfelle faktaOmBeregningTilfelle = FaktaOmBeregningTilfelle.UDEFINERT;

    public BeregningsgrunnlagFaktaOmBeregningTilfelleRestDto() {
    }

    public BeregningsgrunnlagFaktaOmBeregningTilfelleRestDto(FaktaOmBeregningTilfelle p) {
        this.faktaOmBeregningTilfelle = p;
    }

    public FaktaOmBeregningTilfelle getFaktaOmBeregningTilfelle() {
        return faktaOmBeregningTilfelle;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof BeregningsgrunnlagFaktaOmBeregningTilfelleRestDto)) {
            return false;
        }
        BeregningsgrunnlagFaktaOmBeregningTilfelleRestDto that = (BeregningsgrunnlagFaktaOmBeregningTilfelleRestDto) o;
        return Objects.equals(beregningsgrunnlag, that.beregningsgrunnlag) &&
                Objects.equals(faktaOmBeregningTilfelle, that.faktaOmBeregningTilfelle);
    }

    @Override
    public int hashCode() {
        return Objects.hash(beregningsgrunnlag, faktaOmBeregningTilfelle);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private BeregningsgrunnlagFaktaOmBeregningTilfelleRestDto beregningsgrunnlagFaktaOmBeregningTilfelle;

        public Builder() {
            beregningsgrunnlagFaktaOmBeregningTilfelle = new BeregningsgrunnlagFaktaOmBeregningTilfelleRestDto();
        }

        public Builder(FaktaOmBeregningTilfelle p) {
            beregningsgrunnlagFaktaOmBeregningTilfelle = new BeregningsgrunnlagFaktaOmBeregningTilfelleRestDto(p);
        }

        public static Builder kopier(FaktaOmBeregningTilfelle beregningsgrunnlagFaktaOmBeregningTilfelle) {
            return new Builder(beregningsgrunnlagFaktaOmBeregningTilfelle);
        }

        BeregningsgrunnlagFaktaOmBeregningTilfelleRestDto.Builder medFaktaOmBeregningTilfelle(FaktaOmBeregningTilfelle tilfelle) {
            beregningsgrunnlagFaktaOmBeregningTilfelle.faktaOmBeregningTilfelle = tilfelle;
            return this;
        }

        public BeregningsgrunnlagFaktaOmBeregningTilfelleRestDto build(BeregningsgrunnlagRestDto beregningsgrunnlag) {
            beregningsgrunnlagFaktaOmBeregningTilfelle.beregningsgrunnlag = beregningsgrunnlag;
            return beregningsgrunnlagFaktaOmBeregningTilfelle;
        }
    }
}
