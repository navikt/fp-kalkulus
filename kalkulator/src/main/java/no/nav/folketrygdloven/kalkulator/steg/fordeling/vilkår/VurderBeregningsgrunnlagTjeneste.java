package no.nav.folketrygdloven.kalkulator.steg.fordeling.vilkår;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.folketrygdloven.beregningsgrunnlag.RegelmodellOversetter;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.RegelMerknad;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.RegelResultat;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.Beregningsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.vurder.RegelVurderBeregningsgrunnlag;
import no.nav.folketrygdloven.kalkulator.BeregningsgrunnlagFeil;
import no.nav.folketrygdloven.kalkulator.FagsakYtelseTypeRef;
import no.nav.folketrygdloven.kalkulator.JsonMapper;
import no.nav.folketrygdloven.kalkulator.adapter.regelmodelltilvl.MapRegelSporingFraRegelTilVL;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.MapBeregningsgrunnlagFraVLTilRegel;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.input.YtelsespesifiktGrunnlag;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.output.BeregningAksjonspunktResultat;
import no.nav.folketrygdloven.kalkulator.output.BeregningVilkårResultat;
import no.nav.folketrygdloven.kalkulator.output.BeregningsgrunnlagRegelResultat;
import no.nav.folketrygdloven.kalkulator.output.RegelSporingAggregat;
import no.nav.folketrygdloven.kalkulator.output.RegelSporingPeriode;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.BeregningsgrunnlagPeriodeRegelType;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.Vilkårsavslagsårsak;
import no.nav.fpsak.nare.evaluation.Evaluation;

@ApplicationScoped
@FagsakYtelseTypeRef("*")
public class VurderBeregningsgrunnlagTjeneste {

    protected static final String FOR_LAVT_BG_MERKNAD = "1041";

    protected MapBeregningsgrunnlagFraVLTilRegel mapBeregningsgrunnlagFraVLTilRegel;

    public VurderBeregningsgrunnlagTjeneste() {
        // CDI
    }

    @Inject
    public VurderBeregningsgrunnlagTjeneste(MapBeregningsgrunnlagFraVLTilRegel mapBeregningsgrunnlagFraVLTilRegel) {
        this.mapBeregningsgrunnlagFraVLTilRegel = mapBeregningsgrunnlagFraVLTilRegel;
    }

    public BeregningsgrunnlagRegelResultat vurderBeregningsgrunnlag(BeregningsgrunnlagInput input, BeregningsgrunnlagGrunnlagDto oppdatertGrunnlag) {
        // Oversetter foreslått Beregningsgrunnlag -> regelmodell
        var beregningsgrunnlagRegel = mapBeregningsgrunnlagFraVLTilRegel.map(input, oppdatertGrunnlag);
        List<RegelResultat> regelResultater = kjørRegel(input, beregningsgrunnlagRegel);
        List<BeregningAksjonspunktResultat> aksjonspunkter = Collections.emptyList();
        BeregningsgrunnlagDto beregningsgrunnlag = oppdatertGrunnlag.getBeregningsgrunnlag().orElseThrow();
        return mapTilRegelresultat(input, regelResultater, beregningsgrunnlag, aksjonspunkter);
    }

    protected BeregningsgrunnlagRegelResultat mapTilRegelresultat(BeregningsgrunnlagInput input, List<RegelResultat> regelResultater,
                                                                  BeregningsgrunnlagDto beregningsgrunnlag,
                                                                  List<BeregningAksjonspunktResultat> aksjonspunkter) {
        List<Intervall> perioder = beregningsgrunnlag.getBeregningsgrunnlagPerioder().stream().map(BeregningsgrunnlagPeriodeDto::getPeriode).collect(Collectors.toList());
        List<RegelSporingPeriode> regelsporinger = MapRegelSporingFraRegelTilVL.mapRegelsporingPerioder(regelResultater, perioder, BeregningsgrunnlagPeriodeRegelType.VILKÅR_VURDERING);
        BeregningsgrunnlagRegelResultat beregningsgrunnlagRegelResultat = new BeregningsgrunnlagRegelResultat(
                beregningsgrunnlag,
                aksjonspunkter,
                new RegelSporingAggregat(regelsporinger));
        beregningsgrunnlagRegelResultat.setVilkårsresultat(mapTilVilkårResultatListe(regelResultater, beregningsgrunnlag, input.getYtelsespesifiktGrunnlag()));
        return beregningsgrunnlagRegelResultat;
    }

    protected List<BeregningVilkårResultat> mapTilVilkårResultatListe(List<RegelResultat> regelResultater,
                                                                      BeregningsgrunnlagDto beregningsgrunnlag,
                                                                      YtelsespesifiktGrunnlag ytelsesSpesifiktGrunnlag) {
        List<BeregningVilkårResultat> vilkårsResultatListe = new ArrayList<>();
        Iterator<RegelResultat> regelResultatIterator = regelResultater.iterator();
        for (var periode : beregningsgrunnlag.getBeregningsgrunnlagPerioder()) {
            BeregningVilkårResultat vilkårResultat = lagVilkårResultatForPeriode(regelResultatIterator.next(), periode.getPeriode());
            vilkårsResultatListe.add(vilkårResultat);
        }
        return vilkårsResultatListe;
    }

    private BeregningVilkårResultat lagVilkårResultatForPeriode(RegelResultat regelResultat, Intervall periode) {
        boolean erVilkårOppfylt = regelResultat.getMerknader().stream().map(RegelMerknad::getMerknadKode)
                .noneMatch(FOR_LAVT_BG_MERKNAD::equals);
        return new BeregningVilkårResultat(erVilkårOppfylt, erVilkårOppfylt ? null : Vilkårsavslagsårsak.FOR_LAVT_BG, periode);
    }

    protected List<RegelResultat> kjørRegel(BeregningsgrunnlagInput input, Beregningsgrunnlag beregningsgrunnlagRegel) {
        String jsonInput = toJson(beregningsgrunnlagRegel);
        // Evaluerer hver BeregningsgrunnlagPeriode fra foreslått Beregningsgrunnlag
        List<RegelResultat> regelResultater = new ArrayList<>();
        for (BeregningsgrunnlagPeriode periode : beregningsgrunnlagRegel.getBeregningsgrunnlagPerioder()) {
            RegelVurderBeregningsgrunnlag regel = new RegelVurderBeregningsgrunnlag(periode);
            Evaluation evaluation = regel.evaluer(periode);
            regelResultater.add(RegelmodellOversetter.getRegelResultat(evaluation, jsonInput));
        }
        return regelResultater;
    }

    protected String toJson(no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.Beregningsgrunnlag beregningsgrunnlagRegel) {
        return JsonMapper.toJson(beregningsgrunnlagRegel, BeregningsgrunnlagFeil.FEILFACTORY::kanIkkeSerialisereRegelinput);
    }
}
