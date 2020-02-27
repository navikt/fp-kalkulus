package no.nav.folketrygdloven.kalkulus.håndtering.overstyring;

import static no.nav.folketrygdloven.kalkulus.mappers.OppdatererDtoMapper.mapOverstyrBeregningsgrunnlagDto;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.BeregningFaktaOgOverstyringHåndterer;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulus.håndtering.BeregningHåndterer;
import no.nav.folketrygdloven.kalkulus.håndtering.DtoTilServiceAdapter;
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
    public BeregningsgrunnlagGrunnlagDto håndter(OverstyrBeregningsgrunnlagHåndteringDto dto, BeregningsgrunnlagInput beregningsgrunnlagInput) {
        return beregningFaktaOgOverstyringHåndterer.håndterMedOverstyring(beregningsgrunnlagInput, mapOverstyrBeregningsgrunnlagDto(dto));
    }

}
