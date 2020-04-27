package no.nav.folketrygdloven.kalkulator.adapter.regelmodelltilvl;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.SplittetPeriode;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;

@ApplicationScoped
public class MapFastsettBeregningsgrunnlagPerioderFraRegelTilVLUtenAndelendring extends MapFastsettBeregningsgrunnlagPerioderFraRegelTilVL {

    @Override
    protected void mapAndeler(BeregningsgrunnlagDto nyttBeregningsgrunnlag, SplittetPeriode splittetPeriode,
                              List<BeregningsgrunnlagPrStatusOgAndelDto> andelListe, BeregningsgrunnlagPeriodeDto beregningsgrunnlagPeriode) {
        andelListe.forEach(eksisterendeAndel -> mapEksisterendeAndel(beregningsgrunnlagPeriode, eksisterendeAndel));
    }

    private void mapEksisterendeAndel(BeregningsgrunnlagPeriodeDto beregningsgrunnlagPeriode,
                                      BeregningsgrunnlagPrStatusOgAndelDto eksisterendeAndel) {
        BeregningsgrunnlagPrStatusOgAndelDto.Builder andelBuilder = BeregningsgrunnlagPrStatusOgAndelDto.kopier(eksisterendeAndel);
        andelBuilder.build(beregningsgrunnlagPeriode);
    }
}
