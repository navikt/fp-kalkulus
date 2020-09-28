package no.nav.folketrygdloven.kalkulator.output;

import java.util.List;
import java.util.Optional;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;

public class BeregningsgrunnlagRegelResultat {
    private BeregningsgrunnlagDto beregningsgrunnlag;
    private List<BeregningAksjonspunktResultat> aksjonspunkter;
    private List<BeregningVilkårResultat> vilkårsresultat;
    private RegelSporingAggregat regelsporinger;

    public BeregningsgrunnlagRegelResultat(BeregningsgrunnlagDto beregningsgrunnlag,
                                           List<BeregningAksjonspunktResultat> aksjonspunktResultatListe) {
        this.beregningsgrunnlag = beregningsgrunnlag;
        this.aksjonspunkter = aksjonspunktResultatListe;
    }

    public BeregningsgrunnlagRegelResultat(BeregningsgrunnlagDto beregningsgrunnlag, RegelSporingAggregat regelsporinger) {
        this.beregningsgrunnlag = beregningsgrunnlag;
        this.regelsporinger = regelsporinger;
    }

    public BeregningsgrunnlagRegelResultat(BeregningsgrunnlagDto beregningsgrunnlag,
                                           List<BeregningAksjonspunktResultat> aksjonspunkter,
                                           RegelSporingAggregat regelsporinger) {
        this.beregningsgrunnlag = beregningsgrunnlag;
        this.aksjonspunkter = aksjonspunkter;
        this.regelsporinger = regelsporinger;
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

    public Optional<RegelSporingAggregat> getRegelsporinger() {
        return Optional.ofNullable(regelsporinger);
    }
}
