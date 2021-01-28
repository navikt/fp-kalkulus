package no.nav.folketrygdloven.kalkulus.håndtering.avklaraktiviteter;

import javax.enterprise.context.ApplicationScoped;

import no.nav.folketrygdloven.kalkulator.input.HåndterBeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulus.håndtering.BeregningHåndterer;
import no.nav.folketrygdloven.kalkulus.håndtering.DtoTilServiceAdapter;
import no.nav.folketrygdloven.kalkulus.håndtering.HåndteringResultat;
import no.nav.folketrygdloven.kalkulus.håndtering.v1.avklaraktiviteter.AvklarAktiviteterHåndteringDto;
import no.nav.folketrygdloven.kalkulus.håndtering.mapping.OppdatererDtoMapper;

@ApplicationScoped
@DtoTilServiceAdapter(dto = AvklarAktiviteterHåndteringDto.class, adapter = BeregningHåndterer.class)
class AvklarAktiviteterHåndterer implements BeregningHåndterer<AvklarAktiviteterHåndteringDto> {

    @Override
    public HåndteringResultat håndter(AvklarAktiviteterHåndteringDto dto, HåndterBeregningsgrunnlagInput input) {
        BeregningsgrunnlagGrunnlagDto nyttGrunnlag = no.nav.folketrygdloven.kalkulator.aksjonspunkt.AvklarAktiviteterHåndterer.håndter(OppdatererDtoMapper.mapAvklarteAktiviteterDto(dto.getAvklarteAktiviteterDto()), input);
        // TODO Lag endringresultat
        return new HåndteringResultat(nyttGrunnlag, null);
    }

}
