package no.nav.folketrygdloven.kalkulator.steg.refusjon;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import no.nav.folketrygdloven.kalkulator.FagsakYtelseTypeRef;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.output.BeregningAvklaringsbehovResultat;
import no.nav.folketrygdloven.kalkulator.output.BeregningsgrunnlagRegelResultat;
import no.nav.folketrygdloven.kalkulator.output.RegelSporingAggregat;
import no.nav.folketrygdloven.kalkulator.steg.fordeling.periodisering.FordelPerioderTjeneste;

@ApplicationScoped
public class VurderRefusjonBeregningsgrunnlagFelles implements VurderRefusjonBeregningsgrunnlag {
    private FordelPerioderTjeneste fordelPerioderTjeneste;
    private Instance<AvklaringsbehovutledertjenesteVurderRefusjon> aksjonspunkutledere;

    public VurderRefusjonBeregningsgrunnlagFelles() {
        // CDI
    }

    @Inject
    public VurderRefusjonBeregningsgrunnlagFelles(FordelPerioderTjeneste fordelPerioderTjeneste,
                                                  @Any Instance<AvklaringsbehovutledertjenesteVurderRefusjon> avklaringsbehovUtledere) {
        this.fordelPerioderTjeneste = fordelPerioderTjeneste;
        this.aksjonspunkutledere = avklaringsbehovUtledere;
    }

    @Override
    public BeregningsgrunnlagRegelResultat vurderRefusjon(BeregningsgrunnlagInput input) {
        BeregningsgrunnlagRegelResultat resultatFraRefusjonPeriodisering = fordelPerioderTjeneste.fastsettPerioderForRefusjon(input);
        BeregningsgrunnlagRegelResultat resultatFraPeriodisering = fordelPerioderTjeneste.fastsettPerioderForUtbetalingsgradEllerGradering(input, resultatFraRefusjonPeriodisering.getBeregningsgrunnlag());
        List<BeregningAvklaringsbehovResultat> avklaringsbehov = FagsakYtelseTypeRef.Lookup.find(aksjonspunkutledere, input.getFagsakYtelseType())
                .orElseThrow(() -> new IllegalStateException("Fant ikke AksjonspunkutledertjenesteVurderRefusjon for ytelsetype " + input.getFagsakYtelseType().getKode()))
                .utledAvklaringsbehov(input, resultatFraPeriodisering.getBeregningsgrunnlag());
        return new BeregningsgrunnlagRegelResultat(resultatFraPeriodisering.getBeregningsgrunnlag(),
                avklaringsbehov,
                RegelSporingAggregat.konkatiner(resultatFraRefusjonPeriodisering.getRegelsporinger().orElse(null), resultatFraPeriodisering.getRegelsporinger().orElse(null)));
    }
}
