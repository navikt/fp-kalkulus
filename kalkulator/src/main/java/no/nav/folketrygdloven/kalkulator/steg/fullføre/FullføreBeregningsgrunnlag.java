package no.nav.folketrygdloven.kalkulator.steg.fullføre;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.RegelResultat;
import no.nav.folketrygdloven.kalkulator.BeregningsgrunnlagFeil;
import no.nav.folketrygdloven.kalkulator.JsonMapper;
import no.nav.folketrygdloven.kalkulator.adapter.regelmodelltilvl.MapBeregningsgrunnlagFraRegelTilVL;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.MapBeregningsgrunnlagFraVLTilRegel;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.output.BeregningsgrunnlagRegelResultat;
import no.nav.folketrygdloven.kalkulator.output.RegelSporingAggregat;
import no.nav.folketrygdloven.kalkulator.output.RegelSporingPeriode;
import no.nav.folketrygdloven.kalkulator.steg.BeregningsgrunnlagVerifiserer;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagPeriodeRegelType;


public abstract class FullføreBeregningsgrunnlag {
    private MapBeregningsgrunnlagFraVLTilRegel mapBeregningsgrunnlagFraVLTilRegel;
    private MapBeregningsgrunnlagFraRegelTilVL mapBeregningsgrunnlagFraRegelTilVL = new MapBeregningsgrunnlagFraRegelTilVL();

    public FullføreBeregningsgrunnlag() {
        // CDI
    }

    public FullføreBeregningsgrunnlag(MapBeregningsgrunnlagFraVLTilRegel mapBeregningsgrunnlagFraVLTilRegel) {
        this.mapBeregningsgrunnlagFraVLTilRegel = mapBeregningsgrunnlagFraVLTilRegel;
        //for CDI proxy
    }

    public BeregningsgrunnlagRegelResultat fullføreBeregningsgrunnlag(BeregningsgrunnlagInput input) {
        var grunnlag = input.getBeregningsgrunnlagGrunnlag();

        // Oversetter foreslått Beregningsgrunnlag -> regelmodell
        var beregningsgrunnlagRegel = mapBeregningsgrunnlagFraVLTilRegel.map(input, grunnlag);

        // Evaluerer hver BeregningsgrunnlagPeriode fra foreslått Beregningsgrunnlag
        List<RegelResultat> regelResultater = evaluerRegelmodell(beregningsgrunnlagRegel, input);

        // Oversett endelig resultat av regelmodell til fastsatt Beregningsgrunnlag  (+ spore input -> evaluation)
        BeregningsgrunnlagDto beregningsgrunnlag = grunnlag.getBeregningsgrunnlag().orElse(null);
        BeregningsgrunnlagDto fastsattBeregningsgrunnlag = mapBeregningsgrunnlagFraRegelTilVL.mapFastsettBeregningsgrunnlag(beregningsgrunnlagRegel, beregningsgrunnlag);

        List<RegelSporingPeriode> regelsporinger = mapRegelSporinger(regelResultater, fastsattBeregningsgrunnlag);
        BeregningsgrunnlagVerifiserer.verifiserFastsattBeregningsgrunnlag(fastsattBeregningsgrunnlag, input.getYtelsespesifiktGrunnlag());
        return new BeregningsgrunnlagRegelResultat(fastsattBeregningsgrunnlag, new RegelSporingAggregat(regelsporinger));
    }

    private List<RegelSporingPeriode> mapRegelSporinger(List<RegelResultat> regelResultater, BeregningsgrunnlagDto fastsattBeregningsgrunnlag) {
        return mapRegelsporingPerioder(
                regelResultater,
                fastsattBeregningsgrunnlag.getBeregningsgrunnlagPerioder().stream().map(BeregningsgrunnlagPeriodeDto::getPeriode).collect(Collectors.toList()));
    }

    protected abstract List<RegelResultat> evaluerRegelmodell(no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.Beregningsgrunnlag beregningsgrunnlagRegel, BeregningsgrunnlagInput input);

    protected static String toJson(no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.Beregningsgrunnlag beregningsgrunnlagRegel) {
        return JsonMapper.toJson(beregningsgrunnlagRegel, BeregningsgrunnlagFeil.FEILFACTORY::kanIkkeSerialisereRegelinput);
    }

    /**
     * Mapper liste med sporing pr periode til regelresultat. Listen med resultater må vere like lang som listen med perioder.
     *
     * @param regelResultater Liste med resultat pr periode
     * @param perioder        perioder som regel er kjørt for
     * @return Mappet regelsporing for perioder
     */
    public static List<RegelSporingPeriode> mapRegelsporingPerioder(List<RegelResultat> regelResultater,
                                                                    List<Intervall> perioder) {
        if (regelResultater.size() != perioder.size()) {
            throw new IllegalArgumentException("Listene må vere like lange.");
        }
        List<RegelSporingPeriode> regelsporingPerioder = new ArrayList<>();
        var resultatIterator = regelResultater.iterator();
        for (var periode : perioder) {
            RegelResultat resultat = resultatIterator.next();
            var hovedRegelResultat = resultat.getRegelSporing();
            regelsporingPerioder.add(new RegelSporingPeriode(hovedRegelResultat.getSporing(), hovedRegelResultat.getInput(), periode, BeregningsgrunnlagPeriodeRegelType.FASTSETT));
            resultat.getRegelSporingFinnGrenseverdi()
                    .map(res -> new RegelSporingPeriode(hovedRegelResultat.getSporing(), hovedRegelResultat.getInput(), periode, BeregningsgrunnlagPeriodeRegelType.FINN_GRENSEVERDI))
                    .ifPresent(regelsporingPerioder::add);
        }
        return regelsporingPerioder;
    }
}
