package no.nav.folketrygdloven.kalkulator.steg.refusjon;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import no.nav.folketrygdloven.kalkulator.FagsakYtelseTypeRef;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.output.BeregningAvklaringsbehovResultat;
import no.nav.folketrygdloven.kalkulator.output.BeregningsgrunnlagRegelResultat;
import no.nav.folketrygdloven.kalkulator.output.RegelSporingAggregat;
import no.nav.folketrygdloven.kalkulator.steg.fordeling.periodisering.FordelPerioderTjeneste;

@ApplicationScoped
public class VurderRefusjonBeregningsgrunnlag {
    private FordelPerioderTjeneste fordelPerioderTjeneste;
    private Instance<AvklaringsbehovutledertjenesteVurderRefusjon> aksjonspunkutledere;

    public VurderRefusjonBeregningsgrunnlag() {
        // CDI
    }

    @Inject
    public VurderRefusjonBeregningsgrunnlag(FordelPerioderTjeneste fordelPerioderTjeneste,
                                            @Any Instance<AvklaringsbehovutledertjenesteVurderRefusjon> avklaringsbehovUtledere) {
        this.fordelPerioderTjeneste = fordelPerioderTjeneste;
        this.aksjonspunkutledere = avklaringsbehovUtledere;
    }

    public BeregningsgrunnlagRegelResultat vurderRefusjon(BeregningsgrunnlagInput input, BeregningsgrunnlagDto vilkårsvurdertBeregningsgrunnlag) {
        BeregningsgrunnlagRegelResultat resultatFraRefusjonPeriodisering = fordelPerioderTjeneste.fastsettPerioderForRefusjon(input, vilkårsvurdertBeregningsgrunnlag);
        BeregningsgrunnlagRegelResultat resultatFraPeriodisering = fordelPerioderTjeneste.fastsettPerioderForUtbetalingsgradEllerGradering(input, resultatFraRefusjonPeriodisering.getBeregningsgrunnlag());
        List<BeregningAvklaringsbehovResultat> avklaringsbehov = FagsakYtelseTypeRef.Lookup.find(aksjonspunkutledere, input.getFagsakYtelseType())
                .orElseThrow(() -> new IllegalStateException("Fant ikke AksjonspunkutledertjenesteVurderRefusjon for ytelsetype " + input.getFagsakYtelseType().getKode()))
                .utledAvklaringsbehov(input, resultatFraPeriodisering.getBeregningsgrunnlag());
        return new BeregningsgrunnlagRegelResultat(resultatFraPeriodisering.getBeregningsgrunnlag(),
                avklaringsbehov,
                RegelSporingAggregat.konkatiner(resultatFraRefusjonPeriodisering.getRegelsporinger().orElse(null), resultatFraPeriodisering.getRegelsporinger().orElse(null)));
    }
}
