package no.nav.folketrygdloven.kalkulator.steg.refusjon;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import no.nav.folketrygdloven.kalkulator.FagsakYtelseTypeRef;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.output.BeregningAksjonspunktResultat;
import no.nav.folketrygdloven.kalkulator.output.BeregningsgrunnlagRegelResultat;
import no.nav.folketrygdloven.kalkulator.steg.fordeling.periodisering.FordelPerioderTjeneste;

@ApplicationScoped
public class VurderRefusjonBeregningsgrunnlag {
    private FordelPerioderTjeneste fordelPerioderTjeneste;
    private Instance<AksjonspunkutledertjenesteVurderRefusjon> aksjonspunkutledere;

    public VurderRefusjonBeregningsgrunnlag() {
        // CDI
    }

    @Inject
    public VurderRefusjonBeregningsgrunnlag(FordelPerioderTjeneste fordelPerioderTjeneste,
                                            @Any Instance<AksjonspunkutledertjenesteVurderRefusjon> aksjonspunktUtledere) {
        this.fordelPerioderTjeneste = fordelPerioderTjeneste;
        this.aksjonspunkutledere = aksjonspunktUtledere;
    }

    public BeregningsgrunnlagRegelResultat vurderRefusjon(BeregningsgrunnlagInput input, BeregningsgrunnlagDto vilkårsvurdertBeregningsgrunnlag) {
        BeregningsgrunnlagRegelResultat resultatFraPeriodisering = fordelPerioderTjeneste.fastsettPerioderForRefusjonOgGradering(input, vilkårsvurdertBeregningsgrunnlag);
        List<BeregningAksjonspunktResultat> aksjonspunkter = FagsakYtelseTypeRef.Lookup.find(aksjonspunkutledere, input.getFagsakYtelseType())
                .orElseThrow(() -> new IllegalStateException("Fant ikke AksjonspunkutlederVurderRefusjon for ytelsetype " + input.getFagsakYtelseType().getKode()))
                .utledAksjonspunkter(input, resultatFraPeriodisering.getBeregningsgrunnlag());
        return new BeregningsgrunnlagRegelResultat(resultatFraPeriodisering.getBeregningsgrunnlag(),
                aksjonspunkter,
                resultatFraPeriodisering.getRegelsporinger().orElse(null));
    }
}
