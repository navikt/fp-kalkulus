package no.nav.folketrygdloven.kalkulator;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.folketrygdloven.beregningsgrunnlag.RegelmodellOversetter;
import no.nav.folketrygdloven.kalkulator.adapter.regelmodelltilvl.MapBeregningsgrunnlagFraRegelTilVL;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.MapBeregningsgrunnlagFraVLTilRegel;
import no.nav.folketrygdloven.beregningsgrunnlag.fordel.RegelFordelBeregningsgrunnlag;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.behandling.BehandlingReferanse;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.RegelResultat;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.fpsak.nare.evaluation.Evaluation;

@ApplicationScoped
public class FordelBeregningsgrunnlagTjeneste {

    private FastsettBeregningsgrunnlagPerioderTjeneste fastsettBeregningsgrunnlagPerioderTjeneste;

    public FordelBeregningsgrunnlagTjeneste() {
        // CDI
    }

    @Inject
    public FordelBeregningsgrunnlagTjeneste(FastsettBeregningsgrunnlagPerioderTjeneste fastsettBeregningsgrunnlagPerioderTjeneste) {
        this.fastsettBeregningsgrunnlagPerioderTjeneste = fastsettBeregningsgrunnlagPerioderTjeneste;
    }

    public BeregningsgrunnlagDto fordelBeregningsgrunnlag(BeregningsgrunnlagInput input, BeregningsgrunnlagDto beregningsgrunnlag) {
        BeregningsgrunnlagDto bgMedRefusjon = fastsettBeregningsgrunnlagPerioderTjeneste
            .fastsettPerioderForRefusjonOgGradering(input, beregningsgrunnlag);
        var ref = input.getBehandlingReferanse();
        return kjørRegelForOmfordeling(input, ref, bgMedRefusjon);
    }

    private BeregningsgrunnlagDto kjørRegelForOmfordeling(BeregningsgrunnlagInput input, BehandlingReferanse ref, BeregningsgrunnlagDto beregningsgrunnlag) {
        var regelPerioder = MapBeregningsgrunnlagFraVLTilRegel.mapTilFordelingsregel(ref, beregningsgrunnlag, input);
        String regelinput = toJson(regelPerioder);
        List<RegelResultat> regelResultater = new ArrayList<>();
        for (BeregningsgrunnlagPeriode periode : regelPerioder) {
            RegelFordelBeregningsgrunnlag regel = new RegelFordelBeregningsgrunnlag(periode);
            Evaluation evaluation = regel.evaluer(periode);
            regelResultater.add(RegelmodellOversetter.getRegelResultat(evaluation, regelinput));
        }
        return MapBeregningsgrunnlagFraRegelTilVL.mapForFordel(regelPerioder, regelResultater, beregningsgrunnlag);
    }

    private static String toJson(List<no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode> regelPerioder) {
        return JacksonJsonConfig.toJson(regelPerioder, BeregningsgrunnlagFeil.FEILFACTORY::kanIkkeSerialisereRegelinput);
    }

}
