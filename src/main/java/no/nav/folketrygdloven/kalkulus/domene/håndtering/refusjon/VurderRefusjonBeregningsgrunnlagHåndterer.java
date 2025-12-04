package no.nav.folketrygdloven.kalkulus.domene.håndtering.refusjon;


import static no.nav.folketrygdloven.kalkulus.domene.håndtering.mapping.OppdatererDtoMapper.mapVurderRefusjonBeregningsgrunnlagDto;

import jakarta.enterprise.context.ApplicationScoped;

import no.nav.folketrygdloven.kalkulator.input.HåndterBeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulus.domene.håndtering.BeregningHåndterer;
import no.nav.folketrygdloven.kalkulus.domene.håndtering.DtoTilServiceAdapter;
import no.nav.folketrygdloven.kalkulus.domene.håndtering.HåndteringResultat;
import no.nav.folketrygdloven.kalkulus.håndtering.v1.refusjon.VurderRefusjonBeregningsgrunnlagDto;

@ApplicationScoped
@DtoTilServiceAdapter(dto = VurderRefusjonBeregningsgrunnlagDto.class, adapter = BeregningHåndterer.class)
public class VurderRefusjonBeregningsgrunnlagHåndterer implements BeregningHåndterer<VurderRefusjonBeregningsgrunnlagDto> {

    @Override
    public HåndteringResultat håndter(VurderRefusjonBeregningsgrunnlagDto dto, HåndterBeregningsgrunnlagInput beregningsgrunnlagInput) {
        BeregningsgrunnlagGrunnlagDto nyttGrunnlag = no.nav.folketrygdloven.kalkulator.avklaringsbehov.refusjon.VurderRefusjonBeregningsgrunnlagHåndterer.håndter(mapVurderRefusjonBeregningsgrunnlagDto(dto), beregningsgrunnlagInput);
        var endring = UtledEndringForRefusjonOverstyring.utled(nyttGrunnlag, beregningsgrunnlagInput.getForrigeGrunnlagFraHåndteringTilstand());
        return new HåndteringResultat(nyttGrunnlag, endring);
    }
}
