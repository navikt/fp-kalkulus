package no.nav.folketrygdloven.kalkulator;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;

import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.output.BeregningVilkårResultat;
import no.nav.folketrygdloven.kalkulator.output.BeregningsgrunnlagRegelResultat;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.Vilkårsavslagsårsak;

@ApplicationScoped
@FagsakYtelseTypeRef("*")
public class VilkårTjeneste {

    public Optional<BeregningVilkårResultat> lagVilkårResultatFastsettBeregningsaktiviteter(BeregningsgrunnlagInput input, BeregningsgrunnlagRegelResultat beregningsgrunnlagRegelResultat) {
        return Optional.empty();
    }

    public BeregningVilkårResultat lagVilkårResultatFordel(BeregningsgrunnlagRegelResultat beregningsgrunnlagRegelResultat) {
        if (beregningsgrunnlagRegelResultat.getVilkårOppfylt()) {
            return new BeregningVilkårResultat(true);
        }
        return new BeregningVilkårResultat(false, Vilkårsavslagsårsak.FOR_LAVT_BG);
    }


    public Optional<BeregningVilkårResultat> lagVilkårResultatFullføre(BeregningsgrunnlagInput input, BeregningsgrunnlagDto beregningsgrunnlagDto) {
        return Optional.empty();
    }

}
