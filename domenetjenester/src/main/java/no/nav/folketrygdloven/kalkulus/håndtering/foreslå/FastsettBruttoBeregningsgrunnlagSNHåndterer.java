package no.nav.folketrygdloven.kalkulus.håndtering.foreslå;

import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;

import no.nav.folketrygdloven.kalkulator.input.HåndterBeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulus.håndtering.BeregningHåndterer;
import no.nav.folketrygdloven.kalkulus.håndtering.DtoTilServiceAdapter;
import no.nav.folketrygdloven.kalkulus.håndtering.HåndteringResultat;
import no.nav.folketrygdloven.kalkulus.håndtering.UtledEndring;
import no.nav.folketrygdloven.kalkulus.håndtering.mapping.OppdatererDtoMapper;
import no.nav.folketrygdloven.kalkulus.håndtering.v1.foreslå.FastsettBruttoBeregningsgrunnlagSNHåndteringDto;

@ApplicationScoped
@DtoTilServiceAdapter(dto = FastsettBruttoBeregningsgrunnlagSNHåndteringDto.class, adapter = BeregningHåndterer.class)
class FastsettBruttoBeregningsgrunnlagSNHåndterer implements BeregningHåndterer<FastsettBruttoBeregningsgrunnlagSNHåndteringDto> {

    @Override
    public HåndteringResultat håndter(FastsettBruttoBeregningsgrunnlagSNHåndteringDto dto, HåndterBeregningsgrunnlagInput input) {
        BeregningsgrunnlagGrunnlagDto nyttGrunnlag = no.nav.folketrygdloven.kalkulator.avklaringsbehov.FastsettBruttoBeregningsgrunnlagSNHåndterer.håndter(input, OppdatererDtoMapper.mapFastsettBruttoBeregningsgrunnlagSNDto(dto.getFastsettBruttoBeregningsgrunnlagSNDto()));
        Optional<BeregningsgrunnlagGrunnlagDto> forrigeGrunnlag = input.getForrigeGrunnlagFraHåndteringTilstand();
        BeregningsgrunnlagGrunnlagDto grunnlagFraSteg = input.getBeregningsgrunnlagGrunnlag();
        var endring = UtledEndring.utled(nyttGrunnlag, grunnlagFraSteg, forrigeGrunnlag, dto, input.getIayGrunnlag());
        return new HåndteringResultat(nyttGrunnlag, endring);
    }

}
