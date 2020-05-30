package no.nav.folketrygdloven.kalkulator.ytelse.frisinn;

import java.math.BigDecimal;
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
import no.nav.folketrygdloven.kalkulator.VilkårTjenesteFRISINN;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.MapBeregningsgrunnlagFraVLTilRegel;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.output.BeregningVilkårResultat;
import no.nav.folketrygdloven.kalkulator.output.BeregningsgrunnlagRegelResultat;
import no.nav.folketrygdloven.kalkulator.ytelse.utbgradytelse.FullføreBeregningsgrunnlagUtbgrad;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.Vilkårsavslagsårsak;
import no.nav.fpsak.nare.evaluation.Evaluation;

@FagsakYtelseTypeRef("FRISINN")
@ApplicationScoped
public class FullføreBeregningsgrunnlagFRISINN extends FullføreBeregningsgrunnlagUtbgrad {

    private VilkårTjenesteFRISINN vilkårTjeneste;
    private VurderBeregningsgrunnlagTjenesteFRISINN vurderBeregningsgrunnlagTjeneste;

    public FullføreBeregningsgrunnlagFRISINN() {
        // CDI
    }

    @Inject
    public FullføreBeregningsgrunnlagFRISINN(MapBeregningsgrunnlagFraVLTilRegel mapBeregningsgrunnlagFraVLTilRegel,
                                             @FagsakYtelseTypeRef("FRISINN") VilkårTjenesteFRISINN vilkårTjeneste,
                                             @FagsakYtelseTypeRef("FRISINN") VurderBeregningsgrunnlagTjenesteFRISINN vurderBeregningsgrunnlagTjeneste) {
        super(mapBeregningsgrunnlagFraVLTilRegel);
        this.vilkårTjeneste = vilkårTjeneste;
        this.vurderBeregningsgrunnlagTjeneste = vurderBeregningsgrunnlagTjeneste;
    }

    @Override
    protected List<RegelResultat> evaluerRegelmodell(Beregningsgrunnlag beregningsgrunnlagRegel, BeregningsgrunnlagInput bgInput) {
        String input = toJson(beregningsgrunnlagRegel);

        // Vurdere vilkår
        List<BeregningVilkårResultat> beregningVilkårResultatListe = vurderVilkår(bgInput);
        // Finner grenseverdi
        List<String> sporingerFinnGrenseverdi = finnGrenseverdi(beregningsgrunnlagRegel, beregningVilkårResultatListe);

        // Fullfører grenseverdi
        String inputNrTo = toJson(beregningsgrunnlagRegel);
        List<RegelResultat> regelResultater = kjørRegelFullførberegningsgrunnlag(beregningsgrunnlagRegel, inputNrTo);

        leggTilSporingerForFinnGrenseverdi(input, sporingerFinnGrenseverdi, regelResultater);

        return regelResultater;
    }

    private List<BeregningVilkårResultat> vurderVilkår(BeregningsgrunnlagInput bgInput) {
        BeregningsgrunnlagRegelResultat beregningsgrunnlagRegelResultat = vurderBeregningsgrunnlagTjeneste.vurderBeregningsgrunnlag(bgInput, bgInput.getBeregningsgrunnlagGrunnlag());
        return vilkårTjeneste.lagVilkårResultatFordel(bgInput, beregningsgrunnlagRegelResultat);
    }

    protected List<RegelResultat> kjørRegelFullførberegningsgrunnlag(Beregningsgrunnlag beregningsgrunnlagRegel, String input) {
        List<RegelResultat> regelResultater = new ArrayList<>();
        for (BeregningsgrunnlagPeriode periode : beregningsgrunnlagRegel.getBeregningsgrunnlagPerioder()) {
            RegelFullføreBeregningsgrunnlagFRISINN regel = new RegelFullføreBeregningsgrunnlagFRISINN(periode);
            Evaluation evaluation = regel.evaluer(periode);
            regelResultater.add(RegelmodellOversetter.getRegelResultat(evaluation, input));
        }
        return regelResultater;
    }

    private List<String> finnGrenseverdi(Beregningsgrunnlag beregningsgrunnlagRegel, List<BeregningVilkårResultat> beregningVilkårResultatListe) {
        return beregningsgrunnlagRegel.getBeregningsgrunnlagPerioder().stream()
                .map(periode -> {
                    Optional<BeregningVilkårResultat> vilkårResultat = finnVilkårResultatForPeriode(beregningVilkårResultatListe, periode);
                    BeregningsgrunnlagPeriode.builder(periode).medErVilkårOppfylt(vilkårResultat.map(BeregningVilkårResultat::getErVilkårOppfylt).orElse(true));
                    RegelFinnGrenseverdiFRISINN regel = new RegelFinnGrenseverdiFRISINN(periode);
                    Evaluation evaluering = regel.evaluer(periode);
                    return RegelmodellOversetter.getSporing(evaluering);
                }).collect(Collectors.toList());
    }

    private Optional<BeregningVilkårResultat> finnVilkårResultatForPeriode(List<BeregningVilkårResultat> beregningVilkårResultatListe, BeregningsgrunnlagPeriode periode) {
        return beregningVilkårResultatListe.stream().filter(vp -> vp.getPeriode().inkluderer(periode.getBeregningsgrunnlagPeriode().getFom()))
                .findFirst();
    }

}


