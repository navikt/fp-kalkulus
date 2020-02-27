package no.nav.folketrygdloven.kalkulus.håndtering.faktaberegning;

import javax.enterprise.context.ApplicationScoped;

import no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.FastsettBruttoBeregningsgrunnlagSNHåndterer;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulus.håndtering.BeregningHåndterer;
import no.nav.folketrygdloven.kalkulus.håndtering.DtoTilServiceAdapter;
import no.nav.folketrygdloven.kalkulus.håndtering.v1.foreslå.FastsettBruttoBeregningsgrunnlagSNDto;
import no.nav.folketrygdloven.kalkulus.mappers.OppdatererDtoMapper;

@ApplicationScoped
@DtoTilServiceAdapter(dto = FastsettBruttoBeregningsgrunnlagSNDto.class, adapter = BeregningHåndterer.class)
public class FastsettBruttoBgSNHåndterer implements BeregningHåndterer<FastsettBruttoBeregningsgrunnlagSNDto> {

    @Override
    public BeregningsgrunnlagGrunnlagDto håndter(FastsettBruttoBeregningsgrunnlagSNDto dto, BeregningsgrunnlagInput beregningsgrunnlagInput) {
        return FastsettBruttoBeregningsgrunnlagSNHåndterer.håndter(beregningsgrunnlagInput, OppdatererDtoMapper.mapFastsettBruttoBeregningsgrunnlagSNDto(dto));
    }

}
