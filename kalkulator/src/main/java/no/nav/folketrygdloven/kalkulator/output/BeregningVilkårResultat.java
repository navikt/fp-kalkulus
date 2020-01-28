package no.nav.folketrygdloven.kalkulator.output;

public class BeregningVilkårResultat {

    private boolean erVilkårOppfylt;

    public BeregningVilkårResultat(boolean erVilkårOppfylt) {
        this.erVilkårOppfylt = erVilkårOppfylt;
    }

    public boolean getErVilkårOppfylt() {
        return erVilkårOppfylt;
    }
}
