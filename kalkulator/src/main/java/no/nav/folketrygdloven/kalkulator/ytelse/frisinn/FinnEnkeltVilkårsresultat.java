package no.nav.folketrygdloven.kalkulator.ytelse.frisinn;

import java.util.List;
import java.util.Optional;

import no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.FrisinnGrunnlag;
import no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.FrisinnPeriode;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.output.BeregningVilkårResultat;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.FagsakYtelseType;
import no.nav.folketrygdloven.kalkulus.felles.tid.AbstractIntervall;

public class FinnEnkeltVilkårsresultat {

    private FinnEnkeltVilkårsresultat() {
        // Skjul
    }

    public static BeregningVilkårResultat finnEnkeltVilkårsresultatFastsett(List<BeregningVilkårResultat> beregningVilkårResultatListe, BeregningsgrunnlagInput input) {
        if (beregningVilkårResultatListe.isEmpty() || !input.getFagsakYtelseType().equals(FagsakYtelseType.FRISINN)) {
            return null;
        }
        FrisinnGrunnlag frisinnGrunnlag = input.getYtelsespesifiktGrunnlag();
        List<FrisinnPeriode> frisinnPerioder = frisinnGrunnlag.getFrisinnPerioder();
        boolean erAvslått = frisinnPerioder.stream().allMatch(p -> beregningVilkårResultatListe.stream().anyMatch(vp -> vp.getPeriode().overlapper(p.getPeriode())
                && !vp.getErVilkårOppfylt()));
        Intervall vilkårsperiode = Intervall.fraOgMedTilOgMed(input.getSkjæringstidspunktForBeregning(), AbstractIntervall.TIDENES_ENDE);
        if (erAvslått) {
            Optional<BeregningVilkårResultat> avslåttVilkår = beregningVilkårResultatListe.stream().filter(vr -> !vr.getErVilkårOppfylt()).findFirst();
            return avslåttVilkår
                    .map(beregningVilkårResultat -> new BeregningVilkårResultat(false, beregningVilkårResultat.getVilkårsavslagsårsak(), vilkårsperiode))
                    .orElse(null);
        }
        return null;
    }

}
