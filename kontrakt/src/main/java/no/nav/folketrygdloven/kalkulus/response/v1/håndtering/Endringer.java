package no.nav.folketrygdloven.kalkulus.response.v1.håndtering;

/**
 * Beskriver hvilke endringer som er gjort på beregningsgrunnlaget ved løst avklaringsbehov
 */
public class Endringer {

    private BeregningsgrunnlagEndring beregningsgrunnlagEndring;
    private FaktaOmBeregningVurderinger faktaOmBeregningVurderinger;
    private RefusjonoverstyringEndring refusjonoverstyringEndring;

    public Endringer(BeregningsgrunnlagEndring beregningsgrunnlagEndring,
                     FaktaOmBeregningVurderinger faktaOmBeregningVurderinger) {
        this.beregningsgrunnlagEndring = beregningsgrunnlagEndring;
        this.faktaOmBeregningVurderinger = faktaOmBeregningVurderinger;
    }

    public Endringer(RefusjonoverstyringEndring refusjonoverstyringEndring) {
        this.refusjonoverstyringEndring = refusjonoverstyringEndring;
    }

    public Endringer(BeregningsgrunnlagEndring beregningsgrunnlagEndring) {
        this.beregningsgrunnlagEndring = beregningsgrunnlagEndring;
    }

    public BeregningsgrunnlagEndring getBeregningsgrunnlagEndring() {
        return beregningsgrunnlagEndring;
    }

    public FaktaOmBeregningVurderinger getFaktaOmBeregningVurderinger() {
        return faktaOmBeregningVurderinger;
    }

    public RefusjonoverstyringEndring getRefusjonoverstyringEndring() {
        return refusjonoverstyringEndring;
    }

}
