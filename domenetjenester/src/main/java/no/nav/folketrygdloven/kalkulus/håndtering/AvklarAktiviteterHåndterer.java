package no.nav.folketrygdloven.kalkulus.håndtering;

import javax.enterprise.context.ApplicationScoped;

import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulus.håndtering.v1.AvklarAktiviteterHåndteringDto;
import no.nav.folketrygdloven.kalkulus.mappers.OppdatererDtoMapper;

@ApplicationScoped
@DtoTilServiceAdapter(dto = AvklarAktiviteterHåndteringDto.class, adapter = BeregningHåndterer.class)
class AvklarAktiviteterHåndterer implements BeregningHåndterer<AvklarAktiviteterHåndteringDto> {

    @Override
    public BeregningsgrunnlagGrunnlagDto håndter(AvklarAktiviteterHåndteringDto dto, BeregningsgrunnlagInput input) {
        return no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.AvklarAktiviteterHåndterer.håndter(OppdatererDtoMapper.mapAvklarteAktiviteterDto(dto.getAvklarteAktiviteterDto()), input);
    }


}
