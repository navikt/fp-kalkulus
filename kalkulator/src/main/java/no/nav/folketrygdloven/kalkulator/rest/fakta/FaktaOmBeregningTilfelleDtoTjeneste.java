package no.nav.folketrygdloven.kalkulator.rest.fakta;

import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagRestInput;
import no.nav.folketrygdloven.kalkulator.rest.dto.FaktaOmBeregningDto;

public interface FaktaOmBeregningTilfelleDtoTjeneste {

    void lagDto(BeregningsgrunnlagRestInput input, FaktaOmBeregningDto faktaOmBeregningDto);

}
