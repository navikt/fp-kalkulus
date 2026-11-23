package no.nav.folketrygdloven.kalkulus.domene.håndtering.overstyring;

import jakarta.enterprise.context.ApplicationScoped;

import no.nav.folketrygdloven.kalkulator.input.HåndterBeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulus.domene.håndtering.BeregningHåndterer;
import no.nav.folketrygdloven.kalkulus.domene.håndtering.DtoTilServiceAdapter;
import no.nav.folketrygdloven.kalkulus.domene.håndtering.HåndteringResultat;
import no.nav.folketrygdloven.kalkulus.domene.håndtering.UtledEndring;
import no.nav.foreldrepenger.kalkulus.kontrakt.request.håndtering.overstyring.OverstyrRefusjonHåndteringDto;

@ApplicationScoped
@DtoTilServiceAdapter(dto = OverstyrRefusjonHåndteringDto.class, adapter = BeregningHåndterer.class)
class OverstyrRefusjonHåndterer implements BeregningHåndterer<OverstyrRefusjonHåndteringDto> {

    public OverstyrRefusjonHåndterer() {
        // CDI
    }

    @Override
    public HåndteringResultat håndter(OverstyrRefusjonHåndteringDto dto, HåndterBeregningsgrunnlagInput beregningsgrunnlagInput) {
        //TODO vi må lage en håndtererMedOverstyring for refusjon i ft-beregning
        // TODO må vi legge Fordelingsgrunnlaget til HåndterBeregningsgrunnlagInput slik at vi får tak i forrige?
        // var nyttGrunnlag = BeregningFaktaOgOverstyringHåndterer.håndterMedOverstyring(beregningsgrunnlagInput, mapOverstyrRefusjonDto(dto));
        BeregningsgrunnlagGrunnlagDto nyttGrunnlag = new BeregningsgrunnlagGrunnlagDto();
        var forrigeGrunnlag = beregningsgrunnlagInput.getForrigeGrunnlagFraHåndteringTilstand();
        var grunnlagFraSteg = beregningsgrunnlagInput.getBeregningsgrunnlagGrunnlag();
        //Todo vi må endre denne til ritkig utled
        var endring = UtledEndring.standard().utled( nyttGrunnlag, grunnlagFraSteg, forrigeGrunnlag, dto, beregningsgrunnlagInput.getIayGrunnlag());
        return new HåndteringResultat(nyttGrunnlag, endring);
    }

}
