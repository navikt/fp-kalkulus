package no.nav.folketrygdloven.kalkulator.steg.fullføre.ytelse.utbgrad;

import java.util.ArrayList;
import java.util.List;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.RegelResultat;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.fastsett.Beregningsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.fastsett.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.fastsett.MapBeregningsgrunnlagFraVLTilRegel;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.steg.fullføre.FullføreBeregningsgrunnlag;
import no.nav.folketrygdloven.regelmodelloversetter.KalkulusRegler;

public abstract class FullføreBeregningsgrunnlagUtbgrad extends FullføreBeregningsgrunnlag {

    public FullføreBeregningsgrunnlagUtbgrad() {
        // CDI
    }

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

    protected abstract List<String> kjørRegelFinnGrenseverdi(Beregningsgrunnlag beregningsgrunnlagRegel);

}
