package no.nav.folketrygdloven.kalkulus.håndtering.foreslå;

import static no.nav.folketrygdloven.kalkulus.håndtering.mapping.OppdatererDtoMapper.mapVurderVarigEndringEllerNyoppstartetSNDto;

import javax.enterprise.context.ApplicationScoped;

import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulus.håndtering.BeregningHåndterer;
import no.nav.folketrygdloven.kalkulus.håndtering.DtoTilServiceAdapter;
import no.nav.folketrygdloven.kalkulus.håndtering.v1.foreslå.VurderVarigEndringEllerNyoppstartetSNHåndteringDto;

@ApplicationScoped
@DtoTilServiceAdapter(dto = VurderVarigEndringEllerNyoppstartetSNHåndteringDto.class, adapter = BeregningHåndterer.class)
class VurderVarigEndretNyoppstartetSNHåndterer implements BeregningHåndterer<VurderVarigEndringEllerNyoppstartetSNHåndteringDto> {

    @Override
    public BeregningsgrunnlagGrunnlagDto håndter(VurderVarigEndringEllerNyoppstartetSNHåndteringDto dto, BeregningsgrunnlagInput beregningsgrunnlagInput) {
        return no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.VurderVarigEndretNyoppstartetSNHåndterer.håndter(beregningsgrunnlagInput, mapVurderVarigEndringEllerNyoppstartetSNDto(dto.getVurderVarigEndringEllerNyoppstartetSNDto()));
    }

}
