package no.nav.folketrygdloven.kalkulus.håndtering.faktafordeling;

import jakarta.enterprise.context.ApplicationScoped;
import no.nav.folketrygdloven.kalkulator.avklaringsbehov.VurderTilkommetInntektTjeneste;
import no.nav.folketrygdloven.kalkulator.input.HåndterBeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulus.håndtering.BeregningHåndterer;
import no.nav.folketrygdloven.kalkulus.håndtering.DtoTilServiceAdapter;
import no.nav.folketrygdloven.kalkulus.håndtering.HåndteringResultat;
import no.nav.folketrygdloven.kalkulus.håndtering.UtledEndring;
import no.nav.folketrygdloven.kalkulus.håndtering.v1.fordeling.VurderTilkommetInntektHåndteringDto;

@ApplicationScoped
@DtoTilServiceAdapter(dto = VurderTilkommetInntektHåndteringDto.class, adapter = BeregningHåndterer.class)
class VurderTilkommetInntektHåndterer implements BeregningHåndterer<VurderTilkommetInntektHåndteringDto> {

    @Override
    public HåndteringResultat håndter(VurderTilkommetInntektHåndteringDto dto, HåndterBeregningsgrunnlagInput beregningsgrunnlagInput) {
        BeregningsgrunnlagGrunnlagDto nyttGrunnlag = VurderTilkommetInntektTjeneste.løsAvklaringsbehov(dto, beregningsgrunnlagInput);
        BeregningsgrunnlagGrunnlagDto grunnlagFraSteg = beregningsgrunnlagInput.getBeregningsgrunnlagGrunnlag();
        var endring = UtledEndring.utled(nyttGrunnlag, grunnlagFraSteg, beregningsgrunnlagInput.getForrigeGrunnlagFraHåndteringTilstand(), dto, beregningsgrunnlagInput.getIayGrunnlag());
        return new HåndteringResultat(nyttGrunnlag, endring);
    }

}
