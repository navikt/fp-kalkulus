package no.nav.folketrygdloven.kalkulator.ytelse.frisinn;

import java.util.Collections;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import no.nav.folketrygdloven.kalkulator.FagsakYtelseTypeRef;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.MapBeregningsgrunnlagFraVLTilRegel;
import no.nav.folketrygdloven.kalkulator.input.FortsettForeslåBeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.output.BeregningsgrunnlagRegelResultat;
import no.nav.folketrygdloven.kalkulator.steg.fortsettForeslå.FortsettForeslåBeregningsgrunnlag;
import no.nav.folketrygdloven.kalkulus.kodeverk.FagsakYtelseType;

/**
 * FRISINN trenger ikke kjøre dette steget. Hvis det mot formodning skjer, skal ingenting gjøres her.
 * Alt håndteres i foreslå-steget, da frisinn ikke følger samme regler som resten av ytelsene
 * og ikke trenger beregne enkelte statuser før andre.
 */
@ApplicationScoped
@FagsakYtelseTypeRef(FagsakYtelseType.FRISINN)
public class FortsettForeslåBeregningsgrunnlagFRISINN extends FortsettForeslåBeregningsgrunnlag {
    protected MapBeregningsgrunnlagFraVLTilRegel mapBeregningsgrunnlagFraVLTilRegel;

    public FortsettForeslåBeregningsgrunnlagFRISINN() {
        // CDI
    }

    @Inject
    public FortsettForeslåBeregningsgrunnlagFRISINN(MapBeregningsgrunnlagFraVLTilRegel mapBeregningsgrunnlagFraVLTilRegel) {
        this.mapBeregningsgrunnlagFraVLTilRegel = mapBeregningsgrunnlagFraVLTilRegel;
    }

    @Override
    public BeregningsgrunnlagRegelResultat foreslåBeregningsgrunnlagDel2(FortsettForeslåBeregningsgrunnlagInput input) {
        BeregningsgrunnlagDto beregningsgrunnlag = input.getBeregningsgrunnlagGrunnlag().getBeregningsgrunnlag()
                .orElseThrow(() -> new IllegalStateException("Skal ha beregningsgrunnlag her"));
        return new BeregningsgrunnlagRegelResultat(beregningsgrunnlag, Collections.emptyList());
    }
}
