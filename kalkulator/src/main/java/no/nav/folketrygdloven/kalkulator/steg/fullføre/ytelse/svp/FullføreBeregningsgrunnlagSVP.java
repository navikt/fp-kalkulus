package no.nav.folketrygdloven.kalkulator.steg.fullføre.ytelse.svp;

import java.util.List;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.fastsett.Beregningsgrunnlag;
import no.nav.folketrygdloven.kalkulator.steg.fullføre.ytelse.utbgrad.FullføreBeregningsgrunnlagUtbgrad;
import no.nav.folketrygdloven.regelmodelloversetter.KalkulusRegler;

public class FullføreBeregningsgrunnlagSVP extends FullføreBeregningsgrunnlagUtbgrad {

    public FullføreBeregningsgrunnlagSVP() {
        super();
    }

    @Override
    protected List<String> kjørRegelFinnGrenseverdi(Beregningsgrunnlag beregningsgrunnlagRegel) {
        return beregningsgrunnlagRegel.getBeregningsgrunnlagPerioder().stream()
                .map(periode -> KalkulusRegler.finnGrenseverdi(periode).sporing().sporing())
                .collect(Collectors.toList());
    }

}
