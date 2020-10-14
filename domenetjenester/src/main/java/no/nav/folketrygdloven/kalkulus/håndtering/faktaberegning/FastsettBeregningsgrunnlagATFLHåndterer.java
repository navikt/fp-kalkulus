package no.nav.folketrygdloven.kalkulus.håndtering.faktaberegning;


import static no.nav.folketrygdloven.kalkulus.håndtering.mapping.OppdatererDtoMapper.mapFastsettBeregningsgrunnlagATFLDto;

import javax.enterprise.context.ApplicationScoped;

import no.nav.folketrygdloven.kalkulator.input.HåndterBeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulus.håndtering.BeregningHåndterer;
import no.nav.folketrygdloven.kalkulus.håndtering.DtoTilServiceAdapter;
import no.nav.folketrygdloven.kalkulus.håndtering.HåndteringResultat;
import no.nav.folketrygdloven.kalkulus.håndtering.v1.foreslå.FastsettBeregningsgrunnlagATFLHåndteringDto;

@ApplicationScoped
@DtoTilServiceAdapter(dto = FastsettBeregningsgrunnlagATFLHåndteringDto.class, adapter = BeregningHåndterer.class)
public class FastsettBeregningsgrunnlagATFLHåndterer implements BeregningHåndterer<FastsettBeregningsgrunnlagATFLHåndteringDto> {

    @Override
    public HåndteringResultat håndter(FastsettBeregningsgrunnlagATFLHåndteringDto dto, HåndterBeregningsgrunnlagInput beregningsgrunnlagInput) {
        BeregningsgrunnlagGrunnlagDto nyttGrunnlag = no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.FastsettBeregningsgrunnlagATFLHåndterer.håndter(beregningsgrunnlagInput, mapFastsettBeregningsgrunnlagATFLDto(dto));
        // TODO Lag endringresultat
        return new HåndteringResultat(nyttGrunnlag, null);
    }

}
