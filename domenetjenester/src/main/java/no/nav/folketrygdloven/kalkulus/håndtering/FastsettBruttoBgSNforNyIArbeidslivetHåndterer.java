package no.nav.folketrygdloven.kalkulus.håndtering;

import javax.enterprise.context.ApplicationScoped;

import no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.FastsettBruttoBeregningsgrunnlagSNforNyIArbeidslivetHåndterer;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulus.håndtering.v1.FastsettBruttoBeregningsgrunnlagSNforNyIArbeidslivetDto;
import no.nav.folketrygdloven.kalkulus.mappers.OppdatererDtoMapper;

@ApplicationScoped
@DtoTilServiceAdapter(dto = FastsettBruttoBeregningsgrunnlagSNforNyIArbeidslivetDto.class, adapter = BeregningHåndterer.class)
public class FastsettBruttoBgSNforNyIArbeidslivetHåndterer implements BeregningHåndterer<FastsettBruttoBeregningsgrunnlagSNforNyIArbeidslivetDto> {

    @Override
    public BeregningsgrunnlagGrunnlagDto håndter(FastsettBruttoBeregningsgrunnlagSNforNyIArbeidslivetDto dto, BeregningsgrunnlagInput beregningsgrunnlagInput) {
        return FastsettBruttoBeregningsgrunnlagSNforNyIArbeidslivetHåndterer.oppdater(beregningsgrunnlagInput, OppdatererDtoMapper.mapFastsettBruttoBeregningsgrunnlagSNforNyIArbeidslivetDto(dto));
    }

}
