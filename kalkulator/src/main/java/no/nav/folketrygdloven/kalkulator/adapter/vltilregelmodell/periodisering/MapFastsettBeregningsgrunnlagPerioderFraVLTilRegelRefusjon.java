package no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.periodisering;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Gradering;
import no.nav.folketrygdloven.kalkulator.gradering.AndelGradering;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDto;

@ApplicationScoped
public class MapFastsettBeregningsgrunnlagPerioderFraVLTilRegelRefusjon extends MapFastsettBeregningsgrunnlagPerioderFraVLTilRegelRefusjonOgGradering {

    public MapFastsettBeregningsgrunnlagPerioderFraVLTilRegelRefusjon() {
        // For CDI
    }

    @Override
    protected List<Gradering> mapGradering(Collection<AndelGradering> andelGraderinger, YrkesaktivitetDto ya) {
        return Collections.emptyList();
    }

}
