package no.nav.folketrygdloven.kalkulator.steg.fordeling.omfordeling;

import static no.nav.folketrygdloven.kalkulator.adapter.regelmodelltilvl.MapRegelSporingFraRegelTilVL.mapRegelsporingPerioder;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.folketrygdloven.beregningsgrunnlag.RegelmodellOversetter;
import no.nav.folketrygdloven.beregningsgrunnlag.fordel.andelsmessig.RegelFordelBeregningsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.fordel.andelsmessig.modell.FordelAndelModell;
import no.nav.folketrygdloven.beregningsgrunnlag.fordel.andelsmessig.modell.FordelPeriodeModell;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.RegelResultat;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.kalkulator.BeregningsgrunnlagFeil;
import no.nav.folketrygdloven.kalkulator.JsonMapper;
import no.nav.folketrygdloven.kalkulator.adapter.regelmodelltilvl.MapBeregningsgrunnlagFraRegelTilVLFordel;
import no.nav.folketrygdloven.kalkulator.adapter.regelmodelltilvl.MapFraFordelingsmodell;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.MapBeregningsgrunnlagFraVLTilRegel;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.MapTilFordelingsmodell;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.output.BeregningsgrunnlagRegelResultat;
import no.nav.folketrygdloven.kalkulator.output.RegelSporingAggregat;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagPeriodeRegelType;
import no.nav.folketrygdloven.kalkulus.kodeverk.FagsakYtelseType;
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
        // Kjører i første omgang nye fordelingsregler kun for K14, siden dette brukes mye mer av K9 og det derfor er fint å verifisere funksjonalitet i K14 først
        // Etter verifisering kan alle ytelser bruke andelsmessig fordeling og innhold i else kan slettes
        if (erK14YtelseSomBereges(input.getFagsakYtelseType())) {
            return fordelAndelsmessig(input, beregningsgrunnlag);
        } else {
            return fordelUtenHensynTilAndel(input, beregningsgrunnlag);
        }
    }

    private BeregningsgrunnlagRegelResultat fordelUtenHensynTilAndel(BeregningsgrunnlagInput input, BeregningsgrunnlagDto beregningsgrunnlag) {
        var regelPerioder = mapBeregningsgrunnlagFraVLTilRegel.mapTilFordelingsregel(input.getKoblingReferanse(), beregningsgrunnlag, input);
        String regelinput = toJson(regelPerioder);
        List<RegelResultat> regelResultater = new ArrayList<>();
        for (BeregningsgrunnlagPeriode periode : regelPerioder) {
            no.nav.folketrygdloven.beregningsgrunnlag.fordel.RegelFordelBeregningsgrunnlag regel = new no.nav.folketrygdloven.beregningsgrunnlag.fordel.RegelFordelBeregningsgrunnlag(periode);
            Evaluation evaluation = regel.evaluer(periode);
            regelResultater.add(RegelmodellOversetter.getRegelResultat(evaluation, regelinput));
        }
        BeregningsgrunnlagDto nyttBeregningsgrunnlag = mapBeregningsgrunnlagFraRegelTilVL.map(regelPerioder, regelResultater, beregningsgrunnlag);
        List<Intervall> perioder = nyttBeregningsgrunnlag.getBeregningsgrunnlagPerioder().stream().map(BeregningsgrunnlagPeriodeDto::getPeriode).collect(Collectors.toList());
        return new BeregningsgrunnlagRegelResultat(nyttBeregningsgrunnlag,
                new RegelSporingAggregat(mapRegelsporingPerioder(regelResultater, perioder, BeregningsgrunnlagPeriodeRegelType.FORDEL)));
    }

    private BeregningsgrunnlagRegelResultat fordelAndelsmessig(BeregningsgrunnlagInput input, BeregningsgrunnlagDto beregningsgrunnlag) {
        var inputPerioder = MapTilFordelingsmodell.map(beregningsgrunnlag, input);
        var regelinput = toJsonAndelsmessig(inputPerioder);
        List<RegelResultat> regelResultater = new ArrayList<>();
        var outputPerioder = new ArrayList<FordelPeriodeModell>();
        for (FordelPeriodeModell inputPeriode : inputPerioder) {
            List<FordelAndelModell> outputAndeler = new ArrayList<>();
            RegelFordelBeregningsgrunnlag regel = new RegelFordelBeregningsgrunnlag();
            Evaluation evaluation = regel.evaluer(inputPeriode, outputAndeler);
            outputPerioder.add(new FordelPeriodeModell(inputPeriode.getBgPeriode(), outputAndeler));
            regelResultater.add(RegelmodellOversetter.getRegelResultat(evaluation, regelinput));
        }
        var fordeltBG = MapFraFordelingsmodell.map(outputPerioder, regelResultater, beregningsgrunnlag);
        List<Intervall> perioder = fordeltBG.getBeregningsgrunnlagPerioder().stream().map(BeregningsgrunnlagPeriodeDto::getPeriode).collect(Collectors.toList());
        return new BeregningsgrunnlagRegelResultat(fordeltBG,
                new RegelSporingAggregat(mapRegelsporingPerioder(regelResultater, perioder, BeregningsgrunnlagPeriodeRegelType.FORDEL)));
    }

    private boolean erK14YtelseSomBereges(FagsakYtelseType fagsakYtelseType) {
        return Set.of(FagsakYtelseType.FORELDREPENGER, FagsakYtelseType.SVANGERSKAPSPENGER).contains(fagsakYtelseType);
    }

    private static String toJson(List<BeregningsgrunnlagPeriode> regelPerioder) {
        return JsonMapper.toJson(regelPerioder, BeregningsgrunnlagFeil::kanIkkeSerialisereRegelinput);
    }

    private static String toJsonAndelsmessig(List<FordelPeriodeModell> regelPerioder) {
        return JsonMapper.toJson(regelPerioder, BeregningsgrunnlagFeil::kanIkkeSerialisereRegelinput);
    }

}
