package no.nav.folketrygdloven.kalkulator.rest.dto;

import java.util.List;

import no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.dto.FaktaBeregningLagreDto;
import no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.dto.FastsettBeregningsgrunnlagAndelDto;

public class OverstyrBeregningsgrunnlagDto {

    private FaktaBeregningLagreDto fakta;

    private List<FastsettBeregningsgrunnlagAndelDto> overstyrteAndeler;

    public OverstyrBeregningsgrunnlagDto(List<FastsettBeregningsgrunnlagAndelDto> overstyrteAndeler, FaktaBeregningLagreDto fakta) { // NOSONAR
        this.overstyrteAndeler = overstyrteAndeler;
        this.fakta = fakta;
    }

    public FaktaBeregningLagreDto getFakta() {
        return fakta;
    }

    public List<FastsettBeregningsgrunnlagAndelDto> getOverstyrteAndeler() {
        return overstyrteAndeler;
    }
}
