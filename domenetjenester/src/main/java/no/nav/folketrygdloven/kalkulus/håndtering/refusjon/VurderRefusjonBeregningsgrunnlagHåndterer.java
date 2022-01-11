package no.nav.folketrygdloven.kalkulus.håndtering.refusjon;


import static no.nav.folketrygdloven.kalkulus.håndtering.mapping.OppdatererDtoMapper.mapVurderRefusjonBeregningsgrunnlagDto;

import jakarta.enterprise.context.ApplicationScoped;

import no.nav.folketrygdloven.kalkulator.input.HåndterBeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulus.håndtering.BeregningHåndterer;
import no.nav.folketrygdloven.kalkulus.håndtering.DtoTilServiceAdapter;
import no.nav.folketrygdloven.kalkulus.håndtering.HåndteringResultat;
import no.nav.folketrygdloven.kalkulus.håndtering.v1.refusjon.VurderRefusjonBeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulus.response.v1.håndtering.OppdateringRespons;

@ApplicationScoped
@DtoTilServiceAdapter(dto = VurderRefusjonBeregningsgrunnlagDto.class, adapter = BeregningHåndterer.class)
public class VurderRefusjonBeregningsgrunnlagHåndterer implements BeregningHåndterer<VurderRefusjonBeregningsgrunnlagDto> {

    @Override
    public HåndteringResultat håndter(VurderRefusjonBeregningsgrunnlagDto dto, HåndterBeregningsgrunnlagInput beregningsgrunnlagInput) {
        BeregningsgrunnlagGrunnlagDto nyttGrunnlag = no.nav.folketrygdloven.kalkulator.avklaringsbehov.refusjon.VurderRefusjonBeregningsgrunnlagHåndterer.håndter(mapVurderRefusjonBeregningsgrunnlagDto(dto), beregningsgrunnlagInput);
        var endring = UtledEndring.utled(nyttGrunnlag, beregningsgrunnlagInput.getForrigeGrunnlagFraHåndteringTilstand());
        return new HåndteringResultat(nyttGrunnlag, endring);
    }
}
