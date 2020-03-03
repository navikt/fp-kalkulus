package no.nav.folketrygdloven.kalkulus.håndtering.faktaberegning;


import static no.nav.folketrygdloven.kalkulus.håndtering.mapping.OppdatererDtoMapper.mapFastsettBeregningsgrunnlagATFLDto;

import javax.enterprise.context.ApplicationScoped;

import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulus.håndtering.BeregningHåndterer;
import no.nav.folketrygdloven.kalkulus.håndtering.DtoTilServiceAdapter;
import no.nav.folketrygdloven.kalkulus.håndtering.v1.foreslå.FastsettBeregningsgrunnlagATFLHåndteringDto;

@ApplicationScoped
@DtoTilServiceAdapter(dto = FastsettBeregningsgrunnlagATFLHåndteringDto.class, adapter = BeregningHåndterer.class)
public class FastsettBeregningsgrunnlagATFLHåndterer implements BeregningHåndterer<FastsettBeregningsgrunnlagATFLHåndteringDto> {

    @Override
    public BeregningsgrunnlagGrunnlagDto håndter(FastsettBeregningsgrunnlagATFLHåndteringDto dto, BeregningsgrunnlagInput beregningsgrunnlagInput) {
        return no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.FastsettBeregningsgrunnlagATFLHåndterer.håndter(beregningsgrunnlagInput, mapFastsettBeregningsgrunnlagATFLDto(dto));
    }

}
