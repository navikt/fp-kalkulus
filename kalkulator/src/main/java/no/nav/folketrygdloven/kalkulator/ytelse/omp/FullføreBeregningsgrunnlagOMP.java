package no.nav.folketrygdloven.kalkulator.ytelse.omp;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.folketrygdloven.kalkulator.FagsakYtelseTypeRef;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.MapBeregningsgrunnlagFraVLTilRegel;
import no.nav.folketrygdloven.kalkulator.ytelse.utbgradytelse.FullføreBeregningsgrunnlagUtbgrad;

@FagsakYtelseTypeRef("OMP")
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
