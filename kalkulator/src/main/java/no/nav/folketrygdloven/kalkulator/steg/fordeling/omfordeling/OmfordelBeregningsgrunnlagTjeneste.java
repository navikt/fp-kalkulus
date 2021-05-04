package no.nav.folketrygdloven.kalkulator.steg.fordeling.omfordeling;

import static no.nav.folketrygdloven.kalkulator.adapter.regelmodelltilvl.MapRegelSporingFraRegelTilVL.mapRegelsporingPerioder;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.folketrygdloven.beregningsgrunnlag.RegelmodellOversetter;
import no.nav.folketrygdloven.beregningsgrunnlag.fordel.RegelFordelBeregningsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.RegelResultat;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.kalkulator.BeregningsgrunnlagFeil;
import no.nav.folketrygdloven.kalkulator.JsonMapper;
import no.nav.folketrygdloven.kalkulator.adapter.regelmodelltilvl.MapBeregningsgrunnlagFraRegelTilVLFordel;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.MapBeregningsgrunnlagFraVLTilRegel;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.output.BeregningsgrunnlagRegelResultat;
import no.nav.folketrygdloven.kalkulator.output.RegelSporingAggregat;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagPeriodeRegelType;
import no.nav.fpsak.nare.evaluation.Evaluation;

@ApplicationScoped
public class OmfordelBeregningsgrunnlagTjeneste {

    private MapBeregningsgrunnlagFraVLTilRegel mapBeregningsgrunnlagFraVLTilRegel;
    private final MapBeregningsgrunnlagFraRegelTilVLFordel mapBeregningsgrunnlagFraRegelTilVL = new MapBeregningsgrunnlagFraRegelTilVLFordel();

    public OmfordelBeregningsgrunnlagTjeneste() {
        // CDI
    }

    @Inject
    public OmfordelBeregningsgrunnlagTjeneste(MapBeregningsgrunnlagFraVLTilRegel mapBeregningsgrunnlagFraVLTilRegel) {
        this.mapBeregningsgrunnlagFraVLTilRegel = mapBeregningsgrunnlagFraVLTilRegel;
    }

    public BeregningsgrunnlagRegelResultat omfordel(BeregningsgrunnlagInput input, BeregningsgrunnlagDto beregningsgrunnlag) {
        var regelPerioder = mapBeregningsgrunnlagFraVLTilRegel.mapTilFordelingsregel(input.getKoblingReferanse(), beregningsgrunnlag, input);
        String regelinput = toJson(regelPerioder);
        List<RegelResultat> regelResultater = new ArrayList<>();
        for (BeregningsgrunnlagPeriode periode : regelPerioder) {
            RegelFordelBeregningsgrunnlag regel = new RegelFordelBeregningsgrunnlag(periode);
            Evaluation evaluation = regel.evaluer(periode);
            regelResultater.add(RegelmodellOversetter.getRegelResultat(evaluation, regelinput));
        }
        BeregningsgrunnlagDto nyttBeregningsgrunnlag = mapBeregningsgrunnlagFraRegelTilVL.map(regelPerioder, regelResultater, beregningsgrunnlag);
        List<Intervall> perioder = nyttBeregningsgrunnlag.getBeregningsgrunnlagPerioder().stream().map(BeregningsgrunnlagPeriodeDto::getPeriode).collect(Collectors.toList());
        return new BeregningsgrunnlagRegelResultat(nyttBeregningsgrunnlag,
                new RegelSporingAggregat(mapRegelsporingPerioder(regelResultater, perioder, BeregningsgrunnlagPeriodeRegelType.FORDEL)));
    }

    private static String toJson(List<BeregningsgrunnlagPeriode> regelPerioder) {
        return JsonMapper.toJson(regelPerioder, BeregningsgrunnlagFeil::kanIkkeSerialisereRegelinput);
    }

}
