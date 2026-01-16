package no.nav.folketrygdloven.kalkulus.domene.håndtering.avklaraktiviteter;

import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;
import no.nav.folketrygdloven.kalkulator.input.HåndterBeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulus.domene.håndtering.BeregningHåndterer;
import no.nav.folketrygdloven.kalkulus.domene.håndtering.DtoTilServiceAdapter;
import no.nav.folketrygdloven.kalkulus.domene.håndtering.HåndteringResultat;
import no.nav.folketrygdloven.kalkulus.domene.håndtering.UtledEndring;
import no.nav.folketrygdloven.kalkulus.domene.håndtering.mapping.OppdatererDtoMapper;
import no.nav.foreldrepenger.kalkulus.kontrakt.request.håndtering.avklaraktiviteter.AvklarAktiviteterHåndteringDto;

@ApplicationScoped
@DtoTilServiceAdapter(dto = AvklarAktiviteterHåndteringDto.class, adapter = BeregningHåndterer.class)
class AvklarAktiviteterHåndterer implements BeregningHåndterer<AvklarAktiviteterHåndteringDto> {

    @Override
    public HåndteringResultat håndter(AvklarAktiviteterHåndteringDto dto, HåndterBeregningsgrunnlagInput input) {
        BeregningsgrunnlagGrunnlagDto nyttGrunnlag = no.nav.folketrygdloven.kalkulator.avklaringsbehov.AvklarAktiviteterHåndterer.håndter(OppdatererDtoMapper.mapAvklarteAktiviteterDto(dto.getAvklarteAktiviteterDto()), input);
        Optional<BeregningsgrunnlagGrunnlagDto> forrigeGrunnlag = input.getForrigeGrunnlagFraHåndteringTilstand();
        BeregningsgrunnlagGrunnlagDto grunnlagFraSteg = input.getBeregningsgrunnlagGrunnlag();
        var endring = UtledEndring.standard().utled(nyttGrunnlag, grunnlagFraSteg, forrigeGrunnlag, dto, input.getIayGrunnlag());
        return new HåndteringResultat(nyttGrunnlag, endring);
    }

}
