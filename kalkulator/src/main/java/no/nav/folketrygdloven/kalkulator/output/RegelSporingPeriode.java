package no.nav.folketrygdloven.kalkulator.output;

import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagPeriodeRegelType;

public class RegelSporingPeriode {

    private String regelEvaluering;
    private String regelInput;
    private Intervall periode;
    private BeregningsgrunnlagPeriodeRegelType regelType;

    public RegelSporingPeriode(String regelEvaluering, String regelInput, Intervall periode, BeregningsgrunnlagPeriodeRegelType regelType) {
        this.regelEvaluering = regelEvaluering;
        this.regelInput = regelInput;
        this.periode = periode;
        this.regelType = regelType;
    }

    public String getRegelEvaluering() {
        return regelEvaluering;
    }

    public String getRegelInput() {
        return regelInput;
    }

    public Intervall getPeriode() {
        return periode;
    }

    public BeregningsgrunnlagPeriodeRegelType getRegelType() {
        return regelType;
    }
}
