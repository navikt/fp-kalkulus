package no.nav.folketrygdloven.kalkulator;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.folketrygdloven.beregningsgrunnlag.RegelmodellOversetter;
import no.nav.folketrygdloven.beregningsgrunnlag.fordel.RegelFordelBeregningsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.RegelResultat;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.kalkulator.adapter.regelmodelltilvl.MapBeregningsgrunnlagFraRegelTilVL;
import no.nav.folketrygdloven.kalkulator.adapter.regelmodelltilvl.MapBeregningsgrunnlagFraRegelTilVLFordel;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.MapBeregningsgrunnlagFraVLTilRegel;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.behandling.BehandlingReferanse;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.fpsak.nare.evaluation.Evaluation;

@ApplicationScoped
public class FordelBeregningsgrunnlagTjeneste {

    private FastsettBeregningsgrunnlagPerioderTjeneste fastsettBeregningsgrunnlagPerioderTjeneste;
    private MapBeregningsgrunnlagFraVLTilRegel mapBeregningsgrunnlagFraVLTilRegel;
    private MapBeregningsgrunnlagFraRegelTilVLFordel mapBeregningsgrunnlagFraRegelTilVL = new MapBeregningsgrunnlagFraRegelTilVLFordel();

    public FordelBeregningsgrunnlagTjeneste() {
        // CDI
    }

    @Inject
    public FordelBeregningsgrunnlagTjeneste(FastsettBeregningsgrunnlagPerioderTjeneste fastsettBeregningsgrunnlagPerioderTjeneste,
                                            MapBeregningsgrunnlagFraVLTilRegel mapBeregningsgrunnlagFraVLTilRegel) {
        this.fastsettBeregningsgrunnlagPerioderTjeneste = fastsettBeregningsgrunnlagPerioderTjeneste;
        this.mapBeregningsgrunnlagFraVLTilRegel = mapBeregningsgrunnlagFraVLTilRegel;
    }

    public BeregningsgrunnlagDto fordelBeregningsgrunnlag(BeregningsgrunnlagInput input, BeregningsgrunnlagDto beregningsgrunnlag) {
        BeregningsgrunnlagDto bgMedRefusjon = fastsettBeregningsgrunnlagPerioderTjeneste
            .fastsettPerioderForRefusjonOgGradering(input, beregningsgrunnlag);
        var ref = input.getBehandlingReferanse();
        return kjørRegelForOmfordeling(input, ref, bgMedRefusjon);
    }

    private BeregningsgrunnlagDto kjørRegelForOmfordeling(BeregningsgrunnlagInput input, BehandlingReferanse ref, BeregningsgrunnlagDto beregningsgrunnlag) {
        var regelPerioder = mapBeregningsgrunnlagFraVLTilRegel.mapTilFordelingsregel(ref, beregningsgrunnlag, input);
        String regelinput = toJson(regelPerioder);
        List<RegelResultat> regelResultater = new ArrayList<>();
        for (BeregningsgrunnlagPeriode periode : regelPerioder) {
            RegelFordelBeregningsgrunnlag regel = new RegelFordelBeregningsgrunnlag(periode);
            Evaluation evaluation = regel.evaluer(periode);
            regelResultater.add(RegelmodellOversetter.getRegelResultat(evaluation, regelinput));
        }
        return mapBeregningsgrunnlagFraRegelTilVL.map(regelPerioder, regelResultater, beregningsgrunnlag);
    }

    private static String toJson(List<no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode> regelPerioder) {
        return JsonMapper.toJson(regelPerioder, BeregningsgrunnlagFeil.FEILFACTORY::kanIkkeSerialisereRegelinput);
    }

}
