package no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag;

import static no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.BeregningsgrunnlagRegelType.PERIODISERING;

import java.util.Objects;

import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.BeregningsgrunnlagRegelType;

public class BeregningsgrunnlagRegelSporingDto {

    private BeregningsgrunnlagDto beregningsgrunnlag;
    private String regelEvaluering;
    private String regelInput;
    private BeregningsgrunnlagRegelType regelType;

    public BeregningsgrunnlagRegelSporingDto() {
    }

    public BeregningsgrunnlagRegelSporingDto(BeregningsgrunnlagRegelSporingDto value) {
        this.regelEvaluering = value.regelEvaluering;
        this.regelInput = value.regelInput;
        this.regelType = value.regelType;
    }

    BeregningsgrunnlagRegelType getRegelType() {
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
        private BeregningsgrunnlagRegelSporingDto beregningsgrunnlagRegelSporingMal;

        Builder() {
            beregningsgrunnlagRegelSporingMal = new BeregningsgrunnlagRegelSporingDto();
        }

        public Builder(BeregningsgrunnlagRegelSporingDto value) {
            beregningsgrunnlagRegelSporingMal = new BeregningsgrunnlagRegelSporingDto(value);
        }

        public static Builder kopier(BeregningsgrunnlagRegelSporingDto value) {
            return new Builder(value);
        }

        Builder medRegelInput(String regelInput) {
            beregningsgrunnlagRegelSporingMal.regelInput = regelInput;
            return this;
        }

        Builder medRegelEvaluering(String regelEvaluering) {
            beregningsgrunnlagRegelSporingMal.regelEvaluering = regelEvaluering;
            return this;
        }

        Builder medRegelType(BeregningsgrunnlagRegelType regelType) {
            beregningsgrunnlagRegelSporingMal.regelType = regelType;
            return this;
        }

        BeregningsgrunnlagRegelSporingDto build(BeregningsgrunnlagDto beregningsgrunnlag) {
            verifyStateForBuild();
            beregningsgrunnlagRegelSporingMal.beregningsgrunnlag = beregningsgrunnlag;
            beregningsgrunnlag.leggTilBeregningsgrunnlagRegel(beregningsgrunnlagRegelSporingMal);
            return beregningsgrunnlagRegelSporingMal;
        }

        public void verifyStateForBuild() {
            Objects.requireNonNull(beregningsgrunnlagRegelSporingMal.regelType, "regelType");
            Objects.requireNonNull(beregningsgrunnlagRegelSporingMal.regelInput, "regelInput");
            // Periodisering har ingen logg for evaluering, men kun input
            if (!PERIODISERING.equals(beregningsgrunnlagRegelSporingMal.regelType)) {
                Objects.requireNonNull(beregningsgrunnlagRegelSporingMal.regelEvaluering, "regelEvaluering");
            }
        }

    }
}
