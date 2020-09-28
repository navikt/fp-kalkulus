package no.nav.folketrygdloven.kalkulator.verdikjede;

import no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta.AksjonspunktUtlederFaktaOmBeregning;
import no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta.periodisering.FastsettBeregningsgrunnlagPerioderTjeneste;
import no.nav.folketrygdloven.kalkulator.steg.fordeling.FordelBeregningsgrunnlagTjeneste;
import no.nav.folketrygdloven.kalkulator.steg.fullføre.ytelse.fp.FullføreBeregningsgrunnlagFPImpl;

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
