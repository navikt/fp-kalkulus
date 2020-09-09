package no.nav.folketrygdloven.kalkulator.guitjenester.fakta;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;

import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagRestInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.FaktaOmBeregningTilfelle;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.FaktaOmBeregningAndelDto;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.FaktaOmBeregningDto;

@ApplicationScoped
public class VurderLønnsendringDtoTjeneste implements FaktaOmBeregningTilfelleDtoTjeneste {

    @Override
    public void lagDto(BeregningsgrunnlagRestInput input, FaktaOmBeregningDto faktaOmBeregningDto) {
        BeregningsgrunnlagDto beregningsgrunnlag = input.getBeregningsgrunnlag();
        List<FaktaOmBeregningTilfelle> tilfeller = beregningsgrunnlag.getFaktaOmBeregningTilfeller();
        if (tilfeller.contains(FaktaOmBeregningTilfelle.VURDER_LØNNSENDRING)) {
            var ref = input.getKoblingReferanse();
            List<FaktaOmBeregningAndelDto> arbeidsforholdUtenInntektsmeldingDtoList = FaktaOmBeregningAndelDtoTjeneste.lagArbeidsforholdUtenInntektsmeldingDtoList(ref.getAktørId(), beregningsgrunnlag, input.getIayGrunnlag());
            if (!arbeidsforholdUtenInntektsmeldingDtoList.isEmpty()) {
                faktaOmBeregningDto.setArbeidsforholdMedLønnsendringUtenIM(arbeidsforholdUtenInntektsmeldingDtoList);
            }
        }
    }
}
