package no.nav.folketrygdloven.kalkulator.output;

import java.util.List;

import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.Vilkårsavslagsårsak;


public class BeregningResultatAggregat {

    private List<BeregningAksjonspunktResultat> beregningAksjonspunktResultater;

    private BeregningsgrunnlagGrunnlagDto beregningsgrunnlagGrunnlag;

    private BeregningVilkårResultat beregningVilkårResultat;

    public List<BeregningAksjonspunktResultat> getBeregningAksjonspunktResultater() {
        return beregningAksjonspunktResultater;
    }

    public BeregningsgrunnlagGrunnlagDto getBeregningsgrunnlagGrunnlag() {
        return beregningsgrunnlagGrunnlag;
    }

    public BeregningsgrunnlagDto getBeregningsgrunnlag() {
        return beregningsgrunnlagGrunnlag.getBeregningsgrunnlag().orElseThrow(() -> new IllegalStateException("Forventet å ha beregningsgrunnlag"));
    }


    public BeregningVilkårResultat getBeregningVilkårResultat() {
        return beregningVilkårResultat;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private BeregningsgrunnlagGrunnlagDtoBuilder grunnlagBuilder;
        private BeregningsgrunnlagTilstand tilstand;
        private BeregningResultatAggregat kladd;

        public Builder(BeregningsgrunnlagInput input) {
            this.grunnlagBuilder = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(input.getBeregningsgrunnlagGrunnlag());
            this.kladd = new BeregningResultatAggregat();
        }

        private Builder() {
            this.kladd = new BeregningResultatAggregat();
        }

        public static Builder fra(BeregningsgrunnlagInput input) {
            return new Builder(input);
        }

        public Builder medBeregningsgrunnlag(BeregningsgrunnlagDto beregningsgrunnlag, BeregningsgrunnlagTilstand tilstand) {
            grunnlagBuilder.medBeregningsgrunnlag(beregningsgrunnlag);
            this.tilstand = tilstand;
            return this;
        }

        public Builder medAksjonspunkter(List<BeregningAksjonspunktResultat> beregningAksjonspunktResultater) {
            this.kladd.beregningAksjonspunktResultater = beregningAksjonspunktResultater;
            return this;
        }

        public Builder medVilkårResultat(boolean erVilkårOppfylt) {
            this.kladd.beregningVilkårResultat = new BeregningVilkårResultat(erVilkårOppfylt);
            return this;
        }

        public Builder medVilkårAvslått(Vilkårsavslagsårsak vilkårsavslagsårsak) {
            this.kladd.beregningVilkårResultat = new BeregningVilkårResultat(false, vilkårsavslagsårsak);
            return this;
        }

        public BeregningResultatAggregat build() {
            if (this.tilstand != null && this.grunnlagBuilder != null) {
                this.kladd.beregningsgrunnlagGrunnlag = grunnlagBuilder.build(tilstand);
                return kladd;
            } else if (this.kladd.beregningAksjonspunktResultater != null) {
                return kladd;
            }
            throw new IllegalStateException("Må sette enten beregningsgrunnlag eller beregningaksjonspunkter på beregningresultataggregat!");
        }

    }


}
