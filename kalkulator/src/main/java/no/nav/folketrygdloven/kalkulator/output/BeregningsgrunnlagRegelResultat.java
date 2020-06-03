package no.nav.folketrygdloven.kalkulator.output;

import java.util.List;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;

public class BeregningsgrunnlagRegelResultat {
    private BeregningsgrunnlagDto beregningsgrunnlag;
    private List<BeregningAksjonspunktResultat> aksjonspunkter;
    private List<BeregningVilkårResultat> vilkårsresultat;

    public BeregningsgrunnlagRegelResultat(BeregningsgrunnlagDto beregningsgrunnlag, List<BeregningAksjonspunktResultat> aksjonspunktResultatListe) {
        this.beregningsgrunnlag = beregningsgrunnlag;
        this.aksjonspunkter = aksjonspunktResultatListe;
    }

    public BeregningsgrunnlagRegelResultat(BeregningsgrunnlagDto beregningsgrunnlag, List<BeregningAksjonspunktResultat> aksjonspunktResultatListe, List<BeregningVilkårResultat> vilkårsresultat) {
        this.beregningsgrunnlag = beregningsgrunnlag;
        this.aksjonspunkter = aksjonspunktResultatListe;
        this.vilkårsresultat = vilkårsresultat;
    }

    public BeregningsgrunnlagDto getBeregningsgrunnlag() {
        return beregningsgrunnlag;
    }

    public List<BeregningAksjonspunktResultat> getAksjonspunkter() {
        return aksjonspunkter;
    }

    public Boolean getVilkårOppfylt() {

        if (vilkårsresultat != null) {
            return vilkårsresultat.stream().allMatch(BeregningVilkårResultat::getErVilkårOppfylt);
        }

        return null;
    }


    public void setVilkårsresultat(List<BeregningVilkårResultat> vilkårsresultat) {
        this.vilkårsresultat = vilkårsresultat;
    }

    public List<BeregningVilkårResultat> getVilkårsresultat() {
        return vilkårsresultat;
    }
}
