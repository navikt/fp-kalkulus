package no.nav.folketrygdloven.kalkulator.ytelse.fp;

import javax.enterprise.context.ApplicationScoped;

import no.nav.folketrygdloven.kalkulator.FagsakYtelseTypeRef;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.periodisering.MapFastsettBeregningsgrunnlagPerioderFraVLTilRegelRefusjonOgGradering;

@FagsakYtelseTypeRef("FP")
@ApplicationScoped
class MapFastsettBeregningsgrunnlagPerioderFraVLTilRegelRefusjonOgGraderingFP extends MapFastsettBeregningsgrunnlagPerioderFraVLTilRegelRefusjonOgGradering {

    // For CDI (for håndtere at annotation propageres til subklasser)

}
