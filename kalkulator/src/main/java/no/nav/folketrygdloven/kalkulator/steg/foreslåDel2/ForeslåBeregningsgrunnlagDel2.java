package no.nav.folketrygdloven.kalkulator.steg.foreslåDel2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import no.nav.folketrygdloven.beregningsgrunnlag.RegelmodellOversetter;
import no.nav.folketrygdloven.beregningsgrunnlag.fortsettForeslå.RegelFortsettForeslåBeregningsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.RegelResultat;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.Beregningsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.kalkulator.BeregningsgrunnlagFeil;
import no.nav.folketrygdloven.kalkulator.FagsakYtelseTypeRef;
import no.nav.folketrygdloven.kalkulator.JsonMapper;
import no.nav.folketrygdloven.kalkulator.adapter.regelmodelltilvl.MapBeregningsgrunnlagFraRegelTilVL;
import no.nav.folketrygdloven.kalkulator.adapter.regelmodelltilvl.MapRegelSporingFraRegelTilVL;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.MapBeregningsgrunnlagFraVLTilRegel;
import no.nav.folketrygdloven.kalkulator.input.ForeslåBeregningsgrunnlagDel2Input;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.output.BeregningAvklaringsbehovResultat;
import no.nav.folketrygdloven.kalkulator.output.BeregningsgrunnlagRegelResultat;
import no.nav.folketrygdloven.kalkulator.output.RegelSporingAggregat;
import no.nav.folketrygdloven.kalkulator.output.RegelSporingPeriode;
import no.nav.folketrygdloven.kalkulator.steg.BeregningsgrunnlagVerifiserer;
import no.nav.folketrygdloven.kalkulator.steg.foreslå.AvklaringsbehovUtlederForeslåBeregning;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagPeriodeRegelType;
import no.nav.fpsak.nare.evaluation.Evaluation;

@ApplicationScoped
@FagsakYtelseTypeRef()
public class ForeslåBeregningsgrunnlagDel2 {
    protected MapBeregningsgrunnlagFraVLTilRegel mapBeregningsgrunnlagFraVLTilRegel;
    private final MapBeregningsgrunnlagFraRegelTilVL mapBeregningsgrunnlagFraRegelTilVL = new MapBeregningsgrunnlagFraRegelTilVL();

    public ForeslåBeregningsgrunnlagDel2() {
        // CDI
    }

    @Inject
    public ForeslåBeregningsgrunnlagDel2(MapBeregningsgrunnlagFraVLTilRegel mapBeregningsgrunnlagFraVLTilRegel) {
        this.mapBeregningsgrunnlagFraVLTilRegel = mapBeregningsgrunnlagFraVLTilRegel;
    }

    public BeregningsgrunnlagRegelResultat foreslåBeregningsgrunnlagDel2(ForeslåBeregningsgrunnlagDel2Input input) {
        BeregningsgrunnlagGrunnlagDto grunnlag = input.getBeregningsgrunnlagGrunnlag();
        BeregningsgrunnlagDto beregningsgrunnlag = grunnlag.getBeregningsgrunnlag()
                .orElseThrow(() -> new IllegalStateException("Skal ha beregningsgrunnlag her"));


        // Hvis gammelt foreslå-steg er kjørt vil andeler med status SN og MS allerede være foreslått og vi ikke kjøre foreslå del 2
        if (snOgMsErAlleredeForeslått(beregningsgrunnlag)) {
            return new BeregningsgrunnlagRegelResultat(beregningsgrunnlag, Collections.emptyList());
        }


        // Oversetter initielt Beregningsgrunnlag -> regelmodell
        Beregningsgrunnlag regelmodellBeregningsgrunnlag = mapBeregningsgrunnlagFraVLTilRegel.map(input, grunnlag);
        String jsonInput = toJson(regelmodellBeregningsgrunnlag);
        List<RegelResultat> regelResultater = new ArrayList<>();
        for (BeregningsgrunnlagPeriode periode : regelmodellBeregningsgrunnlag.getBeregningsgrunnlagPerioder()) {
            Evaluation evaluation = new RegelFortsettForeslåBeregningsgrunnlag(periode).evaluer(periode);
            regelResultater.add(RegelmodellOversetter.getRegelResultat(evaluation, jsonInput));
        }

        // Oversett endelig resultat av regelmodell til foreslått Beregningsgrunnlag  (+ spore input -> evaluation)
        BeregningsgrunnlagDto foreslåttBeregningsgrunnlag = mapBeregningsgrunnlagFraRegelTilVL.mapForeslåBeregningsgrunnlag(regelmodellBeregningsgrunnlag, beregningsgrunnlag);

        verifiserBeregningsgrunnlag(foreslåttBeregningsgrunnlag);

        List<BeregningAvklaringsbehovResultat> avklaringsbehov = AvklaringsbehovUtlederForeslåBeregning.utledAvklaringsbehov(input, regelResultater);
        List<RegelSporingPeriode> regelsporinger = MapRegelSporingFraRegelTilVL.mapRegelsporingPerioder(
                regelResultater,
                foreslåttBeregningsgrunnlag.getBeregningsgrunnlagPerioder().stream().map(BeregningsgrunnlagPeriodeDto::getPeriode).collect(Collectors.toList()),
                BeregningsgrunnlagPeriodeRegelType.FORESLÅ_2);
        return new BeregningsgrunnlagRegelResultat(foreslåttBeregningsgrunnlag, avklaringsbehov,
                new RegelSporingAggregat(regelsporinger));
    }

    private boolean snOgMsErAlleredeForeslått(BeregningsgrunnlagDto beregningsgrunnlag) {
        var erMS = beregningsgrunnlag.getAktivitetStatuser().stream()
                .anyMatch(status -> status.getAktivitetStatus().equals(AktivitetStatus.MILITÆR_ELLER_SIVIL));
        var erSN = beregningsgrunnlag.getAktivitetStatuser().stream()
                .anyMatch(status -> status.getAktivitetStatus().erSelvstendigNæringsdrivende());
        if (!erMS && !erSN) {
            return false;
        }
        var førstePeriode = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        var snAndelErForeslått = !erSN || førstePeriode.getBeregningsgrunnlagPrStatusOgAndelList().stream().anyMatch(andel -> andel.getAktivitetStatus().equals(AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE)
                && andel.getBruttoPrÅr() != null);
        var msAndelErForeslått = !erMS || førstePeriode.getBeregningsgrunnlagPrStatusOgAndelList().stream().anyMatch(andel -> andel.getAktivitetStatus().equals(AktivitetStatus.MILITÆR_ELLER_SIVIL)
                && andel.getBruttoPrÅr() != null);
        if (snAndelErForeslått && msAndelErForeslått) {
            return true;
        }
        if (!snAndelErForeslått && !msAndelErForeslått) {
            return false;
        }
        String msg = String.format("FEIL: SN og MS er ikke i samme tilstand på vei inn i foreslå 2 steget. Er sn foreslått: %s. Er MS foreslått: %s", snAndelErForeslått, msAndelErForeslått);
        throw new IllegalStateException(msg);
    }

    private void verifiserBeregningsgrunnlag(BeregningsgrunnlagDto foreslåttBeregningsgrunnlag) {
        BeregningsgrunnlagVerifiserer.verifiserForeslåttBeregningsgrunnlagDel2(foreslåttBeregningsgrunnlag);
    }

    protected String toJson(Beregningsgrunnlag beregningsgrunnlagRegel) {
        return JsonMapper.toJson(beregningsgrunnlagRegel, BeregningsgrunnlagFeil::kanIkkeSerialisereRegelinput);
    }

}
