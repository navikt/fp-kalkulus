package no.nav.folketrygdloven.kalkulus.håndtering.faktafordeling;


import static no.nav.folketrygdloven.kalkulus.håndtering.mapping.OppdatererDtoMapper.mapFordelBeregningsgrunnlagDto;

import javax.enterprise.context.ApplicationScoped;

import no.nav.folketrygdloven.kalkulator.input.HåndterBeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulus.håndtering.BeregningHåndterer;
import no.nav.folketrygdloven.kalkulus.håndtering.DtoTilServiceAdapter;
import no.nav.folketrygdloven.kalkulus.håndtering.HåndteringResultat;
import no.nav.folketrygdloven.kalkulus.håndtering.UtledEndring;
import no.nav.folketrygdloven.kalkulus.håndtering.v1.fordeling.FaktaOmFordelingHåndteringDto;

@ApplicationScoped
@DtoTilServiceAdapter(dto = FaktaOmFordelingHåndteringDto.class, adapter = BeregningHåndterer.class)
public class FordelBeregningsgrunnlagHåndterer implements BeregningHåndterer<FaktaOmFordelingHåndteringDto> {

    @Override
    public HåndteringResultat håndter(FaktaOmFordelingHåndteringDto dto, HåndterBeregningsgrunnlagInput beregningsgrunnlagInput) {
        BeregningsgrunnlagGrunnlagDto nyttGrunnlag = no.nav.folketrygdloven.kalkulator.avklaringsbehov.FordelBeregningsgrunnlagHåndterer.håndter(mapFordelBeregningsgrunnlagDto(dto.getFordelBeregningsgrunnlagDto()), beregningsgrunnlagInput);
        BeregningsgrunnlagGrunnlagDto grunnlagFraSteg = beregningsgrunnlagInput.getBeregningsgrunnlagGrunnlag();
        var endring = UtledEndring.utled(nyttGrunnlag,  grunnlagFraSteg, beregningsgrunnlagInput.getForrigeGrunnlagFraHåndteringTilstand(), dto, beregningsgrunnlagInput.getIayGrunnlag());
        return new HåndteringResultat(nyttGrunnlag, endring);
    }

}
