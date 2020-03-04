package no.nav.folketrygdloven.kalkulator.guitjenester.fakta;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;

import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagRestInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.FaktaOmBeregningTilfelle;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.ATogFLISammeOrganisasjonDto;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.FaktaOmBeregningDto;

@ApplicationScoped
public class VurderATFLISammeOrgDtoTjeneste implements FaktaOmBeregningTilfelleDtoTjeneste {

    @Override
    public void lagDto(BeregningsgrunnlagRestInput input, FaktaOmBeregningDto faktaOmBeregningDto) {
        BeregningsgrunnlagDto beregningsgrunnlag = input.getBeregningsgrunnlag();
        List<FaktaOmBeregningTilfelle> tilfeller = beregningsgrunnlag.getFaktaOmBeregningTilfeller();
        if (tilfeller.contains(FaktaOmBeregningTilfelle.VURDER_AT_OG_FL_I_SAMME_ORGANISASJON)) {
            var ref = input.getBehandlingReferanse();
            List<ATogFLISammeOrganisasjonDto> aTogFLISammeOrganisasjonDto = FaktaOmBeregningAndelDtoTjeneste.lagATogFLISAmmeOrganisasjonListe(ref, beregningsgrunnlag, input.getInntektsmeldinger(), input.getIayGrunnlag());
            if (faktaOmBeregningDto.getFrilansAndel() == null) {
                FaktaOmBeregningAndelDtoTjeneste.lagFrilansAndelDto(beregningsgrunnlag, input.getIayGrunnlag()).ifPresent(faktaOmBeregningDto::setFrilansAndel);
            }
            faktaOmBeregningDto.setArbeidstakerOgFrilanserISammeOrganisasjonListe(aTogFLISammeOrganisasjonDto);
        }
    }
}
