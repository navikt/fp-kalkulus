package no.nav.folketrygdloven.kalkulator.output;


import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.BeregningsgrunnlagRegelType;

public class RegelSporingGrunnlag {

    private String regelEvaluering;
    private String regelInput;
    private BeregningsgrunnlagRegelType regelType;

    public RegelSporingGrunnlag(String regelEvaluering, String regelInput, BeregningsgrunnlagRegelType regelType) {
        this.regelEvaluering = regelEvaluering;
        this.regelInput = regelInput;
        this.regelType = regelType;
    }


    public String getRegelEvaluering() {
        return regelEvaluering;
    }

    public String getRegelInput() {
        return regelInput;
    }

    public BeregningsgrunnlagRegelType getRegelType() {
        return regelType;
    }
}
