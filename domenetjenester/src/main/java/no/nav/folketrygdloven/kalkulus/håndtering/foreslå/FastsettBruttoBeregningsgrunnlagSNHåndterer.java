package no.nav.folketrygdloven.kalkulus.håndtering.foreslå;

import javax.enterprise.context.ApplicationScoped;

import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulus.håndtering.BeregningHåndterer;
import no.nav.folketrygdloven.kalkulus.håndtering.DtoTilServiceAdapter;
import no.nav.folketrygdloven.kalkulus.håndtering.v1.foreslå.FastsettBruttoBeregningsgrunnlagSNHåndteringDto;
import no.nav.folketrygdloven.kalkulus.håndtering.mapping.OppdatererDtoMapper;

@ApplicationScoped
@DtoTilServiceAdapter(dto = FastsettBruttoBeregningsgrunnlagSNHåndteringDto.class, adapter = BeregningHåndterer.class)
class FastsettBruttoBeregningsgrunnlagSNHåndterer implements BeregningHåndterer<FastsettBruttoBeregningsgrunnlagSNHåndteringDto> {

    @Override
    public BeregningsgrunnlagGrunnlagDto håndter(FastsettBruttoBeregningsgrunnlagSNHåndteringDto dto, BeregningsgrunnlagInput input) {
        return no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.FastsettBruttoBeregningsgrunnlagSNHåndterer.håndter(input, OppdatererDtoMapper.mapFastsettBruttoBeregningsgrunnlagSNDto(dto.getFastsettBruttoBeregningsgrunnlagSNDto()));
    }

}
