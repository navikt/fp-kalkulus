package no.nav.folketrygdloven.kalkulator.ytelse.frisinn;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.RegelResultat;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.fastsett.Beregningsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.fastsett.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.output.BeregningVilkårResultat;
import no.nav.folketrygdloven.kalkulator.output.BeregningsgrunnlagRegelResultat;
import no.nav.folketrygdloven.kalkulator.steg.fullføre.ytelse.utbgrad.FullføreBeregningsgrunnlagUtbgrad;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.regelmodelloversetter.KalkulusRegler;

public class FullføreBeregningsgrunnlagFRISINN extends FullføreBeregningsgrunnlagUtbgrad {

    private final VurderBeregningsgrunnlagTjenesteFRISINN vurderBeregningsgrunnlagTjeneste = new VurderBeregningsgrunnlagTjenesteFRISINN();

    public FullføreBeregningsgrunnlagFRISINN() {
        super();
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
        List<RegelResultat> regelResultater = kjørRegelFullførberegningsgrunnlag(beregningsgrunnlagRegel);

        leggTilSporingerForFinnGrenseverdi(input, sporingerFinnGrenseverdi, regelResultater);

        return regelResultater;
    }

    private List<BeregningVilkårResultat> vurderVilkår(BeregningsgrunnlagInput bgInput) {
        BeregningsgrunnlagRegelResultat beregningsgrunnlagRegelResultat = vurderBeregningsgrunnlagTjeneste.vurderBeregningsgrunnlag(bgInput, bgInput.getBeregningsgrunnlagGrunnlag());
        return beregningsgrunnlagRegelResultat.getVilkårsresultat();
    }

    @Override
    protected List<String> kjørRegelFinnGrenseverdi(Beregningsgrunnlag beregningsgrunnlagRegel) {
        return beregningsgrunnlagRegel.getBeregningsgrunnlagPerioder().stream()
                .map(periode -> KalkulusRegler.finnGrenseverdi(periode).sporing().sporing())
                .collect(Collectors.toList());
    }


    @Override
    protected List<RegelResultat> kjørRegelFullførberegningsgrunnlag(Beregningsgrunnlag beregningsgrunnlagRegel) {
        List<RegelResultat> regelResultater = new ArrayList<>();
        for (BeregningsgrunnlagPeriode periode : beregningsgrunnlagRegel.getBeregningsgrunnlagPerioder()) {
            regelResultater.add(KalkulusRegler.fullføreBeregningsgrunnlagFRISINN(periode));
        }
        return regelResultater;
    }

    private List<String> finnGrenseverdi(Beregningsgrunnlag beregningsgrunnlagRegel, List<BeregningVilkårResultat> beregningVilkårResultatListe, FrisinnGrunnlag frisinnGrunnlag) {
        List<Intervall> søknadsperioder = FinnSøknadsperioder.finnSøknadsperioder(frisinnGrunnlag);
        return beregningsgrunnlagRegel.getBeregningsgrunnlagPerioder().stream()
                .map(periode -> {
                    Boolean erVilkårOppfylt = erVilkårOppfyltForSøknadsperiode(beregningVilkårResultatListe, søknadsperioder, periode);
                    BeregningsgrunnlagPeriode.oppdater(periode).medErVilkårOppfylt(erVilkårOppfylt);
                    return KalkulusRegler.finnGrenseverdiFRISINN(periode).sporing().sporing();
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


