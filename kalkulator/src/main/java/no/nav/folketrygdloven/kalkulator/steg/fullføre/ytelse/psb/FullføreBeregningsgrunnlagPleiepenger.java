package no.nav.folketrygdloven.kalkulator.steg.fullføre.ytelse.psb;

import java.util.List;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.fastsett.Beregningsgrunnlag;
import no.nav.folketrygdloven.kalkulator.steg.fullføre.ytelse.utbgrad.FullføreBeregningsgrunnlagUtbgrad;
import no.nav.folketrygdloven.regelmodelloversetter.KalkulusRegler;

public class FullføreBeregningsgrunnlagPleiepenger extends FullføreBeregningsgrunnlagUtbgrad {

    public FullføreBeregningsgrunnlagPleiepenger() {
        super();
    }

    @Override
    protected List<String> kjørRegelFinnGrenseverdi(Beregningsgrunnlag beregningsgrunnlagRegel) {
        return beregningsgrunnlagRegel.getBeregningsgrunnlagPerioder().stream()
                .map(periode -> KalkulusRegler.finnGrenseverdi(periode).getRegelSporing().sporing())
                .collect(Collectors.toList());
    }


}
