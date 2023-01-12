package no.nav.folketrygdloven.kalkulator.steg.fordeling.omfordeling;

import static no.nav.folketrygdloven.kalkulator.adapter.regelmodelltilvl.MapRegelSporingFraRegelTilVL.mapRegelsporingPerioder;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import no.nav.folketrygdloven.beregningsgrunnlag.RegelmodellOversetter;
import no.nav.folketrygdloven.beregningsgrunnlag.fordel.RegelFordelBeregningsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.fordel.modell.FordelAndelModell;
import no.nav.folketrygdloven.beregningsgrunnlag.fordel.modell.FordelPeriodeModell;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.RegelResultat;
import no.nav.folketrygdloven.kalkulator.BeregningsgrunnlagFeil;
import no.nav.folketrygdloven.kalkulator.JsonMapper;
import no.nav.folketrygdloven.kalkulator.adapter.regelmodelltilvl.MapFraFordelingsmodell;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.MapTilFordelingsmodell;
import no.nav.folketrygdloven.kalkulator.avklaringsbehov.PerioderTilVurderingTjeneste;
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

    @Inject
    public OmfordelBeregningsgrunnlagTjeneste() {
    }

    public BeregningsgrunnlagRegelResultat omfordel(BeregningsgrunnlagInput input, BeregningsgrunnlagDto beregningsgrunnlag) {
        return fordel(input, beregningsgrunnlag);
    }

    private BeregningsgrunnlagRegelResultat fordel(BeregningsgrunnlagInput input, BeregningsgrunnlagDto beregningsgrunnlag) {
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
        List<Intervall> perioder = inputPerioder.stream()
                .map(FordelPeriodeModell::getBgPeriode)
                .map(p -> Intervall.fraOgMedTilOgMed(p.getFom(), p.getTom()))
                .collect(Collectors.toList());
        return new BeregningsgrunnlagRegelResultat(fordeltBG,
                new RegelSporingAggregat(mapRegelsporingPerioder(regelResultater, perioder, BeregningsgrunnlagPeriodeRegelType.FORDEL)));
    }

    private static String toJsonAndelsmessig(List<FordelPeriodeModell> regelPerioder) {
        return JsonMapper.toJson(regelPerioder, BeregningsgrunnlagFeil::kanIkkeSerialisereRegelinput);
    }

}
