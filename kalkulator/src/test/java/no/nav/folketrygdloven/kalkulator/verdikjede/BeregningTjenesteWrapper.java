package no.nav.folketrygdloven.kalkulator.verdikjede;

import no.nav.folketrygdloven.kalkulator.steg.fordeling.FordelBeregningsgrunnlagTjenesteImpl;
import no.nav.folketrygdloven.kalkulator.steg.fullføre.ytelse.fp.FullføreBeregningsgrunnlagFPImpl;
import no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta.AksjonspunktUtlederFaktaOmBeregning;
import no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta.periodisering.FastsettBeregningsgrunnlagPerioderTjeneste;
import no.nav.folketrygdloven.kalkulator.steg.refusjon.VurderRefusjonBeregningsgrunnlag;

class BeregningTjenesteWrapper {

    private FullføreBeregningsgrunnlagFPImpl fullføreBeregningsgrunnlagTjeneste;
    private FordelBeregningsgrunnlagTjenesteImpl fordelBeregningsgrunnlagTjeneste;
    private AksjonspunktUtlederFaktaOmBeregning aksjonspunktUtlederFaktaOmBeregning;
    private FastsettBeregningsgrunnlagPerioderTjeneste fastsettBeregningsgrunnlagPerioderTjeneste;
    private VurderRefusjonBeregningsgrunnlag vurderRefusjonBeregningsgrunnlag;

    public BeregningTjenesteWrapper(FullføreBeregningsgrunnlagFPImpl fullføreBeregningsgrunnlagTjeneste,
                                    FordelBeregningsgrunnlagTjenesteImpl fordelBeregningsgrunnlagTjeneste,
                                    AksjonspunktUtlederFaktaOmBeregning aksjonspunktUtlederFaktaOmBeregning,
                                    FastsettBeregningsgrunnlagPerioderTjeneste fastsettBeregningsgrunnlagPerioderTjeneste,
                                    VurderRefusjonBeregningsgrunnlag vurderRefusjonBeregningsgrunnlag) {
        this.fullføreBeregningsgrunnlagTjeneste = fullføreBeregningsgrunnlagTjeneste;
        this.fordelBeregningsgrunnlagTjeneste = fordelBeregningsgrunnlagTjeneste;
        this.aksjonspunktUtlederFaktaOmBeregning = aksjonspunktUtlederFaktaOmBeregning;
        this.fastsettBeregningsgrunnlagPerioderTjeneste = fastsettBeregningsgrunnlagPerioderTjeneste;
        this.vurderRefusjonBeregningsgrunnlag = vurderRefusjonBeregningsgrunnlag;
    }

    public FullføreBeregningsgrunnlagFPImpl getFullføreBeregningsgrunnlagTjeneste() {
        return fullføreBeregningsgrunnlagTjeneste;
    }

    public VurderRefusjonBeregningsgrunnlag getVurderRefusjonBeregningsgrunnlagtjeneste() {
        return vurderRefusjonBeregningsgrunnlag;
    }

    public FordelBeregningsgrunnlagTjenesteImpl getFordelBeregningsgrunnlagTjeneste() {
        return fordelBeregningsgrunnlagTjeneste;
    }

    public AksjonspunktUtlederFaktaOmBeregning getAksjonspunktUtlederFaktaOmBeregning() {
        return aksjonspunktUtlederFaktaOmBeregning;
    }

    public FastsettBeregningsgrunnlagPerioderTjeneste getFastsettBeregningsgrunnlagPerioderTjeneste() {
        return fastsettBeregningsgrunnlagPerioderTjeneste;
    }

}
