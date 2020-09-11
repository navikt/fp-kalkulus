package no.nav.folketrygdloven.kalkulator.guitjenester.fakta;

import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagGUIInput;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.FaktaOmBeregningDto;

public interface FaktaOmBeregningTilfelleDtoTjeneste {

    void lagDto(BeregningsgrunnlagGUIInput input, FaktaOmBeregningDto faktaOmBeregningDto);

}
