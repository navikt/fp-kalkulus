package no.nav.folketrygdloven.kalkulator.ytelse.frisinn;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.folketrygdloven.beregningsgrunnlag.RegelmodellOversetter;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.RegelResultat;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.Beregningsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrArbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.vurder.frisinn.RegelVurderBeregningsgrunnlagFRISINN;
import no.nav.folketrygdloven.kalkulator.FagsakYtelseTypeRef;
import no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.FrisinnGrunnlag;
import no.nav.folketrygdloven.kalkulator.VurderBeregningsgrunnlagTjeneste;
import no.nav.folketrygdloven.kalkulator.adapter.regelmodelltilvl.MapBeregningsgrunnlagFraRegelTilVL;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.MapBeregningsgrunnlagFraVLTilRegel;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.output.BeregningAksjonspunktResultat;
import no.nav.folketrygdloven.kalkulator.output.BeregningsgrunnlagRegelResultat;
import no.nav.fpsak.nare.evaluation.Evaluation;

@ApplicationScoped
@FagsakYtelseTypeRef("FRISINN")
public class VurderBeregningsgrunnlagTjenesteFRISINN extends VurderBeregningsgrunnlagTjeneste {

    public VurderBeregningsgrunnlagTjenesteFRISINN() {
        // CDI
    }

    @Inject
    public VurderBeregningsgrunnlagTjenesteFRISINN(MapBeregningsgrunnlagFraVLTilRegel mapBeregningsgrunnlagFraVLTilRegel) {
        super(mapBeregningsgrunnlagFraVLTilRegel);
    }

    @Override
    public BeregningsgrunnlagRegelResultat vurderBeregningsgrunnlag(BeregningsgrunnlagInput input, BeregningsgrunnlagGrunnlagDto oppdatertGrunnlag) {
        // Oversetter foreslått Beregningsgrunnlag -> regelmodell
        var beregningsgrunnlagRegel = mapBeregningsgrunnlagFraVLTilRegel.map(input, oppdatertGrunnlag);
        if (!(input.getYtelsespesifiktGrunnlag() instanceof FrisinnGrunnlag)) {
            throw new IllegalStateException("Har ikke FRISINN grunnlag når frisinnvilkår skal vurderes");
        }
        FrisinnGrunnlag frisinnGrunnlag = input.getYtelsespesifiktGrunnlag();
        settSøktYtelseForStatus(beregningsgrunnlagRegel, AktivitetStatus.FL, frisinnGrunnlag.getSøkerYtelseForFrilans());
        settSøktYtelseForStatus(beregningsgrunnlagRegel, AktivitetStatus.SN, frisinnGrunnlag.getSøkerYtelseForNæring());

        List<RegelResultat> regelResultater = kjørRegel(beregningsgrunnlagRegel);
        BeregningsgrunnlagDto beregningsgrunnlag = MapBeregningsgrunnlagFraRegelTilVL.mapVurdertBeregningsgrunnlag(regelResultater, oppdatertGrunnlag.getBeregningsgrunnlag().orElse(null));
        List<BeregningAksjonspunktResultat> aksjonspunkter = Collections.emptyList();
        boolean vilkårOppfylt = erVilkårOppfylt(regelResultater);
        BeregningsgrunnlagRegelResultat beregningsgrunnlagRegelResultat = new BeregningsgrunnlagRegelResultat(beregningsgrunnlag, aksjonspunkter);
        beregningsgrunnlagRegelResultat.setVilkårOppfylt(vilkårOppfylt);
        return beregningsgrunnlagRegelResultat;
    }

    @Override
    protected List<RegelResultat> kjørRegel(Beregningsgrunnlag beregningsgrunnlagRegel) {
        String jsonInput = toJson(beregningsgrunnlagRegel);
        // Evaluerer hver BeregningsgrunnlagPeriode fra foreslått Beregningsgrunnlag
        List<RegelResultat> regelResultater = new ArrayList<>();
        for (BeregningsgrunnlagPeriode periode : beregningsgrunnlagRegel.getBeregningsgrunnlagPerioder()) {
            RegelVurderBeregningsgrunnlagFRISINN regel = new RegelVurderBeregningsgrunnlagFRISINN(periode);
            Evaluation evaluation = regel.evaluer(periode);
            regelResultater.add(RegelmodellOversetter.getRegelResultat(evaluation, jsonInput));
        }
        return regelResultater;
    }


    public static void settSøktYtelseForStatus(Beregningsgrunnlag beregningsgrunnlagRegel, AktivitetStatus status, boolean erSøktYtelseFor) {
        beregningsgrunnlagRegel.getBeregningsgrunnlagPerioder().forEach(bgPeriode -> {
            if (AktivitetStatus.FL.equals(status)) {
                getBGArbeidsforhold(bgPeriode).stream()
                        .filter(BeregningsgrunnlagPrArbeidsforhold::erFrilanser)
                        .findFirst()
                        .ifPresent(bgFrilans -> bgFrilans.setErSøktYtelseFor(erSøktYtelseFor));
            }
            if (AktivitetStatus.SN.equals(status)) {
                BeregningsgrunnlagPrStatus snAndel = bgPeriode.getBeregningsgrunnlagPrStatus(AktivitetStatus.SN);
                if (snAndel != null) {
                    snAndel.setErSøktYtelseFor(erSøktYtelseFor);
                }
            }
        });
    }

    private static List<BeregningsgrunnlagPrArbeidsforhold> getBGArbeidsforhold(BeregningsgrunnlagPeriode bgPeriode) {
        return Optional.ofNullable(bgPeriode.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL))
                .map(BeregningsgrunnlagPrStatus::getArbeidsforhold)
                .orElse(Collections.emptyList());
    }


}
