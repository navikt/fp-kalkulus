package no.nav.folketrygdloven.kalkulus.håndtering.faktafordeling;


import jakarta.enterprise.context.ApplicationScoped;
import no.nav.folketrygdloven.kalkulator.avklaringsbehov.VurderRepresentererStortingetTjeneste;
import no.nav.folketrygdloven.kalkulator.input.HåndterBeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulus.håndtering.BeregningHåndterer;
import no.nav.folketrygdloven.kalkulus.håndtering.DtoTilServiceAdapter;
import no.nav.folketrygdloven.kalkulus.håndtering.HåndteringResultat;
import no.nav.folketrygdloven.kalkulus.håndtering.UtledEndring;
import no.nav.folketrygdloven.kalkulus.håndtering.v1.fordeling.VurderRepresentererStortingetHåndteringDto;

@ApplicationScoped
@DtoTilServiceAdapter(dto = VurderRepresentererStortingetHåndteringDto.class, adapter = BeregningHåndterer.class)
public class VurderRepresentererStortingetHåndterer implements BeregningHåndterer<VurderRepresentererStortingetHåndteringDto> {

    @Override
    public HåndteringResultat håndter(VurderRepresentererStortingetHåndteringDto dto, HåndterBeregningsgrunnlagInput beregningsgrunnlagInput) {
        BeregningsgrunnlagGrunnlagDto nyttGrunnlag = VurderRepresentererStortingetTjeneste.løsAvklaringsbehov(dto, beregningsgrunnlagInput);
        BeregningsgrunnlagGrunnlagDto grunnlagFraSteg = beregningsgrunnlagInput.getBeregningsgrunnlagGrunnlag();
        var endring = UtledEndring.standard().utled(nyttGrunnlag,  grunnlagFraSteg, beregningsgrunnlagInput.getForrigeGrunnlagFraHåndteringTilstand(), dto, beregningsgrunnlagInput.getIayGrunnlag());
        return new HåndteringResultat(nyttGrunnlag, endring);
    }

}
