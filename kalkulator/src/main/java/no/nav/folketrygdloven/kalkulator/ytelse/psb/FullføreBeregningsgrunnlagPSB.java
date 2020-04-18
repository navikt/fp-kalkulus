package no.nav.folketrygdloven.kalkulator.ytelse.psb;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.folketrygdloven.kalkulator.FagsakYtelseTypeRef;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.MapBeregningsgrunnlagFraVLTilRegel;
import no.nav.folketrygdloven.kalkulator.ytelse.utbgradytelse.FullføreBeregningsgrunnlagUtbgrad;

@FagsakYtelseTypeRef("PSB")
@ApplicationScoped
public class FullføreBeregningsgrunnlagPSB extends FullføreBeregningsgrunnlagUtbgrad {

    public FullføreBeregningsgrunnlagPSB() {
        // CDI
    }

    @Inject
    public FullføreBeregningsgrunnlagPSB(MapBeregningsgrunnlagFraVLTilRegel mapBeregningsgrunnlagFraVLTilRegel) {
        super(mapBeregningsgrunnlagFraVLTilRegel);
    }

}
