package no.nav.folketrygdloven.kalkulator.rest.fakta;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;

import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagRestInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.rest.dto.FaktaOmBeregningAndelDto;
import no.nav.folketrygdloven.kalkulator.rest.dto.FaktaOmBeregningDto;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.FaktaOmBeregningTilfelle;

@ApplicationScoped
public class VurderLønnsendringDtoTjeneste implements FaktaOmBeregningTilfelleDtoTjeneste {

    @Override
    public void lagDto(BeregningsgrunnlagRestInput input, FaktaOmBeregningDto faktaOmBeregningDto) {
        BeregningsgrunnlagDto beregningsgrunnlag = input.getBeregningsgrunnlag();
        List<FaktaOmBeregningTilfelle> tilfeller = beregningsgrunnlag.getFaktaOmBeregningTilfeller();
        if (tilfeller.contains(FaktaOmBeregningTilfelle.VURDER_LØNNSENDRING)) {
            var ref = input.getBehandlingReferanse();
            List<FaktaOmBeregningAndelDto> arbeidsforholdUtenInntektsmeldingDtoList = FaktaOmBeregningAndelDtoTjeneste.lagArbeidsforholdUtenInntektsmeldingDtoList(ref.getAktørId(), beregningsgrunnlag, input.getIayGrunnlag());
            if (!arbeidsforholdUtenInntektsmeldingDtoList.isEmpty()) {
                faktaOmBeregningDto.setArbeidsforholdMedLønnsendringUtenIM(arbeidsforholdUtenInntektsmeldingDtoList);
            }
        }
    }
}
