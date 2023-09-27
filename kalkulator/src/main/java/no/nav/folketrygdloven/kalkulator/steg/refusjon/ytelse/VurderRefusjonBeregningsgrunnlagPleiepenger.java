package no.nav.folketrygdloven.kalkulator.steg.refusjon.ytelse;

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
import no.nav.folketrygdloven.kalkulator.steg.fordeling.periodisering.PeriodiserForAktivitetsgradTjeneste;
import no.nav.folketrygdloven.kalkulator.steg.refusjon.AvklaringsbehovutledertjenesteVurderRefusjon;
import no.nav.folketrygdloven.kalkulator.steg.refusjon.ForlengelsePeriodeTjeneste;
import no.nav.folketrygdloven.kalkulator.steg.refusjon.VurderRefusjonBeregningsgrunnlag;
import no.nav.folketrygdloven.kalkulus.kodeverk.FagsakYtelseType;

@ApplicationScoped
@FagsakYtelseTypeRef(FagsakYtelseType.PLEIEPENGER_SYKT_BARN)
@FagsakYtelseTypeRef(FagsakYtelseType.PLEIEPENGER_NÆRSTÅENDE)
public class VurderRefusjonBeregningsgrunnlagPleiepenger implements VurderRefusjonBeregningsgrunnlag {
    private FordelPerioderTjeneste fordelPerioderTjeneste;
    private Instance<AvklaringsbehovutledertjenesteVurderRefusjon> aksjonspunkutledere;

    public VurderRefusjonBeregningsgrunnlagPleiepenger() {
        // CDI
    }

    @Inject
    public VurderRefusjonBeregningsgrunnlagPleiepenger(FordelPerioderTjeneste fordelPerioderTjeneste,
                                                       @Any Instance<AvklaringsbehovutledertjenesteVurderRefusjon> avklaringsbehovUtledere) {
        this.fordelPerioderTjeneste = fordelPerioderTjeneste;
        this.aksjonspunkutledere = avklaringsbehovUtledere;
    }

    @Override
    public BeregningsgrunnlagRegelResultat vurderRefusjon(BeregningsgrunnlagInput input) {
        BeregningsgrunnlagRegelResultat resultatFraRefusjonPeriodisering = fordelPerioderTjeneste.fastsettPerioderForRefusjon(input);
        BeregningsgrunnlagRegelResultat resultatFraPeriodisering = fordelPerioderTjeneste.fastsettPerioderForUtbetalingsgradEllerGradering(input, resultatFraRefusjonPeriodisering.getBeregningsgrunnlag());
        var splittetVedForlengelse = ForlengelsePeriodeTjeneste.splittVedStartAvForlengelse(input, resultatFraPeriodisering.getBeregningsgrunnlag());
        var splittForAktivitetsgrad = PeriodiserForAktivitetsgradTjeneste.splittVedEndringIAktivitetsgrad(splittetVedForlengelse, input.getYtelsespesifiktGrunnlag());
        List<BeregningAvklaringsbehovResultat> avklaringsbehov = FagsakYtelseTypeRef.Lookup.find(aksjonspunkutledere, input.getFagsakYtelseType())
                .orElseThrow(() -> new IllegalStateException("Fant ikke AksjonspunkutledertjenesteVurderRefusjon for ytelsetype " + input.getFagsakYtelseType().getKode()))
                .utledAvklaringsbehov(input, splittForAktivitetsgrad);
        return new BeregningsgrunnlagRegelResultat(splittForAktivitetsgrad,
                avklaringsbehov,
                RegelSporingAggregat.konkatiner(resultatFraRefusjonPeriodisering.getRegelsporinger().orElse(null), resultatFraPeriodisering.getRegelsporinger().orElse(null)));
    }
}
