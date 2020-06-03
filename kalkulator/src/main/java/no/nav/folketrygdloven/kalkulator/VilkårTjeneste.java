package no.nav.folketrygdloven.kalkulator;

import java.util.List;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;

import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.output.BeregningVilkårResultat;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.felles.tid.AbstractIntervall;

@ApplicationScoped
@FagsakYtelseTypeRef("*")
public class VilkårTjeneste {

    public BeregningVilkårResultat lagVilkårResultatFordel(BeregningsgrunnlagInput input, List<BeregningVilkårResultat> beregningVilkårResultatListe) {
        boolean erAvslått = beregningVilkårResultatListe.stream().anyMatch(vp -> !vp.getErVilkårOppfylt());
        Intervall vilkårsperiode = Intervall.fraOgMedTilOgMed(input.getSkjæringstidspunktForBeregning(), AbstractIntervall.TIDENES_ENDE);
        if (erAvslått) {
            Optional<BeregningVilkårResultat> avslåttVilkår = beregningVilkårResultatListe.stream().filter(vr -> !vr.getErVilkårOppfylt()).findFirst();
            return avslåttVilkår.map(beregningVilkårResultat -> lagAvslag(vilkårsperiode, beregningVilkårResultat))
                    .orElseThrow(() -> new IllegalStateException("Forventer å finne vilkår med avslag."));
        } else {
            return lagInnvilgelse(vilkårsperiode);
        }
    }

    public Optional<BeregningVilkårResultat> lagVilkårResultatFullføre(BeregningsgrunnlagInput input, BeregningsgrunnlagDto beregningsgrunnlagDto) {
        return Optional.empty();
    }

    private BeregningVilkårResultat lagInnvilgelse(Intervall vilkårsperiode) {
        return new BeregningVilkårResultat(true, vilkårsperiode);
    }

    private BeregningVilkårResultat lagAvslag(Intervall vilkårsperiode, BeregningVilkårResultat beregningVilkårResultat) {
        return new BeregningVilkårResultat(false, beregningVilkårResultat.getVilkårsavslagsårsak(), vilkårsperiode);
    }

}
