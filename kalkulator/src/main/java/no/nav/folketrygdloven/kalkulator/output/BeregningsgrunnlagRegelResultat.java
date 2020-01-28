package no.nav.folketrygdloven.kalkulator.output;

import java.util.List;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;

public class BeregningsgrunnlagRegelResultat {
    private BeregningsgrunnlagDto beregningsgrunnlag;
    private List<BeregningAksjonspunktResultat> aksjonspunkter;
    private Boolean vilkårOppfylt;

    public BeregningsgrunnlagRegelResultat(BeregningsgrunnlagDto beregningsgrunnlag, List<BeregningAksjonspunktResultat> aksjonspunktResultatListe) {
        this.beregningsgrunnlag = beregningsgrunnlag;
        this.aksjonspunkter = aksjonspunktResultatListe;
    }

    public BeregningsgrunnlagDto getBeregningsgrunnlag() {
        return beregningsgrunnlag;
    }

    public List<BeregningAksjonspunktResultat> getAksjonspunkter() {
        return aksjonspunkter;
    }

    public Boolean getVilkårOppfylt() {
        return vilkårOppfylt;
    }

    public void setVilkårOppfylt(Boolean vilkårOppfylt) {
        this.vilkårOppfylt = vilkårOppfylt;
    }
}
