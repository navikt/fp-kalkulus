package no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag;


import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.kodeverk.BeregningsgrunnlagPeriodeRegelType;


public class BeregningsgrunnlagPeriodeRegelSporingDto {

    private BeregningsgrunnlagPeriodeDto beregningsgrunnlagPeriode;
    private String regelEvaluering;
    private String regelInput;
    private BeregningsgrunnlagPeriodeRegelType regelType;

    private BeregningsgrunnlagPeriodeRegelSporingDto() {
    }

    public BeregningsgrunnlagPeriodeRegelSporingDto(BeregningsgrunnlagPeriodeRegelSporingDto kopiereFra) {
        this.regelEvaluering = kopiereFra.regelEvaluering;
        this.regelInput = kopiereFra.regelInput;
        this.regelType = kopiereFra.regelType;
    }

    BeregningsgrunnlagPeriodeRegelType getRegelType() {
        return regelType;
    }

    public String getRegelEvaluering() {
        return regelEvaluering;
    }

    public String getRegelInput() {
        return regelInput;
    }

    static Builder ny() {
        return new Builder();
    }

    static class Builder {
        private BeregningsgrunnlagPeriodeRegelSporingDto beregningsgrunnlagPeriodeRegelSporingMal;

        public Builder() {
            beregningsgrunnlagPeriodeRegelSporingMal = new BeregningsgrunnlagPeriodeRegelSporingDto();
        }

        public Builder(BeregningsgrunnlagPeriodeRegelSporingDto value) {
            beregningsgrunnlagPeriodeRegelSporingMal = new BeregningsgrunnlagPeriodeRegelSporingDto(value);
        }

        public static Builder kopier(BeregningsgrunnlagPeriodeRegelSporingDto value) {
            return new Builder(value);
        }

        Builder medRegelInput(String regelInput) {
            beregningsgrunnlagPeriodeRegelSporingMal.regelInput = regelInput;
            return this;
        }

        Builder medRegelEvaluering(String regelEvaluering) {
            beregningsgrunnlagPeriodeRegelSporingMal.regelEvaluering = regelEvaluering;
            return this;
        }

        Builder medRegelType(BeregningsgrunnlagPeriodeRegelType regelType) {
            beregningsgrunnlagPeriodeRegelSporingMal.regelType = regelType;
            return this;
        }

        BeregningsgrunnlagPeriodeRegelSporingDto build(BeregningsgrunnlagPeriodeDto beregningsgrunnlagPeriode) {
            beregningsgrunnlagPeriodeRegelSporingMal.beregningsgrunnlagPeriode = beregningsgrunnlagPeriode;
            beregningsgrunnlagPeriode.leggTilBeregningsgrunnlagPeriodeRegel(beregningsgrunnlagPeriodeRegelSporingMal);
            return beregningsgrunnlagPeriodeRegelSporingMal;
        }
    }

}
