package no.nav.folketrygdloven.kalkulus.håndtering.faktaberegning;

import static no.nav.folketrygdloven.kalkulus.håndtering.mapping.OppdatererDtoMapper.mapTilFaktaOmBeregningLagreDto;

import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import no.nav.folketrygdloven.kalkulator.avklaringsbehov.BeregningFaktaOgOverstyringHåndterer;
import no.nav.folketrygdloven.kalkulator.input.HåndterBeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulus.håndtering.BeregningHåndterer;
import no.nav.folketrygdloven.kalkulus.håndtering.DtoTilServiceAdapter;
import no.nav.folketrygdloven.kalkulus.håndtering.HåndteringResultat;
import no.nav.folketrygdloven.kalkulus.håndtering.UtledEndring;
import no.nav.folketrygdloven.kalkulus.håndtering.v1.fakta.FaktaOmBeregningHåndteringDto;

@ApplicationScoped
@DtoTilServiceAdapter(dto = FaktaOmBeregningHåndteringDto.class, adapter = BeregningHåndterer.class)
class FaktaOmBeregningHåndterer implements BeregningHåndterer<FaktaOmBeregningHåndteringDto>  {

    private BeregningFaktaOgOverstyringHåndterer beregningFaktaOgOverstyringHåndterer;

    public FaktaOmBeregningHåndterer() {
        // CDI
    }

    @Inject
    public FaktaOmBeregningHåndterer(BeregningFaktaOgOverstyringHåndterer beregningFaktaOgOverstyringHåndterer) {
        this.beregningFaktaOgOverstyringHåndterer = beregningFaktaOgOverstyringHåndterer;
    }

    @Override
    public HåndteringResultat håndter(FaktaOmBeregningHåndteringDto dto, HåndterBeregningsgrunnlagInput beregningsgrunnlagInput) {
        BeregningsgrunnlagGrunnlagDto nyttGrunnlag = beregningFaktaOgOverstyringHåndterer.håndter(beregningsgrunnlagInput, mapTilFaktaOmBeregningLagreDto(dto.getFakta()));
        Optional<BeregningsgrunnlagGrunnlagDto> forrigeGrunnlag = beregningsgrunnlagInput.getForrigeGrunnlagFraHåndteringTilstand();
        BeregningsgrunnlagGrunnlagDto grunnlagFraSteg = beregningsgrunnlagInput.getBeregningsgrunnlagGrunnlag();
        var endring = UtledEndring.utled(nyttGrunnlag, grunnlagFraSteg, forrigeGrunnlag, dto, beregningsgrunnlagInput.getIayGrunnlag());
        return new HåndteringResultat(nyttGrunnlag, endring);
    }
}
