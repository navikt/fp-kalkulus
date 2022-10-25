package no.nav.folketrygdloven.kalkulus.håndtering.faktaberegning;

import jakarta.enterprise.context.ApplicationScoped;

import no.nav.folketrygdloven.kalkulator.avklaringsbehov.FastsettBruttoBeregningsgrunnlagSNHåndterer;
import no.nav.folketrygdloven.kalkulator.input.HåndterBeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulus.håndtering.BeregningHåndterer;
import no.nav.folketrygdloven.kalkulus.håndtering.DtoTilServiceAdapter;
import no.nav.folketrygdloven.kalkulus.håndtering.HåndteringResultat;
import no.nav.folketrygdloven.kalkulus.håndtering.mapping.OppdatererDtoMapper;
import no.nav.folketrygdloven.kalkulus.håndtering.v1.foreslå.FastsettBruttoBeregningsgrunnlagDto;

@ApplicationScoped
@DtoTilServiceAdapter(dto = FastsettBruttoBeregningsgrunnlagDto.class, adapter = BeregningHåndterer.class)
public class FastsettBruttoBgSNHåndterer implements BeregningHåndterer<FastsettBruttoBeregningsgrunnlagDto> {

    @Override
    public HåndteringResultat håndter(FastsettBruttoBeregningsgrunnlagDto dto, HåndterBeregningsgrunnlagInput beregningsgrunnlagInput) {
        BeregningsgrunnlagGrunnlagDto nyttGrunnlag = FastsettBruttoBeregningsgrunnlagSNHåndterer.håndter(beregningsgrunnlagInput, OppdatererDtoMapper.mapFastsettBruttoBeregningsgrunnlagSNDto(dto).getBruttoBeregningsgrunnlag());
        // TODO Lag endringresultat
        return new HåndteringResultat(nyttGrunnlag, null);
    }

}
