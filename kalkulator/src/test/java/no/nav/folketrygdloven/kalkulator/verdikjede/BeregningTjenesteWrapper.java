package no.nav.folketrygdloven.kalkulator.verdikjede;

import no.nav.folketrygdloven.kalkulator.AksjonspunktUtlederFaktaOmBeregning;
import no.nav.folketrygdloven.kalkulator.FastsettBeregningsgrunnlagPerioderTjeneste;
import no.nav.folketrygdloven.kalkulator.FordelBeregningsgrunnlagTjeneste;
import no.nav.folketrygdloven.kalkulator.ytelse.fp.FullføreBeregningsgrunnlagFPImpl;

class BeregningTjenesteWrapper {

    private FullføreBeregningsgrunnlagFPImpl fullføreBeregningsgrunnlagTjeneste;
    private FordelBeregningsgrunnlagTjeneste fordelBeregningsgrunnlagTjeneste;
    private AksjonspunktUtlederFaktaOmBeregning aksjonspunktUtlederFaktaOmBeregning;
    private FastsettBeregningsgrunnlagPerioderTjeneste fastsettBeregningsgrunnlagPerioderTjeneste;

    public BeregningTjenesteWrapper(FullføreBeregningsgrunnlagFPImpl fullføreBeregningsgrunnlagTjeneste,
                                    FordelBeregningsgrunnlagTjeneste fordelBeregningsgrunnlagTjeneste,
                                    AksjonspunktUtlederFaktaOmBeregning aksjonspunktUtlederFaktaOmBeregning,
                                    FastsettBeregningsgrunnlagPerioderTjeneste fastsettBeregningsgrunnlagPerioderTjeneste) {
        this.fullføreBeregningsgrunnlagTjeneste = fullføreBeregningsgrunnlagTjeneste;
        this.fordelBeregningsgrunnlagTjeneste = fordelBeregningsgrunnlagTjeneste;
        this.aksjonspunktUtlederFaktaOmBeregning = aksjonspunktUtlederFaktaOmBeregning;
        this.fastsettBeregningsgrunnlagPerioderTjeneste = fastsettBeregningsgrunnlagPerioderTjeneste;
    }

    public FullføreBeregningsgrunnlagFPImpl getFullføreBeregningsgrunnlagTjeneste() {
        return fullføreBeregningsgrunnlagTjeneste;
    }

    public FordelBeregningsgrunnlagTjeneste getFordelBeregningsgrunnlagTjeneste() {
        return fordelBeregningsgrunnlagTjeneste;
    }

    public AksjonspunktUtlederFaktaOmBeregning getAksjonspunktUtlederFaktaOmBeregning() {
        return aksjonspunktUtlederFaktaOmBeregning;
    }

    public FastsettBeregningsgrunnlagPerioderTjeneste getFastsettBeregningsgrunnlagPerioderTjeneste() {
        return fastsettBeregningsgrunnlagPerioderTjeneste;
    }

}
