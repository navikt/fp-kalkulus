package no.nav.folketrygdloven.kalkulator.steg.refusjon.ytelse.omp;

import static no.nav.folketrygdloven.kalkulator.steg.refusjon.ytelse.omp.FastsettPerioderBrukersSøknad.fastsettPerioderForBrukersSøknad;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import no.nav.folketrygdloven.kalkulator.FagsakYtelseTypeRef;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.input.OmsorgspengerGrunnlag;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.output.BeregningAvklaringsbehovResultat;
import no.nav.folketrygdloven.kalkulator.output.BeregningsgrunnlagRegelResultat;
import no.nav.folketrygdloven.kalkulator.output.RegelSporingAggregat;
import no.nav.folketrygdloven.kalkulator.steg.fordeling.periodisering.FordelPerioderTjeneste;
import no.nav.folketrygdloven.kalkulator.steg.refusjon.AvklaringsbehovutledertjenesteVurderRefusjon;

@ApplicationScoped
@FagsakYtelseTypeRef("OMP")
public class VurderRefusjonBeregningsgrunnlagOMP {
    private FordelPerioderTjeneste fordelPerioderTjeneste;
    private Instance<AvklaringsbehovutledertjenesteVurderRefusjon> aksjonspunkutledere;

    public VurderRefusjonBeregningsgrunnlagOMP() {
        // CDI
    }

    @Inject
    public VurderRefusjonBeregningsgrunnlagOMP(FordelPerioderTjeneste fordelPerioderTjeneste,
                                               @Any Instance<AvklaringsbehovutledertjenesteVurderRefusjon> avklaringsbehovUtledere) {
        this.fordelPerioderTjeneste = fordelPerioderTjeneste;
        this.aksjonspunkutledere = avklaringsbehovUtledere;
    }

    public BeregningsgrunnlagRegelResultat vurderRefusjon(BeregningsgrunnlagInput input) {
        BeregningsgrunnlagRegelResultat resultatFraRefusjonPeriodisering = fordelPerioderTjeneste.fastsettPerioderForRefusjon(input);
        BeregningsgrunnlagRegelResultat resultatFraPeriodisering = fordelPerioderTjeneste.fastsettPerioderForUtbetalingsgradEllerGradering(input, resultatFraRefusjonPeriodisering.getBeregningsgrunnlag());
        var periodisertForBrukersSøknad = fastsettPerioderForBrukersSøknad(input.getYtelsespesifiktGrunnlag(), resultatFraPeriodisering.getBeregningsgrunnlag());
        List<BeregningAvklaringsbehovResultat> avklaringsbehov = FagsakYtelseTypeRef.Lookup.find(aksjonspunkutledere, input.getFagsakYtelseType())
                .orElseThrow(() -> new IllegalStateException("Fant ikke AksjonspunkutledertjenesteVurderRefusjon for ytelsetype " + input.getFagsakYtelseType().getKode()))
                .utledAvklaringsbehov(input, periodisertForBrukersSøknad);
        return new BeregningsgrunnlagRegelResultat(periodisertForBrukersSøknad,
                avklaringsbehov,
                RegelSporingAggregat.konkatiner(resultatFraRefusjonPeriodisering.getRegelsporinger().orElse(null), resultatFraPeriodisering.getRegelsporinger().orElse(null)));
    }
}
