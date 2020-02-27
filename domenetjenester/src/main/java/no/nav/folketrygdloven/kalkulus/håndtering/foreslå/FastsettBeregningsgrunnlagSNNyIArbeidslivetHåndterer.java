package no.nav.folketrygdloven.kalkulus.håndtering.foreslå;


import static no.nav.folketrygdloven.kalkulus.mappers.OppdatererDtoMapper.mapFastsettBruttoBeregningsgrunnlagSNforNyIArbeidslivetDto;
import static no.nav.folketrygdloven.kalkulus.mappers.OppdatererDtoMapper.mapFordelBeregningsgrunnlagDto;

import javax.enterprise.context.ApplicationScoped;

import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulus.håndtering.BeregningHåndterer;
import no.nav.folketrygdloven.kalkulus.håndtering.DtoTilServiceAdapter;
import no.nav.folketrygdloven.kalkulus.håndtering.v1.fordeling.FaktaOmFordelingHåndteringDto;
import no.nav.folketrygdloven.kalkulus.håndtering.v1.fordeling.FordelBeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulus.håndtering.v1.foreslå.FastsettBeregningsgrunnlagSNNyIArbeidslivetHåndteringDto;

@ApplicationScoped
@DtoTilServiceAdapter(dto = FastsettBeregningsgrunnlagSNNyIArbeidslivetHåndteringDto.class, adapter = BeregningHåndterer.class)
public class FastsettBeregningsgrunnlagSNNyIArbeidslivetHåndterer implements BeregningHåndterer<FastsettBeregningsgrunnlagSNNyIArbeidslivetHåndteringDto> {

    @Override
    public BeregningsgrunnlagGrunnlagDto håndter(FastsettBeregningsgrunnlagSNNyIArbeidslivetHåndteringDto dto, BeregningsgrunnlagInput beregningsgrunnlagInput) {
        return no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.FastsettBruttoBeregningsgrunnlagSNforNyIArbeidslivetHåndterer.oppdater(beregningsgrunnlagInput, mapFastsettBruttoBeregningsgrunnlagSNforNyIArbeidslivetDto(dto.getFastsettBruttoBeregningsgrunnlagSNforNyIArbeidslivetDto()));
    }

}
