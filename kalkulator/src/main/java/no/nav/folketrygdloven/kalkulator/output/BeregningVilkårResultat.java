package no.nav.folketrygdloven.kalkulator.output;

import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.Vilkårsavslagsårsak;

public class BeregningVilkårResultat {

    private final boolean erVilkårOppfylt;
    private Vilkårsavslagsårsak vilkårsavslagsårsak;

    public BeregningVilkårResultat(boolean erVilkårOppfylt, Vilkårsavslagsårsak vilkårsavslagsårsak) {
        this.erVilkårOppfylt = erVilkårOppfylt;
        this.vilkårsavslagsårsak = vilkårsavslagsårsak;
    }

    public BeregningVilkårResultat(boolean erVilkårOppfylt) {
        this.erVilkårOppfylt = erVilkårOppfylt;
    }

    public boolean getErVilkårOppfylt() {
        return erVilkårOppfylt;
    }

    public Vilkårsavslagsårsak getVilkårsavslagsårsak() {
        return vilkårsavslagsårsak;
    }
}
