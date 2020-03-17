package no.nav.folketrygdloven.kalkulus.håndtering.faktaberegning;

import static no.nav.folketrygdloven.kalkulus.håndtering.mapping.OppdatererDtoMapper.mapTilFaktaOmBeregningLagreDto;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.BeregningFaktaOgOverstyringHåndterer;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulus.håndtering.BeregningHåndterer;
import no.nav.folketrygdloven.kalkulus.håndtering.DtoTilServiceAdapter;
import no.nav.folketrygdloven.kalkulus.håndtering.HåndteringResultat;
import no.nav.folketrygdloven.kalkulus.håndtering.v1.fakta.FaktaOmBeregningHåndteringDto;
import no.nav.folketrygdloven.kalkulus.response.v1.håndtering.OppdateringRespons;

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
    public HåndteringResultat håndter(FaktaOmBeregningHåndteringDto dto, BeregningsgrunnlagInput beregningsgrunnlagInput) {
        BeregningsgrunnlagGrunnlagDto nyttGrunnlag = beregningFaktaOgOverstyringHåndterer.håndter(beregningsgrunnlagInput, mapTilFaktaOmBeregningLagreDto(dto.getFakta()));
        OppdateringRespons endring = UtledEndring.utled(nyttGrunnlag, beregningsgrunnlagInput.hentForrigeBeregningsgrunnlagGrunnlag(BeregningsgrunnlagTilstand.KOFAKBER_UT));
        return new HåndteringResultat(nyttGrunnlag, endring);
    }
}
