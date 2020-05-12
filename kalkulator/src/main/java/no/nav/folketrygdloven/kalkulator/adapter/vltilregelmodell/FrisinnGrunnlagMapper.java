package no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell;

import javax.enterprise.context.ApplicationScoped;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.ytelse.YtelsesSpesifiktGrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.ytelse.frisinn.FrisinnGrunnlag;
import no.nav.folketrygdloven.kalkulator.FagsakYtelseTypeRef;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;

@ApplicationScoped
@FagsakYtelseTypeRef("FRISINN")
public class FrisinnGrunnlagMapper implements YtelsesspesifikkRegelMapper {

    @Override
    public YtelsesSpesifiktGrunnlag map(BeregningsgrunnlagDto beregningsgrunnlagDto, BeregningsgrunnlagInput input) {
        if (!(input.getYtelsespesifiktGrunnlag() instanceof no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.FrisinnGrunnlag)) {
            throw new IllegalStateException("Mangler frisinngrunnlag for frisinnberegning");
        }
        no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.FrisinnGrunnlag frisinnGrunnlag = input.getYtelsespesifiktGrunnlag();
        return new FrisinnGrunnlag(frisinnGrunnlag.getSøkerYtelseForFrilans());
    }

}
