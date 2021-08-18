package no.nav.folketrygdloven.kalkulator.verdikjede;

import no.nav.folketrygdloven.kalkulator.steg.fordeling.FordelBeregningsgrunnlagTjenesteImpl;
import no.nav.folketrygdloven.kalkulator.steg.fullføre.ytelse.fp.FullføreBeregningsgrunnlagFPImpl;
import no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta.AvklaringsbehovUtlederFaktaOmBeregning;
import no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta.periodisering.FastsettBeregningsgrunnlagPerioderTjeneste;
import no.nav.folketrygdloven.kalkulator.steg.refusjon.VurderRefusjonBeregningsgrunnlag;

class BeregningTjenesteWrapper {

    private FullføreBeregningsgrunnlagFPImpl fullføreBeregningsgrunnlagTjeneste;
    private FordelBeregningsgrunnlagTjenesteImpl fordelBeregningsgrunnlagTjeneste;
    private AvklaringsbehovUtlederFaktaOmBeregning avklaringsbehovUtlederFaktaOmBeregning;
    private FastsettBeregningsgrunnlagPerioderTjeneste fastsettBeregningsgrunnlagPerioderTjeneste;
    private VurderRefusjonBeregningsgrunnlag vurderRefusjonBeregningsgrunnlag;

    public BeregningTjenesteWrapper(FullføreBeregningsgrunnlagFPImpl fullføreBeregningsgrunnlagTjeneste,
                                    FordelBeregningsgrunnlagTjenesteImpl fordelBeregningsgrunnlagTjeneste,
                                    AvklaringsbehovUtlederFaktaOmBeregning avklaringsbehovUtlederFaktaOmBeregning,
                                    FastsettBeregningsgrunnlagPerioderTjeneste fastsettBeregningsgrunnlagPerioderTjeneste,
                                    VurderRefusjonBeregningsgrunnlag vurderRefusjonBeregningsgrunnlag) {
        this.fullføreBeregningsgrunnlagTjeneste = fullføreBeregningsgrunnlagTjeneste;
        this.fordelBeregningsgrunnlagTjeneste = fordelBeregningsgrunnlagTjeneste;
        this.avklaringsbehovUtlederFaktaOmBeregning = avklaringsbehovUtlederFaktaOmBeregning;
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

    public AvklaringsbehovUtlederFaktaOmBeregning getAvklaringsbehovUtlederFaktaOmBeregning() {
        return avklaringsbehovUtlederFaktaOmBeregning;
    }

    public FastsettBeregningsgrunnlagPerioderTjeneste getFastsettBeregningsgrunnlagPerioderTjeneste() {
        return fastsettBeregningsgrunnlagPerioderTjeneste;
    }

}
