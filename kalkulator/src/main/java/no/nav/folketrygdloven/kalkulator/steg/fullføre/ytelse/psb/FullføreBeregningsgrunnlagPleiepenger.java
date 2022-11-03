package no.nav.folketrygdloven.kalkulator.steg.fullføre.ytelse.psb;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import no.nav.folketrygdloven.kalkulator.FagsakYtelseTypeRef;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.fastsett.MapBeregningsgrunnlagFraVLTilRegel;
import no.nav.folketrygdloven.kalkulator.steg.fullføre.ytelse.utbgrad.FullføreBeregningsgrunnlagUtbgrad;
import no.nav.folketrygdloven.kalkulus.kodeverk.FagsakYtelseType;

@FagsakYtelseTypeRef(FagsakYtelseType.PLEIEPENGER_SYKT_BARN)
@FagsakYtelseTypeRef(FagsakYtelseType.OPPLÆRINGSPENGER)
@FagsakYtelseTypeRef(FagsakYtelseType.PLEIEPENGER_NÆRSTÅENDE)
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
