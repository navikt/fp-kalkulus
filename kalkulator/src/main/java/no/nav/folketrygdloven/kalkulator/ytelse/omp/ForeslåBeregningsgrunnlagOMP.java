package no.nav.folketrygdloven.kalkulator.ytelse.omp;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.RegelResultat;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.Beregningsgrunnlag;
import no.nav.folketrygdloven.kalkulator.BeregningsgrunnlagVerifiserer;
import no.nav.folketrygdloven.kalkulator.FagsakYtelseTypeRef;
import no.nav.folketrygdloven.kalkulator.FastsettBeregningsgrunnlagPerioderTjeneste;
import no.nav.folketrygdloven.kalkulator.ForeslåBeregningsgrunnlag;
import no.nav.folketrygdloven.kalkulator.adapter.regelmodelltilvl.MapBeregningsgrunnlagFraRegelTilVL;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.MapBeregningsgrunnlagFraVLTilRegel;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.output.BeregningAksjonspunktResultat;
import no.nav.folketrygdloven.kalkulator.output.BeregningsgrunnlagRegelResultat;

@ApplicationScoped
@FagsakYtelseTypeRef("OMP")
public class ForeslåBeregningsgrunnlagOMP extends ForeslåBeregningsgrunnlag {

    private FastsettBeregningsgrunnlagPerioderTjeneste fastsettBeregningsgrunnlagPerioderTjeneste;

    public ForeslåBeregningsgrunnlagOMP() {
        // CDI
    }

    @Inject
    public ForeslåBeregningsgrunnlagOMP(MapBeregningsgrunnlagFraVLTilRegel mapBeregningsgrunnlagFraVLTilRegel,
                                        FastsettBeregningsgrunnlagPerioderTjeneste fastsettBeregningsgrunnlagPerioderTjeneste) {
        super(mapBeregningsgrunnlagFraVLTilRegel);
        this.fastsettBeregningsgrunnlagPerioderTjeneste = fastsettBeregningsgrunnlagPerioderTjeneste;
    }


    /**
     * Foreslå beregningsgrunnlag for Omsorgspenger
     *
     * Splitter på utbetalingsgrad og refusjon før det sendes til regel.
     *
     * @param input BeregningsgrunnlagInput
     * @return Foreslått beregningsgrunnlag
     */
    public BeregningsgrunnlagRegelResultat foreslåBeregningsgrunnlag(BeregningsgrunnlagInput input) {
        BeregningsgrunnlagGrunnlagDto grunnlag = input.getBeregningsgrunnlagGrunnlag();
        BeregningsgrunnlagDto beregningsgrunnlag = grunnlag.getBeregningsgrunnlag()
                .orElseThrow(() -> new IllegalStateException("Skal ha beregningsgrunnlag her"));
        beregningsgrunnlag = fastsettBeregningsgrunnlagPerioderTjeneste.fastsettPerioderUtenAndelendring(input, beregningsgrunnlag);

        // Oversetter initielt Beregningsgrunnlag -> regelmodell
        Beregningsgrunnlag regelmodellBeregningsgrunnlag = mapBeregningsgrunnlagFraVLTilRegel.map(input, grunnlag);
        splittPerioder(input, regelmodellBeregningsgrunnlag, beregningsgrunnlag);
        String jsonInput = toJson(regelmodellBeregningsgrunnlag);
        List<RegelResultat> regelResultater = kjørRegelForeslåBeregningsgrunnlag(regelmodellBeregningsgrunnlag, jsonInput);

        // Oversett endelig resultat av regelmodell til foreslått Beregningsgrunnlag  (+ spore input -> evaluation)
        BeregningsgrunnlagDto foreslåttBeregningsgrunnlag = MapBeregningsgrunnlagFraRegelTilVL.mapForeslåBeregningsgrunnlag(regelmodellBeregningsgrunnlag, regelResultater, beregningsgrunnlag);
        List<BeregningAksjonspunktResultat> aksjonspunkter = utledAksjonspunkter(input, regelResultater);
        BeregningsgrunnlagVerifiserer.verifiserForeslåttBeregningsgrunnlag(foreslåttBeregningsgrunnlag);
        return new BeregningsgrunnlagRegelResultat(foreslåttBeregningsgrunnlag, aksjonspunkter);
    }

}
