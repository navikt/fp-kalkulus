package no.nav.folketrygdloven.kalkulator.ytelse.frisinn;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.folketrygdloven.beregningsgrunnlag.RegelmodellOversetter;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.RegelResultat;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.Beregningsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.ytelse.frisinn.RegelFinnGrenseverdiFRISINN;
import no.nav.folketrygdloven.beregningsgrunnlag.ytelse.frisinn.RegelFullføreBeregningsgrunnlagFRISINN;
import no.nav.folketrygdloven.kalkulator.FagsakYtelseTypeRef;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.MapBeregningsgrunnlagFraVLTilRegel;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.input.StegProsesseringInput;
import no.nav.folketrygdloven.kalkulator.output.BeregningVilkårResultat;
import no.nav.folketrygdloven.kalkulator.output.BeregningsgrunnlagRegelResultat;
import no.nav.folketrygdloven.kalkulator.steg.fullføre.ytelse.utbgrad.FullføreBeregningsgrunnlagUtbgrad;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.fpsak.nare.evaluation.Evaluation;

@FagsakYtelseTypeRef("FRISINN")
@ApplicationScoped
public class FullføreBeregningsgrunnlagFRISINN extends FullføreBeregningsgrunnlagUtbgrad {

    private VurderBeregningsgrunnlagTjenesteFRISINN vurderBeregningsgrunnlagTjeneste;

    public FullføreBeregningsgrunnlagFRISINN() {
        // CDI
    }

    @Inject
    public FullføreBeregningsgrunnlagFRISINN(MapBeregningsgrunnlagFraVLTilRegel mapBeregningsgrunnlagFraVLTilRegel,
                                             @FagsakYtelseTypeRef("FRISINN") VurderBeregningsgrunnlagTjenesteFRISINN vurderBeregningsgrunnlagTjeneste) {
        super(mapBeregningsgrunnlagFraVLTilRegel);
        this.vurderBeregningsgrunnlagTjeneste = vurderBeregningsgrunnlagTjeneste;
    }

    @Override
    protected List<RegelResultat> evaluerRegelmodell(Beregningsgrunnlag beregningsgrunnlagRegel, BeregningsgrunnlagInput bgInput) {
        String input = toJson(beregningsgrunnlagRegel);

        // Vurdere vilkår
        List<BeregningVilkårResultat> beregningVilkårResultatListe = vurderVilkår(bgInput);

        FrisinnGrunnlag frisinnGrunnlag = bgInput.getYtelsespesifiktGrunnlag();
        // Finner grenseverdi
        List<String> sporingerFinnGrenseverdi = finnGrenseverdi(beregningsgrunnlagRegel, beregningVilkårResultatListe, frisinnGrunnlag);

        // Fullfører grenseverdi
        String inputNrTo = toJson(beregningsgrunnlagRegel);
        List<RegelResultat> regelResultater = kjørRegelFullførberegningsgrunnlag(beregningsgrunnlagRegel, inputNrTo);

        leggTilSporingerForFinnGrenseverdi(input, sporingerFinnGrenseverdi, regelResultater);

        return regelResultater;
    }

    private List<BeregningVilkårResultat> vurderVilkår(BeregningsgrunnlagInput bgInput) {
        BeregningsgrunnlagRegelResultat beregningsgrunnlagRegelResultat = vurderBeregningsgrunnlagTjeneste.vurderBeregningsgrunnlag(bgInput, bgInput.getBeregningsgrunnlagGrunnlag());
        return beregningsgrunnlagRegelResultat.getVilkårsresultat();
    }

    @Override
    protected List<RegelResultat> kjørRegelFullførberegningsgrunnlag(Beregningsgrunnlag beregningsgrunnlagRegel, String input) {
        List<RegelResultat> regelResultater = new ArrayList<>();
        for (BeregningsgrunnlagPeriode periode : beregningsgrunnlagRegel.getBeregningsgrunnlagPerioder()) {
            RegelFullføreBeregningsgrunnlagFRISINN regel = new RegelFullføreBeregningsgrunnlagFRISINN(periode);
            Evaluation evaluation = regel.evaluer(periode);
            regelResultater.add(RegelmodellOversetter.getRegelResultat(evaluation, input));
        }
        return regelResultater;
    }

    private List<String> finnGrenseverdi(Beregningsgrunnlag beregningsgrunnlagRegel, List<BeregningVilkårResultat> beregningVilkårResultatListe, FrisinnGrunnlag frisinnGrunnlag) {
        List<Intervall> søknadsperioder = FinnSøknadsperioder.finnSøknadsperioder(frisinnGrunnlag);
        return beregningsgrunnlagRegel.getBeregningsgrunnlagPerioder().stream()
                .map(periode -> {
                    Boolean erVilkårOppfylt = erVilkårOppfyltForSøknadsperiode(beregningVilkårResultatListe, søknadsperioder, periode);
                    BeregningsgrunnlagPeriode.builder(periode).medErVilkårOppfylt(erVilkårOppfylt);
                    RegelFinnGrenseverdiFRISINN regel = new RegelFinnGrenseverdiFRISINN(periode);
                    Evaluation evaluering = regel.evaluer(periode);
                    return RegelmodellOversetter.getSporing(evaluering);
                }).collect(Collectors.toList());
    }

    private Boolean erVilkårOppfyltForSøknadsperiode(List<BeregningVilkårResultat> beregningVilkårResultatListe, List<Intervall> søknadsperioder, BeregningsgrunnlagPeriode periode) {
        Optional<Intervall> søknadsperiode = søknadsperioder.stream().filter(p -> p.inkluderer(periode.getPeriodeFom())).findFirst();
        List<BeregningVilkårResultat> vilkårResultat = søknadsperiode.map(p -> finnVilkårResultatForPeriode(beregningVilkårResultatListe, p)).orElse(List.of());
        return vilkårResultat.stream().anyMatch(BeregningVilkårResultat::getErVilkårOppfylt);
    }

    private List<BeregningVilkårResultat> finnVilkårResultatForPeriode(List<BeregningVilkårResultat> beregningVilkårResultatListe, Intervall periode) {
        return beregningVilkårResultatListe.stream().filter(vp -> periode.overlapper(vp.getPeriode()))
                .collect(Collectors.toList());
    }

}


