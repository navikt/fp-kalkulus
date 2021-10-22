package no.nav.folketrygdloven.kalkulus.response.v1.håndtering;

/**
 * Beskriver hvilke endringer som er gjort på beregningsgrunnlaget ved løst avklaringsbehov
 */
public class Endringer {

    private BeregningsgrunnlagEndring beregningsgrunnlagEndring;
    private FaktaOmBeregningVurderinger faktaOmBeregningVurderinger;
    private VarigEndretNæringEndring varigEndretNæringEndring;
    private RefusjonoverstyringEndring refusjonoverstyringEndring;

    private Endringer() {
    }

    public Endringer(RefusjonoverstyringEndring refusjonoverstyringEndring) {
        this.refusjonoverstyringEndring = refusjonoverstyringEndring;
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

    public VarigEndretNæringEndring getVarigEndretNæringEndring() {
        return varigEndretNæringEndring;
    }

    public static Builder ny() {
        return new Builder();
    }

    public static class Builder {

        private Endringer kladd;

        private Builder() {
            this.kladd = new Endringer();
        }

        public Builder medBeregningsgrunnlagEndring(BeregningsgrunnlagEndring beregningsgrunnlagEndring) {
            this.kladd.beregningsgrunnlagEndring = beregningsgrunnlagEndring;
            return this;
        }

        public Builder medFaktaOmBeregningVurderinger(FaktaOmBeregningVurderinger faktaOmBeregningVurderinger) {
            this.kladd.faktaOmBeregningVurderinger = faktaOmBeregningVurderinger;
            return this;
        }

        public Builder medVarigEndretNæringEndring(VarigEndretNæringEndring varigEndretNæringEndring) {
            this.kladd.varigEndretNæringEndring = varigEndretNæringEndring;
            return this;
        }


        public Endringer build() {
            return kladd;
        }

    }

}
