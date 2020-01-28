package no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.dto;

import java.util.List;

public class FordelBeregningsgrunnlagDto {

    private List<FastsettBeregningsgrunnlagPeriodeDto> endretBeregningsgrunnlagPerioder;

    public FordelBeregningsgrunnlagDto(List<FastsettBeregningsgrunnlagPeriodeDto> endretBeregningsgrunnlagPerioder) { // NOSONAR
        this.endretBeregningsgrunnlagPerioder = endretBeregningsgrunnlagPerioder;
    }

    public List<FastsettBeregningsgrunnlagPeriodeDto> getEndretBeregningsgrunnlagPerioder() {
        return endretBeregningsgrunnlagPerioder;
    }
}
