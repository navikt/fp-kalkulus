package no.nav.folketrygdloven.kalkulus.håndtering.overstyring;

import javax.enterprise.context.ApplicationScoped;

import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulus.håndtering.BeregningHåndterer;
import no.nav.folketrygdloven.kalkulus.håndtering.DtoTilServiceAdapter;
import no.nav.folketrygdloven.kalkulus.håndtering.v1.overstyring.OverstyrBeregningsaktiviteterDto;
import no.nav.folketrygdloven.kalkulus.mappers.OppdatererDtoMapper;

@ApplicationScoped
@DtoTilServiceAdapter(dto = OverstyrBeregningsaktiviteterDto.class, adapter = BeregningHåndterer.class)
public class BeregningsaktivitetOverstyringshåndterer implements BeregningHåndterer<OverstyrBeregningsaktiviteterDto> {

    @Override
    public BeregningsgrunnlagGrunnlagDto håndter(OverstyrBeregningsaktiviteterDto dto, BeregningsgrunnlagInput beregningsgrunnlagInput) {
        return no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.AvklarAktiviteterHåndterer.håndterOverstyring(OppdatererDtoMapper.mapOverstyrBeregningsaktiviteterDto(dto.getBeregningsaktivitetLagreDtoList()), beregningsgrunnlagInput);
    }

}
