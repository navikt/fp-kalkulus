package no.nav.folketrygdloven.kalkulator.ytelse.frisinn;

import java.util.Collections;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import no.nav.folketrygdloven.kalkulator.FagsakYtelseTypeRef;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.MapBeregningsgrunnlagFraVLTilRegel;
import no.nav.folketrygdloven.kalkulator.input.ForeslåBeregningsgrunnlagDel2Input;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.output.BeregningsgrunnlagRegelResultat;
import no.nav.folketrygdloven.kalkulator.steg.foreslåDel2.ForeslåBeregningsgrunnlagDel2;
import no.nav.folketrygdloven.kalkulus.kodeverk.FagsakYtelseType;

/**
 * FRISINN trenger ikke kjøre dette steget. Hvis det mot formodning skjer, skal ingenting gjøres her.
 * Alt håndteres i foreslå-steget, da frisinn ikke følger samme regler som resten av ytelsene
 * og ikke trenger beregne enkelte statuser før andre.
 */
@ApplicationScoped
@FagsakYtelseTypeRef(FagsakYtelseType.FRISINN)
public class ForeslåBeregningsgrunnlagDel2FRISINN extends ForeslåBeregningsgrunnlagDel2 {
    protected MapBeregningsgrunnlagFraVLTilRegel mapBeregningsgrunnlagFraVLTilRegel;

    public ForeslåBeregningsgrunnlagDel2FRISINN() {
        // CDI
    }

    @Inject
    public ForeslåBeregningsgrunnlagDel2FRISINN(MapBeregningsgrunnlagFraVLTilRegel mapBeregningsgrunnlagFraVLTilRegel) {
        this.mapBeregningsgrunnlagFraVLTilRegel = mapBeregningsgrunnlagFraVLTilRegel;
    }

    @Override
    public BeregningsgrunnlagRegelResultat foreslåBeregningsgrunnlagDel2(ForeslåBeregningsgrunnlagDel2Input input) {
        BeregningsgrunnlagDto beregningsgrunnlag = input.getBeregningsgrunnlagGrunnlag().getBeregningsgrunnlag()
                .orElseThrow(() -> new IllegalStateException("Skal ha beregningsgrunnlag her"));
        return new BeregningsgrunnlagRegelResultat(beregningsgrunnlag, Collections.emptyList());
    }
}
