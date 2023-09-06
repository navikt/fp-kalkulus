package no.nav.folketrygdloven.kalkulator.steg.fullføre.ytelse.utbgrad;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.RegelResultat;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.fastsett.Beregningsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.fastsett.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.kalkulator.KonfigurasjonVerdi;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.fastsett.MapBeregningsgrunnlagFraVLTilRegel;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.steg.fordeling.tilkommetInntekt.TilkommetInntektPeriodeTjeneste;
import no.nav.folketrygdloven.kalkulator.steg.fullføre.FullføreBeregningsgrunnlag;
import no.nav.folketrygdloven.regelmodelloversetter.KalkulusRegler;

@ApplicationScoped
public class FullføreBeregningsgrunnlagUtbgrad extends FullføreBeregningsgrunnlag {

    public FullføreBeregningsgrunnlagUtbgrad() {
        // CDI
    }

    @Inject
    public FullføreBeregningsgrunnlagUtbgrad(MapBeregningsgrunnlagFraVLTilRegel mapBeregningsgrunnlagFraVLTilRegel) {
        super(mapBeregningsgrunnlagFraVLTilRegel);
    }

    @Override
    protected List<RegelResultat> evaluerRegelmodell(Beregningsgrunnlag beregningsgrunnlagRegel, BeregningsgrunnlagInput bgInput) {
        String input = toJson(beregningsgrunnlagRegel);
        // Regel for å finne grenseverdi for andre gjennomkjøring
        List<String> sporingerFinnGrenseverdi = kjørRegelFinnGrenseverdi(beregningsgrunnlagRegel);

        //Andre gjennomkjøring av regel
        List<RegelResultat> regelResultater = kjørRegelFullførberegningsgrunnlag(beregningsgrunnlagRegel);

        leggTilSporingerForFinnGrenseverdi(input, sporingerFinnGrenseverdi, regelResultater);

        return regelResultater;
    }

    protected void leggTilSporingerForFinnGrenseverdi(String input, List<String> sporingerFinnGrenseverdi, List<RegelResultat> regelResultater) {
        if (regelResultater.size() == sporingerFinnGrenseverdi.size()) {
            for (int i = 0; i < regelResultater.size(); i++) {
                RegelResultat res = regelResultater.get(i);
                res.medRegelsporingFinnGrenseverdi(input, sporingerFinnGrenseverdi.get(i));
            }
        } else {
            throw new IllegalStateException("Utviklerfeil: Antall kjøringer for finn grenseverdi var ulik fastsetting.");
        }
    }

    protected List<RegelResultat> kjørRegelFullførberegningsgrunnlag(Beregningsgrunnlag beregningsgrunnlagRegel) {
        List<RegelResultat> regelResultater = new ArrayList<>();
        for (BeregningsgrunnlagPeriode periode : beregningsgrunnlagRegel.getBeregningsgrunnlagPerioder()) {
            regelResultater.add(KalkulusRegler.fullføreBeregningsgrunnlag(periode));
        }
        return regelResultater;
    }

    protected List<String> kjørRegelFinnGrenseverdi(Beregningsgrunnlag beregningsgrunnlagRegel) {
        var graderingMotInntektEnabled = KonfigurasjonVerdi.get("GRADERING_MOT_INNTEKT", false);
        return beregningsgrunnlagRegel.getBeregningsgrunnlagPerioder().stream()
                .map(periode -> {
                    if (graderingMotInntektEnabled && !periode.getPeriodeFom().isBefore(TilkommetInntektPeriodeTjeneste.FOM_DATO_GRADERING_MOT_INNTEKT)) {
                        return KalkulusRegler.finnGrenseverdiUtenFordeling(periode).getRegelSporing().sporing();
                    }
                    return KalkulusRegler.finnGrenseverdi(periode).getRegelSporing().sporing();
                })
                .collect(Collectors.toList());
    }

}
