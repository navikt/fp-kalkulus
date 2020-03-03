package no.nav.folketrygdloven.kalkulus.håndtering.faktafordeling;


import static no.nav.folketrygdloven.kalkulus.håndtering.mapping.OppdatererDtoMapper.mapFordelBeregningsgrunnlagDto;

import javax.enterprise.context.ApplicationScoped;

import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulus.håndtering.BeregningHåndterer;
import no.nav.folketrygdloven.kalkulus.håndtering.DtoTilServiceAdapter;
import no.nav.folketrygdloven.kalkulus.håndtering.v1.fordeling.FaktaOmFordelingHåndteringDto;
import no.nav.folketrygdloven.kalkulus.håndtering.v1.fordeling.FordelBeregningsgrunnlagDto;

@ApplicationScoped
@DtoTilServiceAdapter(dto = FordelBeregningsgrunnlagDto.class, adapter = BeregningHåndterer.class)
public class FordelBeregningsgrunnlagHåndterer implements BeregningHåndterer<FaktaOmFordelingHåndteringDto> {

    @Override
    public BeregningsgrunnlagGrunnlagDto håndter(FaktaOmFordelingHåndteringDto dto, BeregningsgrunnlagInput beregningsgrunnlagInput) {
        return no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.FordelBeregningsgrunnlagHåndterer.håndter(mapFordelBeregningsgrunnlagDto(dto.getFordelBeregningsgrunnlagDto()), beregningsgrunnlagInput);
    }

}
