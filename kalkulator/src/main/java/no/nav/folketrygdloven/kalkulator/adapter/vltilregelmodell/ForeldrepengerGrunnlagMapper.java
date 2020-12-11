package no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell;

import javax.enterprise.context.ApplicationScoped;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.ytelse.YtelsesSpesifiktGrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.ytelse.fp.ForeldrepengerGrunnlag;
import no.nav.folketrygdloven.kalkulator.FagsakYtelseTypeRef;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.FaktaOmBeregningTilfelle;

@ApplicationScoped
@FagsakYtelseTypeRef("FP")
public class ForeldrepengerGrunnlagMapper implements YtelsesspesifikkRegelMapper {

    @Override
    public YtelsesSpesifiktGrunnlag map(BeregningsgrunnlagDto beregningsgrunnlag, BeregningsgrunnlagInput input) {
        boolean harVærtBesteberegnet = beregningsgrunnlag.getFaktaOmBeregningTilfeller().stream()
                .anyMatch(tilfelle -> FaktaOmBeregningTilfelle.FASTSETT_BESTEBEREGNING_FØDENDE_KVINNE.equals(FaktaOmBeregningTilfelle.fraKode(tilfelle.getKode())));
        return new ForeldrepengerGrunnlag(harVærtBesteberegnet);
    }

}
