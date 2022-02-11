package no.nav.folketrygdloven.kalkulus.håndtering.avklaraktiviteter;

import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;
import no.nav.folketrygdloven.kalkulator.input.HåndterBeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulus.håndtering.BeregningHåndterer;
import no.nav.folketrygdloven.kalkulus.håndtering.DtoTilServiceAdapter;
import no.nav.folketrygdloven.kalkulus.håndtering.HåndteringResultat;
import no.nav.folketrygdloven.kalkulus.håndtering.UtledEndring;
import no.nav.folketrygdloven.kalkulus.håndtering.mapping.OppdatererDtoMapper;
import no.nav.folketrygdloven.kalkulus.håndtering.v1.avklaraktiviteter.AvklarAktiviteterHåndteringDto;

@ApplicationScoped
@DtoTilServiceAdapter(dto = AvklarAktiviteterHåndteringDto.class, adapter = BeregningHåndterer.class)
class AvklarAktiviteterHåndterer implements BeregningHåndterer<AvklarAktiviteterHåndteringDto> {

    @Override
    public HåndteringResultat håndter(AvklarAktiviteterHåndteringDto dto, HåndterBeregningsgrunnlagInput input) {
        BeregningsgrunnlagGrunnlagDto nyttGrunnlag = no.nav.folketrygdloven.kalkulator.avklaringsbehov.AvklarAktiviteterHåndterer.håndter(OppdatererDtoMapper.mapAvklarteAktiviteterDto(dto.getAvklarteAktiviteterDto()), input);
        Optional<BeregningsgrunnlagGrunnlagDto> forrigeGrunnlag = input.getForrigeGrunnlagFraHåndteringTilstand();
        BeregningsgrunnlagGrunnlagDto grunnlagFraSteg = input.getBeregningsgrunnlagGrunnlag();
        var endring = UtledEndring.utled(nyttGrunnlag, grunnlagFraSteg, forrigeGrunnlag, dto, input.getIayGrunnlag());
        return new HåndteringResultat(nyttGrunnlag, endring);
    }

}
