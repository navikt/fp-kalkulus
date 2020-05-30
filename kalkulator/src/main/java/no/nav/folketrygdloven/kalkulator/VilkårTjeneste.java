package no.nav.folketrygdloven.kalkulator;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;

import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.output.BeregningVilkårResultat;
import no.nav.folketrygdloven.kalkulator.output.BeregningsgrunnlagRegelResultat;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.Vilkårsavslagsårsak;
import no.nav.folketrygdloven.kalkulus.felles.tid.AbstractIntervall;

@ApplicationScoped
@FagsakYtelseTypeRef("*")
public class VilkårTjeneste {

    public List<BeregningVilkårResultat> lagVilkårResultatFordel(BeregningsgrunnlagInput input, BeregningsgrunnlagRegelResultat beregningsgrunnlagRegelResultat) {
        Intervall vilkårsperiode = Intervall.fraOgMedTilOgMed(input.getSkjæringstidspunktForBeregning(), AbstractIntervall.TIDENES_ENDE);
        if (beregningsgrunnlagRegelResultat.getVilkårOppfylt()) {
            return List.of(new BeregningVilkårResultat(true, vilkårsperiode));
        }
        return List.of(new BeregningVilkårResultat(false, Vilkårsavslagsårsak.FOR_LAVT_BG, vilkårsperiode));
    }

    public List<BeregningVilkårResultat> lagVilkårResultatFullføre(BeregningsgrunnlagInput input, BeregningsgrunnlagDto beregningsgrunnlagDto) {
        return List.of();
    }

}
