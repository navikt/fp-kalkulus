package no.nav.folketrygdloven.kalkulator.steg.fullføre.ytelse.svp;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.folketrygdloven.kalkulator.FagsakYtelseTypeRef;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.MapBeregningsgrunnlagFraVLTilRegel;
import no.nav.folketrygdloven.kalkulator.steg.fullføre.ytelse.utbgrad.FullføreBeregningsgrunnlagUtbgrad;

@FagsakYtelseTypeRef("SVP")
@ApplicationScoped
public class FullføreBeregningsgrunnlagSVP extends FullføreBeregningsgrunnlagUtbgrad {

    public FullføreBeregningsgrunnlagSVP() {
        // CDI
    }

    @Inject
    public FullføreBeregningsgrunnlagSVP(MapBeregningsgrunnlagFraVLTilRegel mapBeregningsgrunnlagFraVLTilRegel) {
        super(mapBeregningsgrunnlagFraVLTilRegel);
    }

}
