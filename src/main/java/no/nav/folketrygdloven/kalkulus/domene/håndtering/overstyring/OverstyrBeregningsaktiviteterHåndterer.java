package no.nav.folketrygdloven.kalkulus.domene.håndtering.overstyring;

import jakarta.enterprise.context.ApplicationScoped;
import no.nav.folketrygdloven.kalkulator.avklaringsbehov.AvklarAktiviteterHåndterer;
import no.nav.folketrygdloven.kalkulator.input.HåndterBeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulus.domene.håndtering.BeregningHåndterer;
import no.nav.folketrygdloven.kalkulus.domene.håndtering.DtoTilServiceAdapter;
import no.nav.folketrygdloven.kalkulus.domene.håndtering.HåndteringResultat;
import no.nav.folketrygdloven.kalkulus.domene.håndtering.UtledEndring;
import no.nav.folketrygdloven.kalkulus.domene.håndtering.mapping.OppdatererDtoMapper;
import no.nav.foreldrepenger.kalkulus.kontrakt.request.håndtering.overstyring.OverstyrBeregningsaktiviteterDto;

@ApplicationScoped
@DtoTilServiceAdapter(dto = OverstyrBeregningsaktiviteterDto.class, adapter = BeregningHåndterer.class)
public class OverstyrBeregningsaktiviteterHåndterer implements BeregningHåndterer<OverstyrBeregningsaktiviteterDto> {

    @Override
    public HåndteringResultat håndter(OverstyrBeregningsaktiviteterDto dto, HåndterBeregningsgrunnlagInput beregningsgrunnlagInput) {
        var nyttGrunnlag = AvklarAktiviteterHåndterer.håndterOverstyring(OppdatererDtoMapper.mapOverstyrBeregningsaktiviteterDto(dto.getBeregningsaktivitetLagreDtoList()), beregningsgrunnlagInput);
        var forrigeGrunnlag = beregningsgrunnlagInput.getForrigeGrunnlagFraHåndteringTilstand();
        var grunnlagFraSteg = beregningsgrunnlagInput.getBeregningsgrunnlagGrunnlag();
        var endring = UtledEndring.standard().utled(nyttGrunnlag, grunnlagFraSteg, forrigeGrunnlag, dto, beregningsgrunnlagInput.getIayGrunnlag());
        return new HåndteringResultat(nyttGrunnlag, endring);
    }

}
