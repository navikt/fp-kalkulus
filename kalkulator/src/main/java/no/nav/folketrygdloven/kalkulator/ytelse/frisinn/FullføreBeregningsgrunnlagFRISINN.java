package no.nav.folketrygdloven.kalkulator.ytelse.frisinn;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.RegelResultat;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.Beregningsgrunnlag;
import no.nav.folketrygdloven.kalkulator.FagsakYtelseTypeRef;
import no.nav.folketrygdloven.kalkulator.FullføreBeregningsgrunnlag;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.MapBeregningsgrunnlagFraVLTilRegel;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;

@FagsakYtelseTypeRef("FRISINN")
@ApplicationScoped
public class FullføreBeregningsgrunnlagFRISINN extends FullføreBeregningsgrunnlag {

    public FullføreBeregningsgrunnlagFRISINN() {
        // CDI
    }

    @Inject
    public FullføreBeregningsgrunnlagFRISINN(MapBeregningsgrunnlagFraVLTilRegel mapBeregningsgrunnlagFraVLTilRegel) {
        super(mapBeregningsgrunnlagFraVLTilRegel);
    }

    @Override
    protected List<RegelResultat> evaluerRegelmodell(Beregningsgrunnlag beregningsgrunnlagRegel, BeregningsgrunnlagInput bgInput) {
        throw new IllegalStateException("Mangler beregningsregler for ytelsetype FRISINN");
    }

}
