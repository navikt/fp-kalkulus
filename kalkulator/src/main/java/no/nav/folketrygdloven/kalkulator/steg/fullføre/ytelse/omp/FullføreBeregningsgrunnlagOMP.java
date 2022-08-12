package no.nav.folketrygdloven.kalkulator.steg.fullføre.ytelse.omp;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import no.nav.folketrygdloven.kalkulator.FagsakYtelseTypeRef;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.MapBeregningsgrunnlagFraVLTilRegel;
import no.nav.folketrygdloven.kalkulator.steg.fullføre.ytelse.utbgrad.FullføreBeregningsgrunnlagUtbgrad;
import no.nav.folketrygdloven.kalkulus.kodeverk.FagsakYtelseType;

@FagsakYtelseTypeRef(FagsakYtelseType.OMSORGSPENGER)
@ApplicationScoped
public class FullføreBeregningsgrunnlagOMP extends FullføreBeregningsgrunnlagUtbgrad {

    public FullføreBeregningsgrunnlagOMP() {
        // CDI
    }

    @Inject
    public FullføreBeregningsgrunnlagOMP(MapBeregningsgrunnlagFraVLTilRegel mapBeregningsgrunnlagFraVLTilRegel) {
        super(mapBeregningsgrunnlagFraVLTilRegel);
    }

}
