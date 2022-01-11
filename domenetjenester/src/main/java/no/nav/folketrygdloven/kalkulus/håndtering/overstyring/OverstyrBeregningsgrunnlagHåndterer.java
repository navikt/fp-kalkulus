package no.nav.folketrygdloven.kalkulus.håndtering.overstyring;

import static no.nav.folketrygdloven.kalkulus.håndtering.mapping.OppdatererDtoMapper.mapOverstyrBeregningsgrunnlagDto;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import no.nav.folketrygdloven.kalkulator.avklaringsbehov.BeregningFaktaOgOverstyringHåndterer;
import no.nav.folketrygdloven.kalkulator.input.HåndterBeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulus.håndtering.BeregningHåndterer;
import no.nav.folketrygdloven.kalkulus.håndtering.DtoTilServiceAdapter;
import no.nav.folketrygdloven.kalkulus.håndtering.HåndteringResultat;
import no.nav.folketrygdloven.kalkulus.håndtering.v1.overstyring.OverstyrBeregningsgrunnlagHåndteringDto;

@ApplicationScoped
@DtoTilServiceAdapter(dto = OverstyrBeregningsgrunnlagHåndteringDto.class, adapter = BeregningHåndterer.class)
class OverstyrBeregningsgrunnlagHåndterer implements BeregningHåndterer<OverstyrBeregningsgrunnlagHåndteringDto> {

    private BeregningFaktaOgOverstyringHåndterer beregningFaktaOgOverstyringHåndterer;

    public OverstyrBeregningsgrunnlagHåndterer() {
        // CDI
    }

    @Inject
    public OverstyrBeregningsgrunnlagHåndterer(BeregningFaktaOgOverstyringHåndterer beregningFaktaOgOverstyringHåndterer) {
        this.beregningFaktaOgOverstyringHåndterer = beregningFaktaOgOverstyringHåndterer;
    }

    @Override
    public HåndteringResultat håndter(OverstyrBeregningsgrunnlagHåndteringDto dto, HåndterBeregningsgrunnlagInput beregningsgrunnlagInput) {
        BeregningsgrunnlagGrunnlagDto nyttGrunnlag = beregningFaktaOgOverstyringHåndterer.håndterMedOverstyring(beregningsgrunnlagInput, mapOverstyrBeregningsgrunnlagDto(dto));
        // TODO Lag endringresultat
        return new HåndteringResultat(nyttGrunnlag, null);
    }

}
