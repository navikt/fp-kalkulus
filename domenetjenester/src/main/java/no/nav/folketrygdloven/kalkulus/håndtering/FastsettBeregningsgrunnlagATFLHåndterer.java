package no.nav.folketrygdloven.kalkulus.håndtering;


import static no.nav.folketrygdloven.kalkulus.mappers.OppdatererDtoMapper.mapFastsettBeregningsgrunnlagATFLDto;

import javax.enterprise.context.ApplicationScoped;

import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulus.håndtering.v1.fakta.FastsettBeregningsgrunnlagATFLDto;

@ApplicationScoped
@DtoTilServiceAdapter(dto = FastsettBeregningsgrunnlagATFLDto.class, adapter = BeregningHåndterer.class)
public class FastsettBeregningsgrunnlagATFLHåndterer implements BeregningHåndterer<FastsettBeregningsgrunnlagATFLDto> {

    @Override
    public BeregningsgrunnlagGrunnlagDto håndter(FastsettBeregningsgrunnlagATFLDto dto, BeregningsgrunnlagInput beregningsgrunnlagInput) {
        return no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.FastsettBeregningsgrunnlagATFLHåndterer.håndter(beregningsgrunnlagInput, mapFastsettBeregningsgrunnlagATFLDto(dto));
    }

}
