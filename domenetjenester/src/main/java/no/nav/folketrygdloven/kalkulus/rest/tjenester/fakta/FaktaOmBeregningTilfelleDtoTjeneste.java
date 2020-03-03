package no.nav.folketrygdloven.kalkulus.rest.tjenester.fakta;

import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagRestInput;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.FaktaOmBeregningDto;

public interface FaktaOmBeregningTilfelleDtoTjeneste {

    void lagDto(BeregningsgrunnlagRestInput input, FaktaOmBeregningDto faktaOmBeregningDto);

}
