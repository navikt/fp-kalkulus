package no.nav.folketrygdloven.kalkulator.input;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;

public interface YtelsespesifiktGrunnlag {

    int getDekningsgrad(BeregningsgrunnlagDto vlBeregningsgrunnlag);

    int getGrunnbeløpMilitærHarKravPå();

    void setGrunnbeløpMilitærHarKravPå(int antallGrunnbeløpMilitærHarKravPå);

}
