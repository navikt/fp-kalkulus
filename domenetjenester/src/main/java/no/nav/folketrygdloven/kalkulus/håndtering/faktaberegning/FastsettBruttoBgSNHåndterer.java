package no.nav.folketrygdloven.kalkulus.håndtering.faktaberegning;

import javax.enterprise.context.ApplicationScoped;

import no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.FastsettBruttoBeregningsgrunnlagSNHåndterer;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulus.håndtering.BeregningHåndterer;
import no.nav.folketrygdloven.kalkulus.håndtering.DtoTilServiceAdapter;
import no.nav.folketrygdloven.kalkulus.håndtering.HåndteringResultat;
import no.nav.folketrygdloven.kalkulus.håndtering.v1.foreslå.FastsettBruttoBeregningsgrunnlagSNDto;
import no.nav.folketrygdloven.kalkulus.håndtering.mapping.OppdatererDtoMapper;

@ApplicationScoped
@DtoTilServiceAdapter(dto = FastsettBruttoBeregningsgrunnlagSNDto.class, adapter = BeregningHåndterer.class)
public class FastsettBruttoBgSNHåndterer implements BeregningHåndterer<FastsettBruttoBeregningsgrunnlagSNDto> {

    @Override
    public HåndteringResultat håndter(FastsettBruttoBeregningsgrunnlagSNDto dto, BeregningsgrunnlagInput beregningsgrunnlagInput) {
        BeregningsgrunnlagGrunnlagDto nyttGrunnlag = FastsettBruttoBeregningsgrunnlagSNHåndterer.håndter(beregningsgrunnlagInput, OppdatererDtoMapper.mapFastsettBruttoBeregningsgrunnlagSNDto(dto));
        // TODO Lag endringresultat
        return new HåndteringResultat(nyttGrunnlag, null);
    }

}
