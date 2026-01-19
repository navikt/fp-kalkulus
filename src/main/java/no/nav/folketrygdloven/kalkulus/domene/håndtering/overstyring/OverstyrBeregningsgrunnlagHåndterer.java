package no.nav.folketrygdloven.kalkulus.domene.håndtering.overstyring;

import static no.nav.folketrygdloven.kalkulus.domene.håndtering.mapping.OppdatererDtoMapper.mapOverstyrBeregningsgrunnlagDto;

import jakarta.enterprise.context.ApplicationScoped;
import no.nav.folketrygdloven.kalkulator.avklaringsbehov.BeregningFaktaOgOverstyringHåndterer;
import no.nav.folketrygdloven.kalkulator.input.HåndterBeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulus.domene.håndtering.BeregningHåndterer;
import no.nav.folketrygdloven.kalkulus.domene.håndtering.DtoTilServiceAdapter;
import no.nav.folketrygdloven.kalkulus.domene.håndtering.HåndteringResultat;
import no.nav.folketrygdloven.kalkulus.domene.håndtering.UtledEndring;
import no.nav.foreldrepenger.kalkulus.kontrakt.request.håndtering.overstyring.OverstyrBeregningsgrunnlagHåndteringDto;

@ApplicationScoped
@DtoTilServiceAdapter(dto = OverstyrBeregningsgrunnlagHåndteringDto.class, adapter = BeregningHåndterer.class)
class OverstyrBeregningsgrunnlagHåndterer implements BeregningHåndterer<OverstyrBeregningsgrunnlagHåndteringDto> {

    public OverstyrBeregningsgrunnlagHåndterer() {
        // CDI
    }

    @Override
    public HåndteringResultat håndter(OverstyrBeregningsgrunnlagHåndteringDto dto, HåndterBeregningsgrunnlagInput beregningsgrunnlagInput) {
        var nyttGrunnlag = BeregningFaktaOgOverstyringHåndterer.håndterMedOverstyring(beregningsgrunnlagInput, mapOverstyrBeregningsgrunnlagDto(dto));
        var forrigeGrunnlag = beregningsgrunnlagInput.getForrigeGrunnlagFraHåndteringTilstand();
        var grunnlagFraSteg = beregningsgrunnlagInput.getBeregningsgrunnlagGrunnlag();
        var endring = UtledEndring.standard().utled(nyttGrunnlag, grunnlagFraSteg, forrigeGrunnlag, dto, beregningsgrunnlagInput.getIayGrunnlag());
        return new HåndteringResultat(nyttGrunnlag, endring);
    }

}
