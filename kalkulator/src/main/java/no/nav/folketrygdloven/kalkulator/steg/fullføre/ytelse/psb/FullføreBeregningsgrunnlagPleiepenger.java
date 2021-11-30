package no.nav.folketrygdloven.kalkulator.steg.fullføre.ytelse.psb;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.folketrygdloven.kalkulator.FagsakYtelseTypeRef;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.MapBeregningsgrunnlagFraVLTilRegel;
import no.nav.folketrygdloven.kalkulator.steg.fullføre.ytelse.utbgrad.FullføreBeregningsgrunnlagUtbgrad;

@FagsakYtelseTypeRef("PSB")
@FagsakYtelseTypeRef("PPN")
@ApplicationScoped
public class FullføreBeregningsgrunnlagPleiepenger extends FullføreBeregningsgrunnlagUtbgrad {

    public FullføreBeregningsgrunnlagPleiepenger() {
        // CDI
    }

    @Inject
    public FullføreBeregningsgrunnlagPleiepenger(MapBeregningsgrunnlagFraVLTilRegel mapBeregningsgrunnlagFraVLTilRegel) {
        super(mapBeregningsgrunnlagFraVLTilRegel);
    }

}
