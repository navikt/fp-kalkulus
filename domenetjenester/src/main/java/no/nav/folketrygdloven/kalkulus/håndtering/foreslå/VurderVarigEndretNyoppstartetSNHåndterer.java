package no.nav.folketrygdloven.kalkulus.håndtering.foreslå;

import static no.nav.folketrygdloven.kalkulus.håndtering.mapping.OppdatererDtoMapper.mapVurderVarigEndringEllerNyoppstartetSNDto;

import javax.enterprise.context.ApplicationScoped;

import no.nav.folketrygdloven.kalkulator.input.HåndterBeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulus.håndtering.BeregningHåndterer;
import no.nav.folketrygdloven.kalkulus.håndtering.DtoTilServiceAdapter;
import no.nav.folketrygdloven.kalkulus.håndtering.HåndteringResultat;
import no.nav.folketrygdloven.kalkulus.håndtering.v1.foreslå.VurderVarigEndringEllerNyoppstartetSNHåndteringDto;

@ApplicationScoped
@DtoTilServiceAdapter(dto = VurderVarigEndringEllerNyoppstartetSNHåndteringDto.class, adapter = BeregningHåndterer.class)
class VurderVarigEndretNyoppstartetSNHåndterer implements BeregningHåndterer<VurderVarigEndringEllerNyoppstartetSNHåndteringDto> {

    @Override
    public HåndteringResultat håndter(VurderVarigEndringEllerNyoppstartetSNHåndteringDto dto, HåndterBeregningsgrunnlagInput beregningsgrunnlagInput) {
        BeregningsgrunnlagGrunnlagDto nyttGrunnlag = no.nav.folketrygdloven.kalkulator.aksjonspunkt.VurderVarigEndretNyoppstartetSNHåndterer.håndter(beregningsgrunnlagInput, mapVurderVarigEndringEllerNyoppstartetSNDto(dto.getVurderVarigEndringEllerNyoppstartetSNDto()));
        // TODO Lag endringresultat
        return new HåndteringResultat(nyttGrunnlag, null);
    }

}
