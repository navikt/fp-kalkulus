package no.nav.folketrygdloven.kalkulus.håndtering.overstyring;

import javax.enterprise.context.ApplicationScoped;

import no.nav.folketrygdloven.kalkulator.avklaringsbehov.AvklarAktiviteterHåndterer;
import no.nav.folketrygdloven.kalkulator.input.HåndterBeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulus.håndtering.BeregningHåndterer;
import no.nav.folketrygdloven.kalkulus.håndtering.DtoTilServiceAdapter;
import no.nav.folketrygdloven.kalkulus.håndtering.HåndteringResultat;
import no.nav.folketrygdloven.kalkulus.håndtering.mapping.OppdatererDtoMapper;
import no.nav.folketrygdloven.kalkulus.håndtering.v1.overstyring.OverstyrBeregningsaktiviteterDto;

@ApplicationScoped
@DtoTilServiceAdapter(dto = OverstyrBeregningsaktiviteterDto.class, adapter = BeregningHåndterer.class)
public class BeregningsaktivitetOverstyringshåndterer implements BeregningHåndterer<OverstyrBeregningsaktiviteterDto> {

    @Override
    public HåndteringResultat håndter(OverstyrBeregningsaktiviteterDto dto, HåndterBeregningsgrunnlagInput beregningsgrunnlagInput) {
        BeregningsgrunnlagGrunnlagDto nyttGrunnlag = AvklarAktiviteterHåndterer.håndterOverstyring(OppdatererDtoMapper.mapOverstyrBeregningsaktiviteterDto(dto.getBeregningsaktivitetLagreDtoList()), beregningsgrunnlagInput);
        // TODO Lag endringresultat
        return new HåndteringResultat(nyttGrunnlag, null);
    }

}
