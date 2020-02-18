package no.nav.folketrygdloven.kalkulus.håndtering;

import static no.nav.folketrygdloven.kalkulus.mappers.OppdatererDtoMapper.mapVurderVarigEndringEllerNyoppstartetSNDto;

import javax.enterprise.context.ApplicationScoped;

import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulus.håndtering.v1.fakta.VurderVarigEndringEllerNyoppstartetSNDto;

@ApplicationScoped
@DtoTilServiceAdapter(dto = VurderVarigEndringEllerNyoppstartetSNDto.class, adapter = BeregningHåndterer.class)
class VurderVarigEndretNyoppstartetSNHåndterer implements BeregningHåndterer<VurderVarigEndringEllerNyoppstartetSNDto> {

    @Override
    public BeregningsgrunnlagGrunnlagDto håndter(VurderVarigEndringEllerNyoppstartetSNDto dto, BeregningsgrunnlagInput beregningsgrunnlagInput) {
        return no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.VurderVarigEndretNyoppstartetSNHåndterer.håndter(beregningsgrunnlagInput, mapVurderVarigEndringEllerNyoppstartetSNDto(dto));
    }

}
