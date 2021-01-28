package no.nav.folketrygdloven.kalkulus.håndtering.foreslå;


import static no.nav.folketrygdloven.kalkulus.håndtering.mapping.OppdatererDtoMapper.mapFastsettBruttoBeregningsgrunnlagSNforNyIArbeidslivetDto;

import javax.enterprise.context.ApplicationScoped;

import no.nav.folketrygdloven.kalkulator.aksjonspunkt.FastsettBruttoBeregningsgrunnlagSNforNyIArbeidslivetHåndterer;
import no.nav.folketrygdloven.kalkulator.input.HåndterBeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulus.håndtering.BeregningHåndterer;
import no.nav.folketrygdloven.kalkulus.håndtering.DtoTilServiceAdapter;
import no.nav.folketrygdloven.kalkulus.håndtering.HåndteringResultat;
import no.nav.folketrygdloven.kalkulus.håndtering.v1.foreslå.FastsettBeregningsgrunnlagSNNyIArbeidslivetHåndteringDto;

@ApplicationScoped
@DtoTilServiceAdapter(dto = FastsettBeregningsgrunnlagSNNyIArbeidslivetHåndteringDto.class, adapter = BeregningHåndterer.class)
public class FastsettBeregningsgrunnlagSNNyIArbeidslivetHåndterer implements BeregningHåndterer<FastsettBeregningsgrunnlagSNNyIArbeidslivetHåndteringDto> {

    @Override
    public HåndteringResultat håndter(FastsettBeregningsgrunnlagSNNyIArbeidslivetHåndteringDto dto, HåndterBeregningsgrunnlagInput beregningsgrunnlagInput) {
        BeregningsgrunnlagGrunnlagDto nyttGrunnlag = FastsettBruttoBeregningsgrunnlagSNforNyIArbeidslivetHåndterer.oppdater(beregningsgrunnlagInput, mapFastsettBruttoBeregningsgrunnlagSNforNyIArbeidslivetDto(dto.getFastsettBruttoBeregningsgrunnlagSNforNyIArbeidslivetDto()));
        // TODO Lag endringresultat
        return new HåndteringResultat(nyttGrunnlag, null);
    }

}
