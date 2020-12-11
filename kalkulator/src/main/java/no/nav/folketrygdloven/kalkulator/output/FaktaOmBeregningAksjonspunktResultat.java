package no.nav.folketrygdloven.kalkulator.output;

import java.util.ArrayList;
import java.util.List;

import no.nav.folketrygdloven.kalkulus.kodeverk.FaktaOmBeregningTilfelle;


public class FaktaOmBeregningAksjonspunktResultat {

    public final static FaktaOmBeregningAksjonspunktResultat INGEN_AKSJONSPUNKTER = new FaktaOmBeregningAksjonspunktResultat();

    private List<BeregningAksjonspunktResultat> beregningAksjonspunktResultatList = new ArrayList<>();

    private List<FaktaOmBeregningTilfelle> faktaOmBeregningTilfeller = new ArrayList<>();

    private FaktaOmBeregningAksjonspunktResultat() { }

    public FaktaOmBeregningAksjonspunktResultat(List<BeregningAksjonspunktResultat> beregningAksjonspunktResultatList, List<FaktaOmBeregningTilfelle> faktaOmBeregningTilfeller) {
        this.beregningAksjonspunktResultatList = beregningAksjonspunktResultatList;
        this.faktaOmBeregningTilfeller = faktaOmBeregningTilfeller;
    }


    public List<BeregningAksjonspunktResultat> getBeregningAksjonspunktResultatList() {
        return beregningAksjonspunktResultatList;
    }

    public List<FaktaOmBeregningTilfelle> getFaktaOmBeregningTilfeller() {
        return faktaOmBeregningTilfeller;
    }

}
